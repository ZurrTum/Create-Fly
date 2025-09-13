package com.zurrtum.create;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.decoration.encasing.EncasingRegistry;
import com.zurrtum.create.content.equipment.armor.AllArmorMaterials;
import com.zurrtum.create.content.equipment.armor.AllEquipmentAssetKeys;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes;
import com.zurrtum.create.content.equipment.tool.AllToolMaterials;
import com.zurrtum.create.content.fluids.AllFlowCollision;
import com.zurrtum.create.content.fluids.tank.BoilerHeaters;
import com.zurrtum.create.content.kinetics.TorquePropagator;
import com.zurrtum.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.logistics.packagePort.AllPackagePortTargetTypes;
import com.zurrtum.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.zurrtum.create.content.schematics.ServerSchematicLoader;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.track.AllPortalTracks;
import com.zurrtum.create.foundation.CreateNBTProcessors;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllPlacedFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllPlacementModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class Create implements ModInitializer {
    public static final String MOD_ID = "create";
    public static final String NAME = "Create";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion()
        .getFriendlyString().split("\\+")[0];
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static boolean Lazy = false;

    public static MinecraftServer SERVER;
    public static TorquePropagator TORQUE_PROPAGATOR;
    public static GlobalRailwayManager RAILWAYS;
    public static RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER;
    public static GlobalLogisticsManager LOGISTICS;
    public static ServerSchematicLoader SCHEMATIC_RECEIVER;

    @Override
    public void onInitialize() {
        AllConfigs.register();
        if (Lazy) {
            register();
        }
    }

    public static void register() {
        TORQUE_PROPAGATOR = new TorquePropagator();
        RAILWAYS = new GlobalRailwayManager();
        REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
        LOGISTICS = new GlobalLogisticsManager();
        SCHEMATIC_RECEIVER = new ServerSchematicLoader();
        CreateRegistryKeys.register();
        CreateRegistries.register();
        AllFluids.register();
        AllPackageStyles.register();
        AllToolMaterials.register();
        AllArmorMaterials.register();
        AllBlocks.register();
        EncasingRegistry.register();
        BlockStressValues.register();
        AllItems.register();
        AllFlowCollision.register();
        AllFluidTags.register();
        AllBlockTags.register();
        AllItemTags.register();
        AllMountedItemStorageTypeTags.register();
        AllContraptionTypeTags.register();
        AllEntityTags.register();
        AllSoundEvents.register();
        AllParticleTypes.register();
        AllDataComponents.register();
        AllDamageTypes.register();
        AllPackets.register();
        AllCreativeModeTabs.register();
        AllContraptionTypes.register();
        AllEntityTypes.register();
        AllBlockEntityTypes.register();
        AllAdvancements.register();
        AllRecipeTypes.register();
        AllRecipeSerializers.register();
        AllRecipeSets.register();
        AllFluidItemInventory.register();
        AllTransfer.register();
        AllOpenPipeEffectHandlers.register();
        AllArmInteractionPointTypes.register();
        AllFanProcessingTypes.register();
        BoilerHeaters.register();
        AllSynchedDatas.register();
        AllMountedStorageTypes.register();
        AllMovementBehaviours.register();
        AllContraptionMovementSettings.register();
        AllInteractionBehaviours.register();
        AllEquipmentAssetKeys.register();
        AllTrackMaterials.register();
        AllDisplayTargets.register();
        AllDisplaySources.register();
        AllMapDecorationTypes.register();
        AllBogeySizes.register();
        AllBogeyStyles.register();
        AllPortalTracks.register();
        AllSchedules.register();
        AllMenuTypes.register();
        AllAssemblyRecipeNames.register();
        AllPotatoProjectileRenderModes.register();
        AllPotatoProjectileBlockHitActions.register();
        AllPotatoProjectileEntityHitActions.register();
        AllDynamicRegistries.register();
        AllItemAttributeTypes.register();
        AllPackagePortTargetTypes.register();
        AllUnpackingHandlers.register();
        AllFuelTimes.register();
        AllStructureProcessorTypes.register();
        CreateNBTProcessors.register();
        AllFeatures.register();
        AllConfiguredFeatures.register();
        AllPlacedFeatures.register();
        AllPlacementModifiers.register();
    }
}