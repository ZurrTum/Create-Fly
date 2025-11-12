package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;

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
    public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
        super.render(matricesEntry, vertexConsumer);
        secondaryShaft.translate(-.5f, .25f, .5f).center().rotateX(angle).uncenter().light(light).overlay(OverlayTexture.NO_OVERLAY)
            .renderInto(matricesEntry, vertexConsumer);
        secondaryShaft.translate(-.5f, .25f, -1.5f).center().rotateX(angle).uncenter().light(light).overlay(OverlayTexture.NO_OVERLAY)
            .renderInto(matricesEntry, vertexConsumer);
        drive.scale(0.998046875f).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        belt.scale(0.998046875f).light(light).overlay(OverlayTexture.NO_OVERLAY).shiftUVScrolling(AllSpriteShifts.BOGEY_BELT, scroll)
            .renderInto(matricesEntry, vertexConsumer);
        piston.translate(0, 0, pistonOffset).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 1, 0).rotateX(angle).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        pin.translate(0, 1, 0).rotateX(angle).translate(0, 0.25f, 0).rotateX(-angle).light(light).overlay(OverlayTexture.NO_OVERLAY)
            .renderInto(matricesEntry, vertexConsumer);
    }
}
