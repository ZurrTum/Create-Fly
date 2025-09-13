package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class StraightPipeBlockEntity extends SmartBlockEntity {

    public StraightPipeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GLASS_FLUID_PIPE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new StraightPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return FluidPropagator.getSharedTriggers();
    }

    public static class StraightPipeFluidTransportBehaviour extends FluidTransportBehaviour {

        public StraightPipeFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return state.contains(AxisPipeBlock.AXIS) && state.get(AxisPipeBlock.AXIS) == direction.getAxis();
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
            BlockState otherState = world.getBlockState(pos.offset(direction));

            Axis axis = IAxisPipe.getAxisOf(state);
            Axis otherAxis = IAxisPipe.getAxisOf(otherState);

            if (attachment == AttachmentTypes.RIM && state.getBlock() instanceof FluidValveBlock)
                return AttachmentTypes.NONE;
            if (attachment == AttachmentTypes.RIM && !(state.getBlock() instanceof GlassFluidPipeBlock) && otherState.getBlock() instanceof GlassFluidPipeBlock)
                return AttachmentTypes.PARTIAL_RIM;

            if (attachment == AttachmentTypes.RIM && FluidPipeBlock.isPipe(otherState))
                return AttachmentTypes.NONE;
            if (axis == otherAxis && axis != null)
                return AttachmentTypes.NONE;

            if (otherState.getBlock() instanceof FluidValveBlock && FluidValveBlock.getPipeAxis(otherState) == direction.getAxis())
                return AttachmentTypes.NONE;

            return attachment.withoutConnector();
        }

    }

}
