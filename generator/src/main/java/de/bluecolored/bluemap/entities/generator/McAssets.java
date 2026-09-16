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
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


final class McAssets implements AutoCloseable {

    private static final String TEXTURE_PREFIX = "assets/minecraft/textures/";
    private static final String VARIANT_PREFIX = "data/minecraft/";
    private static final String RENDERER_PREFIX = "net/minecraft/client/renderer/entity/";
    private static final Pattern TEXTURE_REFERENCE = Pattern.compile("textures/entity/[a-z0-9_/]+");

    private final ZipFile jar;
    private final Set<String> textures = new HashSet<>();
    private final Map<String, List<String>> rendererTextures = new HashMap<>();
    private final Map<String, List<Variant>> variants = new TreeMap<>();

    /**
     * A single data-driven entity-variant, with its textures keyed by sub-type
     */
    record Variant(String entity, String name, String model, Map<String, String> adult, Map<String, String> baby) {}

    McAssets(Path clientJar) throws IOException {
        this.jar = new ZipFile(clientJar.toFile());
        index();
    }

    boolean hasTexture(String texture) {
        return textures.contains(texture);
    }

    List<String> texturesNamed(String name) {
        String suffix = "/" + name;
        return textures.stream()
                .filter(texture -> texture.startsWith("entity/") && texture.endsWith(suffix))
                .sorted()
                .toList();
    }

    List<String> texturesStartingWith(String prefix) {
        return textures.stream()
                .filter(texture -> texture.startsWith("entity/")
                        && texture.substring(texture.lastIndexOf('/') + 1).startsWith(prefix))
                .sorted()
                .toList();
    }

    List<String> texturesIn(String folder) {
        String prefix = folder + "/";
        return textures.stream()
                .filter(texture -> texture.startsWith(prefix) && texture.indexOf('/', prefix.length()) < 0)
                .sorted()
                .toList();
    }

    /** All variants of the given entity-name, in registry-order. */
    List<Variant> variants(String entity) {
        return variants.getOrDefault(entity, List.of());
    }

    Map<String, List<Variant>> variants() {
        return Collections.unmodifiableMap(variants);
    }

    /** Texture-paths referenced by the compiled renderer- and render-layer-classes, keyed by simple class-name. */
    Map<String, List<String>> rendererTextures() {
        return Collections.unmodifiableMap(rendererTextures);
    }

    private void index() throws IOException {
        var entries = jar.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String path = entry.getName();

            if (path.startsWith(TEXTURE_PREFIX) && path.endsWith(".png")) {
                textures.add(path.substring(TEXTURE_PREFIX.length(), path.length() - ".png".length()));
                continue;
            }

            if (path.startsWith(RENDERER_PREFIX) && path.endsWith(".class")) {
                indexRendererTextures(entry);
                continue;
            }

            if (path.startsWith(VARIANT_PREFIX) && path.endsWith(".json")) indexVariant(entry);
        }
    }

    private void indexRendererTextures(ZipEntry entry) throws IOException {
        String name = entry.getName();
        name = name.substring(name.lastIndexOf('/') + 1, name.length() - ".class".length());

        String content;
        try (var in = jar.getInputStream(entry)) {
            content = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }

        List<String> found = new ArrayList<>();
        Matcher matcher = TEXTURE_REFERENCE.matcher(content);
        while (matcher.find()) found.add(matcher.group());
        if (!found.isEmpty()) rendererTextures.put(name, found);
    }

    private void indexVariant(ZipEntry entry) throws IOException {
        String path = entry.getName().substring(VARIANT_PREFIX.length());
        int slash = path.indexOf('/');
        if (slash < 0) return;

        String registry = path.substring(0, slash);
        if (!registry.endsWith("_variant")) return;
        String entity = registry.substring(0, registry.length() - "_variant".length());
        String name = path.substring(slash + 1, path.length() - ".json".length());

        JsonObject json;
        try (Reader reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
            json = new Gson().fromJson(reader, JsonObject.class);
        }

        Map<String, String> adult = assets(json, "asset_id", "assets");
        if (adult.isEmpty()) return;
        Map<String, String> baby = assets(json, "baby_asset_id", "baby_assets");

        String model = json.has("model") ? json.get("model").getAsString() : null;
        variants.computeIfAbsent(entity, key -> new ArrayList<>()).add(new Variant(entity, name, model, adult, baby));
    }

    private static Map<String, String> assets(JsonObject json, String singleKey, String mapKey) {
        Map<String, String> assets = new LinkedHashMap<>();
        if (json.has(singleKey)) assets.put("", texture(json.get(singleKey).getAsString()));
        if (json.has(mapKey)) {
            for (Map.Entry<String, JsonElement> asset : json.getAsJsonObject(mapKey).entrySet())
                assets.put(asset.getKey(), texture(asset.getValue().getAsString()));
        }
        return assets;
    }

    private static String texture(String assetId) {
        return assetId.startsWith("minecraft:") ? assetId.substring("minecraft:".length()) : assetId;
    }

    @Override
    public void close() throws IOException {
        jar.close();
    }

}
