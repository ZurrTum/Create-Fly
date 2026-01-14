package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity.StraightPipeFluidTransportBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class SmartFluidPipeBlockEntity extends SmartBlockEntity implements Clearable {

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

    @Override
    public void clear() {
        filter.setFilter(ItemStack.EMPTY);
    }

    private void onFilterChanged(ItemStack newFilter) {
        if (!world.isClient())
            FluidPropagator.propagateChangedPipe(world, pos, getCachedState());
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