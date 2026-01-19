package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BlockEntityRenderHelper {
    /**
     * Renders the given list of BlockEntities, skipping those not marked in shouldRenderBEs,
     * and marking those that error in erroredBEsOut.
     *
     * @param blockEntities   The list of BlockEntities to render.
     * @param shouldRenderBEs A BitSet marking which BlockEntities in the list should be rendered. This will not be modified.
     * @param erroredBEsOut   A BitSet to mark BlockEntities that error during rendering. This will be modified.
     */
    @Nullable
    public static BlockEntityListRenderState getBlockEntitiesRenderState(
        boolean supportsVisualization,
        List<BlockEntity> blockEntities,
        BitSet shouldRenderBEs,
        BitSet erroredBEsOut,
        @Nullable VirtualRenderWorld renderLevel,
        Level realLevel,
        @Nullable Matrix4f lightTransform,
        Vec3 camera,
        float pt
    ) {
        int size = blockEntities.size();
        if (size == 0) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        BlockEntityRenderDispatcher dispatcher = mc.getBlockEntityRenderDispatcher();
        List<BlockEntityRenderState> states = new ArrayList<>();
        for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < size; i = shouldRenderBEs.nextSetBit(i + 1)) {
            BlockEntity blockEntity = blockEntities.get(i);
            if (supportsVisualization && VisualizationHelper.skipVanillaRender(blockEntity)) {
                continue;
            }

            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getRenderer(blockEntity);
            if (renderer == null) {
                // Don't bother looping over it again if we can't do anything with it.
                erroredBEsOut.set(i);
                continue;
            }

            try {
                BlockEntityRenderState renderState = renderer.createRenderState();
                int realLevelLight = LevelRenderer.getLightCoords(realLevel, getLightPos(lightTransform, blockEntity.getBlockPos()));
                if (renderLevel != null) {
                    renderLevel.setExternalLight(realLevelLight);
                }
                renderer.extractRenderState(blockEntity, renderState, pt, camera, null);
                if (renderLevel == null) {
                    renderState.lightCoords = realLevelLight;
                }
                states.add(renderState);
            } catch (Exception e) {
                // Prevent this BE from causing more issues in the future.
                erroredBEsOut.set(i);

                String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType()) + " could not be rendered virtually.";
                if (AllConfigs.client().explainRenderErrors.get())
                    Create.LOGGER.error(message, e);
                else
                    Create.LOGGER.error(message);
            }
        }

        if (renderLevel != null) {
            renderLevel.resetExternalLight();
        }
        if (states.isEmpty()) {
            return null;
        }
        return new BlockEntityListRenderState(dispatcher, camera, BlockPos.containing(camera), states);
    }

    private static BlockPos getLightPos(@Nullable Matrix4f lightTransform, BlockPos contraptionPos) {
        if (lightTransform != null) {
            Vector4f lightVec = new Vector4f(contraptionPos.getX() + .5f, contraptionPos.getY() + .5f, contraptionPos.getZ() + .5f, 1);
            lightVec.mul(lightTransform);
            return BlockPos.containing(lightVec.x(), lightVec.y(), lightVec.z());
        } else {
            return contraptionPos;
        }
    }

    public record BlockEntityListRenderState(
        BlockEntityRenderDispatcher dispatcher, Vec3 camera, BlockPos cameraPos, List<BlockEntityRenderState> states
    ) {
        public void render(PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
            Vec3 prevPos = cameraRenderState.pos;
            BlockPos prevBlockPos = cameraRenderState.blockPos;
            Vec3 prevEntityPos = cameraRenderState.entityPos;
            cameraRenderState.pos = camera;
            cameraRenderState.blockPos = cameraPos;
            cameraRenderState.entityPos = new Vec3(
                prevEntityPos.x - prevPos.x + camera.x,
                prevEntityPos.y - prevPos.y + camera.y,
                prevEntityPos.z - prevPos.z + camera.z
            );
            for (BlockEntityRenderState state : states) {
                BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getRenderer(state);
                if (renderer == null) {
                    continue;
                }
                BlockPos pos = state.blockPos;
                matrices.pushPose();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                renderer.submit(state, matrices, queue, cameraRenderState);
                matrices.popPose();
            }
            cameraRenderState.pos = prevPos;
            cameraRenderState.blockPos = prevBlockPos;
            cameraRenderState.entityPos = prevEntityPos;
        }
    }
}
