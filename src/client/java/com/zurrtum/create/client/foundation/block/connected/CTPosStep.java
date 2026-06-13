package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record CTPosStep(int mask, int flag, int sh, int sv, boolean isBoth, boolean isVertical, boolean ph,
                        boolean pv) {
    public CTPosStep(int flag, int sh, int sv) {
        this(0, flag, sh, sv);
    }

    public CTPosStep(int mask, int flag, int sh, int sv) {
        this(mask, flag, sh, sv, sh != 0 && sv != 0, sv != 0, sh > 0, sv > 0);
    }

    public boolean test(
        ConnectedTextureBehaviour behaviour,
        int context,
        boolean flipH,
        boolean flipV,
        BlockAndTintGetter reader,
        BlockPos currentPos,
        MutableBlockPos targetPos,
        BlockState trueCurrentState,
        BlockState connectiveCurrentState,
        Direction textureSide,
        Direction horizontal,
        Direction vertical
    ) {
        if ((context & mask) != mask) {
            return false;
        }
        if (isBoth) {
            return behaviour.testConnection(
                reader,
                currentPos,
                updatePos(targetPos, currentPos, horizontal, flipH ? -sh : sh, vertical, flipV ? -sv : sv),
                trueCurrentState,
                connectiveCurrentState,
                textureSide,
                flipH == ph ? horizontal.getOpposite() : horizontal,
                flipV == pv ? vertical.getOpposite() : vertical
            );
        }
        if (isVertical) {
            return behaviour.testConnection(
                reader,
                currentPos,
                updatePos(targetPos, currentPos, vertical, flipV ? -sv : sv),
                trueCurrentState,
                connectiveCurrentState,
                textureSide,
                null,
                flipV == pv ? vertical.getOpposite() : vertical
            );
        }
        return behaviour.testConnection(
            reader,
            currentPos,
            updatePos(targetPos, currentPos, horizontal, flipH ? -sh : sh),
            trueCurrentState,
            connectiveCurrentState,
            textureSide,
            flipH == ph ? horizontal.getOpposite() : horizontal,
            null
        );
    }

    private static BlockPos updatePos(
        MutableBlockPos dest,
        BlockPos pos,
        Direction direction1,
        int steps1,
        Direction direction2,
        int steps2
    ) {
        return dest.set(
            pos.getX() + direction1.getStepX() * steps1 + direction2.getStepX() * steps2,
            pos.getY() + direction1.getStepY() * steps1 + direction2.getStepY() * steps2,
            pos.getZ() + direction1.getStepZ() * steps1 + direction2.getStepZ() * steps2
        );
    }

    private static BlockPos updatePos(MutableBlockPos dest, BlockPos pos, Direction direction, int steps) {
        return dest.set(
            pos.getX() + direction.getStepX() * steps,
            pos.getY() + direction.getStepY() * steps,
            pos.getZ() + direction.getStepZ() * steps
        );
    }
}
