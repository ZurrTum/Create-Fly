package com.zurrtum.create.client.content.kinetics.press;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MechanicalPressRenderer extends KineticBlockEntityRenderer<MechanicalPressBlockEntity, MechanicalPressRenderer.MechanicalPressRenderState> {
    public MechanicalPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MechanicalPressRenderState createRenderState() {
        return new MechanicalPressRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalPressBlockEntity be,
        MechanicalPressRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
        state.head = CachedBuffers.partialFacing(
            AllPartialModels.MECHANICAL_PRESS_HEAD,
            blockState,
            blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
        );
        state.offset = -(pressingBehaviour.getRenderedHeadOffset(tickProgress) * pressingBehaviour.mode.headOffset);
    }

    @Override
    protected RenderType getRenderType(MechanicalPressBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    protected BlockState getRenderedBlockState(MechanicalPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class MechanicalPressRenderState extends KineticRenderState {
        SuperByteBuffer head;
        public float offset;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            head.translate(0, offset, 0);
            head.light(lightCoords);
            head.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
