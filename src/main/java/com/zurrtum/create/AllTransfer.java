package com.zurrtum.create;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedDirectionInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryWrapper;
import com.zurrtum.create.infrastructure.transfer.FluidItemContext;
import com.zurrtum.create.infrastructure.transfer.FluidItemInventoryWrapper;
import com.zurrtum.create.infrastructure.transfer.InventoryWrapper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllTransfer {
    public static final boolean DISABLE = !FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1");
    private static final Map<Storage<ItemVariant>, Container> WRAPPERS_ITEM = new MapMaker().weakValues().makeMap();
    private static final Map<Storage<FluidVariant>, FluidInventory> WRAPPERS_FLUID = new MapMaker().weakValues().makeMap();

    public static Supplier<Container> getCacheInventory(
        ServerLevel world,
        BlockPos pos,
        Direction direction,
        BiPredicate<BlockEntity, Direction> filter
    ) {
        if (DISABLE) {
            return null;
        }
        BlockApiCache<Storage<ItemVariant>, @Nullable Direction> cache = BlockApiCache.create(ItemStorage.SIDED, world, pos);
        return () -> {
            Storage<ItemVariant> inventory = cache.find(direction);
            if (inventory == null || (filter != null && !filter.test(cache.getBlockEntity(), direction))) {
                return null;
            }
            return WRAPPERS_ITEM.computeIfAbsent(inventory, InventoryWrapper::of);
        };
    }

    public static Container getInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        if (DISABLE) {
            return null;
        }
        Storage<ItemVariant> inventory = ItemStorage.SIDED.find(world, pos, state, blockEntity, direction);
        if (inventory == null) {
            return null;
        }
        return WRAPPERS_ITEM.computeIfAbsent(inventory, InventoryWrapper::of);
    }

    public static boolean hasFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        if (DISABLE) {
            return false;
        }
        return FluidStorage.SIDED.find(world, pos, state, blockEntity, direction) != null;
    }

    public static Supplier<FluidInventory> getCacheFluidInventory(ServerLevel world, BlockPos pos, Direction direction) {
        if (DISABLE) {
            return null;
        }
        BlockApiCache<Storage<FluidVariant>, @Nullable Direction> cache = BlockApiCache.create(FluidStorage.SIDED, world, pos);
        return () -> {
            Storage<FluidVariant> inventory = cache.find(direction);
            if (inventory == null) {
                return null;
            }
            return WRAPPERS_FLUID.computeIfAbsent(inventory, FluidInventoryWrapper::of);
        };
    }

    public static FluidInventory getFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        if (DISABLE) {
            return null;
        }
        Storage<FluidVariant> inventory = FluidStorage.SIDED.find(world, pos, state, blockEntity, direction);
        if (inventory == null) {
            return null;
        }
        return WRAPPERS_FLUID.computeIfAbsent(inventory, FluidInventoryWrapper::of);
    }

    public static boolean hasFluidInventory(ItemStack stack) {
        if (DISABLE) {
            return false;
        }
        FluidItemContext context = FluidItemContext.of(stack);
        boolean result = FluidStorage.ITEM.find(stack, context) != null;
        context.close();
        return result;
    }

    public static FluidItemInventory getFluidInventory(ItemStack stack) {
        if (DISABLE) {
            return null;
        }
        FluidItemContext context = FluidItemContext.of(stack);
        Storage<FluidVariant> inventory = FluidStorage.ITEM.find(stack, context);
        if (inventory == null) {
            context.close();
            return null;
        }
        return FluidItemInventoryWrapper.of(inventory, context);
    }

    private static <T extends SmartBlockEntity> void registerItemSide(BlockEntityType<T> type, Function<T, Container> factory) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerItemSide(BlockEntityType<T> type, BiFunction<T, Direction, Container> factory) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedDirectionInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedDirectionInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(BlockEntityType<T> type, Function<T, FluidInventory> factory) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedFluidInventoryBehaviour<>(be, factory));
        FluidStorage.SIDED.registerForBlockEntity(CachedFluidInventoryBehaviour::get, type);
    }

    public static void register() {
        if (DISABLE) {
            return;
        }
        registerItemSide(AllBlockEntityTypes.DEPOT, be -> be.depotBehaviour.itemHandler);
        registerItemSide(AllBlockEntityTypes.WEIGHTED_EJECTOR, be -> be.depotBehaviour.itemHandler);
        registerItemSide(
            AllBlockEntityTypes.BELT, be -> {
                if (!BeltBlock.canTransportObjects(be.getBlockState()))
                    return null;
                if (!be.isRemoved() && be.itemHandler == null)
                    be.initializeItemHandler();
                return be.itemHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.MILLSTONE, be -> be.capability);
        registerItemSide(AllBlockEntityTypes.SAW, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.BASIN, be -> be.itemCapability);
        registerItemSide(
            AllBlockEntityTypes.ANDESITE_TUNNEL, be -> {
                if (be.cap == null) {
                    Level world = be.getLevel();
                    BlockPos pos = be.getBlockPos();
                    BlockState state = world.getBlockState(pos.below());
                    if (state.is(AllBlocks.BELT)) {
                        BlockEntity beBelow = world.getBlockEntity(pos.below());
                        if (beBelow != null) {
                            Container capBelow = ItemHelper.getInventory(world, pos.below(), state, beBelow, Direction.UP);
                            if (capBelow != null) {
                                be.cap = capBelow;
                            }
                        }
                    }
                }
                return be.cap;
            }
        );
        registerItemSide(AllBlockEntityTypes.BRASS_TUNNEL, be -> be.tunnelCapability);
        registerItemSide(AllBlockEntityTypes.CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.SMART_CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, be -> be.capability);
        registerItemSide(
            AllBlockEntityTypes.ITEM_DRAIN, (be, context) -> {
                if (context != null && context.getAxis().isHorizontal())
                    return be.itemHandlers.get(context);
                return null;
            }
        );
        registerItemSide(
            AllBlockEntityTypes.DEPLOYER, be -> {
                if (be.invHandler == null)
                    be.initHandler();
                return be.invHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.MECHANICAL_CRAFTER, MechanicalCrafterBlockEntity::getInvCapability);
        registerItemSide(AllBlockEntityTypes.CREATIVE_CRATE, be -> be.inv);
        registerItemSide(AllBlockEntityTypes.PACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.REPACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_POSTBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_FROGPORT, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TOOLBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TRACK_STATION, be -> be.depotBehaviour.itemHandler);
        registerFluidSide(
            AllBlockEntityTypes.FLUID_TANK, be -> {
                if (be.fluidCapability == null)
                    be.refreshCapability();
                return be.fluidCapability;
            }
        );
        registerFluidSide(AllBlockEntityTypes.BASIN, be -> be.fluidCapability);
        registerFluidSide(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, be -> be.capability);
        registerFluidSide(AllBlockEntityTypes.HOSE_PULLEY, be -> be.handler);
        registerFluidSide(AllBlockEntityTypes.SPOUT, be -> be.tank.getCapability());
    }
}
