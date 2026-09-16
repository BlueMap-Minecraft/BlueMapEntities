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
import de.bluecolored.bluemap.entities.entity.Sheep;

public class SheepRenderer extends CustomResourceModelRenderer {

    /** {@code DyeColor#getTextureDiffuseColor} darkened by 0.75 (mc does it) */
    private static final int[] WOOL_COLORS = {
            0xE6E6E6, 0xBA6015, 0x953A8D, 0x2B86A3,
            0xBEA22D, 0x609517, 0xB6687F, 0x353B3D,
            0x757571, 0x107575, 0x66258A, 0x2D337F,
            0x623F25, 0x465D10, 0x84221C, 0x151518
    };

    private final ResourcePath<Model>
            SHEEP_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/main"),
            SHEEP_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep_baby/main"),
            SHEEP_ADULT_WOOL = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/wool"),
            SHEEP_BABY_WOOL = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep_baby/wool"),
            SHEEP_ADULT_WOOL_UNDERCOAT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/wool_undercoat");

    public SheepRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof Sheep sheep)) return;

        boolean isBaby = sheep.getAge() < 0;

        // render base body
        ResourcePath<Model> baseModel;
        if (isBaby) {
            baseModel = SHEEP_BABY;
        } else {
            baseModel = SHEEP_ADULT;
        }
        super.render(entity, block, baseModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);

        // render wool layer if not sheared, tinted by the wool-color
        if (!sheep.isSheared()) {
            int color = sheep.getColor() & 0xFF;
            if (color >= WOOL_COLORS.length) color = 0;
            int woolColor = 0xFF000000 | WOOL_COLORS[color];
            TintColorProvider woolTint = (index, target) -> target.set(woolColor, true);

            ResourcePath<Model> woolModel;
            if (isBaby) {
                woolModel = SHEEP_BABY_WOOL;
            } else {
                woolModel = SHEEP_ADULT_WOOL;
            }
            super.render(entity, block, woolModel.getResource(getModelProvider()), woolTint, tileModel);

            if (!isBaby && color != 0) {
                super.render(entity, block, SHEEP_ADULT_WOOL_UNDERCOAT.getResource(getModelProvider()), woolTint, tileModel);
            }
        }

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
