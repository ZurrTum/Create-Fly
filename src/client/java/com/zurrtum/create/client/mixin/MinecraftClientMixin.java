package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.ghostblock.GhostBlocks;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.zurrtum.create.client.content.contraptions.actors.trainControls.ControlsHandler;
import com.zurrtum.create.client.content.contraptions.chassis.ChassisRangeDisplay;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingHandlerClient;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingRenderer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionRenderInfoManager;
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
import com.zurrtum.create.content.contraptions.minecart.CouplingPhysics;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.kinetics.drill.CobbleGenOptimisation;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    @Final
    private ReloadableResourceManagerImpl resourceManager;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    private Window window;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManagerImpl;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReload;"))
    private void flywheel$onBeginInitialResourceReload(RunArgs args, CallbackInfo ci) {
        FlwImpl.freezeRegistries();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setVsync(Z)V"))
    private void register(RunArgs args, CallbackInfo ci) {
        resourceManager.registerReloader(ObjLoader.INSTANCE);
        resourceManager.registerReloader(FlwProgramsReloader.INSTANCE);
        resourceManager.registerReloader(TrainHatInfoReloadListener.LISTENER);
    }

    @Inject(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/resource/ReloadableResourceManagerImpl"))
    private void init(RunArgs args, CallbackInfo ci) {
        UIRenderHelper.init();
    }

    @Inject(method = "onResolutionChanged()V", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        UIRenderHelper.updateWindowSize(window);
    }

    @Inject(method = "method_53522", at = @At("HEAD"))
    private void endReload(MinecraftClient.LoadingContext loadingContext, Optional<Throwable> error, CallbackInfo ci) {
        BackendManagerImpl.onEndClientResourceReload(error.isPresent());
        RendererReloadCache.onReloadLevelRenderer();
    }

    @Inject(method = "method_24228", at = @At("HEAD"))
    private void endReload(
        boolean bl,
        MinecraftClient.LoadingContext loadingContext,
        CompletableFuture<Void> completableFuture,
        Optional<Throwable> error,
        CallbackInfo ci
    ) {
        BackendManagerImpl.onEndClientResourceReload(error.isPresent());
        RendererReloadCache.onReloadLevelRenderer();
    }

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void unload(ClientWorld world, CallbackInfo ci) {
        if (world != null) {
            LevelAttached.invalidateLevel(world);
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tickPre(CallbackInfo ci) {
        AnimationTickHolder.tick();
        if (world == null || player == null) {
            return;
        }
        MinecraftClient mc = (MinecraftClient) (Object) this;
        PlacementClient.tick(mc);
        GhostBlocks.getInstance().tickGhosts();
        Outliner.getInstance().tickOutlines();
        LinkedControllerClientHandler.tick(mc);
        ControlsHandler.tick(mc);
        AirCurrentClient.tickClientPlayerSounds();
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickPost(CallbackInfo ci) {
        if (world == null || player == null) {
            return;
        }
        MinecraftClient mc = (MinecraftClient) (Object) this;
        SoundScapes.tick();
        Create.SCHEMATIC_SENDER.tick(mc);
        Create.SCHEMATIC_AND_QUILL_HANDLER.tick(mc);
        Create.GLUE_HANDLER.tick(mc);
        Create.SCHEMATIC_HANDLER.tick(mc);
        Create.ZAPPER_RENDER_HANDLER.tick();
        Create.POTATO_CANNON_RENDER_HANDLER.tick();
        Create.SOUL_PULSE_EFFECT_HANDLER.tick(world);
        GlobalRailwayManagerClient.tick(mc);
        ContraptionHandlerClient.tick(world);
        CapabilityMinecartController.tick(world);
        CouplingPhysics.tick(world);
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
        ContraptionRenderInfoManager.tickFor(mc);
        BlueprintOverlayRenderer.tick(mc);
        ToolboxHandlerClient.clientTick();
        TrackTargetingClient.clientTick(mc);
        TrackPlacementClient.clientTick(mc);
        TrainRelocatorClient.clientTick(mc);
        ClickToLinkHandler.clientTick(mc);
        CurvedTrackInteraction.clientTick(mc);
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

    @Inject(method = "render(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V"))
    private void render(boolean tick, CallbackInfo ci) {
        TurntableHandler.gameRenderFrame((MinecraftClient) (Object) this);
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void tick(CallbackInfo ci) {
        VisualizationEventHandler.onClientTick((MinecraftClient) (Object) this, world);
    }

    @Inject(method = "doItemUse()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void doItemUse(CallbackInfo ci, @Local Hand hand) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (hand == Hand.MAIN_HAND && (CurvedTrackInteraction.onClickInput(mc, false) || Create.GLUE_HANDLER.onMouseInput(
            mc,
            false
        ) || FactoryPanelConnectionHandler.onRightClick(mc) || ChainConveyorConnectionHandler.onRightClick(mc) || TrainRelocatorClient.onClicked(mc) || ChainConveyorInteractionHandler.onUse(
            mc) || PackagePortTargetSelectionHandler.onUse(mc) || ChainPackageInteractionHandler.onUse(mc))) {
            player.swingHand(hand);
            ci.cancel();
        } else if (ContraptionHandlerClient.rightClickingOnContraptionsGetsHandledLocally(mc, hand)) {
            ci.cancel();
        }
        if (hand == Hand.MAIN_HAND) {
            LinkedControllerClientHandler.deactivateInLectern(player);
        }
    }

    @WrapOperation(method = "handleBlockBreaking(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean handleBlockBreaking(ClientPlayerInteractionManager instance, BlockPos pos, Direction direction, Operation<Boolean> original) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        return CurvedTrackInteraction.onClickInput(mc, true) || Create.GLUE_HANDLER.onMouseInput(mc, true) || original.call(instance, pos, direction);
    }

    @Inject(method = "doAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"), cancellable = true)
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (CurvedTrackInteraction.onClickInput(mc, true) || Create.GLUE_HANDLER.onMouseInput(mc, true)) {
            player.swingHand(Hand.MAIN_HAND);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V"))
    private void missingAttack(CallbackInfoReturnable<Boolean> cir) {
        player.networkHandler.sendPacket(AllPackets.LEFT_CLICK);
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("HEAD"))
    private void onJoinWorld(CallbackInfo ci) {
        if (world != null) {
            onUnloadWorld(ci);
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;clear()V"))
    private void onUnloadWorld(CallbackInfo ci) {
        Create.SCHEMATIC_HANDLER.updateRenderers();
        ContraptionRenderInfoManager.resetAll();
        Create.SOUL_PULSE_EFFECT_HANDLER.refresh();
        AnimationTickHolder.reset();
        ControlsHandler.levelUnloaded();
        WorldAttached.invalidateWorld(world);
        CobbleGenOptimisation.invalidateWorld(world);
    }

    @Inject(method = "doItemPick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;hasControlDown()Z"), cancellable = true)
    private void doItemPick(CallbackInfo ci) {
        if (ToolboxHandlerClient.onPickItem((MinecraftClient) (Object) this)) {
            ci.cancel();
        }
    }
}
