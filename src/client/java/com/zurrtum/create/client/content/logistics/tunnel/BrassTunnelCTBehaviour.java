package com.zurrtum.create.client.content.logistics.tunnel;

import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class BrassTunnelCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public @Nullable CTType getDataType(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
        if (!(world.getBlockEntity(pos) instanceof BrassTunnelBlockEntity tunnelBE) || !tunnelBE.hasDistributionBehaviour())
            return null;
        return super.getDataType(world, pos, state, direction);
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        return direction == Direction.UP ? AllSpriteShifts.BRASS_TUNNEL_TOP : null;
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction face) {
        return true;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        int yDiff = otherPos.getY() - pos.getY();
        int zDiff = otherPos.getZ() - pos.getZ();
        if (yDiff != 0)
            return false;

        if (!(reader.getBlockEntity(pos) instanceof BrassTunnelBlockEntity tunnelBE))
            return false;
        boolean leftSide = zDiff > 0;
        return tunnelBE.isConnected(leftSide);
    }

}
