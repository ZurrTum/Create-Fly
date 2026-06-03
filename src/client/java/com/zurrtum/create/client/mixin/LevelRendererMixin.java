package com.zurrtum.create.client.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.ghostblock.GhostBlocks;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.DefaultSuperRenderTypeBuffer.Dispatcher;
import com.zurrtum.create.client.catnip.render.EntityBlockLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockLightLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockMultipleLayer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
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
import com.zurrtum.create.client.infrastructure.render.BreakingRenderStateInfo;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.SortedSet;

import static com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel.getBlockDestroyModel;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void checkPoseStack(PoseStack poseStack);

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

    @Inject(method = "lambda$addMainPass$0(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;ZLorg/joml/Matrix4fc;)V", at = @At("TAIL"))
    private void renderAfterParticles(
        GpuBufferSlice terrainFog,
        LevelRenderState levelRenderState,
        ProfilerFiller profiler,
        ChunkSectionsToRender chunkSectionsToRender,
        ResourceHandle<RenderTarget> entityOutlineTarget,
        ResourceHandle<RenderTarget> translucentTarget,
        ResourceHandle<RenderTarget> mainTarget,
        ResourceHandle<RenderTarget> itemEntityTarget,
        ResourceHandle<RenderTarget> particleTarget,
        boolean renderOutline,
        Matrix4fc modelViewMatrix,
        CallbackInfo ci,
        @Local Vec3 cameraPos,
        @Local PoseStack ms
    ) {
        Dispatcher dispatcher = Dispatcher.getInstance();
        SuperRenderTypeBuffer buffer = dispatcher.getBuffer();
        SubmitNodeStorage queue = dispatcher.getSubmitNodeStorage();
        GhostBlocks.getInstance().renderAll(minecraft, ms, buffer, cameraPos);
        Outliner.getInstance().renderOutlines(minecraft, ms, buffer, cameraPos, AnimationTickHolder.getPartialTicks());
        TrackBlockOutline.drawCurveSelection(minecraft, ms, buffer, cameraPos);
        TrackTargetingClient.render(minecraft, ms, queue, cameraPos);
        CouplingRenderer.renderAll(minecraft, ms, queue, cameraPos);
        CarriageCouplingRenderer.renderAll(minecraft, ms, queue, cameraPos);
        Create.SCHEMATIC_HANDLER.render(minecraft, ms, buffer, queue, cameraPos);
        ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, cameraPos);
        SymmetryHandlerClient.onRenderWorld(minecraft, ms, buffer, cameraPos);
        dispatcher.draw(ms);
        checkPoseStack(ms);
        ContraptionPlayerPassengerRotation.frame(minecraft);
    }

    @Inject(method = "allChanged()V", at = @At("RETURN"))
    private void flywheel$reload(CallbackInfo ci) {
        if (level != null) {
            EntityBlockLightLayer.clear();
            EntityBlockLayer.clear();
            EntityBlockMultipleLayer.clear();
        }
    }

    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/BlockDestructionProgress;updateTick(I)V"))
    private void onDestroyBlockProgress(
        int entityId,
        BlockPos pos,
        int progress,
        CallbackInfo ci,
        @Local BlockDestructionProgress progressObj
    ) {
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

    @Inject(method = "renderBlockOutline(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;highContrast()Z", ordinal = 0), cancellable = true)
    private void onRenderBlockOutline(
        BufferSource vertexConsumers,
        PoseStack matrices,
        boolean renderBlockOutline,
        LevelRenderState renderStates,
        CallbackInfo ci,
        @Local Vec3 cameraPos,
        @Local BlockOutlineRenderState state
    ) {
        if (ChainConveyorInteractionHandler.hideVanillaBlockSelection() || ClipboardValueSettingsClientHandler.drawCustomBlockSelection(minecraft,
            state.pos(),
            vertexConsumers,
            cameraPos,
            matrices
        ) || TrackBlockOutline.drawCustomBlockSelection(
            minecraft,
            state.pos(),
            vertexConsumers,
            cameraPos,
            matrices
        )) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "getLightCoords(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int getLuminance(
        BlockState state,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockAndLightGetter world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof CopycatBlock block) {
            return block.getLuminance(world, pos);
        }
        return original.call(state);
    }

    @Inject(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At("HEAD"))
    private void init(
        Camera camera,
        LevelRenderState levelRenderState,
        CallbackInfo ci,
        @Share("models") LocalRef<BlockStateModelSet> ref
    ) {
        ref.set(minecraft.getModelManager().getBlockStateModelSet());
    }

    @ModifyArg(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> E addInfo(E e, @Share("models") LocalRef<BlockStateModelSet> ref) {
        BlockBreakingRenderState state = (BlockBreakingRenderState) e;
        BlockState blockState = state.blockState();
        ((BreakingRenderStateInfo) e).create$setRenderModel(getBlockDestroyModel(
            ref.get().get(blockState),
            level,
            state.blockPos(),
            blockState
        ));
        return e;
    }

    @ModifyArg(method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitBreakingBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;JI)V"), order = 1)
    private BlockStateModel modifyModel(BlockStateModel model, @Local BlockBreakingRenderState renderState) {
        BlockStateModel renderModel = ((BreakingRenderStateInfo) (Object) renderState).create$getRenderModel();
        return renderModel != null ? renderModel : model;
    }
}
