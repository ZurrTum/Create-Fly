package com.zurrtum.create.client.model.ao;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.CardinalLighting;

public class DownData extends AoFaceData {
    @Override
    protected final float shade(CardinalLighting cardinalLighting) {
        return cardinalLighting.down();
    }

    @Override
    protected final void moveU(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x);
    }

    @Override
    protected final void moveV(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z);
    }

    @Override
    protected final void movePartialFace(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y);
    }

    @Override
    protected final void moveFullFace(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y - 1);
    }

    @Override
    protected final void moveU0(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x - 1);
    }

    @Override
    protected final void moveU1(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x + 1);
    }

    @Override
    protected final void moveV0(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z - 1);
    }

    @Override
    protected final void moveV1(MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z + 1);
    }
}
