package com.zurrtum.create.client.content.logistics.vault;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class ItemVaultCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable Sprite sprite) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        boolean small = !ItemVaultBlock.isLarge(state);
        if (vaultBlockAxis == null)
            return null;

        if (direction.getAxis() == vaultBlockAxis)
            return AllSpriteShifts.VAULT_FRONT.get(small);
        if (direction == Direction.UP)
            return AllSpriteShifts.VAULT_TOP.get(small);
        if (direction == Direction.DOWN)
            return AllSpriteShifts.VAULT_BOTTOM.get(small);

        return AllSpriteShifts.VAULT_SIDE.get(small);
    }

    @Override
    protected Direction getUpDirection(BlockRenderView reader, BlockPos pos, BlockState state, Direction face) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        boolean alongX = vaultBlockAxis == Axis.X;
        if (face.getAxis().isVertical() && alongX)
            return super.getUpDirection(reader, pos, state, face).rotateYClockwise();
        if (face.getAxis() == vaultBlockAxis || face.getAxis().isVertical())
            return super.getUpDirection(reader, pos, state, face);
        return Direction.from(vaultBlockAxis, alongX ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
    }

    @Override
    protected Direction getRightDirection(BlockRenderView reader, BlockPos pos, BlockState state, Direction face) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        if (face.getAxis().isVertical() && vaultBlockAxis == Axis.X)
            return super.getRightDirection(reader, pos, state, face).rotateYClockwise();
        if (face.getAxis() == vaultBlockAxis || face.getAxis().isVertical())
            return super.getRightDirection(reader, pos, state, face);
        return Direction.from(Axis.Y, face.getDirection());
    }

    public boolean buildContextForOccludedDirections() {
        return super.buildContextForOccludedDirections();
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return state == other && ConnectivityHandler.isConnected(
            reader,
            pos,
            otherPos
        ); //ItemVaultConnectivityHandler.isConnected(reader, pos, otherPos);
    }

}
