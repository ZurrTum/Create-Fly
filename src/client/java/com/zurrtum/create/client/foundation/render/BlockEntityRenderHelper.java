package com.zurrtum.create.client.foundation.render;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
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

import java.util.HashSet;
import java.util.Set;

public class BlockEntityRenderHelper {

    public static void renderBlockEntities(World world, Iterable<BlockEntity> customRenderBEs, MatrixStack ms, VertexConsumerProvider buffer) {
        renderBlockEntities(world, null, customRenderBEs, ms, null, buffer);
    }

    public static void renderBlockEntities(
        World world,
        Iterable<BlockEntity> customRenderBEs,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        float pt
    ) {
        renderBlockEntities(world, null, customRenderBEs, ms, null, buffer, pt);
    }

    public static void renderBlockEntities(
        World world,
        @Nullable VirtualRenderWorld renderWorld,
        Iterable<BlockEntity> customRenderBEs,
        MatrixStack ms,
        @Nullable Matrix4f lightTransform,
        VertexConsumerProvider buffer
    ) {
        renderBlockEntities(world, renderWorld, customRenderBEs, ms, lightTransform, buffer, AnimationTickHolder.getPartialTicks());
    }

    public static void renderBlockEntities(
        World realLevel,
        @Nullable VirtualRenderWorld renderLevel,
        Iterable<BlockEntity> customRenderBEs,
        MatrixStack ms,
        @Nullable Matrix4f lightTransform,
        VertexConsumerProvider buffer,
        float pt
    ) {
        // First, make sure all BEs have the render level.
        // Need to do this outside of the main loop in case BEs query the level from other virtual BEs.
        // e.g. double chests specifically fetch light from both their own and their neighbor's level,
        // which is honestly kind of silly, but easy to work around here.
        if (renderLevel != null) {
            for (var be : customRenderBEs) {
                be.setWorld(renderLevel);
            }
        }

        Set<BlockEntity> toRemove = new HashSet<>();

        // Main loop, time to render.
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockEntityRenderDispatcher dispatcher = mc.getBlockEntityRenderDispatcher();
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        for (BlockEntity blockEntity : customRenderBEs) {
            if (VisualizationManager.supportsVisualization(realLevel) && VisualizationHelper.skipVanillaRender(blockEntity))
                continue;

            BlockEntityRenderer<BlockEntity> renderer = dispatcher.get(blockEntity);
            if (renderer == null) {
                // Don't bother looping over it again if we can't do anything with it.
                toRemove.add(blockEntity);
                continue;
            }

            if (renderLevel == null && !renderer.isInRenderDistance(blockEntity, realLevel instanceof SchematicLevel ? Vec3d.ZERO : camera))
                continue;

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
                toRemove.add(blockEntity);

                String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType()) + " could not be rendered virtually.";
                if (AllConfigs.client().explainRenderErrors.get())
                    Create.LOGGER.error(message, e);
                else
                    Create.LOGGER.error(message);
            }

            ms.pop();
        }

        // Now reset all the BEs' levels.
        if (renderLevel != null) {
            renderLevel.resetExternalLight();

            for (var be : customRenderBEs) {
                be.setWorld(realLevel);
            }
        }

        // And finally, cull any BEs that misbehaved.
        if (!toRemove.isEmpty()) {
            var it = customRenderBEs.iterator();
            while (it.hasNext()) {
                if (toRemove.contains(it.next())) {
                    it.remove();
                }
            }
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
