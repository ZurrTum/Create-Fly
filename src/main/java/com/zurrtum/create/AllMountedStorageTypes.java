package com.zurrtum.create;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.chest.ChestMountedStorageType;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.content.contraptions.behaviour.dispenser.storage.DispenserMountedStorageType;
import com.zurrtum.create.content.equipment.toolbox.ToolboxMountedStorageType;
import com.zurrtum.create.content.fluids.tank.storage.FluidTankMountedStorageType;
import com.zurrtum.create.content.fluids.tank.storage.creative.CreativeFluidTankMountedStorageType;
import com.zurrtum.create.content.logistics.crate.CreativeCrateMountedStorageType;
import com.zurrtum.create.content.logistics.depot.storage.DepotMountedStorageType;
import com.zurrtum.create.content.logistics.vault.ItemVaultMountedStorageType;
import com.zurrtum.create.impl.contraption.storage.FallbackMountedStorageType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import static com.zurrtum.create.Create.MOD_ID;

public class AllMountedStorageTypes {
    public static final DepotMountedStorageType DEPOT = register("depot", new DepotMountedStorageType());
    public static final CreativeCrateMountedStorageType CREATIVE_CRATE = register("creative_crate", new CreativeCrateMountedStorageType());
    public static final ItemVaultMountedStorageType VAULT = register("vault", new ItemVaultMountedStorageType());
    public static final ToolboxMountedStorageType TOOLBOX = register("toolbox", new ToolboxMountedStorageType());
    public static final ChestMountedStorageType CHEST = register("chest", new ChestMountedStorageType());
    public static final DispenserMountedStorageType DISPENSER = register("dispenser", new DispenserMountedStorageType());
    public static final SimpleMountedStorageType<SimpleMountedStorage> SIMPLE = register("simple", new SimpleMountedStorageType.Impl());
    public static final FallbackMountedStorageType FALLBACK = register("fallback", new FallbackMountedStorageType());
    public static final FluidTankMountedStorageType FLUID_TANK = register("fluid_tank", new FluidTankMountedStorageType());
    public static final CreativeFluidTankMountedStorageType CREATIVE_FLUID_TANK = register(
        "creative_fluid_tank",
        new CreativeFluidTankMountedStorageType()
    );

    private static <T extends MountedItemStorageType<?>> T register(String id, T type) {
        return Registry.register(
            CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE,
            ResourceKey.create(CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, id)),
            type
        );
    }

    private static <T extends MountedFluidStorageType<?>> T register(String id, T type) {
        return Registry.register(
            CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE,
            ResourceKey.create(CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, id)),
            type
        );
    }

    public static void register(MountedItemStorageType<?> type, Block... blocks) {
        for (Block block : blocks) {
            MountedItemStorageType.REGISTRY.register(block, type);
        }
    }

    @SuppressWarnings("deprecation")
    public static void register(MountedItemStorageType<?> type, TagKey<Block> tag) {
        MountedItemStorageType.REGISTRY.registerProvider(block -> block.builtInRegistryHolder().is(tag) ? type : null);
    }

    public static void register(MountedFluidStorageType<?> type, Block... blocks) {
        for (Block block : blocks) {
            MountedFluidStorageType.REGISTRY.register(block, type);
        }
    }

    public static void register() {
        register(DEPOT, AllBlocks.DEPOT);
        register(CREATIVE_CRATE, AllBlocks.CREATIVE_CRATE);
        register(VAULT, AllBlocks.ITEM_VAULT);
        register(
            TOOLBOX,
            AllBlocks.WHITE_TOOLBOX,
            AllBlocks.ORANGE_TOOLBOX,
            AllBlocks.MAGENTA_TOOLBOX,
            AllBlocks.LIGHT_BLUE_TOOLBOX,
            AllBlocks.YELLOW_TOOLBOX,
            AllBlocks.LIME_TOOLBOX,
            AllBlocks.PINK_TOOLBOX,
            AllBlocks.GRAY_TOOLBOX,
            AllBlocks.LIGHT_GRAY_TOOLBOX,
            AllBlocks.CYAN_TOOLBOX,
            AllBlocks.PURPLE_TOOLBOX,
            AllBlocks.BLUE_TOOLBOX,
            AllBlocks.BROWN_TOOLBOX,
            AllBlocks.GREEN_TOOLBOX,
            AllBlocks.RED_TOOLBOX,
            AllBlocks.BLACK_TOOLBOX
        );
        register(CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST);
        register(DISPENSER, Blocks.DISPENSER, Blocks.DROPPER);
        register(SIMPLE, AllBlockTags.SIMPLE_MOUNTED_STORAGE);
        register(FALLBACK, AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST);
        register(FLUID_TANK, AllBlocks.FLUID_TANK);
    }
}
