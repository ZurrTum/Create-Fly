package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EjectorItemEntityRenderer extends ItemEntityRenderer {
    public EjectorItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new RenderState();
    }

    @Override
    protected float getShadowRadius(ItemEntityRenderState state) {
        if (((RenderState) state).alive) {
            return super.getShadowRadius(state);
        }
        return 0;
    }

    @Override
    public void extractRenderState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f) {
        super.extractRenderState(itemEntity, itemEntityRenderState, f);
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        RenderState state = (RenderState) itemEntityRenderState;
        state.alive = entity.isAlive();
        if (state.alive) {
            if (entity.data.initAge == -1) {
                itemEntityRenderState.ageInTicks = 0;
            } else {
                itemEntityRenderState.ageInTicks = (entity.age - entity.data.initAge + f) / 10.0F;
            }
        } else {
            state.rotateY = entity.data.rotateY;
            float time = entity.progress + f;
            state.rotateX = Mth.DEG_TO_RAD * time * 40;
            state.location = entity.getLaunchedItemLocation(time).subtract(entity.position());
        }
        itemEntityRenderState.bobOffset = entity.data.animateOffset;
    }

    @Override
    public AABB getBoundingBoxForCulling(ItemEntity itemEntity) {
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        if (entity.isAlive()) {
            return entity.getBoundingBox();
        } else {
            return entity.data.renderBox;
        }
    }

    @Override
    public void submit(
        ItemEntityRenderState itemEntityRenderState,
        PoseStack matrixStack,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState
    ) {
        if (!itemEntityRenderState.item.isEmpty()) {
            RenderState state = (RenderState) itemEntityRenderState;
            AABB box = state.item.getModelBoundingBox();
            matrixStack.pushPose();
            float f = -((float) box.minY) + 0.0625F;
            matrixStack.translate(0, state.bobOffset + f, -0.0625f);
            if (!state.alive) {
                matrixStack.translate(state.location);
                matrixStack.translate(0, 0.25f, 0);
                if (state.rotateY != 0) {
                    matrixStack.mulPose(Axis.YP.rotation(state.rotateY));
                }
                matrixStack.mulPose(Axis.XP.rotation(state.rotateX));
                matrixStack.translate(0, -0.25f, 0);
            } else if (state.ageInTicks > 0) {
                float g = Mth.sin(state.ageInTicks) * 0.1F + 0.1F;
                matrixStack.translate(0, g, 0);
                matrixStack.mulPose(Axis.YP.rotation(state.ageInTicks / 2F));
            }
            submitMultipleFromCount(matrixStack, queue, state.lightCoords, state, random, box);
            matrixStack.popPose();

            if (state.alive) {
                if (state.leashStates != null) {
                    for (EntityRenderState.LeashState leashData : state.leashStates) {
                        queue.submitLeash(matrixStack, leashData);
                    }
                }

                submitNameTag(state, matrixStack, queue, cameraRenderState);
            }
        }
    }

    public static class RenderState extends ItemEntityRenderState {
        public boolean alive;
        public float rotateY;
        public float rotateX;
        public Vec3 location;
    }
}
