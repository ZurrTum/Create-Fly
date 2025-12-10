package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BasinFluidParticle extends FluidParticle {
    private static final Vector3f DIAGONAL_PLANE = new Vector3f(-1.0F, 0.0F, 0.0F);
    private static final Vector3f VERTICAL_PLANE = new Vector3f(0.0F, 0.0F, -1.0F);
    BlockPos basinPos;
    Vec3d targetPos;
    Vec3d centerOfBasin;
    float yOffset;

    public BasinFluidParticle(
        ClientWorld world,
        Fluid fluid,
        ComponentChanges components,
        FluidConfig config,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz
    ) {
        super(world, fluid, components, config, x, y, z, vx, vy, vz);
        gravityStrength = 0;
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        yOffset = world.random.nextFloat() * 1 / 32f;
        y += yOffset;
        scale = 0;
        maxAge = 60;
        Vec3d currentPos = new Vec3d(x, y, z);
        basinPos = BlockPos.ofFloored(currentPos);
        centerOfBasin = VecHelper.getCenterOf(basinPos);

        if (vx != 0) {
            maxAge = 20;
            Vec3d centerOf = VecHelper.getCenterOf(basinPos);
            Vec3d diff = currentPos.subtract(centerOf).multiply(1, 0, 1).normalize().multiply(.375);
            targetPos = centerOf.add(diff);
            lastX = this.x = centerOfBasin.x;
            lastZ = this.z = centerOfBasin.z;
        }
    }

    @Override
    public void tick() {
        super.tick();
        scale = targetPos != null ? Math.max(1 / 32f, ((1f * age) / maxAge) / 8) : 1 / 8f * (1 - ((Math.abs(age - (maxAge / 2)) / (1f * maxAge))));

        if (age % 2 == 0) {
            if (!world.getBlockState(basinPos).isOf(AllBlocks.BASIN) && !BasinBlock.isBasin(world, basinPos)) {
                markDead();
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(basinPos);
            if (blockEntity instanceof BasinBlockEntity) {
                float totalUnits = ((BasinBlockEntity) blockEntity).getTotalFluidUnits(0);
                if (totalUnits < 1)
                    totalUnits = 0;
                float fluidLevel = MathHelper.clamp(totalUnits / 162000, 0, 1);
                y = 2 / 16f + basinPos.getY() + 12 / 16f * fluidLevel + yOffset;
            }

        }

        if (targetPos != null) {
            float progess = (1f * age) / maxAge;
            Vec3d currentPos = centerOfBasin.add(targetPos.subtract(centerOfBasin).multiply(progess));
            x = currentPos.x;
            z = currentPos.z;
        }
    }

    @Override
    protected void updateColor() {
        this.alpha = 0.9F;
    }

    @Override
    protected int getBrightness(float p_189214_1_) {
        return LightmapTextureManager.MAX_LIGHT_COORDINATE;
    }

    @Override
    public void render(VertexConsumer vb, Camera info, float pt) {
        Quaternionf rotation = info.getRotation();
        Vector3f diagonalPlane = info.getDiagonalPlane();
        Vector3f verticalPlane = info.getVerticalPlane();
        Quaternionf prevRotation = new Quaternionf(rotation);
        Vector3f prevDiagonalPlane = new Vector3f(diagonalPlane);
        Vector3f prevVerticalPlane = new Vector3f(verticalPlane);
        rotation.set(-1, 0, 0, 1);
        rotation.normalize();
        diagonalPlane.set(DIAGONAL_PLANE);
        verticalPlane.set(VERTICAL_PLANE);
        super.render(vb, info, pt);
        rotation.set(0, 0, 0, 1);
        rotation.mul(prevRotation);
        diagonalPlane.set(prevDiagonalPlane);
        verticalPlane.set(prevVerticalPlane);
    }

    @Override
    protected boolean canEvaporate() {
        return false;
    }

    public static class Factory implements ParticleFactory<FluidParticleData> {
        @Override
        public Particle createParticle(FluidParticleData data, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            FluidConfig config = AllFluidConfigs.get(data.fluid());
            if (config == null) {
                return null;
            }
            return new BasinFluidParticle(world, data.fluid(), data.components(), config, x, y, z, vx, vy, vz);
        }
    }
}
