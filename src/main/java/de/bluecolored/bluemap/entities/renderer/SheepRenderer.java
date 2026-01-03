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

    private final ResourcePath<Model>
            SHEEP_ADULT = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/adult"),
            SHEEP_BABY = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/baby"),
            SHEEP_ADULT_WOOL = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/adult_wool"),
            SHEEP_BABY_WOOL = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/sheep/baby_wool");

    public SheepRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof Sheep sheep)) return;

        // render base body
        ResourcePath<Model> baseModel;
        if (sheep.getAge() < 0) {
            baseModel = SHEEP_BABY;
        } else {
            baseModel = SHEEP_ADULT;
        }
        super.render(entity, block, baseModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);

        // render wool layer if not sheared (TODO wool tint)
        if (!sheep.isSheared()) {
            ResourcePath<Model> woolModel;
            if (sheep.getAge() < 0) {
                woolModel = SHEEP_BABY_WOOL;
            } else {
                woolModel = SHEEP_ADULT_WOOL;
            }
            super.render(entity, block, woolModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        }

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
