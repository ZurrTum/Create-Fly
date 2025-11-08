package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class EjectorItemEntityRenderer extends ItemEntityRenderer {
    public EjectorItemEntityRenderer(EntityRendererFactory.Context context) {
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
    public void updateRenderState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f) {
        super.updateRenderState(itemEntity, itemEntityRenderState, f);
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        RenderState state = (RenderState) itemEntityRenderState;
        state.alive = entity.isAlive();
        if (state.alive) {
            if (entity.data.initAge == -1) {
                itemEntityRenderState.age = 0;
            } else {
                itemEntityRenderState.age = (entity.age - entity.data.initAge + f) / 10.0F;
            }
        } else {
            state.rotateY = entity.data.rotateY;
            float time = entity.progress + f;
            state.rotateX = MathHelper.RADIANS_PER_DEGREE * time * 40;
            state.location = entity.getLaunchedItemLocation(time).subtract(entity.getPos());
        }
        itemEntityRenderState.uniqueOffset = entity.data.animateOffset;
    }

    @Override
    public Box getBoundingBox(ItemEntity itemEntity) {
        EjectorItemEntity entity = (EjectorItemEntity) itemEntity;
        if (entity.isAlive()) {
            return entity.getBoundingBox();
        } else {
            return entity.data.renderBox;
        }
    }

    @Override
    public void render(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (!itemEntityRenderState.itemRenderState.isEmpty()) {
            RenderState state = (RenderState) itemEntityRenderState;
            Box box = itemEntityRenderState.itemRenderState.getModelBoundingBox();
            matrixStack.push();
            float f = -((float) box.minY) + 0.0625F;
            matrixStack.translate(0, state.uniqueOffset + f, -0.0625f);
            if (!state.alive) {
                matrixStack.translate(state.location);
                matrixStack.translate(0, 0.25f, 0);
                if (state.rotateY != 0) {
                    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(state.rotateY));
                }
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(state.rotateX));
                matrixStack.translate(0, -0.25f, 0);
            } else if (itemEntityRenderState.age > 0) {
                float g = MathHelper.sin(itemEntityRenderState.age) * 0.1F + 0.1F;
                matrixStack.translate(0, g, 0);
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(itemEntityRenderState.age / 2F));
            }
            renderStack(matrixStack, vertexConsumerProvider, i, itemEntityRenderState, random, box);
            matrixStack.pop();

            if (state.alive) {
                if (state.leashDatas != null) {
                    for (EntityRenderState.LeashData leashData : state.leashDatas) {
                        renderLeash(matrixStack, vertexConsumerProvider, leashData);
                    }
                }

                if (state.displayName != null) {
                    this.renderLabelIfPresent(state, state.displayName, matrixStack, vertexConsumerProvider, i);
                }
            }
        }
    }

    public static class RenderState extends ItemEntityRenderState {
        public boolean alive;
        public float rotateY;
        public float rotateX;
        public Vec3d location;
    }
}
