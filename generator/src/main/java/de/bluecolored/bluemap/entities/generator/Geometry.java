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

import java.util.List;
import java.util.Map;

/**
 * Geometry of one baked minecraft model-layer
 */
record Geometry(int textureWidth, int textureHeight, List<Element> elements, List<String> warnings) {

    /** The same geometry with only the elements whose part-name matches one of the given part-paths. */
    Geometry filter(List<String> parts, boolean keepMatching) {
        List<Element> filtered = elements.stream()
                .filter(element -> matches(element.name(), parts) == keepMatching)
                .toList();
        return new Geometry(textureWidth, textureHeight, filtered, warnings);
    }

    private static boolean matches(String name, List<String> parts) {
        return parts.stream().anyMatch(part -> name.equals(part) || name.startsWith(part + "/") || name.startsWith(part + "_"));
    }

    Geometry inflate(float grow) {
        if (grow == 0) return this;

        List<Element> grown = elements.stream()
                .map(element -> new Element(
                        element.name(),
                        offset(element.from(), -grow),
                        offset(element.to(), grow),
                        element.rotation(),
                        element.rotationOrigin(),
                        element.faces()
                ))
                .toList();
        return new Geometry(textureWidth, textureHeight, grown, warnings);
    }

    private static float[] offset(float[] position, float amount) {
        float[] offset = new float[position.length];
        for (int i = 0; i < position.length; i++) offset[i] = position[i] + amount;
        return offset;
    }

    /**
     * @param rotation nullable, {x, y, z} in degrees, applied by bluemap as rotateYXZ around {@code rotationOrigin}
     * @param faces direction ("north", "up", ...) to uv {x1, y1, x2, y2} in 16-space
     */
    record Element(
            String name,
            float[] from,
            float[] to,
            float[] rotation,
            float[] rotationOrigin,
            Map<String, float[]> faces
    ) {}

}
