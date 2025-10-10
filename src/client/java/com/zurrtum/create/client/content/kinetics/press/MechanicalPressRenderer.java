package com.zurrtum.create.client.content.kinetics.press;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MechanicalPressRenderer extends KineticBlockEntityRenderer<MechanicalPressBlockEntity, MechanicalPressRenderer.MechanicalPressRenderState> {
    public MechanicalPressRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public MechanicalPressRenderState createRenderState() {
        return new MechanicalPressRenderState();
    }

    @Override
    public void updateRenderState(
        MechanicalPressBlockEntity be,
        MechanicalPressRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getCachedState();
        PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
        state.head = CachedBuffers.partialFacing(AllPartialModels.MECHANICAL_PRESS_HEAD, blockState, blockState.get(Properties.HORIZONTAL_FACING));
        state.offset = -(pressingBehaviour.getRenderedHeadOffset(tickProgress) * pressingBehaviour.mode.headOffset);
    }

    @Override
    protected RenderLayer getRenderType(MechanicalPressBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
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
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            head.translate(0, offset, 0);
            head.light(lightmapCoordinates);
            head.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
