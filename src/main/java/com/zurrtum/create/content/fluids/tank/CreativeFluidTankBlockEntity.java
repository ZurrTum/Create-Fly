package com.zurrtum.create.content.fluids.tank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeFluidTankBlockEntity extends FluidTankBlockEntity {

    public CreativeFluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CREATIVE_FLUID_TANK, pos, state);
    }

    @Override
    protected FluidTank createInventory() {
        return new CreativeFluidTankInventory(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    public static class CreativeFluidTankInventory extends FluidTank {
        public static final Codec<CreativeFluidTankInventory> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FluidTank::getMaxAmountPerStack)
        ).apply(i, CreativeFluidTankInventory::new));

        private final Consumer<FluidStack> updateCallback;

        public CreativeFluidTankInventory(int capacity, Consumer<FluidStack> updateCallback) {
            super(capacity);
            this.updateCallback = updateCallback;
        }

        private CreativeFluidTankInventory(FluidStack stack, int capacity) {
            super(capacity);
            this.fluid = stack;
            this.updateCallback = null;
        }

        @Override
        public void setCapacity(int capacity) {
            super.setCapacity(capacity);
            markDirty();
        }

        @Override
        public int insert(FluidStack stack) {
            return stack.getAmount();
        }

        @Override
        public int insert(FluidStack stack, int maxAmount) {
            return maxAmount;
        }

        @Override
        public int countSpace(FluidStack stack) {
            return stack.getAmount();
        }

        @Override
        public int countSpace(FluidStack stack, int maxAmount) {
            return maxAmount;
        }

        @Override
        public int extract(FluidStack stack) {
            return stack.getAmount();
        }

        @Override
        public int extract(FluidStack stack, int maxAmount) {
            return maxAmount;
        }

        @Override
        public boolean preciseInsert(FluidStack stack) {
            return true;
        }

        @Override
        public boolean preciseExtract(FluidStack stack) {
            return true;
        }

        @Override
        public void markDirty() {
            fluid.setAmount(getMaxAmountPerStack());
            if (updateCallback != null) {
                updateCallback.accept(fluid);
            }
        }
    }
}
