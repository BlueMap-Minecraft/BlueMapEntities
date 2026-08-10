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

import com.flowpowered.math.vector.Vector3f;
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
import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import de.bluecolored.bluemap.entities.entity.HappyGhast;

public class GhastRenderer extends CustomResourceModelRenderer {

    private final ResourcePath<Model>
            GHAST = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/ghast/ghast"),
            HAPPY_GHAST = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/ghast/happy_ghast_adult"),
            GHASTLING = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/ghast/happy_ghast_baby");

    public GhastRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof MCAEntity ghast)) return;

        // choose correct model & scale
        ResourcePath<Model> model;
        Vector3f scale;
        if (ghast instanceof HappyGhast) {
            if (((HappyGhast) ghast).getAge() < 0) {
                model = GHASTLING;
                scale = new Vector3f(0.9f, 0.9f, 0.9f);
            } else {
                model = HAPPY_GHAST;
                scale = new Vector3f(4f, 4f, 4f);
            }
        } else {
            model = GHAST;
            scale = new Vector3f(4.5, 4.5, 4.5);
        }

        // render chosen model
        super.render(entity, block, model.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        tileModel.scale(scale.getX(), scale.getY(), scale.getZ());

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
