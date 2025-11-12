package com.zurrtum.create;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.zurrtum.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
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
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllTransfer {
    public static final boolean DISABLE = !FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1");
    public static Map<Class<? extends SmartBlockEntity>, List<Function<? extends SmartBlockEntity, BlockEntityBehaviour<?>>>> ALL;
    private static final Map<Storage<ItemVariant>, Container> WRAPPERS_ITEM = new MapMaker().weakValues().makeMap();
    private static final Map<Storage<FluidVariant>, FluidInventory> WRAPPERS_FLUID = new MapMaker().weakValues().makeMap();

    @SuppressWarnings("unchecked")
    public static <T extends SmartBlockEntity> void addBehaviours(T blockEntity, ArrayList<BlockEntityBehaviour<?>> behaviours) {
        if (DISABLE) {
            return;
        }
        List<Function<? extends SmartBlockEntity, BlockEntityBehaviour<?>>> list = ALL.get(blockEntity.getClass());
        if (list == null) {
            return;
        }
        for (Function<? extends SmartBlockEntity, BlockEntityBehaviour<?>> factory : list) {
            behaviours.add(((Function<T, BlockEntityBehaviour<?>>) factory).apply(blockEntity));
        }
    }

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

    private static <T extends SmartBlockEntity> void registerItemSide(Class<T> target, BlockEntityType<T> type, Function<T, Container> factory) {
        ALL.computeIfAbsent(target, t -> new ArrayList<>()).add((T be) -> new CachedInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
        Class<T> target,
        BlockEntityType<T> type,
        BiFunction<T, Direction, Container> factory
    ) {
        ALL.computeIfAbsent(target, t -> new ArrayList<>()).add((T be) -> new CachedDirectionInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedDirectionInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
        Class<T> target,
        BlockEntityType<T> type,
        Function<T, FluidInventory> factory
    ) {
        ALL.computeIfAbsent(target, t -> new ArrayList<>()).add((T be) -> new CachedFluidInventoryBehaviour<>(be, factory));
        FluidStorage.SIDED.registerForBlockEntity(CachedFluidInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
        Class<T> target,
        BlockEntityType<T> type,
        Function<T, FluidInventory> factory,
        BiPredicate<T, Direction> valid
    ) {
        ALL.computeIfAbsent(target, t -> new ArrayList<>()).add((T be) -> new CachedFluidInventoryBehaviour<>(be, factory));
        FluidStorage.SIDED.registerForBlockEntity(
            (be, side) -> {
                if (valid.test(be, side)) {
                    return CachedFluidInventoryBehaviour.get(be, side);
                } else {
                    return null;
                }
            }, type
        );
    }

    public static void register() {
        if (DISABLE) {
            return;
        }
        ALL = new Reference2ObjectArrayMap<>();
        registerItemSide(DepotBlockEntity.class, AllBlockEntityTypes.DEPOT, be -> be.depotBehaviour.itemHandler);
        registerItemSide(EjectorBlockEntity.class, AllBlockEntityTypes.WEIGHTED_EJECTOR, be -> be.depotBehaviour.itemHandler);
        registerItemSide(
            BeltBlockEntity.class, AllBlockEntityTypes.BELT, be -> {
                if (!BeltBlock.canTransportObjects(be.getBlockState()))
                    return null;
                if (!be.isRemoved() && be.itemHandler == null)
                    be.initializeItemHandler();
                return be.itemHandler;
            }
        );
        registerItemSide(MillstoneBlockEntity.class, AllBlockEntityTypes.MILLSTONE, be -> be.capability);
        registerItemSide(SawBlockEntity.class, AllBlockEntityTypes.SAW, be -> be.inventory);
        registerItemSide(BasinBlockEntity.class, AllBlockEntityTypes.BASIN, be -> be.itemCapability);
        registerItemSide(
            BeltTunnelBlockEntity.class, AllBlockEntityTypes.ANDESITE_TUNNEL, be -> {
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
        registerItemSide(BrassTunnelBlockEntity.class, AllBlockEntityTypes.BRASS_TUNNEL, be -> be.tunnelCapability);
        registerItemSide(ChuteBlockEntity.class, AllBlockEntityTypes.CHUTE, be -> be.itemHandler);
        registerItemSide(SmartChuteBlockEntity.class, AllBlockEntityTypes.SMART_CHUTE, be -> be.itemHandler);
        registerItemSide(PortableItemInterfaceBlockEntity.class, AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, be -> be.capability);
        registerItemSide(
            ItemDrainBlockEntity.class, AllBlockEntityTypes.ITEM_DRAIN, (be, context) -> {
                if (context != null && context.getAxis().isHorizontal())
                    return be.itemHandlers.get(context);
                return null;
            }
        );
        registerItemSide(
            DeployerBlockEntity.class, AllBlockEntityTypes.DEPLOYER, be -> {
                if (be.invHandler == null)
                    be.initHandler();
                return be.invHandler;
            }
        );
        registerItemSide(CrushingWheelControllerBlockEntity.class, AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, be -> be.inventory);
        registerItemSide(MechanicalCrafterBlockEntity.class, AllBlockEntityTypes.MECHANICAL_CRAFTER, MechanicalCrafterBlockEntity::getInvCapability);
        registerItemSide(CreativeCrateBlockEntity.class, AllBlockEntityTypes.CREATIVE_CRATE, be -> be.inv);
        registerItemSide(PackagerBlockEntity.class, AllBlockEntityTypes.PACKAGER, be -> be.inventory);
        registerItemSide(RepackagerBlockEntity.class, AllBlockEntityTypes.REPACKAGER, be -> be.inventory);
        registerItemSide(PostboxBlockEntity.class, AllBlockEntityTypes.PACKAGE_POSTBOX, be -> be.inventory);
        registerItemSide(FrogportBlockEntity.class, AllBlockEntityTypes.PACKAGE_FROGPORT, be -> be.inventory);
        registerItemSide(ToolboxBlockEntity.class, AllBlockEntityTypes.TOOLBOX, be -> be.inventory);
        registerItemSide(StationBlockEntity.class, AllBlockEntityTypes.TRACK_STATION, be -> be.depotBehaviour.itemHandler);
        registerFluidSide(
            FluidTankBlockEntity.class, AllBlockEntityTypes.FLUID_TANK, be -> {
                if (be.fluidCapability == null)
                    be.refreshCapability();
                return be.fluidCapability;
            }
        );
        registerFluidSide(BasinBlockEntity.class, AllBlockEntityTypes.BASIN, be -> be.fluidCapability);
        registerFluidSide(PortableFluidInterfaceBlockEntity.class, AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, be -> be.capability);
        registerFluidSide(HosePulleyBlockEntity.class, AllBlockEntityTypes.HOSE_PULLEY, be -> be.handler);
        registerFluidSide(SpoutBlockEntity.class, AllBlockEntityTypes.SPOUT, be -> be.tank.getCapability());
    }
}
