package de.bluecolored.bluemap.entities.block;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/**
 * Used to simulate a void world to be able to utilize BlueMap's default block-renderer to render a single block
 */
public class VoidBlockNeighborhood extends BlockNeighborhood {

    public VoidBlockNeighborhood(ResourcePack resourcePack) {
        super(new VoidWorld(), resourcePack, new VoidRenderSettings(), DimensionType.OVERWORLD);
    }

}
