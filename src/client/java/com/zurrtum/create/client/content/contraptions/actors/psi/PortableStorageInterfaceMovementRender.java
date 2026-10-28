package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class PortableStorageInterfaceMovementRender implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new PSIActorVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    @Nullable
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        PoseStack.Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        BlockPos pos = context.localPos;
        PortableStorageInterfaceMovementRenderState state = new PortableStorageInterfaceMovementRenderState();
        BlockState blockState = context.state;
        float renderPartialTicks = AnimationTickHolder.getPartialTicks();
        LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
        Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);
        float yRadians = Radians.horizontal(facing);
        float xRadians = facing == Direction.UP ? 0 : facing == Direction.DOWN ? Radians.DEG_180 : Radians.DEG_90;
        float topOffset = animation.getValue(renderPartialTicks);
        float middleOffset = topOffset * 0.5f + 0.375f;
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        SuperByteBuffer middle = CachedBuffers.partial(
            PortableStorageInterfaceRenderer.getMiddleForState(
                blockState,
                animation.settled()
            ), blockState
        ).transform(transform).translate(pos).center().rotateY(yRadians).rotateX(xRadians).uncenter();
        SuperByteBuffer top = CachedBuffers.partial(
            PortableStorageInterfaceRenderer.getTopForState(blockState),
            blockState
        );
        SuperByteBuffer.copyTransform(middle, top);
        state.middle = middle.translate(0, middleOffset, 0).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        state.top = top.translate(0, topOffset, 0).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        return state;
    }

    public static class PortableStorageInterfaceMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState middle;
        public @UnknownNullability SuperByteBufferRenderState top;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            middle.submit(matrices, queue);
            top.submit(matrices, queue);
        }
    }
}
