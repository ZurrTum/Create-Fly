package com.zurrtum.create.client.infrastructure.particle;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class RotationIndicatorParticle extends AnimatedParticle {

    protected float radius;
    protected float radius1;
    protected float radius2;
    protected float speed;
    protected Axis axis;
    protected Vec3d origin;
    protected Vec3d offset;

    private RotationIndicatorParticle(
        ClientWorld world,
        double x,
        double y,
        double z,
        int color,
        float radius1,
        float radius2,
        float speed,
        Axis axis,
        int lifeSpan,
        SpriteProvider sprite
    ) {
        super(world, x, y, z, sprite, 0);
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.origin = new Vec3d(x, y, z);
        this.scale *= 0.75F;
        this.maxAge = lifeSpan + this.random.nextInt(32);
        this.setTargetColor(color);
        this.setColor(Color.mixColors(color, 0xFFFFFF, .5f));
        this.updateSprite(sprite);
        this.radius1 = radius1;
        this.radius = radius1;
        this.radius2 = radius2;
        this.speed = speed;
        this.axis = axis;
        this.offset = axis.isHorizontal() ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        move(0, 0, 0);
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
    }

    @Override
    public void tick() {
        super.tick();
        radius += (radius2 - radius) * .1f;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        super.render(buffer, renderInfo, partialTicks);
    }

    public void move(double x, double y, double z) {
        float time = AnimationTickHolder.getTicks(world);
        float angle = ((time * speed) % 360) - (speed / 2 * age * (((float) age) / maxAge));
        if (speed < 0 && axis.isVertical())
            angle += 180;
        Vec3d position = VecHelper.rotate(this.offset.multiply(radius), angle, axis).add(origin);
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public static class Factory implements ParticleFactory<RotationIndicatorParticleData> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(
            RotationIndicatorParticleData data,
            ClientWorld worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            if (worldIn == mc.world && (player == null || !GogglesItem.isWearingGoggles(player))) {
                return null;
            }
            return new RotationIndicatorParticle(
                worldIn,
                x,
                y,
                z,
                data.color(),
                data.radius1(),
                data.radius2(),
                data.speed(),
                data.axis(),
                data.lifeSpan(),
                this.spriteSet
            );
        }
    }

}