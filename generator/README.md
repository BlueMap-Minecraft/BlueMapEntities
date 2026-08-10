# Entity-model generator

Generates the bluemap entity-models in `src/main/resources/assets/minecraft/models/entity` from the model-layers that
are compiled into the vanilla minecraft client-jar, so they do not have to be rebuilt by hand for every
minecraft-version.

```bash
./gradlew :generator:generateEntityModels                      # current version (see mcVersion below)
./gradlew :generator:generateEntityModels -PmcVersion=26.3     # a different minecraft-version
./gradlew :generator:generateEntityModels -Players='sheep|cow' # only some layers
./gradlew :generator:generateEntityModels -PoutDir=/tmp/models # write somewhere else (dry-run)
```

The client-jar and its libraries are downloaded from mojangs launcher-metadata into `generator/build/minecraft/<version>`
(~100 MB, once per version). Nothing of it is shipped with the addon, only the generated models are.

Gradle itself has to run on a JDK **21-24** (its kotlin-dsl compiler does not run on 25 yet), the generator is compiled
and run with a java-25 toolchain, since that is what the minecraft client-jar is built for.

## How it works

`LayerDefinitions.createRoots()` returns every entity-model-layer minecraft knows (358 in 26.2). Each layer is baked
(`LayerDefinition.bakeRoot()`) and walked with `ModelPart.visit(...)`, which yields every cube together with the
accumulated part-transform. Minecraft builds entity-models mirrored and upside-down (the entity-renderer applies a
`scale(-1, -1, 1)`) with the origin 1.5 blocks above the feet, so every coordinate is converted with
`x' = -x`, `y' = 24 - y`, `z' = z`, and the part-hierarchy is flattened into the single rotation a bluemap-element
supports. Uv-coordinates are taken from the baked polygon-vertices, which also covers mirrored and deformed cubes.

## Output

One geometry-model per layer, plus a thin model per texture-variant that only overrides the texture:

```
entity/sheep/main.json                 minecraft:sheep#main
entity/sheep/wool.json                 minecraft:sheep#wool         (overlay-layer)
entity/sheep_baby/main.json            minecraft:sheep_baby#main    (baby-model)
entity/cat/main_tabby.json             variant, parent: entity/cat/main
entity/zombie/helmet_iron.json         equipment-material of minecraft:zombie#helmet
```

Textures use the vanilla paths (`entity/sheep/sheep`) and are resolved, in this order, from
`config/textures.json`, the data-driven variant-registries of the client-jar (cat, cow, pig, chicken, frog, wolf, ...),
the equipment-textures (`entity/equipment/...`), the vanilla naming-convention, the texture-family of the entity
(axolotl, horse, mooshroom, ...) and finally the texture-paths referenced by the compiled renderer-classes.

`generator/build/generation-report.txt` lists which models were added, changed and removed, where every texture came
from, and which layers could not be resolved.

## Configuration

| file | purpose |
|---|---|
| `config/textures.json` | pins a texture (string) or a set of variant-textures (array) for `<model>#<layer>` |
| `config/variant-models.json` | maps the `model` field of a variant-registry-entry to its model-id |
| `config/equipment.json` | maps `<model>#<layer>` to a folder in `entity/equipment` |
| `overrides/<path>.json` | replaces a generated model entirely, e.g. for hand-fixed geometry |
| `generated-index.txt` | the files of the last run, used to delete models that vanished from minecraft |

## Limits

- Layers are generated in their base pose. Animations (`setupAnim`) and renderer-side tints are not part of the
  model-data and stay the job of the renderers in `de.bluecolored.bluemap.entities.renderer`.
- Which layers an entity stacks (wool over sheep, saddle over pig, armor over zombie) is renderer-logic in minecraft,
  so it stays renderer-logic here too - the generator only provides the models.
- Elements that need a rotation around more than one axis (60 models in 26.2) require **bluemap-core 5.14 or newer**,
  everything else also works with older versions.
