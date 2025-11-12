package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity.StraightPipeFluidTransportBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SmartFluidPipeBlockEntity extends SmartBlockEntity {

    private ServerFilteringBehaviour filter;

    public SmartFluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SMART_FLUID_PIPE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new SmartPipeBehaviour(this));
        behaviours.add(filter = new ServerFilteringBehaviour(this).forFluids().withCallback(this::onFilterChanged));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return FluidPropagator.getSharedTriggers();
    }

    private void onFilterChanged(ItemStack newFilter) {
        if (!level.isClientSide())
            FluidPropagator.propagateChangedPipe(level, worldPosition, getBlockState());
    }

    class SmartPipeBehaviour extends StraightPipeFluidTransportBehaviour {

        public SmartPipeBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
            if (fluid.isEmpty() || filter != null && filter.test(fluid))
                return super.canPullFluidFrom(fluid, state, direction);
            return false;
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return state.getBlock() instanceof SmartFluidPipeBlock && SmartFluidPipeBlock.getPipeAxis(state) == direction.getAxis();
        }

    }
}