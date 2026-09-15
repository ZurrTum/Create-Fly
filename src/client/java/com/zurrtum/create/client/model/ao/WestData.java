package com.zurrtum.create.client.model.ao;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;

public class WestData extends AoFaceData {
    @Override
    protected float shade(CardinalLighting cardinalLighting) {
        return cardinalLighting.west();
    }

    @Override
    protected void moveU(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y);
    }

    @Override
    protected void moveV(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z);
    }

    @Override
    protected void movePartialFace(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x);
    }

    @Override
    protected void moveFullFace(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setX(x - 1);
    }

    @Override
    protected void moveU0(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y + 1);
    }

    @Override
    protected void moveU1(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setY(y - 1);
    }

    @Override
    protected void moveV0(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z - 1);
    }

    @Override
    protected void moveV1(BlockPos.MutableBlockPos searchPos, int x, int y, int z) {
        searchPos.setZ(z + 1);
    }
}
