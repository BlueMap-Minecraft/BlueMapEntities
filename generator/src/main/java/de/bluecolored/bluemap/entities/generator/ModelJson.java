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
package de.bluecolored.bluemap.entities.generator;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The bluemap entity-model json. Null fields are left out when serialized, so the same records describe both a
 * geometry-model and a variant-model that only inherits from it.
 */
record ModelJson(
        @SerializedName("format_version") String formatVersion,
        String credit,
        @SerializedName("texture_size") int[] textureSize,
        String parent,
        Map<String, String> textures,
        List<Element> elements
) {

    private static final float ROTATION_EPSILON = 1e-4f;
    private static final String[] AXES = { "x", "y", "z" };

    record Element(String name, float[] from, float[] to, Rotation rotation, Map<String, Face> faces) {}

    /** Either axis/angle (single-axis, understood by older bluemap-versions too) or the x/y/z euler-angles. */
    record Rotation(Float angle, String axis, Float x, Float y, Float z, float[] origin) {}

    record Face(float[] uv, String texture, Integer tintindex) {}

    static ModelJson geometry(Geometry geometry, String texture, String minecraftVersion, String layer, Integer tintindex) {
        List<Element> elements = geometry.elements().stream().map(element -> element(element, tintindex)).toList();
        return new ModelJson(
                minecraftVersion,
                "Generated from minecraft " + minecraftVersion + " (" + layer + ")",
                new int[] { geometry.textureWidth(), geometry.textureHeight() },
                null,
                texture == null ? Map.of() : Map.of("0", texture),
                elements
        );
    }

    static ModelJson variant(String parent, String texture) {
        Map<String, String> textures = texture == null ? null : Map.of("0", texture);
        return new ModelJson(null, null, null, "minecraft:entity/" + parent, textures, null);
    }

    private static Element element(Geometry.Element element, Integer tintindex) {
        Map<String, Face> faces = new LinkedHashMap<>();
        element.faces().forEach((direction, uv) -> faces.put(direction, new Face(uv, "#0", tintindex)));

        return new Element(
                element.name(),
                element.from(),
                element.to(),
                rotation(element.rotation(), element.rotationOrigin()),
                faces
        );
    }

    private static Rotation rotation(float[] rotation, float[] origin) {
        if (rotation == null) return null;

        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(rotation[(axis + 1) % 3]) > ROTATION_EPSILON) continue;
            if (Math.abs(rotation[(axis + 2) % 3]) > ROTATION_EPSILON) continue;

            return new Rotation(rotation[axis], AXES[axis], null, null, null, origin);
        }

        return new Rotation(null, null, rotation[0], rotation[1], rotation[2], origin);
    }

}
