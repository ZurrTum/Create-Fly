package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class FluidPipeBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static FluidPipeBlockEntity pipe(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(AllBlockEntityTypes.FLUID_PIPE, pos, state);
    }

    public static FluidPipeBlockEntity encased(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(AllBlockEntityTypes.ENCASED_FLUID_PIPE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new StandardPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this, this::canHaveBracket));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return FluidPropagator.getSharedTriggers();
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        BracketedBlockEntityBehaviour bracketBehaviour = getBehaviour(BracketedBlockEntityBehaviour.TYPE);
        if (bracketBehaviour != null) {
            bracketBehaviour.transformBracket(transform);
        }
    }

    private boolean canHaveBracket(BlockState state) {
        return !(state.getBlock() instanceof EncasedPipeBlock);
    }

    class StandardPipeFluidTransportBehaviour extends FluidTransportBehaviour {

        public StandardPipeFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return (FluidPipeBlock.isPipe(state) || state.getBlock() instanceof EncasedPipeBlock) && state.get(FluidPipeBlock.FACING_PROPERTIES.get(
                direction));
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

            BlockPos offsetPos = pos.offset(direction);
            BlockState otherState = world.getBlockState(offsetPos);

            if (state.getBlock() instanceof EncasedPipeBlock && attachment != AttachmentTypes.DRAIN)
                return AttachmentTypes.NONE;

            if (attachment == AttachmentTypes.RIM) {
                if (!FluidPipeBlock.isPipe(otherState) && !(otherState.getBlock() instanceof EncasedPipeBlock) && !(otherState.getBlock() instanceof GlassFluidPipeBlock)) {
                    FluidTransportBehaviour pipeBehaviour = BlockEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
                    if (pipeBehaviour != null && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite()))
                        return AttachmentTypes.DETAILED_CONNECTION;
                }

                if (!FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
                    return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis() ? AttachmentTypes.CONNECTION : AttachmentTypes.DETAILED_CONNECTION;
            }

            if (attachment == AttachmentTypes.NONE && state.get(FluidPipeBlock.FACING_PROPERTIES.get(direction)))
                return AttachmentTypes.DETAILED_CONNECTION;

            return attachment;
        }

    }

}
