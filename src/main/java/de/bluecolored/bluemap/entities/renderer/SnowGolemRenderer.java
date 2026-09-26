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
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.entitystate.Part;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.Entity;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.entities.block.VoidBlockNeighborhood;
import de.bluecolored.bluemap.entities.entity.SnowGolem;

import java.util.Map;

public class SnowGolemRenderer extends CustomResourceModelRenderer {

    private static final BlockState PUMPKIN = new BlockState(Key.minecraft("carved_pumpkin"), Map.of(
            "facing", "north"
    ));

    private final ResourcePath<Model>
            SNOW_GOLEM = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/snow_golem/main");

    private final BlockStateModelRenderer pumpkinRenderer;
    private final BlockNeighborhood voidWorld;
    private final Color blockColor = new Color();

    public SnowGolemRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
        pumpkinRenderer = new BlockStateModelRenderer(resourcePack, textureGallery, renderSettings);
        voidWorld = new VoidBlockNeighborhood(resourcePack);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof SnowGolem golem)) return;

        // render pumpkin-head
        if (golem.isHasPumpkin()) {
            pumpkinRenderer.render(voidWorld, PUMPKIN, tileModel, blockColor);

            MatrixM4f transform = new MatrixM4f();
            transform.translate(-0.5f, -0.5f, -0.5f);
            transform.scale(0.6f, 0.6f, 0.6f);
            transform.translate(0f, 1.5f, 0f);
            tileModel.transform(transform);
        }

        // render model
        super.render(entity, block, SNOW_GOLEM, TintColorProvider.NO_TINT, tileModel);

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
