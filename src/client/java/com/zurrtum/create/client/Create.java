package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.compat.CompatMod;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueSelectionHandler;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffectHandler;
import com.zurrtum.create.client.content.equipment.potatoCannon.PotatoCannonRenderHandler;
import com.zurrtum.create.client.content.equipment.zapper.ZapperRenderHandler;
import com.zurrtum.create.client.content.schematics.client.ClientSchematicLoader;
import com.zurrtum.create.client.content.schematics.client.SchematicAndQuillHandler;
import com.zurrtum.create.client.content.schematics.client.SchematicHandler;
import com.zurrtum.create.client.flywheel.impl.Flywheel;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsClient;
import com.zurrtum.create.client.foundation.ponder.CreatePonderPlugin;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.vanillin.Vanillin;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class Create implements ClientModInitializer {
    public static SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER;
    public static ValueSettingsClient VALUE_SETTINGS_HANDLER;
    public static SuperGlueSelectionHandler GLUE_HANDLER;
    public static GlobalRailwayManager RAILWAYS;
    public static PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER;
    public static ClientSchematicLoader SCHEMATIC_SENDER;
    public static SchematicHandler SCHEMATIC_HANDLER;
    public static SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER;
    public static ZapperRenderHandler ZAPPER_RENDER_HANDLER;

    @Override
    public void onInitializeClient() {
        new Flywheel().onInitializeClient();
        new Ponder().onInitializeClient();
        new Vanillin().onInitializeClient();
        SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();
        VALUE_SETTINGS_HANDLER = new ValueSettingsClient();
        GLUE_HANDLER = new SuperGlueSelectionHandler();
        RAILWAYS = new GlobalRailwayManager();
        POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
        SCHEMATIC_SENDER = new ClientSchematicLoader();
        SCHEMATIC_HANDLER = new SchematicHandler();
        SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
        ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
        AllConfigs.register();
        AllFluidConfigs.register();
        AllHandle.register();
        AllKeys.register();
        AllCasings.register();
        AllCTBehaviours.register();
        AllModels.register();
        AllPartialModels.register();
        AllEntityRenders.register();
        AllBlockEntityRenders.register();
        AllBlockEntityBehaviours.register();
        AllEntityBehaviours.register();
        AllItemTooltips.register();
        AllBlockLayers.register();
        AllBufferCaches.register(SuperByteBufferCache.getInstance());
        AllExtensions.register();
        AllMovementRenders.register();
        AllDisplaySourceRenders.register();
        AllTrackMaterialModels.register();
        AllTrackRenders.register();
        AllBogeyStyleRenders.register();
        AllTrainIcons.register();
        AllScheduleRenders.register();
        AllMenuScreens.register();
        AllPotatoProjectileTransforms.register();
        PonderIndex.addPlugin(new CreatePonderPlugin());
        CompatMod.register();
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}