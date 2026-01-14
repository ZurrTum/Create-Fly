package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.client.foundation.render.AllRenderPipelines;
import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import com.zurrtum.create.infrastructure.particle.CubeParticleData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static com.zurrtum.create.Create.MOD_ID;

public class CubeParticle extends Particle {
    private static final RenderLayer LAYER = RenderLayer.of(
        MOD_ID + ":cube",
        1536,
        false,
        false,
        AllRenderPipelines.CUBE,
        RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(PonderSpecialTextures.BLANK.getLocation(), false))
            .target(FabricLoader.getInstance().isModLoaded("iris") ? RenderPhase.MAIN_TARGET : RenderLayer.PARTICLES_TARGET)
            .lightmap(RenderLayer.ENABLE_LIGHTMAP).build(false)
    );

    public static final Vec3d[] CUBE = {
        // TOP
        new Vec3d(1, 1, -1), new Vec3d(1, 1, 1), new Vec3d(-1, 1, 1), new Vec3d(-1, 1, -1),

        // BOTTOM
        new Vec3d(-1, -1, -1), new Vec3d(-1, -1, 1), new Vec3d(1, -1, 1), new Vec3d(1, -1, -1),

        // FRONT
        new Vec3d(-1, -1, 1), new Vec3d(-1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, -1, 1),

        // BACK
        new Vec3d(1, -1, -1), new Vec3d(1, 1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, -1, -1),

        // LEFT
        new Vec3d(-1, -1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, 1, 1), new Vec3d(-1, -1, 1),

        // RIGHT
        new Vec3d(1, -1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, -1), new Vec3d(1, -1, -1)};

    protected float scale;
    protected boolean hot;

    public CubeParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.velocityX = motionX;
        this.velocityY = motionY;
        this.velocityZ = motionZ;

        setScale(0.2F);
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.setBoundingBoxSpacing(scale * 0.5f, scale * 0.5f);
    }

    public void averageAge(int age) {
        this.maxAge = (int) (age + (random.nextDouble() * 2D - 1D) * 8);
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }

    private boolean billowing = false;

    @Override
    public void tick() {
        if (this.hot && this.age > 0) {
            if (this.lastY == this.y) {
                billowing = true;
                stopped = false; // Prevent motion being ignored due to vertical collision
                if (this.velocityX == 0 && this.velocityZ == 0) {
                    Vec3d diff = Vec3d.of(BlockPos.ofFloored(x, y, z)).add(0.5, 0.5, 0.5).subtract(x, y, z);
                    this.velocityX = -diff.x * 0.1;
                    this.velocityZ = -diff.z * 0.1;
                }
                this.velocityX *= 1.1;
                this.velocityY *= 0.9;
                this.velocityZ *= 1.1;
            } else if (billowing) {
                this.velocityY *= 1.2;
            }
        }
        super.tick();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickProgress) {
    }

    @Override
    public void renderCustom(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera renderInfo, float tickProgress) {
        VertexConsumer buffer = vertexConsumers.getBuffer(LAYER);
        Vec3d projectedView = renderInfo.getPos();
        float lerpedX = (float) (MathHelper.lerp(tickProgress, this.lastX, this.x) - projectedView.getX());
        float lerpedY = (float) (MathHelper.lerp(tickProgress, this.lastY, this.y) - projectedView.getY());
        float lerpedZ = (float) (MathHelper.lerp(tickProgress, this.lastZ, this.z) - projectedView.getZ());

        // int light = getBrightnessForRender(p_225606_3_);
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        double ageMultiplier = 1 - Math.pow(MathHelper.clamp(age + tickProgress, 0, maxAge), 3) / Math.pow(maxAge, 3);

        for (int i = 0; i < 6; i++) {
            // 6 faces to a cube
            for (int j = 0; j < 4; j++) {
                Vec3d vec = CUBE[i * 4 + j].multiply(-1);
                vec = vec
                    /* .rotate(?) */.multiply(scale * ageMultiplier).add(lerpedX, lerpedY, lerpedZ);

                buffer.vertex((float) vec.x, (float) vec.y, (float) vec.z).texture((float) j / 2, j % 2).color(red, green, blue, alpha).light(light);
            }
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public static class Factory implements ParticleFactory<CubeParticleData> {

        @Override
        public Particle createParticle(
            CubeParticleData data,
            ClientWorld world,
            double x,
            double y,
            double z,
            double motionX,
            double motionY,
            double motionZ
        ) {
            CubeParticle particle = new CubeParticle(world, x, y, z, motionX, motionY, motionZ);
            particle.setColor(data.red(), data.green(), data.blue());
            particle.setScale(data.scale());
            particle.averageAge(data.avgAge());
            particle.setHot(data.hot());
            return particle;
        }
    }
}
