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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Serializes a {@link ModelJson}. Coordinates, uvs and faces are written inline. Numbers are rounded
 */
final class ModelWriter {

    private static final int DECIMALS = 5;

    private static final Gson GSON = gson();

    private static Gson gson() {
        GsonBuilder builder = new GsonBuilder();
        register(builder, float[].class, ModelWriter::inline);
        register(builder, int[].class, ModelWriter::inline);
        register(builder, Float.class, ModelWriter::number);
        register(builder, ModelJson.Face.class, ModelWriter::inline);
        return builder.create();
    }

    private ModelWriter() {}

    static String write(Geometry geometry, String texture, String minecraftVersion, String layer, Integer tintindex) {
        return write(ModelJson.geometry(geometry, texture, minecraftVersion, layer, tintindex));
    }

    /** A texture-variant of an already written model, which only overrides its texture. */
    static String writeVariant(String parent, String texture) {
        return write(ModelJson.variant(parent, texture));
    }

    /** A model that is nothing but a second name for an already written one. */
    static String writeAlias(String target) {
        return write(ModelJson.variant(target, null));
    }

    private static String write(ModelJson model) {
        StringWriter target = new StringWriter();
        try (JsonWriter writer = new JsonWriter(target)) {
            writer.setIndent("\t");
            GSON.toJson(model, ModelJson.class, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return target + "\n";
    }

    private static String inline(ModelJson.Face face) {
        return "{\"uv\": " + inline(face.uv()) + ", \"texture\": \"" + face.texture() + "\""
                + (face.tintindex() == null ? "" : ", \"tintindex\": " + face.tintindex()) + "}";
    }

    private static String inline(float[] values) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (float value : values) joiner.add(number(value));
        return joiner.toString();
    }

    private static String inline(int[] values) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (int value : values) joiner.add(Integer.toString(value));
        return joiner.toString();
    }

    private static String number(float value) {
        BigDecimal rounded = BigDecimal.valueOf(value)
                .setScale(DECIMALS, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return rounded.signum() == 0 ? "0" : rounded.toPlainString();
    }

    private static <T> void register(GsonBuilder builder, Class<T> type, Function<T, String> format) {
        builder.registerTypeAdapter(type, new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                out.jsonValue(format.apply(value));
            }

            @Override
            public T read(JsonReader in) {
                throw new UnsupportedOperationException();
            }

        }.nullSafe());
    }

}
