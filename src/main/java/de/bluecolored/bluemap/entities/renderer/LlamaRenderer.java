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
import de.bluecolored.bluemap.entities.Addon;
import de.bluecolored.bluemap.entities.data.EquipmentData;
import de.bluecolored.bluemap.entities.data.ItemStack;
import de.bluecolored.bluemap.entities.entity.Llama;
import de.bluecolored.bluemap.entities.entity.TraderLlama;

import java.util.Optional;

public class LlamaRenderer extends CustomResourceModelRenderer {

    private final ResourcePath<Model>
            LLAMA_CHEST = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, "entity/llama/llama_chest");

    public LlamaRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);
    }

    @Override
    public void render(Entity entity, BlockNeighborhood block, Part part, TileModelView tileModel) {
        if (!(entity instanceof Llama llama)) return;

        boolean isTraderLlama = entity instanceof TraderLlama;
        boolean isBaby = llama.getAge() < 0;

        // base model "entity/llama/color/llama_{age}_{color}"
        String baseModelPath = "entity/llama/color/llama_" + (isBaby ? "baby_" : "adult_");

        switch (llama.getVariant()) {
            case CREAMY -> baseModelPath += "creamy";
            case WHITE -> baseModelPath += "white";
            case BROWN -> baseModelPath += "brown";
            case GRAY -> baseModelPath += "gray";
        };

        ResourcePath<Model> baseModel = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, baseModelPath);
        super.render(entity, block, baseModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);


        // chest model (only if adult)
        if (llama.isWithChest() && !isBaby) {
            super.render(entity, block, LLAMA_CHEST.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        }


        // decoration model "entity/llama/carpet/llama_{age}_{color}"
        String decorationModelPath = "entity/llama/carpet/llama_" + (isBaby ? "baby_" : "adult_");

        switch (
                Optional.ofNullable(llama.getEquipment())
                        .map(EquipmentData::getBody)
                        .map(ItemStack::getId)
                        .map(Key::getFormatted)
                        .orElse("")
        ) {
            case "minecraft:black_carpet" -> decorationModelPath += "black";
            case "minecraft:blue_carpet" ->  decorationModelPath += "blue";
            case "minecraft:brown_carpet" -> decorationModelPath += "brown";
            case "minecraft:cyan_carpet" -> decorationModelPath += "cyan";
            case "minecraft:gray_carpet" -> decorationModelPath += "gray";
            case "minecraft:green_carpet" -> decorationModelPath += "green";
            case "minecraft:light_blue_carpet" -> decorationModelPath += "light_blue";
            case "minecraft:light_gray_carpet" -> decorationModelPath += "light_gray";
            case "minecraft:lime_carpet" -> decorationModelPath += "lime";
            case "minecraft:magenta_carpet" -> decorationModelPath += "magenta";
            case "minecraft:orange_carpet" -> decorationModelPath += "orange";
            case "minecraft:pink_carpet" -> decorationModelPath += "pink";
            case "minecraft:purple_carpet" -> decorationModelPath += "purple";
            case "minecraft:red_carpet" -> decorationModelPath += "red";
            case "minecraft:white_carpet" -> decorationModelPath += "white";
            case "minecraft:yellow_carpet" -> decorationModelPath += "yellow";
            default -> decorationModelPath = null;
        };

        if (decorationModelPath == null && isTraderLlama) {
            // trader llama without carpet uses special decoration model
            decorationModelPath = "entity/llama/carpet/llama_" + (isBaby ? "baby" : "adult") + "_trader_llama";
        }

        if (decorationModelPath != null) {
            ResourcePath<Model> decorationModel = new ResourcePath<>(Key.MINECRAFT_NAMESPACE, decorationModelPath);
            super.render(entity, block, decorationModel.getResource(getModelProvider()), TintColorProvider.NO_TINT, tileModel);
        }

        // apply part transform
        if (part.isTransformed())
            tileModel.transform(part.getTransformMatrix());
    }

}
