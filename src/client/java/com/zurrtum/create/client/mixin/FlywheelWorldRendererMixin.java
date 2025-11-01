package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.FlwImplXplat;
import com.zurrtum.create.client.flywheel.impl.event.RenderContextImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.BlockBreakingInfo;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.SortedSet;

@Mixin(value = WorldRenderer.class, priority = 1001) // Higher priority to go after Sodium
public class FlywheelWorldRendererMixin {
    @Shadow
    @Nullable
    private ClientWorld world;

    @Shadow
    @Final
    public BufferBuilderStorage bufferBuilders;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Unique
    @Nullable
    private RenderContextImpl flywheel$renderContext;

    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/chunk/light/LightingProvider;doLightUpdates()I"))
    private void flywheel$beginRender(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
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
            (WorldRenderer) (Object) this,
            world,
            bufferBuilders,
            positionMatrix,
            matrix4f,
            camera,
            tickCounter.getTickProgress(false)
        );

        VisualizationManager manager = VisualizationManager.get(world);
        if (manager != null) {
            manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void flywheel$endRender(CallbackInfo ci) {
        flywheel$renderContext = null;
    }

    @Inject(method = "reload()V", at = @At("RETURN"))
    private void flywheel$reload(CallbackInfo ci) {
        if (world != null) {
            FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(world);
        }
    }

    @Inject(method = "renderBlockEntities", at = @At(value = "HEAD"))
    private void flywheel$beforeBlockEntities(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(world);
            if (manager != null) {
                manager.renderDispatcher().afterEntities(flywheel$renderContext);
            }
        }
    }

    @Inject(method = "renderBlockDamage", at = @At(value = "HEAD"))
    private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
        if (flywheel$renderContext != null) {
            VisualizationManager manager = VisualizationManager.get(world);
            if (manager != null) {
                manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, blockBreakingProgressions);
            }
        }
    }

    @WrapOperation(method = "fillEntityRenderStates(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/render/state/WorldRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<Entity> flywheel$decideNotToRenderEntity(Iterable<Entity> instance, Operation<Iterator<Entity>> original) {
        return VisualizationHelper.skipVanillaRender(world, original.call(instance));
    }
}
