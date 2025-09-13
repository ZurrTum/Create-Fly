package com.zurrtum.create.api.contraption.storage.item.simple;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Widely-applicable mounted storage implementation.
 * Gets an item handler from the mounted block, copies it to an ItemStackHandler,
 * and then copies the inventory back to the target when unmounting.
 * All blocks for which this mounted storage is registered must provide an
 * {@link Inventory} to {@link ItemInventoryProvider}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 * It is also possible to extend this class to create your own implementation.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
    public static final MapCodec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

    public SimpleMountedStorage(MountedItemStorageType<?> type, Inventory handler) {
        super(type, copyToItemStackHandler(handler));
    }

    public SimpleMountedStorage(Inventory handler) {
        this(AllMountedStorageTypes.SIMPLE, handler);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be == null)
            return;

        Inventory cap = ItemHelper.getInventory(level, pos, state, be, null);
        if (cap != null) {
            validate(cap).ifPresent(handler -> {
                for (int i = 0, size = handler.size(); i < size; i++) {
                    handler.setStack(i, getStack(i));
                }
            });
        }
    }

    /**
     * Make sure the targeted handler is valid for copying items back into.
     * It is highly recommended to call super in overrides.
     */
    protected Optional<Inventory> validate(Inventory handler) {
        if (handler.size() == this.size()) {
            return Optional.of(handler);
        } else {
            return Optional.empty();
        }
    }

    public static <T extends SimpleMountedStorage> MapCodec<T> codec(Function<Inventory, T> factory) {
        return CreateCodecs.ITEM_STACK_HANDLER.xmap(factory, storage -> storage.wrapped).fieldOf("value");
    }
}
