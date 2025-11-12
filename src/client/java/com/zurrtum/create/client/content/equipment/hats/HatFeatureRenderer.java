package com.zurrtum.create.client.content.equipment.hats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfo;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HatFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public HatFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack ms, SubmitNodeCollector queue, int light, S renderState, float limbAngle, float limbDistance) {
        HatState hatState = (HatState) renderState;
        PartialModel hat = hatState.create$getHat();
        if (hat == null)
            return;

        M entityModel = getParentModel();
        ms.pushPose();

        var msr = TransformStack.of(ms);
        TrainHatInfo info = hatState.create$getHatInfo();
        List<ModelPart> partsToHead;
        if (entityModel instanceof HeadedModel model) {
            partsToHead = TrainHatInfo.getAdjustedPart(info, model.getHead(), "");
            partsToHead.addFirst(entityModel.root());
        } else if (info != TrainHatInfoReloadListener.DEFAULT) {
            partsToHead = TrainHatInfo.getAdjustedPart(info, entityModel.root(), "head");
        } else {
            ms.popPose();
            return;
        }

        if (!partsToHead.isEmpty()) {
            partsToHead.forEach(part -> part.translateAndRotate(ms));

            ModelPart lastChild = partsToHead.get(partsToHead.size() - 1);
            if (!lastChild.isEmpty()) {
                ModelPart.Cube cube = lastChild.cubes.get(Mth.clamp(info.cubeIndex(), 0, lastChild.cubes.size() - 1));
                ms.translate(info.offset().x() / 16.0F, (cube.minY - cube.maxY + info.offset().y()) / 16.0F, info.offset().z() / 16.0F);
                float max = Math.max(cube.maxX - cube.minX, cube.maxZ - cube.minZ) / 8.0F * info.scale();
                ms.scale(max, max, max);
            }

            ms.scale(1, -1, -1);
            ms.translate(0, -2.25F / 16.0F, 0);
            msr.rotateXDegrees(-8.5F);
            BlockState air = Blocks.AIR.defaultBlockState();
            HatRenderState state = new HatRenderState(CachedBuffers.partial(hat, air), light);
            queue.submitCustomGeometry(ms, Sheets.cutoutBlockSheet(), state);
        }

        ms.popPose();
    }

    public record HatRenderState(SuperByteBuffer hat, int light) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            hat.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
