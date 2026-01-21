package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class CachedFluidInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedFluidInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, @Nullable FluidInventory> factory;
    private Function<@Nullable Direction, @Nullable Storage<FluidVariant>> getter;

    public CachedFluidInventoryBehaviour(T be, Function<T, @Nullable FluidInventory> factory) {
        super(be);
        this.factory = factory;
        reset();
    }

    public static @Nullable <T extends SmartBlockEntity> Storage<FluidVariant> get(T be, @Nullable Direction side) {
        return be.getBehaviour(TYPE).get(side);
    }

    @Nullable
    public Storage<FluidVariant> get(@Nullable Direction side) {
        return getter.apply(side);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Storage<FluidVariant> firstGet(@Nullable Direction direction) {
        FluidInventory inventory = factory.apply(blockEntity);
        if (inventory == null) {
            return null;
        }
        Storage<FluidVariant> storage = FluidInventoryStorage.of(inventory, null);
        if (inventory instanceof SidedFluidInventory) {
            @Nullable Storage<FluidVariant>[] sides = new Storage[6];
            getter = side -> {
                if (side == null) {
                    return storage;
                } else {
                    int i = side.get3DDataValue();
                    Storage<FluidVariant> sideStorage = sides[i];
                    if (sideStorage == null) {
                        sideStorage = sides[i] = FluidInventoryStorage.of(inventory, side);
                    }
                    return sideStorage;
                }
            };
        } else {
            getter = _ -> storage;
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
