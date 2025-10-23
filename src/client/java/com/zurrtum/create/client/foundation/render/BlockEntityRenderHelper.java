package com.zurrtum.create.client.foundation.render;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

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
    public static void renderBlockEntities(
        List<BlockEntity> blockEntities,
        BitSet shouldRenderBEs,
        BitSet erroredBEsOut,
        @Nullable VirtualRenderWorld renderLevel,
        World realLevel,
        MatrixStack ms,
        @Nullable Matrix4f lightTransform,
        VertexConsumerProvider buffer,
        float pt
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockEntityRenderDispatcher dispatcher = mc.getBlockEntityRenderDispatcher();
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < blockEntities.size(); i = shouldRenderBEs.nextSetBit(i + 1)) {
            BlockEntity blockEntity = blockEntities.get(i);
            if (VisualizationManager.supportsVisualization(realLevel) && VisualizationHelper.skipVanillaRender(blockEntity))
                continue;

            BlockEntityRenderer<BlockEntity> renderer = dispatcher.get(blockEntity);
            if (renderer == null) {
                // Don't bother looping over it again if we can't do anything with it.
                erroredBEsOut.set(i);
                continue;
            }

            BlockPos pos = blockEntity.getPos();
            ms.push();
            TransformStack.of(ms).translate(pos);

            try {
                int realLevelLight = WorldRenderer.getLightmapCoordinates(realLevel, getLightPos(lightTransform, pos));

                int light;
                if (renderLevel != null) {
                    renderLevel.setExternalLight(realLevelLight);
                    light = WorldRenderer.getLightmapCoordinates(renderLevel, pos);
                } else {
                    light = realLevelLight;
                }

                renderer.render(blockEntity, pt, ms, buffer, light, OverlayTexture.DEFAULT_UV, camera);

            } catch (Exception e) {
                // Prevent this BE from causing more issues in the future.
                erroredBEsOut.set(i);

                String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType()) + " could not be rendered virtually.";
                if (AllConfigs.client().explainRenderErrors.get())
                    Create.LOGGER.error(message, e);
                else
                    Create.LOGGER.error(message);
            }

            ms.pop();
        }

        if (renderLevel != null) {
            renderLevel.resetExternalLight();
        }
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

}
