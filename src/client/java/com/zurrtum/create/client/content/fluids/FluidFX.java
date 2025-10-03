package com.zurrtum.create.client.content.fluids;

import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FluidFX {

    static Random r = Random.create();

    public static void splash(BlockPos pos, Fluid fluid) {
        if (fluid == Fluids.EMPTY)
            return;

        FluidState defaultState = fluid.getDefaultState();
        if (defaultState == null || defaultState.isEmpty()) {
            return;
        }

        BlockStateParticleEffect blockParticleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, defaultState.getBlockState());
        Vec3d center = VecHelper.getCenterOf(pos);

        for (int i = 0; i < 20; i++) {
            Vec3d v = VecHelper.offsetRandomly(Vec3d.ZERO, r, .25f);
            particle(blockParticleData, center.add(v), v);
        }

    }

    public static ParticleEffect getFluidParticle(FluidStack fluid) {
        return new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, fluid.getFluid(), fluid.getComponentChanges());
    }

    public static ParticleEffect getDrippingParticle(FluidStack fluid) {
        ParticleEffect particle = null;
        if (FluidHelper.isWater(fluid.getFluid()))
            particle = ParticleTypes.DRIPPING_WATER;
        if (FluidHelper.isLava(fluid.getFluid()))
            particle = ParticleTypes.DRIPPING_LAVA;
        if (particle == null)
            particle = new FluidParticleData(AllParticleTypes.FLUID_DRIP, fluid.getFluid(), fluid.getComponentChanges());
        return particle;
    }

    public static void spawnRimParticles(World world, BlockPos pos, Direction side, int amount, ParticleEffect particle, float rimRadius) {
        Vec3d directionVec = Vec3d.of(side.getVector());
        for (int i = 0; i < amount; i++) {
            Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, 1).normalize();
            vec = VecHelper.clampComponentWise(vec, rimRadius).multiply(VecHelper.axisAlingedPlaneOf(directionVec))
                .add(directionVec.multiply(.45 + r.nextFloat() / 16f));
            Vec3d m = vec.multiply(.05f);
            vec = vec.add(VecHelper.getCenterOf(pos));

            world.addImportantParticleClient(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
        }
    }

    public static void spawnPouringLiquid(
        World world,
        BlockPos pos,
        int amount,
        ParticleEffect particle,
        float rimRadius,
        Vec3d directionVec,
        boolean inbound
    ) {
        for (int i = 0; i < amount; i++) {
            Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, rimRadius * .75f);
            vec = vec.multiply(VecHelper.axisAlingedPlaneOf(directionVec)).add(directionVec.multiply(.5 + r.nextFloat() / 4f));
            Vec3d m = vec.multiply(1 / 4f);
            Vec3d centerOf = VecHelper.getCenterOf(pos);
            vec = vec.add(centerOf);
            if (inbound) {
                vec = vec.add(m);
                m = centerOf.add(directionVec.multiply(.5)).subtract(vec).multiply(1 / 16f);
            }
            world.addImportantParticleClient(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
        }
    }

    private static void particle(ParticleEffect data, Vec3d pos, Vec3d motion) {
        world().addParticleClient(data, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    private static World world() {
        return MinecraftClient.getInstance().world;
    }

}
