package com.zurrtum.create.content.fluids;

import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class FlowSource {
    BlockFace location;

    public FlowSource(BlockFace location) {
        this.location = location;
    }

    public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
        return Optional.ofNullable(provideHandler())
            .flatMap(tank -> tank.stream(location.getOppositeFace()).filter(stack -> !stack.isEmpty() && extractionPredicate.test(stack)).findFirst()
                .map(stack -> stack.copyWithAmount(1))).orElse(FluidStack.EMPTY);
    }

    // Layer III. PFIs need active attention to prevent them from disengaging early
    public void keepAlive() {
    }

    public abstract boolean isEndpoint();

    public void manageSource(Level world, BlockEntity networkBE) {
    }

    public void whileFlowPresent(Level world, boolean pulling) {
    }

    public @Nullable FluidInventory provideHandler() {
        return null;
    }

    public static class FluidHandler extends FlowSource {
        @Nullable Supplier<FluidInventory> fluidHandlerCache;

        public FluidHandler(BlockFace location) {
            super(location);
        }

        public void manageSource(Level world, BlockEntity networkBE) {
            if (fluidHandlerCache == null) {
                BlockPos pos = location.getConnectedPos();
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    Direction side = location.getOppositeFace();
                    if (world instanceof ServerLevel serverWorld) {
                        fluidHandlerCache = FluidHelper.getFluidInventoryCache(serverWorld, pos, side);
                    } else if (networkBE instanceof SmartBlockEntity smartBE && smartBE.isVirtual()) {
                        fluidHandlerCache = () -> FluidHelper.getFluidInventory(world, pos, null, blockEntity, side);
                    }
                }
            }
        }

        @Override
        @Nullable
        public FluidInventory provideHandler() {
            return fluidHandlerCache != null ? fluidHandlerCache.get() : null;
        }

        @Override
        public boolean isEndpoint() {
            return true;
        }
    }

    public static class OtherPipe extends FlowSource {
        WeakReference<FluidTransportBehaviour> cached;

        public OtherPipe(BlockFace location) {
            super(location);
        }

        @Override
        public void manageSource(Level world, BlockEntity networkBE) {
            if (cached != null && cached.get() != null && !cached.get().blockEntity.isRemoved())
                return;
            cached = null;
            FluidTransportBehaviour fluidTransportBehaviour = BlockEntityBehaviour.get(
                world,
                location.getConnectedPos(),
                FluidTransportBehaviour.TYPE
            );
            if (fluidTransportBehaviour != null)
                cached = new WeakReference<>(fluidTransportBehaviour);
        }

        @Override
        public FluidStack provideFluid(Predicate<FluidStack> extractionPredicate) {
            if (cached == null || cached.get() == null)
                return FluidStack.EMPTY;
            FluidTransportBehaviour behaviour = cached.get();
            FluidStack providedOutwardFluid = behaviour.getProvidedOutwardFluid(location.getOppositeFace());
            return extractionPredicate.test(providedOutwardFluid) ? providedOutwardFluid : FluidStack.EMPTY;
        }

        @Override
        public boolean isEndpoint() {
            return false;
        }

    }

    public static class Blocked extends FlowSource {

        public Blocked(BlockFace location) {
            super(location);
        }

        @Override
        public boolean isEndpoint() {
            return false;
        }

    }

}
