package com.zurrtum.create.client.content.equipment.hats;

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfo;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class HatFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends FeatureRenderer<S, M> {
    public HatFeatureRenderer(FeatureRendererContext<S, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack ms, OrderedRenderCommandQueue queue, int light, S renderState, float limbAngle, float limbDistance) {
        HatState hatState = (HatState) renderState;
        PartialModel hat = hatState.create$getHat();
        if (hat == null)
            return;

        M entityModel = getContextModel();
        ms.push();

        var msr = TransformStack.of(ms);
        TrainHatInfo info = hatState.create$getHatInfo();
        ModelPart lastChild;
        if (entityModel instanceof ModelWithHead model) {
            List<ModelPart> partsToHead = TrainHatInfo.getAdjustedPart(info, model.getHead(), "");
            entityModel.getRootPart().applyTransform(ms);
            model.applyTransform(ms);
            int size = partsToHead.size();
            for (int i = 1; i < size; i++) {
                partsToHead.get(i).applyTransform(ms);
            }
            lastChild = partsToHead.get(size - 1);
        } else if (info != TrainHatInfoReloadListener.DEFAULT) {
            List<ModelPart> partsToHead = TrainHatInfo.getAdjustedPart(info, entityModel.getRootPart(), "head");
            partsToHead.forEach(part -> part.applyTransform(ms));
            lastChild = partsToHead.getLast();
        } else {
            ms.pop();
            return;
        }


        if (!lastChild.isEmpty()) {
            ModelPart.Cuboid cube = lastChild.cuboids.get(MathHelper.clamp(info.cubeIndex(), 0, lastChild.cuboids.size() - 1));
            ms.translate(info.offset().getX() / 16.0F, (cube.minY - cube.maxY + info.offset().getY()) / 16.0F, info.offset().getZ() / 16.0F);
            float max = Math.max(cube.maxX - cube.minX, cube.maxZ - cube.minZ) / 8.0F * info.scale();
            ms.scale(max, max, max);
        }

        ms.scale(1, -1, -1);
        ms.translate(0, -2.25F / 16.0F, 0);
        msr.rotateXDegrees(-8.5F);
        BlockState air = Blocks.AIR.getDefaultState();
        HatRenderState state = new HatRenderState(CachedBuffers.partial(hat, air), light);
        queue.submitCustom(ms, TexturedRenderLayers.getEntityCutout(), state);

        ms.pop();
    }

    public record HatRenderState(SuperByteBuffer hat, int light) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            hat.disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
