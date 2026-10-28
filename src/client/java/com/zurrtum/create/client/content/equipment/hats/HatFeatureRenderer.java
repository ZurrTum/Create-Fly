package com.zurrtum.create.client.content.equipment.hats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfo;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
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
    public void submit(
        PoseStack ms,
        SubmitNodeCollector queue,
        int light,
        S renderState,
        float limbAngle,
        float limbDistance
    ) {
        HatState hatState = (HatState) renderState;
        PartialModel hat = hatState.create$getHat();
        if (hat == null) {
            return;
        }

        M entityModel = getParentModel();
        ms.pushPose();

        TrainHatInfo info = hatState.create$getHatInfo();
        ModelPart lastChild;
        if (entityModel instanceof HeadedModel model) {
            List<ModelPart> partsToHead = TrainHatInfo.getAdjustedPart(info, model.getHead(), "");
            entityModel.root().translateAndRotate(ms);
            model.translateToHead(ms);
            int size = partsToHead.size();
            for (int i = 1; i < size; i++) {
                partsToHead.get(i).translateAndRotate(ms);
            }
            lastChild = partsToHead.get(size - 1);
        } else if (info != TrainHatInfoReloadListener.DEFAULT) {
            List<ModelPart> partsToHead = TrainHatInfo.getAdjustedPart(info, entityModel.root(), "head");
            partsToHead.forEach(part -> part.translateAndRotate(ms));
            lastChild = partsToHead.getLast();
        } else {
            ms.popPose();
            return;
        }

        Pose pose = ms.last();
        if (!lastChild.isEmpty()) {
            ModelPart.Cube cube = lastChild.cubes.get(Mth.clamp(info.cubeIndex(), 0, lastChild.cubes.size() - 1));
            pose.translate(
                (float) (info.offset().x() / 16.0F),
                (float) ((cube.minY - cube.maxY + info.offset().y()) / 16.0F),
                (float) (info.offset().z() / 16.0F)
            );
            float max = Math.max(cube.maxX - cube.minX, cube.maxZ - cube.minZ) / 8.0F * info.scale();
            pose.scale(max, max, max);
        }

        pose.scale(1, -1, -1);
        pose.translate(0, -2.25F / 16.0F, 0);
        pose.rotate(Axis.XP, Radians.DEG_351P5);
        BlockState air = Blocks.AIR.defaultBlockState();
        CachedBuffers.partial(hat, air).light(light).submit(ms, queue);
        ms.popPose();
    }
}
