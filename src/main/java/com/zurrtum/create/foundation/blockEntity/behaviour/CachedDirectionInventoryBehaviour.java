package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class CachedDirectionInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final BiFunction<T, Direction, Inventory> factory;
    @SuppressWarnings("unchecked")
    Storage<ItemVariant>[] sides = new Storage[7];

    public CachedDirectionInventoryBehaviour(T be, BiFunction<T, Direction, Inventory> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> Storage<ItemVariant> get(T be, @Nullable Direction side) {
        return be.getBehaviour(TYPE).get(side);
    }

    public Storage<ItemVariant> get(Direction side) {
        int i = side == null ? 6 : side.getIndex();
        Storage<ItemVariant> sideStorage = sides[i];
        if (sideStorage == null) {
            Inventory inventory = factory.apply(blockEntity, side);
            if (inventory != null) {
                sideStorage = sides[i] = InventoryStorage.of(inventory, null);
            }
        }
        return sideStorage;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
