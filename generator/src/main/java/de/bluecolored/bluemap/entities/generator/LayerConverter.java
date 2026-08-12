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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minecraft builds entity-models in a y-down / x-mirrored space with the origin 1.5 blocks above the feet, so every position is converted with
 * {@code x' = -x}, {@code y' = 24 - y}, {@code z' = z}. Part-transforms are flattened into a single rotation around the parts pivot
 * </p>
 */
final class LayerConverter {

    private static final float[] FLIP = { -1, -1, 1 };
    private static final float Y_OFFSET = 24;

    /**
     * Some models have hardcoded offsets just to annoy me
     */
    private static final Map<String, Float> Y_OFFSETS = Map.of(
            "boat", 6f,
            "chest_boat", 6f,
            "minecart", 6f,
            "ender_dragon", 52f,
            "sulfur_cube", 8f
    );
    private static final float POSITION_EPSILON = 1e-3f;
    private static final float ANGLE_EPSILON = 1e-4f;
    private static final String[] DIRECTIONS = { "down", "up", "north", "south", "west", "east" };

    private LayerConverter() {}

    static float yOffset(String model) {
        String family = model.contains("/") ? model.substring(0, model.indexOf('/')) : model;
        Float offset = Y_OFFSETS.get(family);
        if (offset == null && family.endsWith("_minecart")) offset = Y_OFFSETS.get("minecart");
        if (offset == null && family.endsWith("_small")) offset = Y_OFFSETS.get(base(family));
        return offset != null ? offset : Y_OFFSET;
    }

    private static String base(String family) {
        return family.substring(0, family.lastIndexOf('_'));
    }

    static Geometry convert(LayerDefinition layer, float yOffset) {
        int[] textureSize = textureSize(layer);
        List<Geometry.Element> elements = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        layer.bakeRoot().visit(new PoseStack(), (pose, path, index, cube) -> {
            String name = name(path, index);
            Geometry.Element element = convertCube(pose.pose(), name, cube, yOffset, warnings);
            if (finite(element)) elements.add(element);
            else warnings.add(name + ": dropped, contains a non-finite coordinate");
        });

        elements.sort(Comparator.comparing(Geometry.Element::name));
        return new Geometry(textureSize[0], textureSize[1], elements, warnings);
    }

    private static boolean finite(Geometry.Element element) {
        List<float[]> values = new ArrayList<>(element.faces().values());
        values.add(element.from());
        values.add(element.to());
        if (element.rotation() != null) {
            values.add(element.rotation());
            values.add(element.rotationOrigin());
        }

        for (float[] value : values)
            for (float number : value)
                if (!Float.isFinite(number)) return false;
        return true;
    }

    private static String name(String path, int index) {
        String name = path.startsWith("/") ? path.substring(1) : path;
        return index == 0 ? name : name + "_" + index;
    }

    private static Geometry.Element convertCube(Matrix4f matrix, String name, ModelPart.Cube cube, float yOffset, List<String> warnings) {

        // linear part of the accumulated part-transform, columns are the local axes
        float[][] linear = {
                { matrix.m00(), matrix.m10(), matrix.m20() },
                { matrix.m01(), matrix.m11(), matrix.m21() },
                { matrix.m02(), matrix.m12(), matrix.m22() }
        };

        float[] scale = new float[3];
        for (int col = 0; col < 3; col++) {
            scale[col] = (float) Math.sqrt(
                    linear[0][col] * linear[0][col] +
                    linear[1][col] * linear[1][col] +
                    linear[2][col] * linear[2][col]
            );
            if (scale[col] < ANGLE_EPSILON) {
                warnings.add(name + ": degenerate part-scale, using 1");
                scale[col] = 1;
            }
            for (int row = 0; row < 3; row++) linear[row][col] /= scale[col];
        }

        // conjugate the rotation with the coordinate-flip, so an un-rotated part stays un-rotated
        float[][] rotation = new float[3][3];
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                rotation[row][col] = FLIP[row] * FLIP[col] * linear[row][col];

        // the parts pivot becomes the rotation-origin of the element
        float[] origin = {
                FLIP[0] * matrix.m30() * 16,
                FLIP[1] * matrix.m31() * 16 + yOffset,
                FLIP[2] * matrix.m32() * 16
        };

        // the vertices, not minX/maxX, carry the cube-deformation ("grow") minecraft applies to a cube
        float[][] bounds = bounds(cube);
        float[] min = { bounds[0][0] * scale[0], bounds[0][1] * scale[1], bounds[0][2] * scale[2] };
        float[] max = { bounds[1][0] * scale[0], bounds[1][1] * scale[1], bounds[1][2] * scale[2] };
        float[] localMin = { -max[0], -max[1], min[2] };
        float[] localMax = { -min[0], -min[1], max[2] };

        float[] from = new float[3], to = new float[3];
        for (int i = 0; i < 3; i++) {
            from[i] = localMin[i] + origin[i];
            to[i] = localMax[i] + origin[i];
        }

        float[] euler = euler(rotation, name, warnings);
        Map<String, float[]> faces = faces(cube, localMin, localMax, scale, name, warnings);

        return new Geometry.Element(name, from, to, euler, euler == null ? null : origin, faces);
    }

    private static float[][] bounds(ModelPart.Cube cube) {
        float[] min = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
        float[] max = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };

        for (ModelPart.Polygon polygon : cube.polygons) {
            for (ModelPart.Vertex vertex : polygon.vertices()) {
                float[] position = { vertex.x(), vertex.y(), vertex.z() };
                for (int i = 0; i < 3; i++) {
                    min[i] = Math.min(min[i], position[i]);
                    max[i] = Math.max(max[i], position[i]);
                }
            }
        }

        if (min[0] > max[0]) return new float[][] {
                { cube.minX, cube.minY, cube.minZ },
                { cube.maxX, cube.maxY, cube.maxZ }
        };
        return new float[][] { min, max };
    }

    private static Map<String, float[]> faces(
            ModelPart.Cube cube, float[] localMin, float[] localMax, float[] scale,
            String name, List<String> warnings
    ) {
        Map<String, float[]> faces = new LinkedHashMap<>();
        for (ModelPart.Polygon polygon : cube.polygons) {
            int direction = direction(polygon.normal());
            if (direction < 0) {
                warnings.add(name + ": skipped face with non-axis-aligned normal " + polygon.normal());
                continue;
            }

            float[][] corners = corners(direction, localMin, localMax);
            float[][] uvs = new float[4][];
            for (int i = 0; i < 4; i++) {
                uvs[i] = uv(polygon, corners[i], scale);
                if (uvs[i] == null) {
                    warnings.add(name + ": no matching vertex for the " + DIRECTIONS[direction] + "-face");
                    break;
                }
            }
            if (uvs[3] == null) continue;

            // bluemap maps the uv-corners (x1,y2) (x2,y2) (x2,y1) (x1,y1) onto the face-corners in that order
            faces.put(DIRECTIONS[direction], new float[] { uvs[0][0], uvs[2][1], uvs[1][0], uvs[0][1] });
        }
        return faces;
    }

    private static float[] uv(ModelPart.Polygon polygon, float[] corner, float[] scale) {
        float x = FLIP[0] * corner[0] / scale[0];
        float y = FLIP[1] * corner[1] / scale[1];
        float z = FLIP[2] * corner[2] / scale[2];

        for (ModelPart.Vertex vertex : polygon.vertices()) {
            if (
                    Math.abs(vertex.x() - x) < POSITION_EPSILON &&
                    Math.abs(vertex.y() - y) < POSITION_EPSILON &&
                    Math.abs(vertex.z() - z) < POSITION_EPSILON
            ) return new float[] { vertex.u() * 16, vertex.v() * 16 };
        }
        return null;
    }

    private static int direction(Vector3fc normal) {
        float x = FLIP[0] * normal.x(), y = FLIP[1] * normal.y(), z = FLIP[2] * normal.z();
        if (y < -0.9f) return 0;
        if (y > 0.9f) return 1;
        if (z < -0.9f) return 2;
        if (z > 0.9f) return 3;
        if (x < -0.9f) return 4;
        if (x > 0.9f) return 5;
        return -1;
    }

    private static float[][] corners(int direction, float[] min, float[] max) {
        return switch (direction) {
            case 0 -> new float[][] {
                    { min[0], min[1], min[2] }, { max[0], min[1], min[2] },
                    { max[0], min[1], max[2] }, { min[0], min[1], max[2] } };
            case 1 -> new float[][] {
                    { min[0], max[1], max[2] }, { max[0], max[1], max[2] },
                    { max[0], max[1], min[2] }, { min[0], max[1], min[2] } };
            case 2 -> new float[][] {
                    { max[0], min[1], min[2] }, { min[0], min[1], min[2] },
                    { min[0], max[1], min[2] }, { max[0], max[1], min[2] } };
            case 3 -> new float[][] {
                    { min[0], min[1], max[2] }, { max[0], min[1], max[2] },
                    { max[0], max[1], max[2] }, { min[0], max[1], max[2] } };
            case 4 -> new float[][] {
                    { min[0], min[1], min[2] }, { min[0], min[1], max[2] },
                    { min[0], max[1], max[2] }, { min[0], max[1], min[2] } };
            default -> new float[][] {
                    { max[0], min[1], max[2] }, { max[0], min[1], min[2] },
                    { max[0], max[1], min[2] }, { max[0], max[1], max[2] } };
        };
    }

    /**
     * Decomposes a rotation-matrix into the y-x-z euler-angles (in degrees)
     */
    private static float[] euler(float[][] r, String name, List<String> warnings) {
        double x, y, z;
        if (Math.abs(r[1][2]) < 0.99999) {
            x = Math.asin(-r[1][2]);
            y = Math.atan2(r[0][2], r[2][2]);
            z = Math.atan2(r[1][0], r[1][1]);
        } else {
            x = r[1][2] < 0 ? Math.PI / 2 : -Math.PI / 2;
            y = Math.atan2(-r[2][0], r[0][0]);
            z = 0;
        }

        float[] euler = {
                (float) Math.toDegrees(x),
                (float) Math.toDegrees(y),
                (float) Math.toDegrees(z)
        };

        float error = error(r, euler);
        if (error > 1e-3f) warnings.add(name + ": rotation could not be decomposed exactly (error " + error + ")");

        if (Math.abs(euler[0]) < ANGLE_EPSILON && Math.abs(euler[1]) < ANGLE_EPSILON && Math.abs(euler[2]) < ANGLE_EPSILON)
            return null;
        return euler;
    }

    private static float error(float[][] r, float[] euler) {
        double x = Math.toRadians(euler[0]), y = Math.toRadians(euler[1]), z = Math.toRadians(euler[2]);
        double sx = Math.sin(x), cx = Math.cos(x);
        double sy = Math.sin(y), cy = Math.cos(y);
        double sz = Math.sin(z), cz = Math.cos(z);

        double[][] check = {
                { cy * cz + sy * sx * sz, -cy * sz + sy * sx * cz, sy * cx },
                { cx * sz, cx * cz, -sx },
                { -sy * cz + cy * sx * sz, sy * sz + cy * sx * cz, cy * cx }
        };

        float error = 0;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                error = Math.max(error, (float) Math.abs(check[row][col] - r[row][col]));
        return error;
    }

    private static int[] textureSize(LayerDefinition layer) {
        try {
            Field materialField = LayerDefinition.class.getDeclaredField("material");
            materialField.setAccessible(true);
            Object material = materialField.get(layer);

            Field width = material.getClass().getDeclaredField("xTexSize");
            Field height = material.getClass().getDeclaredField("yTexSize");
            width.setAccessible(true);
            height.setAccessible(true);
            return new int[] { width.getInt(material), height.getInt(material) };
        } catch (ReflectiveOperationException e) {
            return new int[] { 64, 64 };
        }
    }

}
