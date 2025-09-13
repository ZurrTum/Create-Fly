package com.zurrtum.create.content.fluids.pipes.valve;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity.StraightPipeFluidTransportBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class FluidValveBlockEntity extends KineticBlockEntity {

    public LerpedFloat pointer;

    public FluidValveBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FLUID_VALVE, pos, state);
        pointer = LerpedFloat.linear().startWithValue(0).chase(0, 0, Chaser.LINEAR);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        float speed = getSpeed();
        pointer.chase(speed > 0 ? 1 : 0, getChaseSpeed(), Chaser.LINEAR);
        sendData();
    }

    @Override
    public void tick() {
        super.tick();
        pointer.tickChaser();

        if (world.isClient)
            return;

        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof FluidValveBlock))
            return;
        boolean stateOpen = blockState.get(FluidValveBlock.ENABLED);

        if (stateOpen && pointer.getValue() == 0) {
            switchToBlockState(world, pos, blockState.with(FluidValveBlock.ENABLED, false));
            return;
        }
        if (!stateOpen && pointer.getValue() == 1) {
            switchToBlockState(world, pos, blockState.with(FluidValveBlock.ENABLED, true));
        }
    }

    private float getChaseSpeed() {
        return MathHelper.clamp(Math.abs(getSpeed()) / 16 / 20, 0, 1);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        pointer.write(view);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        pointer.read(view, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new ValvePipeBehaviour(this));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return FluidPropagator.getSharedTriggers();
    }

    static class ValvePipeBehaviour extends StraightPipeFluidTransportBehaviour {

        public ValvePipeBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return FluidValveBlock.getPipeAxis(state) == direction.getAxis();
        }

        @Override
        public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
            if (state.contains(FluidValveBlock.ENABLED) && state.get(FluidValveBlock.ENABLED))
                return super.canPullFluidFrom(fluid, state, direction);
            return false;
        }

    }

}
