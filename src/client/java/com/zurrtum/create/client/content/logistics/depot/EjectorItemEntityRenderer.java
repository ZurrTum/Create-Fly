package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

public class EjectorItemEntityRenderer extends ItemEntityRenderer {
    public EjectorItemEntityRenderer(Context context) {
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
            state.isPackage = PackageItem.isPackage(entity.getItem());
            float time = entity.progress + f;
            if (state.isPackage) {
                state.rotateY = time * 20;
            } else {
                state.rotateY = entity.data.rotateY;
                state.rotateX = time * 40;
            }
            state.location = entity.getLaunchedItemLocation(time).subtract(entity.position());
        }
        itemEntityRenderState.bobOffset = entity.data.animateOffset;
    }

    @Override
    public AABB getBoundingBoxForCulling(ItemEntity itemEntity, float partialTicks) {
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        if (entity.isAlive()) {
            return entity.getBoundingBox();
        }
        return entity.data.renderBox;
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
            Pose pose = matrixStack.last();
            float f = -((float) box.minY) + 0.0625F;
            pose.translate(0, state.bobOffset + f, -0.0625f);
            if (!state.alive) {
                Vec3 location = state.location;
                pose.translate((float) location.x, (float) location.y, (float) location.z);
                pose.translate(0, 0.25f, 0);
                if (state.isPackage) {
                    pose.translate(0, 0.25f, 0);
                    pose.scale(3.0f, 3.0f, 3.0f);
                }
                if (state.rotateY != 0) {
                    pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.rotateY);
                }
                if (state.rotateX != 0) {
                    pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.rotateX);
                }
                pose.translate(0, -0.25f, 0);
            } else if (state.ageInTicks > 0) {
                float g = Mth.sin(state.ageInTicks) * 0.1F + 0.1F;
                pose.translate(0, g, 0);
                pose.rotate(Axis.YP, state.ageInTicks / 2.0F);
            }
            submitMultipleFromCount(matrixStack, queue, state.lightCoords, state, random, box);
            matrixStack.popPose();

            if (state.alive) {
                if (state.leashStates != null) {
                    for (LeashState leashData : state.leashStates) {
                        queue.submitLeash(matrixStack, leashData);
                    }
                }

                submitNameDisplay(state, matrixStack, queue, cameraRenderState);
            }
        }
    }

    public static class RenderState extends ItemEntityRenderState {
        public boolean alive;
        public float rotateY;
        public float rotateX;
        public @UnknownNullability Vec3 location;
        public boolean isPackage;
    }
}
