package com.zurrtum.create.client.infrastructure.particle;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RotationIndicatorParticle extends SimpleAnimatedParticle {

    protected float radius;
    protected float radius1;
    protected float radius2;
    protected float speed;
    protected Axis axis;
    protected Vec3 origin;
    protected Vec3 offset;

    private RotationIndicatorParticle(
        ClientLevel world,
        double x,
        double y,
        double z,
        int color,
        float radius1,
        float radius2,
        float speed,
        Axis axis,
        int lifeSpan,
        SpriteSet sprite,
        RandomSource random
    ) {
        super(world, x, y, z, sprite, 0);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.origin = new Vec3(x, y, z);
        this.quadSize *= 0.75F;
        this.lifetime = lifeSpan + random.nextInt(32);
        this.setFadeColor(color);
        this.setColor(Color.mixColors(color, 0xFFFFFF, .5f));
        this.setSpriteFromAge(sprite);
        this.radius1 = radius1;
        this.radius = radius1;
        this.radius2 = radius2;
        this.speed = speed;
        this.axis = axis;
        this.offset = axis.isHorizontal() ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        move(0, 0, 0);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public void tick() {
        super.tick();
        radius += (radius2 - radius) * .1f;
    }

    public void move(double x, double y, double z) {
        float time = AnimationTickHolder.getTicks(level);
        float angle = ((time * speed) % 360) - (speed / 2 * age * (((float) age) / lifetime));
        if (speed < 0 && axis.isVertical())
            angle += 180;
        Vec3 position = VecHelper.rotate(this.offset.scale(radius), angle, axis).add(origin);
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public static class Factory implements ParticleProvider<RotationIndicatorParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        @Nullable
        public Particle createParticle(
            RotationIndicatorParticleData data,
            ClientLevel worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random
        ) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (worldIn == mc.level && (player == null || !GogglesItem.isWearingGoggles(player))) {
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
                this.spriteSet,
                random
            );
        }
    }

}