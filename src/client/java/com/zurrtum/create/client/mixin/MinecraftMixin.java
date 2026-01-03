package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.ghostblock.GhostBlocks;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.zurrtum.create.client.content.contraptions.actors.trainControls.ControlsHandler;
import com.zurrtum.create.client.content.contraptions.chassis.ChassisRangeDisplay;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingHandlerClient;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingRenderer;
import com.zurrtum.create.client.content.contraptions.wrench.RadialWrenchHandler;
import com.zurrtum.create.client.content.decoration.girder.GirderWrenchBehaviorHandler;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorStealthOverlay;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.content.equipment.clipboard.ClipboardValueSettingsClientHandler;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.zurrtum.create.client.content.equipment.symmetryWand.SymmetryHandlerClient;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import com.zurrtum.create.client.content.equipment.zapper.terrainzapper.WorldshaperRenderHandler;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.content.kinetics.belt.item.BeltConnectorHandler;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorInteractionHandler;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainPackageInteractionHandler;
import com.zurrtum.create.client.content.kinetics.fan.AirCurrentClient;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.zurrtum.create.client.content.kinetics.turntable.TurntableHandler;
import com.zurrtum.create.client.content.logistics.depot.EjectorTargetHandler;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.client.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.zurrtum.create.client.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import com.zurrtum.create.client.content.logistics.tableCloth.TableClothOverlayRenderer;
import com.zurrtum.create.client.content.redstone.displayLink.ClickToLinkHandler;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.trains.CameraDistanceModifier;
import com.zurrtum.create.client.content.trains.GlobalRailwayManagerClient;
import com.zurrtum.create.client.content.trains.TrainHUD;
import com.zurrtum.create.client.content.trains.entity.TrainRelocatorClient;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.content.trains.track.CurvedTrackInteraction;
import com.zurrtum.create.client.content.trains.track.TrackPlacementClient;
import com.zurrtum.create.client.content.trains.track.TrackTargetingClient;
import com.zurrtum.create.client.flywheel.backend.compile.FlwProgramsReloader;
import com.zurrtum.create.client.flywheel.impl.BackendManagerImpl;
import com.zurrtum.create.client.flywheel.impl.FlwImpl;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationEventHandler;
import com.zurrtum.create.client.flywheel.lib.util.LevelAttached;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.foundation.utility.ServerSpeedProvider;
import com.zurrtum.create.client.model.obj.ObjLoader;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.kinetics.drill.CobbleGenOptimisation;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    protected abstract void startUseItem();

    @Shadow
    private int rightClickDelay;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private void flywheel$onBeginInitialResourceReload(GameConfig args, CallbackInfo ci) {
        FlwImpl.freezeRegistries();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateVsync(Z)V"))
    private void register(GameConfig args, CallbackInfo ci) {
        resourceManager.registerReloadListener(ObjLoader.INSTANCE);
        resourceManager.registerReloadListener(FlwProgramsReloader.INSTANCE);
        resourceManager.registerReloadListener(TrainHatInfoReloadListener.LISTENER);
    }

    @Inject(method = "onGameLoadFinished(Lnet/minecraft/client/Minecraft$GameLoadCookie;)V", at = @At("HEAD"))
    private void endReload(Minecraft.GameLoadCookie cookie, CallbackInfo ci) {
        BackendManagerImpl.onEndClientResourceReload();
        RendererReloadCache.onReloadLevelRenderer();
    }

    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
    private void unload(ClientLevel world, CallbackInfo ci) {
        if (world != null) {
            LevelAttached.invalidateLevel(world);
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tickPre(CallbackInfo ci) {
        AnimationTickHolder.tick();
        PonderTooltipHandler.tick();
        if (level == null || player == null) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        PlacementClient.tick(mc);
        GhostBlocks.getInstance().tickGhosts();
        Outliner.getInstance().tickOutlines();
        LinkedControllerClientHandler.tick(mc);
        ControlsHandler.tick(mc);
        AirCurrentClient.tickClientPlayerSounds();
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        if (level == null || player == null) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        SoundScapes.tick();
        Create.SCHEMATIC_SENDER.tick(mc);
        Create.SCHEMATIC_AND_QUILL_HANDLER.tick(mc);
        Create.GLUE_HANDLER.tick(mc);
        Create.SCHEMATIC_HANDLER.tick(mc);
        Create.ZAPPER_RENDER_HANDLER.tick();
        Create.POTATO_CANNON_RENDER_HANDLER.tick();
        Create.SOUL_PULSE_EFFECT_HANDLER.tick(level);
        GlobalRailwayManagerClient.tick(mc);
        ContraptionHandlerClient.tick(level);
        CapabilityMinecartController.tick(level);
        ServerSpeedProvider.clientTick(mc);
        BeltConnectorHandler.tick(mc);
        FilteringRenderer.tick(mc);
        LinkRenderer.tick(mc);
        ScrollValueRenderer.tick(mc);
        ChassisRangeDisplay.tick(mc);
        EdgeInteractionRenderer.tick(mc);
        GirderWrenchBehaviorHandler.tick(mc);
        WorldshaperRenderHandler.tick(mc);
        CouplingHandlerClient.tick(mc);
        CouplingRenderer.tickDebugModeRenders(mc);
        KineticDebugger.tick(mc);
        ExtendoGripRenderHandler.tick(mc);
        ArmInteractionPointHandler.tick(mc);
        EjectorTargetHandler.tick(mc);
        BlueprintOverlayRenderer.tick(mc);
        ToolboxHandlerClient.clientTick();
        RadialWrenchHandler.clientTick();
        TrackTargetingClient.clientTick(mc);
        TrackPlacementClient.clientTick(mc);
        TrainRelocatorClient.clientTick(mc);
        ClickToLinkHandler.clientTick(mc);
        CurvedTrackInteraction.clientTick(mc);
        CameraDistanceModifier.tick();
        TrainHUD.tick(mc);
        ClipboardValueSettingsClientHandler.clientTick(mc);
        Create.VALUE_SETTINGS_HANDLER.tick(mc);
        ScrollValueHandler.tick(mc);
        ContraptionPlayerPassengerRotation.tick();
        ChainConveyorInteractionHandler.clientTick(mc);
        ChainConveyorRidingHandler.clientTick(mc);
        ChainConveyorConnectionHandler.clientTick(mc);
        PackagePortTargetSelectionHandler.tick(mc);
        LogisticallyLinkedClientHandler.tick(mc);
        TableClothOverlayRenderer.tick(mc);
        CardboardArmorStealthOverlay.clientTick(mc);
        FactoryPanelConnectionHandler.clientTick(mc);
        TickBasedCache.clientTick();
        SymmetryHandlerClient.onClientTick(mc);
    }

    @Inject(method = "renderFrame(ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void render(boolean recordGpuUtilization, boolean renderLevel, CallbackInfo ci) {
        TurntableHandler.gameRenderFrame((Minecraft) (Object) this);
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void tick(CallbackInfo ci) {
        VisualizationEventHandler.onClientTick((Minecraft) (Object) this, level);
    }

    @Inject(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void doItemUse(CallbackInfo ci, @Local InteractionHand hand) {
        Minecraft mc = (Minecraft) (Object) this;
        if (hand == InteractionHand.MAIN_HAND && (CurvedTrackInteraction.onClickInput(mc, false) || Create.GLUE_HANDLER.onMouseInput(
            mc,
            false
        ) || FactoryPanelConnectionHandler.onRightClick(mc) || ChainConveyorConnectionHandler.onRightClick(mc) || TrainRelocatorClient.onClicked(mc) || ChainConveyorInteractionHandler.onUse(
            mc) || PackagePortTargetSelectionHandler.onUse(mc) || ChainPackageInteractionHandler.onUse(mc))) {
            player.swing(hand);
            ci.cancel();
        } else if (ContraptionHandlerClient.rightClickingOnContraptionsGetsHandledLocally(mc, hand)) {
            ci.cancel();
        }
        if (hand == InteractionHand.MAIN_HAND) {
            LinkedControllerClientHandler.deactivateInLectern(player);
        }
    }

    @WrapOperation(method = "continueAttack(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean handleBlockBreaking(MultiPlayerGameMode instance, BlockPos pos, Direction direction, Operation<Boolean> original) {
        Minecraft mc = (Minecraft) (Object) this;
        return CurvedTrackInteraction.onClickInput(mc, true) || Create.GLUE_HANDLER.onMouseInput(mc, true) || original.call(instance, pos, direction);
    }

    @Inject(method = "startAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = (Minecraft) (Object) this;
        if (CurvedTrackInteraction.onClickInput(mc, true) || Create.GLUE_HANDLER.onMouseInput(mc, true)) {
            player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
    private void missingAttack(CallbackInfoReturnable<Boolean> cir) {
        player.connection.send(AllPackets.LEFT_CLICK);
    }

    @Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
    private void onJoinWorld(CallbackInfo ci) {
        if (level != null) {
            onUnloadWorld(null);
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;onDisconnected()V"))
    private void onUnloadWorld(CallbackInfo ci) {
        Create.SCHEMATIC_HANDLER.updateRenderers();
        Create.SOUL_PULSE_EFFECT_HANDLER.refresh();
        AnimationTickHolder.reset();
        ControlsHandler.levelUnloaded();
        WorldAttached.invalidateWorld(level);
        CobbleGenOptimisation.invalidateWorld(level);
    }

    @Inject(method = "pickBlock()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"), cancellable = true)
    private void doItemPick(CallbackInfo ci) {
        if (ToolboxHandlerClient.onPickItem((Minecraft) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;getRuntime()Ljava/lang/Runtime;"))
    private void run(CallbackInfo ci) {
        PonderIndex.registerAll();
    }

    @WrapOperation(method = "handleKeybinds()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    private boolean onUse(KeyMapping instance, Operation<Boolean> original) {
        if (player.getActiveItem().getItem() instanceof LinkedControllerItem) {
            if (rightClickDelay == 0 && original.call(instance)) {
                startUseItem();
            }
            return true;
        }
        return original.call(instance);
    }
}
