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

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Finds the vanilla textures for a model-layer.
 */
final class TextureResolver {

    private static final String BABY_SUFFIX = "_baby";
    private static final String EQUIPMENT = "entity/equipment/";
    private static final Set<String> ARMOR_LAYERS = Set.of("helmet", "chestplate", "boots", "leggings");
    private static final List<String> ALIAS_SUFFIXES = List.of("_small", "_medium", "_big", "_large", "_no_hat");

    private final McAssets assets;
    private final Map<String, JsonElement> textureOverrides;
    private final Map<String, Map<String, String>> variantModelOverrides;
    private final Map<String, String> equipmentOverrides;
    private final Set<String> knownModels;
    private final Map<String, List<Variant>> dataVariants = new TreeMap<>();
    private final List<Alias> aliases = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /** A texture-only variation of a model-layer, written as an additional model with the given name-suffix. */
    record Variant(String suffix, String texture) {}

    /** A model that only points to another one, so variants stay reachable under the entity they belong to. */
    record Alias(String path, String target) {}

    record Resolution(String texture, List<Variant> variants, String source) {

        boolean isEmpty() {
            return texture == null && variants.isEmpty();
        }

    }

    TextureResolver(
            McAssets assets,
            Map<String, JsonElement> textureOverrides,
            Map<String, Map<String, String>> variantModelOverrides,
            Map<String, String> equipmentOverrides,
            Set<String> knownModels
    ) {
        this.assets = assets;
        this.textureOverrides = textureOverrides;
        this.variantModelOverrides = variantModelOverrides;
        this.equipmentOverrides = equipmentOverrides;
        this.knownModels = knownModels;
        indexDataVariants();
    }

    List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }

    List<Alias> aliases() {
        return Collections.unmodifiableList(aliases);
    }

    Resolution resolve(String model, String layer) {
        JsonElement override = textureOverrides.get(model + "#" + layer);
        if (override != null) return fromOverride(override);

        if (layer.equals("main")) {
            List<Variant> variants = dataVariants.get(model);
            if (variants != null) return new Resolution(null, variants, "variant-registry");
        }

        List<Variant> equipment = equipment(model, layer);
        if (!equipment.isEmpty()) return new Resolution(null, equipment, "equipment");

        for (String candidate : conventions(model, layer))
            if (assets.hasTexture(candidate)) return new Resolution(candidate, List.of(), "convention");

        String byName = byName(model, layer);
        if (byName != null) return new Resolution(byName, List.of(), "texture-name");

        List<Variant> family = family(model, layer);
        if (family.size() == 1) return new Resolution(family.getFirst().texture(), List.of(), "texture-family");
        if (!family.isEmpty()) return new Resolution(null, family, "texture-family");

        List<String> fromClasses = fromRendererClasses(model, layer);
        for (String name : names(model, layer))
            for (String candidate : fromClasses)
                if (compact(candidate.substring(candidate.lastIndexOf('/') + 1)).equals(compact(name)))
                    return new Resolution(candidate, List.of(), "renderer-class");
        if (fromClasses.size() == 1) return new Resolution(fromClasses.getFirst(), List.of(), "renderer-class");
        if (!fromClasses.isEmpty()) {
            List<Variant> variants = new ArrayList<>();
            fromClasses.forEach(texture -> variants.add(variant(texture, null)));
            return new Resolution(null, variants, "renderer-class");
        }

        return new Resolution(null, List.of(), "unresolved");
    }

    /** A pinned texture: a single texture, an array (suffix = texture-name) or an object (suffix = key). */
    private Resolution fromOverride(JsonElement override) {
        List<Variant> variants = new ArrayList<>();

        if (override.isJsonArray())
            for (JsonElement texture : override.getAsJsonArray()) variants.add(variant(texture.getAsString(), null));
        else if (override.isJsonObject())
            override.getAsJsonObject().asMap()
                    .forEach((suffix, texture) -> variants.add(new Variant(suffix, texture.getAsString())));
        else
            return new Resolution(override.getAsString(), List.of(), "config");

        return new Resolution(null, variants, "config");
    }

    /** Armor, saddles and other equipment: one variant per material-texture of the matching equipment-folder. */
    private List<Variant> equipment(String model, String layer) {
        String folder = equipmentFolder(model, layer);
        if (folder == null) return List.of();

        List<Variant> variants = new ArrayList<>();
        for (String texture : assets.texturesIn(EQUIPMENT + folder)) variants.add(variant(texture, null));
        return variants;
    }

    private String equipmentFolder(String model, String layer) {
        String override = equipmentOverrides.get(model + "#" + layer);
        if (override != null) return override;

        String folder = null;
        if (ARMOR_LAYERS.contains(layer)) {
            folder = layer.equals("leggings") ? "humanoid_leggings" : "humanoid";
            if (model.endsWith(BABY_SUFFIX) && exists(folder + BABY_SUFFIX)) folder += BABY_SUFFIX;
        } else if (layer.equals("saddle")) {
            folder = model + "_saddle";
        } else if (layer.equals("decor")) {
            folder = base(model) + "_body";
        } else if (layer.equals("main") && (model.endsWith("_armor") || model.endsWith("_harness"))) {
            folder = model.substring(0, model.lastIndexOf('_')) + "_body";
        }

        return folder != null && exists(folder) ? folder : null;
    }

    private boolean exists(String folder) {
        return !assets.texturesIn(EQUIPMENT + folder).isEmpty();
    }

    private List<String> conventions(String model, String layer) {
        String base = base(model);
        List<String> candidates = new ArrayList<>();

        if (layer.equals("main")) {
            candidates.add("entity/" + base + "/" + model);
            candidates.add("entity/" + model + "/" + model);
            candidates.add("entity/" + model);
            candidates.add("entity/" + base + "/" + base);
            candidates.add("entity/" + base);
        } else {
            // baby-textures of a layer keep the baby-suffix last
            if (!model.equals(base)) {
                candidates.add("entity/" + base + "/" + base + "_" + layer + BABY_SUFFIX);
                candidates.add("entity/" + base + "/" + layer + BABY_SUFFIX);
            }

            candidates.add("entity/" + base + "/" + model + "_" + layer);
            candidates.add("entity/" + base + "/" + base + "_" + layer);
            candidates.add("entity/" + model + "/" + model + "_" + layer);
            candidates.add("entity/" + base + "/" + layer);
            candidates.add("entity/" + model + "/" + layer);
            candidates.add("entity/" + layer);
        }

        return candidates;
    }

    /** Vanilla often keeps a texture in the folder of its entity-family ({@code entity/zombie/husk}), so search by name. */
    private String byName(String model, String layer) {
        for (String name : names(model, layer)) {
            List<String> textures = assets.texturesNamed(name);
            if (textures.size() == 1) return textures.getFirst();
        }
        return null;
    }

    /** The texture file-names an entity-model may use, most specific first. */
    private List<String> names(String model, String layer) {
        List<String> names = new ArrayList<>();
        for (String alias : aliases(model)) {
            if (layer.equals("main")) {
                names.add(alias);
            } else {
                if (alias.endsWith(BABY_SUFFIX)) {
                    names.add(base(alias) + "_" + layer + BABY_SUFFIX);
                    names.add(base(alias) + "_" + layer + "_layer" + BABY_SUFFIX);
                }
                names.add(alias + "_" + layer);
                names.add(alias + "_" + layer + "_layer");
            }
        }
        return names;
    }

    /** The model-id plus the shortened forms minecraft uses */
    private static List<String> aliases(String model) {
        Set<String> aliases = new LinkedHashSet<>();
        aliases.add(model);
        aliases.add(base(model));

        for (String alias : List.copyOf(aliases)) {
            for (String suffix : ALIAS_SUFFIXES)
                if (alias.endsWith(suffix)) aliases.add(alias.substring(0, alias.length() - suffix.length()));
        }
        return List.copyOf(aliases);
    }

    /**
     * Variants that are hard-coded in minecraft instead of being data-driven (axolotl, mooshroom, horse, ...)
     */
    private List<Variant> family(String model, String layer) {
        boolean baby = model.endsWith(BABY_SUFFIX);

        for (String name : names(model, layer)) {
            List<Variant> variants = new ArrayList<>();
            for (String texture : assets.texturesStartingWith(name + "_")) {
                if (texture.endsWith(BABY_SUFFIX) != baby) continue;

                String suffix = texture.substring(texture.lastIndexOf('/') + 1 + name.length() + 1);
                if (baby) suffix = suffix.substring(0, suffix.length() - BABY_SUFFIX.length());
                variants.add(new Variant(suffix, texture));
            }
            if (!variants.isEmpty()) return variants;
        }
        return List.of();
    }

    private List<String> fromRendererClasses(String model, String layer) {
        List<String> names = aliases(model).stream().map(TextureResolver::camel).toList();
        String layerName = camel(layer);

        Set<String> candidates = new LinkedHashSet<>();
        assets.rendererTextures().forEach((className, textures) -> {
            if (names.stream().noneMatch(className::startsWith)) return;

            boolean matchesLayer = layer.equals("main")
                    ? names.stream().anyMatch(name -> className.equals(name + "Renderer"))
                    : className.contains(layerName);
            if (!matchesLayer) return;

            textures.forEach(texture -> {
                String id = texture.substring("textures/".length());
                if (assets.hasTexture(id)) candidates.add(id);
            });
        });

        return List.copyOf(candidates);
    }

    private void indexDataVariants() {
        assets.variants().forEach((entity, variants) -> {
            for (McAssets.Variant variant : variants) {
                String model = targetModel(entity, variant.model());
                if (model == null) {
                    if (knownModels.contains(entity))
                        warnings.add("variant " + entity + "/" + variant.name() + ": no model '" + variant.model() + "'");
                    continue;
                }

                Map<String, String> adult = variant.adult();
                Map<String, String> baby = variant.baby().isEmpty() ? variant.adult() : variant.baby();

                // not every variant-model has a baby-version (cold_pig has none), then the plain baby-model is used
                String babyModel = model + BABY_SUFFIX;
                if (!knownModels.contains(babyModel)) babyModel = entity + BABY_SUFFIX;

                register(model, variant, adult);
                register(babyModel, variant, baby);

                // variants with an own model (cold_cow, cold_chicken, ...) also get an alias under the entity itself,
                // so a renderer can always look up "entity/<entity>/main_<variant>"
                if (model.equals(entity)) continue;
                alias(entity, model, variant, adult);
                alias(entity + BABY_SUFFIX, babyModel, variant, baby);
            }
        });
    }

    private void alias(String model, String target, McAssets.Variant variant, Map<String, String> textures) {
        if (model.equals(target) || !knownModels.contains(model) || !knownModels.contains(target)) return;
        textures.keySet().forEach(sub -> {
            String suffix = suffix(variant, sub);
            aliases.add(new Alias(model + "/main_" + suffix, target + "/main_" + suffix));
        });
    }

    private void register(String model, McAssets.Variant variant, Map<String, String> textures) {
        if (!knownModels.contains(model)) return;

        List<Variant> list = dataVariants.computeIfAbsent(model, key -> new ArrayList<>());
        textures.forEach((sub, texture) -> list.add(new Variant(suffix(variant, sub), texture)));
    }

    private static String suffix(McAssets.Variant variant, String sub) {
        return sub.isEmpty() ? variant.name() : variant.name() + "_" + sub;
    }

    private String targetModel(String entity, String model) {
        if (model == null) return knownModels.contains(entity) ? entity : null;

        String override = variantModelOverrides.getOrDefault(entity, Map.of()).get(model);
        if (override != null) return override;

        for (String candidate : List.of(model + "_" + entity, entity + "_" + model))
            if (knownModels.contains(candidate)) return candidate;

        return null;
    }

    private static Variant variant(String texture, String suffix) {
        if (suffix != null) return new Variant(suffix, texture);
        return new Variant(texture.substring(texture.lastIndexOf('/') + 1), texture);
    }

    /** Where is the _ in polarbear? WHERE? CODE HAS IT!! */
    private static String compact(String name) {
        return name.replace("_", "");
    }

    private static String base(String model) {
        return model.endsWith(BABY_SUFFIX) ? model.substring(0, model.length() - BABY_SUFFIX.length()) : model;
    }

    private static String camel(String name) {
        StringBuilder camel = new StringBuilder();
        for (String part : name.split("[_/]")) {
            if (part.isEmpty()) continue;
            camel.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return camel.toString();
    }

}
