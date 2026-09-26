package de.bluecolored.bluemap.entities.block;

import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.mask.Mask;

public class VoidRenderSettings implements RenderSettings {

    @Override
    public int getRemoveCavesBelowY() {
        return -100000;
    }

    @Override
    public int getCaveDetectionOceanFloor() {
        return -100000;
    }

    @Override
    public boolean isCaveDetectionUsesBlockLight() {
        return false;
    }

    @Override
    public float getAmbientLight() {
        return 15;
    }

    @Override
    public Mask getRenderMask() {
        return Mask.ALL;
    }

    @Override
    public boolean isSaveHiresLayer() {
        return true;
    }

    @Override
    public boolean isRenderTopOnly() {
        return false;
    }

}
