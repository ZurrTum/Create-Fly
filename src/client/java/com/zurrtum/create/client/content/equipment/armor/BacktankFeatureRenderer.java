package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class BacktankFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<? super S>> extends FeatureRenderer<S, M> {
    public BacktankFeatureRenderer(FeatureRendererContext<S, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack ms, OrderedRenderCommandQueue queue, int light, S entityState, float limbAngle, float limbDistance) {
        if (entityState.pose == EntityPose.SLEEPING || !(entityState.equippedChestStack.getItem() instanceof BacktankItem item)) {
            return;
        }
        BacktankRenderState state = new BacktankRenderState();
        BlockState blockState = item.getBlock().getDefaultState().with(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
        state.backtank = CachedBuffers.block(blockState);
        state.cogs = CachedBuffers.partial(BacktankRenderer.getCogsModel(blockState), blockState);
        state.nob = CachedBuffers.partial(BacktankRenderer.getShaftModel(blockState), blockState);
        state.light = light;
        state.yRot = MathHelper.RADIANS_PER_DEGREE * 180;
        state.angle = AngleHelper.rad(2 * AnimationTickHolder.getRenderTime() % 360);

        ms.push();
        getContextModel().body.applyTransform(ms);
        ms.translate(-1 / 2f, 10 / 16f, 1f);
        ms.scale(1, -1, -1);
        List<RenderLayer> list = ItemRenderer.getGlintRenderLayers(
            TexturedRenderLayers.getEntityCutout(),
            false,
            entityState.equippedChestStack.hasGlint()
        );
        for (int i = 0; i < list.size(); i++) {
            queue.getBatchingQueue(i).submitCustom(ms, list.get(i), state);
        }
        ms.pop();
    }

    public static class BacktankRenderState implements OrderedRenderCommandQueue.Custom {
        public SuperByteBuffer backtank;
        public SuperByteBuffer cogs;
        public SuperByteBuffer nob;
        public int light;
        public float yRot;
        public float angle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            backtank.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
            nob.disableDiffuse().translate(0, -0.1875f, 0).light(light).renderInto(matricesEntry, vertexConsumer);
            cogs.center().rotateY(yRot).uncenter().translate(0, 0.40625f, 0.6875f).rotate(angle, Direction.EAST).translate(0, -0.40625f, -0.6875f);
            cogs.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
