package com.zurrtum.create.client.foundation.render;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

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
        World realLevel,
        @Nullable Matrix4f lightTransform,
        Vec3d camera,
        float pt
    ) {
        int size = blockEntities.size();
        if (size == 0) {
            return null;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockEntityRenderManager dispatcher = mc.getBlockEntityRenderDispatcher();
        List<BlockEntityRenderState> states = new ArrayList<>();
        for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < size; i = shouldRenderBEs.nextSetBit(i + 1)) {
            BlockEntity blockEntity = blockEntities.get(i);
            if (supportsVisualization && VisualizationHelper.skipVanillaRender(blockEntity)) {
                continue;
            }

            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.get(blockEntity);
            if (renderer == null) {
                // Don't bother looping over it again if we can't do anything with it.
                erroredBEsOut.set(i);
                continue;
            }

            try {
                BlockEntityRenderState renderState = renderer.createRenderState();
                int realLevelLight = WorldRenderer.getLightmapCoordinates(realLevel, getLightPos(lightTransform, blockEntity.getPos()));
                if (renderLevel != null) {
                    renderLevel.setExternalLight(realLevelLight);
                }
                renderer.updateRenderState(blockEntity, renderState, pt, camera, null);
                if (renderLevel == null) {
                    renderState.lightmapCoordinates = realLevelLight;
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
        return new BlockEntityListRenderState(dispatcher, camera, BlockPos.ofFloored(camera), states);
    }

    private static BlockPos getLightPos(@Nullable Matrix4f lightTransform, BlockPos contraptionPos) {
        if (lightTransform != null) {
            Vector4f lightVec = new Vector4f(contraptionPos.getX() + .5f, contraptionPos.getY() + .5f, contraptionPos.getZ() + .5f, 1);
            lightVec.mul(lightTransform);
            return BlockPos.ofFloored(lightVec.x(), lightVec.y(), lightVec.z());
        } else {
            return contraptionPos;
        }
    }

    public record BlockEntityListRenderState(
        BlockEntityRenderManager dispatcher, Vec3d camera, BlockPos cameraPos, List<BlockEntityRenderState> states
    ) {
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
            Vec3d prevPos = cameraRenderState.pos;
            BlockPos prevBlockPos = cameraRenderState.blockPos;
            Vec3d prevEntityPos = cameraRenderState.entityPos;
            cameraRenderState.pos = camera;
            cameraRenderState.blockPos = cameraPos;
            cameraRenderState.entityPos = new Vec3d(
                prevEntityPos.x - prevPos.x + camera.x,
                prevEntityPos.y - prevPos.y + camera.y,
                prevEntityPos.z - prevPos.z + camera.z
            );
            for (BlockEntityRenderState state : states) {
                BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getByRenderState(state);
                if (renderer == null) {
                    continue;
                }
                BlockPos pos = state.pos;
                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                renderer.render(state, matrices, queue, cameraRenderState);
                matrices.pop();
            }
            cameraRenderState.pos = prevPos;
            cameraRenderState.blockPos = prevBlockPos;
            cameraRenderState.entityPos = prevEntityPos;
        }
    }
}
