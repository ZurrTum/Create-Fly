package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface FluidInventoryStorage extends SlottedStorage<FluidVariant> {
    Map<FluidVariant, FluidStack> CACHE = new MapMaker().weakKeys().makeMap();

    static FluidStack getCachedStack(FluidVariant variant) {
        return CACHE.computeIfAbsent(variant, FluidInventoryStorage::toStack);
    }

    static FluidStack toStack(FluidVariant variant) {
        return new FluidStack(variant.getFluid(), 1, variant.getComponents());
    }

    static FluidInventoryStorage of(FluidInventory inventory) {
        Objects.requireNonNull(inventory, "Null fluid inventory is not supported.");
        return FluidInventoryStorageImpl.of(inventory);
    }

    static FluidInventoryStorage of(FluidInventory inventory, @Nullable Direction direction) {
        Objects.requireNonNull(inventory, "Null fluid inventory is not supported.");
        return FluidInventoryStorageImpl.of(inventory, direction);
    }

    static boolean matches(FluidVariant variant, FluidStack stack) {
        if (!variant.isOf(stack.getFluid())) {
            return false;
        }
        DataComponentPatch stackComponents = stack.getComponentChanges();
        DataComponentPatch variantComponents = variant.getComponents();
        if (stackComponents == variantComponents) {
            return true;
        }
        return stackComponents.map.reference2ObjectEntrySet().containsAll(variantComponents.map.reference2ObjectEntrySet());
    }

    @Override
    @UnmodifiableView
    List<SingleSlotStorage<FluidVariant>> getSlots();

    @Override
    default int getSlotCount() {
        return getSlots().size();
    }

    @Override
    default SingleSlotStorage<FluidVariant> getSlot(int slot) {
        return getSlots().get(slot);
    }

    @SuppressWarnings("UnstableApiUsage")
    static String toString(@Nullable FluidInventory inventory) {
        if (inventory == null) {
            return "~~NULL~~";
        } else {
            String result = inventory.toString();

            if (inventory instanceof BlockEntity blockEntity) {
                result += " (%s, %s)".formatted(
                    blockEntity.getBlockState(),
                    DebugMessages.forGlobalPos(blockEntity.getLevel(), blockEntity.getBlockPos())
                );
            }

            return result;
        }
    }
}
