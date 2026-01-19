package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PortableStorageInterfaceRenderer implements BlockEntityRenderer<PortableStorageInterfaceBlockEntity, PortableStorageInterfaceRenderer.PortableStorageInterfaceRenderState> {
    public PortableStorageInterfaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public PortableStorageInterfaceRenderState createRenderState() {
        return new PortableStorageInterfaceRenderState();
    }

    @Override
    public void extractRenderState(
        PortableStorageInterfaceBlockEntity be,
        PortableStorageInterfaceRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderTypes.solidMovingBlock();
        state.middle = CachedBuffers.partial(getMiddleForState(state.blockState, be.isConnected()), state.blockState);
        state.top = CachedBuffers.partial(getTopForState(state.blockState), state.blockState);
        Direction facing = state.blockState.getValue(PortableStorageInterfaceBlock.FACING);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        state.topOffset = be.getExtensionDistance(tickProgress);
        state.middleOffset = state.topOffset * 0.5f + 0.375f;
    }

    @Override
    public void submit(PortableStorageInterfaceRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static PartialModel getMiddleForState(BlockState state, boolean lit) {
        if (state.is(AllBlocks.PORTABLE_FLUID_INTERFACE))
            return lit ? AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE;
        return lit ? AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE;
    }

    public static PartialModel getTopForState(BlockState state) {
        if (state.is(AllBlocks.PORTABLE_FLUID_INTERFACE))
            return AllPartialModels.PORTABLE_FLUID_INTERFACE_TOP;
        return AllPartialModels.PORTABLE_STORAGE_INTERFACE_TOP;
    }

    public static class PortableStorageInterfaceRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer middle;
        public SuperByteBuffer top;
        public float yRot;
        public float xRot;
        public float middleOffset;
        public float topOffset;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            middle.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, middleOffset, 0).light(lightCoords)
                .renderInto(matricesEntry, vertexConsumer);
            top.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, topOffset, 0).light(lightCoords)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
