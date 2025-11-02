package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class CustomRotationParticle extends AnimatedParticle {
    protected boolean mirror;
    protected int loopLength;

    public CustomRotationParticle(ClientWorld worldIn, double x, double y, double z, SpriteProvider spriteSet, float yAccel) {
        super(worldIn, x, y, z, spriteSet, yAccel);
    }

    public void selectSpriteLoopingWithAge(SpriteProvider sprite) {
        int loopFrame = age % loopLength;
        this.setSprite(sprite.getSprite(loopFrame, loopLength));
    }

    public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
        Quaternionf quaternion = new Quaternionf(camera.getRotation());
        if (zRotation != 0.0F) {
            float angle = MathHelper.lerp(partialTicks, lastZRotation, zRotation);
            quaternion.mul(RotationAxis.POSITIVE_Z.rotation(angle));
        }
        return quaternion;
    }

    @Override
    public void render(BillboardParticleSubmittable submittable, Camera camera, float partialTicks) {
        Vec3d cameraPos = camera.getPos();
        float originX = (float) (MathHelper.lerp(partialTicks, lastX, x) - cameraPos.getX());
        float originY = (float) (MathHelper.lerp(partialTicks, lastY, y) - cameraPos.getY());
        float originZ = (float) (MathHelper.lerp(partialTicks, lastZ, z) - cameraPos.getZ());
        Quaternionf rotation = getCustomRotation(camera, partialTicks);
        submittable.render(
            getRenderType(),
            originX,
            originY,
            originZ,
            rotation.x,
            rotation.y,
            rotation.z,
            rotation.w,
            getSize(partialTicks),
            mirror ? getMaxU() : getMinU(),
            mirror ? getMinU() : getMaxU(),
            getMinV(),
            getMaxV(),
            ColorHelper.fromFloats(alpha, red, green, blue),
            ShadersModHelper.isShaderPackInUse() ? LightmapTextureManager.pack(12, 15) : getBrightness(partialTicks)
        );
    }
}
