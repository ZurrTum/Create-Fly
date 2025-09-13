package com.zurrtum.create.client.infrastructure.particle;

import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
        if (this.angle != 0.0F) {
            float angle = MathHelper.lerp(partialTicks, this.lastAngle, this.angle);
            quaternion.mul(RotationAxis.POSITIVE_Z.rotation(angle));
        }
        return quaternion;
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        Vec3d cameraPos = camera.getPos();
        float originX = (float) (MathHelper.lerp(partialTicks, lastX, x) - cameraPos.getX());
        float originY = (float) (MathHelper.lerp(partialTicks, lastY, y) - cameraPos.getY());
        float originZ = (float) (MathHelper.lerp(partialTicks, lastZ, z) - cameraPos.getZ());

        Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(
            1.0F,
            1.0F,
            0.0F
        ), new Vector3f(1.0F, -1.0F, 0.0F)};
        float scale = getSize(partialTicks);

        Quaternionf rotation = getCustomRotation(camera, partialTicks);
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(rotation);
            vertex.mul(scale);
            vertex.add(originX, originY, originZ);
        }

        float minU = mirror ? getMaxU() : getMinU();
        float maxU = mirror ? getMinU() : getMaxU();
        float minV = getMinV();
        float maxV = getMaxV();
        //TODO ShadersModHelper.isShaderPackInUse()
        int brightness = false ? LightmapTextureManager.pack(12, 15) : getBrightness(partialTicks);
        builder.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).texture(maxU, maxV).color(red, green, blue, alpha).light(brightness);
        builder.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).texture(maxU, minV).color(red, green, blue, alpha).light(brightness);
        builder.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).texture(minU, minV).color(red, green, blue, alpha).light(brightness);
        builder.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).texture(minU, maxV).color(red, green, blue, alpha).light(brightness);
    }
}
