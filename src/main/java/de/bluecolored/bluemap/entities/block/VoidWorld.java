package de.bluecolored.bluemap.entities.block;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import org.jetbrains.annotations.Nullable;

public class VoidWorld implements BlockAccess {

    private final LightData lightData = new LightData(15, 0);

    private int x, y, z;

    public VoidWorld() {}

    public VoidWorld(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public BlockAccess copy() {
        return new VoidWorld(x, y, z);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public BlockState getBlockState() {
        return BlockState.AIR;
    }

    @Override
    public LightData getLightData() {
        return lightData.set(15, 0);
    }

    @Override
    public Biome getBiome() {
        return Biome.DEFAULT;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public boolean hasOceanFloorY() {
        return false;
    }

    @Override
    public int getOceanFloorY() {
        return 0;
    }

}
