package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class CustomRotationParticle extends SimpleAnimatedParticle {
    protected boolean mirror;
    protected int loopLength;

    public CustomRotationParticle(ClientLevel worldIn, double x, double y, double z, SpriteSet spriteSet, float yAccel) {
        super(worldIn, x, y, z, spriteSet, yAccel);
    }

    public void selectSpriteLoopingWithAge(SpriteSet sprite) {
        int loopFrame = age % loopLength;
        this.setSprite(sprite.get(loopFrame, loopLength));
    }

    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        Quaternionf quaternion = new Quaternionf(camera.rotation());
        if (roll != 0.0F) {
            float angle = Mth.lerp(partialTicks, oRoll, roll);
            quaternion.mul(Axis.ZP.rotation(angle));
        }
        return quaternion;
    }

    @Override
    public void extract(QuadParticleRenderState submittable, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float originX = (float) (Mth.lerp(partialTicks, xo, x) - cameraPos.x());
        float originY = (float) (Mth.lerp(partialTicks, yo, y) - cameraPos.y());
        float originZ = (float) (Mth.lerp(partialTicks, zo, z) - cameraPos.z());
        Quaternionf rotation = getCustomRotation(camera, partialTicks);
        submittable.add(
            getLayer(),
            originX,
            originY,
            originZ,
            rotation.x,
            rotation.y,
            rotation.z,
            rotation.w,
            getQuadSize(partialTicks),
            mirror ? getU1() : getU0(),
            mirror ? getU0() : getU1(),
            getV0(),
            getV1(),
            ARGB.colorFromFloat(alpha, rCol, gCol, bCol),
            ShadersModHelper.isShaderPackInUse() ? LightCoordsUtil.pack(12, 15) : getLightCoords(partialTicks)
        );
    }
}
