package com.zurrtum.create.client.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
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

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private ClientWorld world;

    @Shadow
    @Final
    private DefaultFramebufferSet framebufferSet;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     * This gets called when a block is marked for rerender by vanilla.
     */
    @Inject(method = "scheduleBlockRerenderIfNeeded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V", at = @At("TAIL"))
    private void flywheel$checkUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(world);
        if (manager == null) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
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

    @Inject(method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudRenderModeValue()Lnet/minecraft/client/option/CloudRenderMode;"))
    private void renderAfterParticles(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
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
        FramePass framePass = frameGraphBuilder.createPass("after_particles");
        this.framebufferSet.mainFramebuffer = framePass.transfer(this.framebufferSet.mainFramebuffer);
        framePass.setRenderer(() -> {
            MatrixStack ms = new MatrixStack();
            Vec3d cameraPos = camera.getPos();
            SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
            GhostBlocks.getInstance().renderAll(client, ms, buffer, cameraPos);
            Outliner.getInstance().renderOutlines(client, ms, buffer, cameraPos, tickProgress);
            TrackBlockOutline.drawCurveSelection(client, ms, buffer, cameraPos);
            TrackTargetingClient.render(client, ms, buffer, cameraPos);
            CouplingRenderer.renderAll(client, ms, buffer, cameraPos);
            CarriageCouplingRenderer.renderAll(client, ms, buffer, cameraPos);
            Create.SCHEMATIC_HANDLER.render(client, ms, buffer, cameraPos);
            ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, cameraPos);
            SymmetryHandlerClient.onRenderWorld(client, ms, buffer, cameraPos);
            buffer.draw();
            client.gameRenderer.getEntityRenderDispatcher().render();
            ContraptionPlayerPassengerRotation.frame(client);
        });
    }

    @Inject(method = "setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/BlockBreakingInfo;setLastUpdateTick(I)V"))
    private void onDestroyBlockProgress(int entityId, BlockPos pos, int progress, CallbackInfo ci, @Local BlockBreakingInfo progressObj) {
        BlockState state = world.getBlockState(pos);
        MultiPosDestructionHandler handler = AllExtensions.MULTI_POS.get(state.getBlock());
        if (handler != null) {
            Set<BlockPos> extraPositions = handler.getExtraPositions(world, pos, state, progress);
            if (extraPositions != null) {
                extraPositions.remove(pos);
                ((BlockDestructionProgressExtension) progressObj).create$setExtraPositions(extraPositions);
                for (BlockPos extraPos : extraPositions) {
                    blockBreakingProgressions.computeIfAbsent(extraPos.asLong(), l -> Sets.newTreeSet()).add(progressObj);
                }
            }
        }
    }

    @Inject(method = "removeBlockBreakingInfo(Lnet/minecraft/entity/player/BlockBreakingInfo;)V", at = @At("RETURN"))
    private void onRemoveProgress(BlockBreakingInfo progress, CallbackInfo ci) {
        Set<BlockPos> extraPositions = ((BlockDestructionProgressExtension) progress).create$getExtraPositions();
        if (extraPositions != null) {
            for (BlockPos extraPos : extraPositions) {
                long l = extraPos.asLong();
                Set<BlockBreakingInfo> set = blockBreakingProgressions.get(l);
                if (set != null) {
                    set.remove(progress);
                    if (set.isEmpty()) {
                        blockBreakingProgressions.remove(l);
                    }
                }
            }
        }
    }

    @Inject(method = "renderTargetBlockOutline(Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/util/math/MatrixStack;ZLnet/minecraft/client/render/state/WorldRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/state/OutlineRenderState;highContrast()Z", ordinal = 0), cancellable = true)
    private void onRenderBlockOutline(
        VertexConsumerProvider.Immediate vertexConsumers,
        MatrixStack matrices,
        boolean renderBlockOutline,
        WorldRenderState renderStates,
        CallbackInfo ci,
        @Local Vec3d cameraPos,
        @Local OutlineRenderState state
    ) {
        if (ChainConveyorInteractionHandler.hideVanillaBlockSelection() || ClipboardValueSettingsClientHandler.drawCustomBlockSelection(
            client,
            state.pos(),
            vertexConsumers,
            cameraPos,
            matrices
        ) || TrackBlockOutline.drawCustomBlockSelection(client, state.pos(), vertexConsumers, cameraPos, matrices)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "getLightmapCoordinates(Lnet/minecraft/client/render/WorldRenderer$BrightnessGetter;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getLuminance()I"))
    private static int getLuminance(
        BlockState state,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockRenderView world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(world, pos);
        }
        return original.call(state);
    }

    @Inject(method = "renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl;)V", at = @At("HEAD"))
    private void markSpriteActive(CallbackInfo ci) {
        SodiumCompat.markSpriteActive(client);
    }
}
