package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CachedInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, Inventory> factory;
    private Function<Direction, Storage<ItemVariant>> getter;

    public CachedInventoryBehaviour(T be, Function<T, Inventory> factory) {
        super(be);
        this.factory = factory;
        reset();
    }

    public static @Nullable <T extends SmartBlockEntity> Storage<ItemVariant> get(T be, @Nullable Direction side) {
        return be.getBehaviour(TYPE).get(side);
    }

    public Storage<ItemVariant> get(Direction side) {
        return getter.apply(side);
    }

    @SuppressWarnings("unchecked")
    private Storage<ItemVariant> firstGet(Direction direction) {
        Inventory inventory = factory.apply(blockEntity);
        if (inventory == null) {
            return null;
        }
        Storage<ItemVariant> storage = InventoryStorage.of(inventory, null);
        if (inventory instanceof SidedInventory) {
            Storage<ItemVariant>[] sides = new Storage[6];
            getter = side -> {
                if (side == null) {
                    return storage;
                } else {
                    int i = side.getIndex();
                    Storage<ItemVariant> sideStorage = sides[i];
                    if (sideStorage == null) {
                        sideStorage = sides[i] = InventoryStorage.of(inventory, side);
                    }
                    return sideStorage;
                }
            };
        } else {
            getter = side -> storage;
        }
        return getter.apply(direction);
    }

    public void reset() {
        getter = this::firstGet;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
