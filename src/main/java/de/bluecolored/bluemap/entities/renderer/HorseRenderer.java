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
import de.bluecolored.bluemap.entities.entity.Horse;

public class HorseRenderer extends CustomResourceModelRenderer {

    public HorseRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof Horse horse)) return;

        boolean isBaby = horse.getAge() < 0;

        // render base model "entity/horse/color/horse_{age}_{color}"
        String baseModelPath = "entity/horse/color/horse_" + (isBaby ? "baby_" : "adult_");
        switch (horse.getBaseColor()) {
            case 0 -> baseModelPath += "white";
            case 1 -> baseModelPath += "creamy";
            case 2 -> baseModelPath += "chestnut";
            case 3 -> baseModelPath += "brown";
            case 4 -> baseModelPath += "black";
            case 5 -> baseModelPath += "gray";
            case 6 -> baseModelPath += "darkbrown";
            default -> baseModelPath += "unknown";
        }
        ResourcePath<Model> baseModel = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, baseModelPath);
        super.render(entity, block, baseModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);


        // render markings model "entity/horse/markings/horse_markings_{age}_{markings}" if present
        String markingModelPath = "entity/horse/marking/horse_" + (isBaby ? "baby_" : "adult_");
        switch (horse.getMarkings()) {
            case 0 -> markingModelPath = null; // no markings
            case 1 -> markingModelPath += "white";
            case 2 -> markingModelPath += "whitefield";
            case 3 -> markingModelPath += "whitedots";
            case 4 -> markingModelPath += "blackdots";
            default -> markingModelPath += "unknown";
        }
        if (markingModelPath != null) {
            ResourcePath<Model> markingModel = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, markingModelPath);
            super.render(entity, block, markingModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        }

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
