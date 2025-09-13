package com.zurrtum.create.client.content.contraptions.actors.seat;

import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

public class SeatRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
    public SeatRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(SeatEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void updateRenderState(SeatEntity entity, EntityRenderState state, float tickProgress) {
    }

    @Override
    public void render(EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
    }
}
