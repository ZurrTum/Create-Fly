package com.zurrtum.create.client.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BacktankFeatureRenderer<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> extends RenderLayer<S, M> {
    public BacktankFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack ms, SubmitNodeCollector queue, int light, S entityState, float limbAngle, float limbDistance) {
        if (entityState.pose == Pose.SLEEPING || !(entityState.chestEquipment.getItem() instanceof BacktankItem item)) {
            return;
        }
        BacktankRenderState state = new BacktankRenderState();
        BlockState blockState = item.getBlock().defaultBlockState().setValue(BacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
        state.backtank = CachedBuffers.block(blockState);
        state.cogs = CachedBuffers.partial(BacktankRenderer.getCogsModel(blockState), blockState);
        state.nob = CachedBuffers.partial(BacktankRenderer.getShaftModel(blockState), blockState);
        state.light = light;
        state.yRot = Mth.DEG_TO_RAD * 180;
        state.angle = AngleHelper.rad(2 * AnimationTickHolder.getRenderTime() % 360);

        ms.pushPose();
        getParentModel().body.translateAndRotate(ms);
        ms.translate(-1 / 2f, 10 / 16f, 1f);
        ms.scale(1, -1, -1);
        List<RenderType> list = ItemRenderer.getFoilRenderTypes(Sheets.cutoutBlockSheet(), false, entityState.chestEquipment.hasFoil());
        for (int i = 0; i < list.size(); i++) {
            queue.order(i).submitCustomGeometry(ms, list.get(i), state);
        }
        ms.popPose();
    }

    public static class BacktankRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public SuperByteBuffer backtank;
        public SuperByteBuffer cogs;
        public SuperByteBuffer nob;
        public int light;
        public float yRot;
        public float angle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            backtank.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
            nob.disableDiffuse().translate(0, -0.1875f, 0).light(light).renderInto(matricesEntry, vertexConsumer);
            cogs.center().rotateY(yRot).uncenter().translate(0, 0.40625f, 0.6875f).rotate(angle, Direction.EAST).translate(0, -0.40625f, -0.6875f);
            cogs.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
