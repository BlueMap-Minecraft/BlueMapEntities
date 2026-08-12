/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.entities.renderer;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.entitystate.Part;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.world.Entity;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.entities.entity.VillagerData;
import de.bluecolored.bluemap.entities.entity.VillagerDataHolder;

import java.util.Set;

public class VillagerRenderer extends CustomResourceModelRenderer {

    private static final String VILLAGER = "villager";
    private static final String[] LEVELS = { "stone", "iron", "gold", "emerald", "diamond" };

    /**
     * The hat-rules of a villager live in the {@code *.png.mcmeta} files, which bluemap does not read. Only the
     * adult villager biome-textures declare one, the zombie- and baby-textures do not.
     */
    private static final Set<String> FULL_HAT_TYPES = Set.of("desert", "snow");
    private static final Set<String> FULL_HAT_PROFESSIONS = Set.of("farmer", "fisherman", "fletcher", "librarian", "shepherd");
    private static final Set<String> PARTIAL_HAT_PROFESSIONS = Set.of("butcher");

    public VillagerRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof VillagerDataHolder villager)) return;

        VillagerData data = villager.getVillagerData();
        String type = data != null ? data.getRawType() : VillagerData.DEFAULT_TYPE;
        String profession = data != null ? data.getRawProfession() : VillagerData.PROFESSION_NONE;

        String name = entity.getId().getValue();
        boolean baby = villager.isBaby();
        String folder = "entity/" + name + (baby ? "_baby" : "");

        // the skin, everything else is clothing drawn on slightly grown copies of it
        Model main = model(folder + "/main");
        if (main == null) return;
        super.render(entity, block, main, TintColorProvider.NO_TINT, tileModel);

        String typeFolder = hatVisible(name, type, profession, baby) ? folder : folder + "_no_hat";
        Model biome = model(typeFolder + "/type_" + type);
        if (biome == null) biome = model(typeFolder + "/type_" + VillagerData.DEFAULT_TYPE);
        if (biome != null) super.render(entity, block, biome, TintColorProvider.NO_TINT, tileModel);

        // babies never show what they do for a living
        if (!baby && !VillagerData.PROFESSION_NONE.equals(profession)) {
            Model job = model(folder + "/profession_" + profession);
            if (job != null) super.render(entity, block, job, TintColorProvider.NO_TINT, tileModel);

            if (!VillagerData.PROFESSION_NITWIT.equals(profession)) {
                Model badge = model(folder + "/level_" + LEVELS[level(data)]);
                if (badge != null) super.render(entity, block, badge, TintColorProvider.NO_TINT, tileModel);
            }
        }

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

    /** The biome-hat gives way to a profession that brings its own. */
    private static boolean hatVisible(String name, String type, String profession, boolean baby) {
        if (FULL_HAT_PROFESSIONS.contains(profession)) return false;
        if (!PARTIAL_HAT_PROFESSIONS.contains(profession)) return true;
        return baby || !VILLAGER.equals(name) || !FULL_HAT_TYPES.contains(type);
    }

    private static int level(VillagerData data) {
        if (data == null) return 0;
        return Math.clamp(data.getLevel(), 1, LEVELS.length) - 1;
    }

}
