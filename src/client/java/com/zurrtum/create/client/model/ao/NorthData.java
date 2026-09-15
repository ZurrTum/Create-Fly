package com.zurrtum.create.client.model.ao;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;

public class NorthData extends AoFaceData {
    @Override
    protected final float shade(CardinalLighting cardinalLighting) {
        return cardinalLighting.north();
    }

    @Override
    protected final void moveU(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y);
    }

    @Override
    protected final void moveV(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x);
    }

    @Override
    protected final void movePartialFace(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z);
    }

    @Override
    protected final void moveFullFace(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z - 1);
    }

    @Override
    protected final void moveU0(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y + 1);
    }

    @Override
    protected final void moveU1(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y - 1);
    }

    @Override
    protected final void moveV0(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x + 1);
    }

    @Override
    protected final void moveV1(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x - 1);
    }
}
