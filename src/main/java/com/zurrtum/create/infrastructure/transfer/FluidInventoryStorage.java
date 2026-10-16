package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
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
        return new FluidStack(variant.getFluid(), 1, variant.getComponentsPatch());
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
        PatchedDataComponentMap stackComponents = stack.directComponents();
        DataComponentMap otherStackComponents = variant.getComponents();
        if (stackComponents == otherStackComponents) {
            return true;
        }
        if (otherStackComponents == DataComponentMap.EMPTY) {
            Reference2ObjectMap<DataComponentType<?>, Object> stackComponentMap = stackComponents.patch;
            int stackComponentCount = stackComponentMap.size();
            if (stackComponentCount == 0) {
                return true;
            }
            return stackComponentCount == 1 && stackComponentMap.containsKey(AllDataComponents.FLUID_MAX_CAPACITY);
        }
        return FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(
            stackComponents,
            (PatchedDataComponentMap) otherStackComponents
        );
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
        }
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
