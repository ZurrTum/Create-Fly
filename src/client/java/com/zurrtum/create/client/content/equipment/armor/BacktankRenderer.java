package com.zurrtum.create.client.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BacktankRenderer extends KineticBlockEntityRenderer<BacktankBlockEntity, BacktankRenderer.BacktankRenderState> {
    public BacktankRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BacktankRenderState createRenderState() {
        return new BacktankRenderState();
    }

    @Override
    public void extractRenderState(
        BacktankBlockEntity be,
        BacktankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
            state.layer = RenderType.solid();
        }
        state.cogs = CachedBuffers.partial(getCogsModel(state.blockState), state.blockState);
        state.yRot = Mth.DEG_TO_RAD * 180 + AngleHelper.horizontalAngle(state.blockState.getValue(BacktankBlock.HORIZONTAL_FACING));
        state.rotate = AngleHelper.rad(be.getSpeed() / 4f * AnimationTickHolder.getRenderTime(be.getLevel()) % 360);
    }

    @Override
    public void submit(BacktankRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    @Override
    protected RenderType getRenderType(BacktankBlockEntity be, BlockState state) {
        return RenderType.solid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BacktankBlockEntity be, BacktankRenderState state) {
        return CachedBuffers.partial(getShaftModel(state.blockState), state.blockState);
    }

    public static PartialModel getCogsModel(BlockState state) {
        if (state.is(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_COGS;
        }
        return AllPartialModels.COPPER_BACKTANK_COGS;
    }

    public static PartialModel getShaftModel(BlockState state) {
        if (state.is(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_SHAFT;
        }
        return AllPartialModels.COPPER_BACKTANK_SHAFT;
    }

    public static class BacktankRenderState extends KineticRenderState {
        public SuperByteBuffer cogs;
        public float yRot;
        public float rotate;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            cogs.center().rotateY(yRot).uncenter();
            cogs.translate(0, 0.40625f, 0.6875f).rotate(rotate, Direction.EAST).translate(0, -0.40625f, -0.6875f);
            cogs.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
