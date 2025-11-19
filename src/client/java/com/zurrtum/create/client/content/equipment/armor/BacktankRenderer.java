package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BacktankRenderer extends KineticBlockEntityRenderer<BacktankBlockEntity, BacktankRenderer.BacktankRenderState> {
    public BacktankRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BacktankRenderState createRenderState() {
        return new BacktankRenderState();
    }

    @Override
    public void updateRenderState(
        BacktankBlockEntity be,
        BacktankRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
            state.layer = RenderLayer.getSolid();
        }
        state.cogs = CachedBuffers.partial(getCogsModel(state.blockState), state.blockState);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * (180 + AngleHelper.horizontalAngle(state.blockState.get(BacktankBlock.HORIZONTAL_FACING)));
        state.rotate = AngleHelper.rad(be.getSpeed() / 4f * AnimationTickHolder.getRenderTime(be.getWorld()) % 360);
    }

    @Override
    public void render(BacktankRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    @Override
    protected RenderLayer getRenderType(BacktankBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BacktankBlockEntity be, BacktankRenderState state) {
        return CachedBuffers.partial(getShaftModel(state.blockState), state.blockState);
    }

    public static PartialModel getCogsModel(BlockState state) {
        if (state.isOf(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_COGS;
        }
        return AllPartialModels.COPPER_BACKTANK_COGS;
    }

    public static PartialModel getShaftModel(BlockState state) {
        if (state.isOf(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_SHAFT;
        }
        return AllPartialModels.COPPER_BACKTANK_SHAFT;
    }

    public static class BacktankRenderState extends KineticRenderState {
        public SuperByteBuffer cogs;
        public float yRot;
        public float rotate;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            cogs.center().rotateY(yRot).uncenter();
            cogs.translate(0, 0.40625f, 0.6875f).rotate(rotate, Direction.EAST).translate(0, -0.40625f, -0.6875f);
            cogs.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
