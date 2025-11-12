package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class BasinFluidParticle extends FluidParticle {
    BlockPos basinPos;
    Vec3 targetPos;
    Vec3 centerOfBasin;
    float yOffset;

    public BasinFluidParticle(
        ClientLevel world,
        Fluid fluid,
        DataComponentPatch components,
        FluidConfig config,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(world, fluid, components, config, x, y, z, vx, vy, vz, random);
        gravity = 0;
        xd = 0;
        yd = 0;
        zd = 0;
        yOffset = random.nextFloat() * 1 / 32f;
        y += yOffset;
        quadSize = 0;
        lifetime = 60;
        Vec3 currentPos = new Vec3(x, y, z);
        basinPos = BlockPos.containing(currentPos);
        centerOfBasin = VecHelper.getCenterOf(basinPos);

        if (vx != 0) {
            lifetime = 20;
            Vec3 centerOf = VecHelper.getCenterOf(basinPos);
            Vec3 diff = currentPos.subtract(centerOf).multiply(1, 0, 1).normalize().scale(.375);
            targetPos = centerOf.add(diff);
            xo = this.x = centerOfBasin.x;
            zo = this.z = centerOfBasin.z;
        }
    }

    @Override
    public void tick() {
        super.tick();
        quadSize = targetPos != null ? Math.max(
            1 / 32f,
            ((1f * age) / lifetime) / 8
        ) : 1 / 8f * (1 - ((Math.abs(age - (lifetime / 2)) / (1f * lifetime))));

        if (age % 2 == 0) {
            if (!level.getBlockState(basinPos).is(AllBlocks.BASIN) && !BasinBlock.isBasin(level, basinPos)) {
                remove();
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(basinPos);
            if (blockEntity instanceof BasinBlockEntity) {
                float totalUnits = ((BasinBlockEntity) blockEntity).getTotalFluidUnits(0);
                if (totalUnits < 1)
                    totalUnits = 0;
                float fluidLevel = Mth.clamp(totalUnits / 162000, 0, 1);
                y = 2 / 16f + basinPos.getY() + 12 / 16f * fluidLevel + yOffset;
            }

        }

        if (targetPos != null) {
            float progess = (1f * age) / lifetime;
            Vec3 currentPos = centerOfBasin.add(targetPos.subtract(centerOfBasin).scale(progess));
            x = currentPos.x;
            z = currentPos.z;
        }
    }

    @Override
    protected void updateColor() {
        this.alpha = 0.9F;
    }

    @Override
    protected int getLightColor(float p_189214_1_) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void extract(QuadParticleRenderState submittable, Camera info, float pt) {
        Quaternionf rotation = info.rotation();
        Quaternionf prevRotation = new Quaternionf(rotation);
        rotation.set(-1, 0, 0, 1);
        rotation.normalize();
        super.extract(submittable, info, pt);
        rotation.set(0, 0, 0, 1);
        rotation.mul(prevRotation);
    }

    @Override
    protected boolean canEvaporate() {
        return false;
    }

    public static class Factory implements ParticleProvider<FluidParticleData> {
        @Override
        public Particle createParticle(
            FluidParticleData data,
            ClientLevel world,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            RandomSource random
        ) {
            FluidConfig config = AllFluidConfigs.get(data.fluid());
            if (config == null) {
                return null;
            }
            return new BasinFluidParticle(world, data.fluid(), data.components(), config, x, y, z, vx, vy, vz, random);
        }
    }
}
