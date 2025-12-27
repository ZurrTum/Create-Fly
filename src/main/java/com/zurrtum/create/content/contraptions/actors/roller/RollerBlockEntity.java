package com.zurrtum.create.content.contraptions.actors.roller;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
        if (appliedState.getBlock() instanceof EntityBlock)
            return false;
        if (appliedState.getBlock() instanceof StairBlock)
            return false;
        VoxelShape shape = appliedState.getShape(level, worldPosition);
        if (shape.isEmpty() || !shape.bounds().equals(Shapes.block().bounds()))
            return false;
        VoxelShape collisionShape = appliedState.getCollisionShape(level, worldPosition);
        return !collisionShape.isEmpty();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    public float getAnimatedSpeed() {
        return manuallyAnimatedSpeed;
    }

    public void setAnimatedSpeed(float speed) {
        manuallyAnimatedSpeed = speed;
    }

    public void searchForSharedValues() {
        BlockState blockState = getBlockState();
        Direction facing = blockState.getValueOrElse(RollerBlock.FACING, Direction.SOUTH);

        for (int side : Iterate.positiveAndNegative) {
            BlockPos pos = this.worldPosition.relative(facing.getClockWise(), side);
            if (level.getBlockState(pos) != blockState)
                continue;
            if (!(level.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
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
        if (dontPropagate || level.isClientSide())
            return;
        BlockState blockState = getBlockState();
        Direction facing = blockState.getValueOrElse(RollerBlock.FACING, Direction.SOUTH);

        for (int side : Iterate.positiveAndNegative) {
            for (int i = 1; i < 100; i++) {
                BlockPos pos = this.worldPosition.relative(facing.getClockWise(), side * i);
                if (level.getBlockState(pos) != blockState)
                    break;
                if (!(level.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
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