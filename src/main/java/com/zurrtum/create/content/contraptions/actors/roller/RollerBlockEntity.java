package com.zurrtum.create.content.contraptions.actors.roller;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.List;

public class RollerBlockEntity extends SmartBlockEntity {

    // For simulations such as Ponder
    private float manuallyAnimatedSpeed;

    public ServerFilteringBehaviour filtering;
    public ServerScrollOptionBehaviour<RollingMode> mode;

    private boolean dontPropagate;

    public RollerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_ROLLER, pos, state);
        dontPropagate = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this));
        behaviours.add(mode = new ServerScrollOptionBehaviour<>(RollingMode.class, this));

        filtering.withCallback(this::onFilterChanged);
        filtering.withPredicate(this::isValidMaterial);
        mode.withCallback(this::onModeChanged);
    }

    protected void onModeChanged(int mode) {
        shareValuesToAdjacent();
    }

    protected void onFilterChanged(ItemStack newFilter) {
        shareValuesToAdjacent();
    }

    protected boolean isValidMaterial(ItemStack newFilter) {
        if (newFilter.isEmpty())
            return true;
        BlockState appliedState = RollerMovementBehaviour.getStateToPaveWith(newFilter);
        if (appliedState.isAir())
            return false;
        if (appliedState.getBlock() instanceof BlockEntityProvider)
            return false;
        if (appliedState.getBlock() instanceof StairsBlock)
            return false;
        VoxelShape shape = appliedState.getOutlineShape(world, pos);
        if (shape.isEmpty() || !shape.getBoundingBox().equals(VoxelShapes.fullCube().getBoundingBox()))
            return false;
        VoxelShape collisionShape = appliedState.getCollisionShape(world, pos);
        return !collisionShape.isEmpty();
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(1);
    }

    public float getAnimatedSpeed() {
        return manuallyAnimatedSpeed;
    }

    public void setAnimatedSpeed(float speed) {
        manuallyAnimatedSpeed = speed;
    }

    public void searchForSharedValues() {
        BlockState blockState = getCachedState();
        Direction facing = blockState.get(RollerBlock.FACING, Direction.SOUTH);

        for (int side : Iterate.positiveAndNegative) {
            BlockPos pos = this.pos.offset(facing.rotateYClockwise(), side);
            if (world.getBlockState(pos) != blockState)
                continue;
            if (!(world.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
                continue;
            acceptSharedValues(otherRoller.mode.getValue(), otherRoller.filtering.getFilter());
            shareValuesToAdjacent();
            break;
        }
    }

    protected void acceptSharedValues(int mode, ItemStack filter) {
        dontPropagate = true;
        this.filtering.setFilter(filter.copy());
        this.mode.setValue(mode);
        dontPropagate = false;
        notifyUpdate();
    }

    public void shareValuesToAdjacent() {
        if (dontPropagate || world.isClient())
            return;
        BlockState blockState = getCachedState();
        Direction facing = blockState.get(RollerBlock.FACING, Direction.SOUTH);

        for (int side : Iterate.positiveAndNegative) {
            for (int i = 1; i < 100; i++) {
                BlockPos pos = this.pos.offset(facing.rotateYClockwise(), side * i);
                if (world.getBlockState(pos) != blockState)
                    break;
                if (!(world.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
                    break;
                otherRoller.acceptSharedValues(mode.getValue(), filtering.getFilter());
            }
        }
    }

    public enum RollingMode {
        TUNNEL_PAVE,
        STRAIGHT_FILL,
        WIDE_FILL;
    }
}