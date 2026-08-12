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
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.entitystate.Part;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.Entity;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.entities.entity.TropicalFish;

public class TropicalFishRenderer extends CustomResourceModelRenderer {

    /** {@code DyeColor#getTextureDiffuseColor} */
    private static final int[] DYE_COLORS = {
            0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA,
            0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
            0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
            0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
    };

    private static final int PATTERNS = 6;

    public TropicalFishRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof TropicalFish tropicalFish)) return;

        boolean isLarge = tropicalFish.isLarge();
        int pattern = tropicalFish.getPattern();
        if (pattern >= PATTERNS) pattern = 0;

        // "entity/tropical_fish_{small|large}/main" tint base-color
        String fishModel = "entity/tropical_fish_" + (isLarge ? "large" : "small") + "/";
        ResourcePath<Model> baseModel = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, fishModel + "main");
        super.render(entity, block, baseModel.getResource(getModelProvider()), tint(tropicalFish.getBaseColor()), tileModel);

        // "entity/tropical_fish_{small|large}/pattern_tropical_{a|b}_pattern_{1-6}"
        String patternModel = fishModel + "pattern_tropical_" + (isLarge ? "b" : "a") + "_pattern_" + (pattern + 1);
        ResourcePath<Model> patternPath = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, patternModel);
        super.render(entity, block, patternPath.getResource(getModelProvider()), tint(tropicalFish.getPatternColor()), tileModel);

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

    private TintColorProvider tint(int dyeColor) {
        int color = 0xFF000000 | DYE_COLORS[dyeColor & 0xF];
        return (index, target) -> target.set(color, true);
    }

}
