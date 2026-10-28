package com.zurrtum.create.client.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;

public class BacktankFeatureRenderer<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> extends RenderLayer<S, M> {
    public BacktankFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Override
    public void submit(
        PoseStack ms,
        SubmitNodeCollector queue,
        int light,
        S entityState,
        float limbAngle,
        float limbDistance
    ) {
        if (entityState.pose == Pose.SLEEPING || !(entityState.chestEquipment.getItem() instanceof BacktankItem item)) {
            return;
        }
        BlockState blockState = item.getBlock().defaultBlockState()
            .setValue(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
        SuperByteBufferRenderState backtank = CachedBuffers.block(blockState).light(light).extractRenderState();
        SuperByteBufferRenderState cogs = CachedBuffers.partial(BacktankRenderer.getCogsModel(blockState), blockState)
            .light(light).rotateYCentered(Radians.DEG_180).translate(0, 0.40625f, 0.6875f)
            .rotateX(Mth.DEG_TO_RAD * (2 * AnimationTickHolder.getRenderTime() % 360)).translate(0, -0.40625f, -0.6875f)
            .extractRenderState();
        SuperByteBufferRenderState nob = CachedBuffers.partial(BacktankRenderer.getShaftModel(blockState), blockState)
            .light(light).translate(0, -0.1875f, 0).extractRenderState();

        ms.pushPose();
        getParentModel().body.translateAndRotate(ms);
        PoseStack.Pose pose = ms.last();
        pose.translate(-0.5f, 0.625f, 1.0f);
        pose.scale(1, -1, -1);
        backtank.submit(ms, queue);
        cogs.submit(ms, queue);
        nob.submit(ms, queue);
        if (entityState.chestEquipment.hasFoil()) {
            RenderType glint = RenderTypes.entityGlint();
            backtank.submit(glint, ms, queue);
            cogs.submit(glint, ms, queue);
            nob.submit(glint, ms, queue);
        }
        ms.popPose();
    }
}
