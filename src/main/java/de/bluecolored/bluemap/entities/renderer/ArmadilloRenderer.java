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
import de.bluecolored.bluemap.entities.entity.Armadillo;
import de.bluecolored.bluemap.entities.entity.Bee;

public class ArmadilloRenderer extends CustomResourceModelRenderer {

    private final ResourcePath<Model>
            ARMADILLO_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/armadillo/armadillo_adult"),
            ARMADILLO_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/armadillo/armadillo_baby"),
            ARMADILLO_ADULT_SCARED = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/armadillo/armadillo_adult_scared"),
            ARMADILLO_BABY_SCARED = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/armadillo/armadillo_baby_scared");

    public ArmadilloRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof Armadillo armadillo)) return;

        // choose correct model
        ResourcePath<Model> model;
        boolean isBaby = armadillo.getAge() < 0;
        switch (armadillo.getState()) {
            case IDLE -> {
                if (isBaby) model = ARMADILLO_BABY;
                else model = ARMADILLO_ADULT;
            }
            case SCARED -> {
                if (isBaby) model = ARMADILLO_BABY_SCARED;
                else model = ARMADILLO_ADULT_SCARED;
            }
            default -> model = ARMADILLO_ADULT;
        }

        // render chosen model
        super.render(entity, block, model.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
