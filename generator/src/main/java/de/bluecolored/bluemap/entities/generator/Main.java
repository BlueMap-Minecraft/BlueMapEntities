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
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.SharedConstants;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.Bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * One model per model-layer {@code entity/<model>/<layer>.json}.
 * One model per data-driven variant {@code entity/<model>/<layer>_<variant>.json}.
 * Baby-models & overlay-layers separate layers.
 */
public final class Main {

    private static final String CONFIG_TEXTURES = "textures.json";
    private static final String CONFIG_VARIANT_MODELS = "variant-models.json";
    private static final String CONFIG_EQUIPMENT = "equipment.json";
    private static final String CONFIG_SPLIT_PARTS = "split-parts.json";
    private static final String CONFIG_TINTS = "tints.json";

    /** worn equipment that is skipped for now */
    private static final Set<String> SKIPPED_LAYERS = Set.of("helmet", "chestplate", "boots", "leggings");
    private static final Set<String> SKIPPED_MODELS = Set.of("elytra", "elytra_baby");

    private final Arguments arguments;
    private final Report report = new Report();
    private Map<String, Map<String, List<String>>> splitParts = Map.of();
    private Map<String, Integer> tints = Map.of();

    private Main(Arguments arguments) {
        this.arguments = arguments;
    }

    public static void main(String[] args) throws Exception {
        new Main(Arguments.parse(args)).run();
    }

    private void run() throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        Map<ModelLayerLocation, LayerDefinition> layers = new TreeMap<>(Main::compare);
        layers.putAll(LayerDefinitions.createRoots());

        splitParts = readConfig(CONFIG_SPLIT_PARTS, new TypeToken<>() {});
        tints = readConfig(CONFIG_TINTS, new TypeToken<>() {});

        try (McAssets assets = new McAssets(arguments.clientJar)) {
            Set<String> models = new LinkedHashSet<>();
            layers.keySet().forEach(layer -> models.add(layer.model().getPath()));

            TextureResolver textures = new TextureResolver(
                    assets,
                    readConfig(CONFIG_TEXTURES, new TypeToken<Map<String, JsonElement>>() {}),
                    readConfig(CONFIG_VARIANT_MODELS, new TypeToken<Map<String, Map<String, String>>>() {}),
                    readConfig(CONFIG_EQUIPMENT, new TypeToken<Map<String, String>>() {}),
                    models
            );
            textures.warnings().forEach(report::warning);

            Set<String> written = new TreeSet<>();
            for (Map.Entry<ModelLayerLocation, LayerDefinition> entry : layers.entrySet())
                generate(entry.getKey(), entry.getValue(), textures, written);

            for (TextureResolver.Alias alias : textures.aliases()) {
                if (!written.contains(alias.target() + ".json")) continue;
                write(alias.path(), ModelWriter.writeAlias(alias.target()), written);
            }

            removeStale(written);
            writeIndex(written);
        }

        report.write(arguments.report);
        System.out.println(report.summary());
    }

    private void generate(
            ModelLayerLocation location, LayerDefinition definition,
            TextureResolver textures, Set<String> written
    ) throws IOException {
        String model = location.model().getPath();
        String layer = location.layer();
        String key = model + "#" + layer;

        if (!arguments.include.matcher(key).find()) return;
        if (arguments.exclude != null && arguments.exclude.matcher(key).find()) return;
        if (isEquipment(model, layer)) return;

        Geometry geometry = LayerConverter.convert(definition);
        geometry.warnings().forEach(warning -> report.warning(key + ": " + warning));

        if (geometry.elements().isEmpty()) {
            report.empty(key);
            return;
        }

        TextureResolver.Resolution resolution = textures.resolve(model, layer);
        if (resolution.isEmpty()) report.unresolved(key);
        else report.resolved(key, resolution.source(), resolution.variants().size());

        // parts minecraft only shows sometimes (the chest of a donkey, ...) become models of their own
        Geometry remaining = geometry;
        for (Map.Entry<String, List<String>> group : splitParts.getOrDefault(key, Map.of()).entrySet()) {
            Geometry parts = geometry.filter(group.getValue(), true);
            if (parts.elements().isEmpty()) {
                report.warning(key + ": no parts matched the split-group '" + group.getKey() + "'");
                continue;
            }

            writeModel(model + "/" + group.getKey(), parts, resolution, key, written);
            remaining = remaining.filter(group.getValue(), false);
        }

        writeModel(model + "/" + layer, remaining, resolution, key, written);
    }

    private static boolean isEquipment(String model, String layer) {
        return SKIPPED_LAYERS.contains(layer) || SKIPPED_MODELS.contains(model) || model.endsWith("_armor");
    }

    private void writeModel(
            String path, Geometry geometry, TextureResolver.Resolution resolution,
            String key, Set<String> written
    ) throws IOException {
        write(path, ModelWriter.write(geometry, resolution.texture(), arguments.minecraftVersion, key, tints.get(key)), written);

        for (TextureResolver.Variant variant : resolution.variants())
            write(path + "_" + variant.suffix(), ModelWriter.writeVariant(path, variant.texture()), written);
    }

    private void write(String name, String generated, Set<String> written) throws IOException {
        String path = name + ".json";
        written.add(path);

        Path override = arguments.overrides.resolve(path);
        boolean isOverridden = Files.isRegularFile(override);
        String content = isOverridden ? Files.readString(override, StandardCharsets.UTF_8) : generated;
        if (isOverridden) report.overridden(path);

        Path file = arguments.out.resolve(path);
        if (Files.isRegularFile(file)) {
            if (Files.readString(file, StandardCharsets.UTF_8).equals(content)) {
                report.unchanged(path);
                return;
            }
            report.changed(path);
        } else {
            report.added(path);
        }

        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    /** Deletes models that were generated by a previous run but do not exist in minecraft anymore. */
    private void removeStale(Set<String> written) throws IOException {
        for (String path : readIndex()) {
            if (written.contains(path)) continue;

            Path file = arguments.out.resolve(path);
            if (Files.deleteIfExists(file)) report.removed(path);

            Path directory = file.getParent();
            while (directory != null && directory.startsWith(arguments.out) && !directory.equals(arguments.out)) {
                try (var entries = Files.list(directory)) {
                    if (entries.findAny().isPresent()) break;
                }
                Files.delete(directory);
                directory = directory.getParent();
            }
        }
    }

    private List<String> readIndex() throws IOException {
        if (!Files.isRegularFile(arguments.index)) return List.of();
        return Files.readAllLines(arguments.index, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
    }

    private void writeIndex(Set<String> written) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# generated by the :generator module - do not edit");
        lines.addAll(written);
        Files.createDirectories(arguments.index.getParent());
        Files.write(arguments.index, lines, StandardCharsets.UTF_8);
    }

    private <T> T readConfig(String name, TypeToken<T> type) throws IOException {
        Path file = arguments.config.resolve(name);
        if (!Files.isRegularFile(file)) return new Gson().fromJson("{}", type);
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T config = new Gson().fromJson(reader, type);
            return config != null ? config : new Gson().fromJson("{}", type);
        }
    }

    private static int compare(ModelLayerLocation a, ModelLayerLocation b) {
        int model = a.model().toString().compareTo(b.model().toString());
        return model != 0 ? model : a.layer().compareTo(b.layer());
    }

    private record Arguments(
            Path clientJar, Path out, Path config, Path overrides, Path report, Path index,
            String minecraftVersion, Pattern include, Pattern exclude
    ) {

        static Arguments parse(String[] args) {
            Map<String, String> values = new HashMap<>();
            for (int i = 0; i < args.length - 1; i += 2) values.put(args[i], args[i + 1]);

            Path config = Path.of(required(values, "--config"));
            return new Arguments(
                    Path.of(required(values, "--client")),
                    Path.of(required(values, "--out")),
                    config,
                    Path.of(values.getOrDefault("--overrides", config.resolveSibling("overrides").toString())),
                    Path.of(values.getOrDefault("--report", "generation-report.txt")),
                    Path.of(values.getOrDefault("--index", config.resolveSibling("generated-index.txt").toString())),
                    values.getOrDefault("--mc-version", "unknown"),
                    Pattern.compile(values.getOrDefault("--include", ".*")),
                    values.containsKey("--exclude") ? Pattern.compile(values.get("--exclude")) : null
            );
        }

        private static String required(Map<String, String> values, String key) {
            String value = values.get(key);
            if (value == null) throw new IllegalArgumentException("missing argument: " + key);
            return value;
        }

    }

}
