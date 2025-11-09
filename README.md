# About
This is a native BlueMap addon that aims to add a renderer for each (vanilla) entity.
This means that when bluemap renders your map, it will also show entities where they are stored in the world-files at the time of rendering.  
Note that the entities will be static. They will not move around. They will only be updated when bluemap
renders the entities map-tile again.

Currently only a small subset of entity types is supported. If you want to add more entity-types and features,
feel free to create a PR! :)
In the future this is planned to be added directly to BlueMap, but this can only happen if there are a lot more entity-types
supported.

**Currently supported entity-types:**
- Bee
- Breeze
- Cat
- Chicken
- Cod
- Cow
- Fox
- Iron Golem
- Llama
- Ocelot
- Pig
- Pufferfish
- Salmon
- Tropical Fish
- Zombie

# Usage
Download the `BlueMapEntities.jar` from the releases and place it in the `packs` folder next to your
bluemap-configs. You will need to purge/re-render the map to see the entities appear.
