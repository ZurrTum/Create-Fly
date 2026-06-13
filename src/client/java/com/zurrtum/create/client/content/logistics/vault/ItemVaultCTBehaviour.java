package com.zurrtum.create.client.content.logistics.vault;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ItemVaultCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    @Nullable
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        if (vaultBlockAxis == null) {
            return null;
        }

        boolean large = ItemVaultBlock.isLarge(state);
        if (direction.getAxis() == vaultBlockAxis) {
            return large ? AllSpriteShifts.VAULT_FRONT_LARGE : AllSpriteShifts.VAULT_FRONT_MEDIUM;
        }
        if (direction == Direction.UP) {
            return large ? AllSpriteShifts.VAULT_TOP_LARGE : AllSpriteShifts.VAULT_TOP_MEDIUM;
        }
        if (direction == Direction.DOWN) {
            return large ? AllSpriteShifts.VAULT_BOTTOM_LARGE : AllSpriteShifts.VAULT_BOTTOM_MEDIUM;
        }
        return large ? AllSpriteShifts.VAULT_SIDE_LARGE : AllSpriteShifts.VAULT_SIDE_MEDIUM;
    }

    @Override
    protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        boolean alongX = vaultBlockAxis == Axis.X;
        if (face.getAxis().isVertical() && alongX) {
            return super.getUpDirection(reader, pos, state, face).getClockWise();
        }
        if (face.getAxis() == vaultBlockAxis || face.getAxis().isVertical()) {
            return super.getUpDirection(reader, pos, state, face);
        }
        return Direction.fromAxisAndDirection(vaultBlockAxis, alongX ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
    }

    @Override
    protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(state);
        if (face.getAxis().isVertical() && vaultBlockAxis == Axis.X) {
            return super.getRightDirection(reader, pos, state, face).getClockWise();
        }
        if (face.getAxis() == vaultBlockAxis || face.getAxis().isVertical()) {
            return super.getRightDirection(reader, pos, state, face);
        }
        return Direction.fromAxisAndDirection(Axis.Y, face.getAxisDirection());
    }

    public boolean buildContextForOccludedDirections() {
        return super.buildContextForOccludedDirections();
    }

    @Override
    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face
    ) {
        return state == other && ConnectivityHandler.isConnected(
            reader,
            pos,
            otherPos
        ); //ItemVaultConnectivityHandler.isConnected(reader, pos, otherPos);
    }

}
