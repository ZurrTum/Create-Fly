package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;

public class BacktankFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<? super S>> extends FeatureRenderer<S, M> {
    public BacktankFeatureRenderer(FeatureRendererContext<S, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack ms, VertexConsumerProvider buffer, int light, S state, float limbAngle, float limbDistance) {
        if (state.pose == EntityPose.SLEEPING || !(state.equippedChestStack.getItem() instanceof BacktankItem item)) {
            return;
        }

        boolean hasGlint = state.equippedChestStack.hasGlint();
        VertexConsumer vc = ItemRenderer.getItemGlintConsumer(buffer, TexturedRenderLayers.getEntityCutout(), false, hasGlint);
        BlockState renderedState = item.getBlock().getDefaultState().with(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
        SuperByteBuffer backtank = CachedBuffers.block(renderedState);
        SuperByteBuffer cogs = CachedBuffers.partial(BacktankRenderer.getCogsModel(renderedState), renderedState);
        SuperByteBuffer nob = CachedBuffers.partial(BacktankRenderer.getShaftModel(renderedState), renderedState);

        ms.push();

        getContextModel().body.applyTransform(ms);
        ms.translate(-1 / 2f, 10 / 16f, 1f);
        ms.scale(1, -1, -1);

        backtank.disableDiffuse().light(light).renderInto(ms, vc);

        nob.disableDiffuse().translate(0, -3f / 16, 0).light(light).renderInto(ms, vc);

        cogs.center().rotateYDegrees(180).uncenter().translate(0, 6.5f / 16, 11f / 16)
            .rotate(AngleHelper.rad(2 * AnimationTickHolder.getRenderTime() % 360), Direction.EAST).translate(0, -6.5f / 16, -11f / 16);

        cogs.disableDiffuse().light(light).renderInto(ms, vc);

        ms.pop();
    }
}
