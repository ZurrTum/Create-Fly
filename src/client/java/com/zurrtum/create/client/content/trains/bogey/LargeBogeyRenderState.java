package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class LargeBogeyRenderState extends StandardBogeyRenderState {
    public SuperByteBuffer secondaryShaft;
    public SuperByteBuffer drive;
    public SuperByteBuffer belt;
    public float scroll;
    public SuperByteBuffer piston;
    public float pistonOffset;
    public SuperByteBuffer wheels;
    public SuperByteBuffer pin;

    @Override
    public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
        super.render(matricesEntry, vertexConsumer);
        secondaryShaft.translate(-.5f, .25f, .5f).center().rotateX(angle).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
            .renderInto(matricesEntry, vertexConsumer);
        secondaryShaft.translate(-.5f, .25f, -1.5f).center().rotateX(angle).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
            .renderInto(matricesEntry, vertexConsumer);
        drive.scale(0.998046875f).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        belt.scale(0.998046875f).light(light).overlay(OverlayTexture.DEFAULT_UV).shiftUVScrolling(AllSpriteShifts.BOGEY_BELT, scroll)
            .renderInto(matricesEntry, vertexConsumer);
        piston.translate(0, 0, pistonOffset).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 1, 0).rotateX(angle).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        pin.translate(0, 1, 0).rotateX(angle).translate(0, 0.25f, 0).rotateX(-angle).light(light).overlay(OverlayTexture.DEFAULT_UV)
            .renderInto(matricesEntry, vertexConsumer);
    }
}
