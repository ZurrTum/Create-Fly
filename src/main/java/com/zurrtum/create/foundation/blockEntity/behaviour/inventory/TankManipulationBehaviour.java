package com.zurrtum.create.foundation.blockEntity.behaviour.inventory;

import com.google.common.base.Predicates;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class TankManipulationBehaviour extends CapManipulationBehaviourBase<FluidInventory, TankManipulationBehaviour> {

    public static final BehaviourType<TankManipulationBehaviour> OBSERVE = new BehaviourType<>();
    private final BehaviourType<TankManipulationBehaviour> behaviourType;

    public TankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
        this(OBSERVE, be, target);
    }

    private TankManipulationBehaviour(BehaviourType<TankManipulationBehaviour> type, SmartBlockEntity be, InterfaceProvider target) {
        super(be, target);
        behaviourType = type;
    }

    public FluidStack extractAny() {
        FluidInventory inventory = getInventory();
        if (inventory == null)
            return FluidStack.EMPTY;
        Predicate<FluidStack> filterTest = getFilterTest(Predicates.alwaysTrue());
        if (simulateNext) {
            return inventory.count(filterTest);
        } else {
            return inventory.extract(filterTest);
        }
    }

    protected Predicate<FluidStack> getFilterTest(Predicate<FluidStack> customFilter) {
        Predicate<FluidStack> test = customFilter;
        ServerFilteringBehaviour filter = blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        if (filter != null)
            test = customFilter.and(filter::test);
        return test;
    }

    @Override
    @Nullable
    protected FluidInventory getCapability(Level world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        return FluidHelper.getFluidInventory(world, pos, null, blockEntity, side);
    }

    @Override
    public BehaviourType<?> getType() {
        return behaviourType;
    }

}
