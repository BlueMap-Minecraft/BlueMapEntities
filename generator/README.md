# Entity-model generator

Generates the bluemap entity-models in `src/main/resources/assets/minecraft/models/entity` from the model-layers that
are compiled into the vanilla minecraft client-jar, so they do not have to be rebuilt by hand for every
minecraft-version.

```bash
./gradlew :generator:generateEntityModels
./gradlew :generator:generateEntityModels -PmcVersion=26.3     # a different minecraft-version
./gradlew :generator:generateEntityModels -Players='sheep|cow' # only some layers
./gradlew :generator:generateEntityModels -PoutDir=/tmp/models # testing
```

`LayerDefinitions.createRoots()` returns every entity-model-layer minecraft knows (358 in 26.2). 
Each layer is baked (`LayerDefinition.bakeRoot()`) and walked with `ModelPart.visit(...)`.

`generator/build/generation-report.txt` lists which models were added, changed and removed, where every texture came
from, and which layers could not be resolved.

## Config

| file                         | purpose                                                                       |
|------------------------------|-------------------------------------------------------------------------------|
| `config/textures.json`       | pins a texture: string (one), array (variants), object (variants after key)   |
| `config/variant-models.json` | maps the `model` field to its model-id                                        |
| `config/equipment.json`      | maps a layer to a folder in `entity/equipment`                                |
| `config/split-parts.json`    | in model variants (like chests on horse-ish mobs)                             |
| `overrides/<path>.json`      | replacing for manual edited entities                                          |
