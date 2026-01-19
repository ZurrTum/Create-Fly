package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.FlwImplXplat;
import com.zurrtum.create.client.flywheel.impl.event.RenderContextImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.SortedSet;

@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after Sodium
public class FlywheelLevelRendererMixin {
    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    public RenderBuffers renderBuffers;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Unique
    @Nullable
    private RenderContextImpl flywheel$renderContext;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"))
    private void flywheel$beginRender(
        GraphicsResourceAllocator allocator,
        DeltaTracker tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f matrix4f,
        Matrix4f projectionMatrix,
        GpuBufferSlice fog,
        Vector4f fogColor,
        boolean shouldRenderSky,
        CallbackInfo ci
    ) {
        flywheel$renderContext = RenderContextImpl.create(
            (LevelRenderer) (Object) this,
            level,
            renderBuffers,
            positionMatrix,
            matrix4f,
            camera,
            tickCounter.getGameTimeDeltaPartialTick(false)
        );

        VisualizationManager manager = VisualizationManager.get(level);
        if (manager != null) {
            manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void flywheel$endRender(CallbackInfo ci) {
        flywheel$renderContext = null;
    }

    @Inject(method = "allChanged()V", at = @At("RETURN"))
    private void flywheel$reload(CallbackInfo ci) {
        if (level != null) {
            FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
        }
    }

    @Inject(method = "submitBlockEntities", at = @At(value = "HEAD"))
    private void flywheel$beforeBlockEntities(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(level);
            if (manager != null) {
                manager.renderDispatcher().afterEntities(flywheel$renderContext);
            }
        }
    }

    @Inject(method = "renderBlockDestroyAnimation", at = @At(value = "HEAD"))
    private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(level);
            if (manager != null) {
                manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, destructionProgress);
            }
        }
    }

    @WrapOperation(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<Entity> flywheel$decideNotToRenderEntity(Iterable<Entity> instance, Operation<Iterator<Entity>> original) {
        return VisualizationHelper.skipVanillaRender(level, original.call(instance));
    }
}
