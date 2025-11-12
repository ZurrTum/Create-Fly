package com.zurrtum.create.client.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.ghostblock.GhostBlocks;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.DefaultSuperRenderTypeBuffer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.compat.sodium.SodiumCompat;
import com.zurrtum.create.client.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingRenderer;
import com.zurrtum.create.client.content.equipment.clipboard.ClipboardValueSettingsClientHandler;
import com.zurrtum.create.client.content.equipment.symmetryWand.SymmetryHandlerClient;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorInteractionHandler;
import com.zurrtum.create.client.content.trains.entity.CarriageCouplingRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.content.trains.track.TrackTargetingClient;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.block.render.BlockDestructionProgressExtension;
import com.zurrtum.create.client.foundation.block.render.MultiPosDestructionHandler;
import com.zurrtum.create.foundation.block.LightControlBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * This gets called when a block is marked for rerender by vanilla.
     */
    @Inject(method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void flywheel$checkUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(level);
        if (manager == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        var blockEntities = manager.blockEntities();
        if (oldState != newState) {
            blockEntities.queueRemove(blockEntity);
            blockEntities.queueAdd(blockEntity);
        } else {
            // I don't think this is possible to reach in vanilla
            blockEntities.queueUpdate(blockEntity);
        }
    }

    @Inject(method = "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;"))
    private void renderAfterParticles(
        GraphicsResourceAllocator allocator,
        DeltaTracker tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f matrix4f,
        Matrix4f projectionMatrix,
        GpuBufferSlice fogBuffer,
        Vector4f fogColor,
        boolean renderSky,
        CallbackInfo ci,
        @Local FrameGraphBuilder frameGraphBuilder,
        @Local float tickProgress
    ) {
        FramePass framePass = frameGraphBuilder.addPass("after_particles");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        framePass.executes(() -> {
            PoseStack ms = new PoseStack();
            Vec3 cameraPos = camera.getPosition();
            SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
            GhostBlocks.getInstance().renderAll(minecraft, ms, buffer, cameraPos);
            Outliner.getInstance().renderOutlines(minecraft, ms, buffer, cameraPos, tickProgress);
            TrackBlockOutline.drawCurveSelection(minecraft, ms, buffer, cameraPos);
            TrackTargetingClient.render(minecraft, ms, buffer, cameraPos);
            CouplingRenderer.renderAll(minecraft, ms, buffer, cameraPos);
            CarriageCouplingRenderer.renderAll(minecraft, ms, buffer, cameraPos);
            Create.SCHEMATIC_HANDLER.render(minecraft, ms, buffer, cameraPos);
            ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, cameraPos);
            SymmetryHandlerClient.onRenderWorld(minecraft, ms, buffer, cameraPos);
            buffer.draw();
            minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
            ContraptionPlayerPassengerRotation.frame(minecraft);
        });
    }

    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/BlockDestructionProgress;updateTick(I)V"))
    private void onDestroyBlockProgress(int entityId, BlockPos pos, int progress, CallbackInfo ci, @Local BlockDestructionProgress progressObj) {
        BlockState state = level.getBlockState(pos);
        MultiPosDestructionHandler handler = AllExtensions.MULTI_POS.get(state.getBlock());
        if (handler != null) {
            Set<BlockPos> extraPositions = handler.getExtraPositions(level, pos, state, progress);
            if (extraPositions != null) {
                extraPositions.remove(pos);
                ((BlockDestructionProgressExtension) progressObj).create$setExtraPositions(extraPositions);
                for (BlockPos extraPos : extraPositions) {
                    destructionProgress.computeIfAbsent(extraPos.asLong(), l -> Sets.newTreeSet()).add(progressObj);
                }
            }
        }
    }

    @Inject(method = "removeProgress(Lnet/minecraft/server/level/BlockDestructionProgress;)V", at = @At("RETURN"))
    private void onRemoveProgress(BlockDestructionProgress progress, CallbackInfo ci) {
        Set<BlockPos> extraPositions = ((BlockDestructionProgressExtension) progress).create$getExtraPositions();
        if (extraPositions != null) {
            for (BlockPos extraPos : extraPositions) {
                long l = extraPos.asLong();
                Set<BlockDestructionProgress> set = destructionProgress.get(l);
                if (set != null) {
                    set.remove(progress);
                    if (set.isEmpty()) {
                        destructionProgress.remove(l);
                    }
                }
            }
        }
    }

    @Inject(method = "renderBlockOutline(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/minecraft/client/renderer/state/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/BlockOutlineRenderState;highContrast()Z", ordinal = 0), cancellable = true)
    private void onRenderBlockOutline(
        MultiBufferSource.BufferSource vertexConsumers,
        PoseStack matrices,
        boolean renderBlockOutline,
        LevelRenderState renderStates,
        CallbackInfo ci,
        @Local Vec3 cameraPos,
        @Local BlockOutlineRenderState state
    ) {
        if (ChainConveyorInteractionHandler.hideVanillaBlockSelection() || ClipboardValueSettingsClientHandler.drawCustomBlockSelection(
            minecraft,
            state.pos(),
            vertexConsumers,
            cameraPos,
            matrices
        ) || TrackBlockOutline.drawCustomBlockSelection(minecraft, state.pos(), vertexConsumers, cameraPos, matrices)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int getLuminance(
        BlockState state,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockAndTintGetter world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(world, pos);
        }
        return original.call(state);
    }

    @Inject(method = "submitBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeStorage;)V", at = @At("HEAD"))
    private void markSpriteActive(CallbackInfo ci) {
        SodiumCompat.markSpriteActive(minecraft);
    }
}
