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
import de.bluecolored.bluemap.entities.entity.*;

public class ChestedHorseRenderer extends CustomResourceModelRenderer {

    private final ResourcePath<Model>
            MULE_HORSE_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/color/horse_adult_mule"),
            MULE_HORSE_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/color/horse_baby_mule"),
            DONKEY_HORSE_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/color/horse_adult_donkey"),
            DONKEY_HORSE_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/color/horse_baby_donkey"),
            CHEST_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/horse_adult_chest"),
            CHEST_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/horse/horse_baby_chest");

    public ChestedHorseRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof ChestedHorse horse)) return;

        // render horse model
        ResourcePath<Model> model;
        boolean isBaby = horse.getAge() < 0;
        switch (horse) {
            case Mule ignored -> {
                if (isBaby) model = MULE_HORSE_BABY;
                else model = MULE_HORSE_ADULT;
            }
            case Donkey ignored -> {
                if (isBaby) model = DONKEY_HORSE_BABY;
                else model = DONKEY_HORSE_ADULT;
            }
            default -> model = DONKEY_HORSE_ADULT;
        }
        super.render(entity, block, model.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);

        // render chest model if present
        if (horse.isChested()) {
            ResourcePath<Model> chestModel;
            if (isBaby) chestModel = CHEST_BABY;
            else chestModel = CHEST_ADULT;
            super.render(entity, block, chestModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        }


        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
