package com.zurrtum.create.content.materials;

import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ExperienceBlock extends Block {
    public static final SoundType SOUND = new SoundType(
        1,
        .5f,
        SoundEvents.AMETHYST_BLOCK_BREAK,
        SoundEvents.AMETHYST_BLOCK_STEP,
        SoundEvents.AMETHYST_BLOCK_PLACE,
        SoundEvents.AMETHYST_BLOCK_HIT,
        SoundEvents.AMETHYST_BLOCK_FALL
    );

    public ExperienceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRand) {
        if (pRand.nextInt(5) != 0)
            return;
        Vec3 vec3 = VecHelper.clampComponentWise(VecHelper.offsetRandomly(Vec3.ZERO, pRand, .75f), .55f).add(VecHelper.getCenterOf(pPos));
        pLevel.addParticle(
            ParticleTypes.END_ROD,
            vec3.x,
            vec3.y,
            vec3.z,
            pRand.nextGaussian() * 0.005D,
            pRand.nextGaussian() * 0.005D,
            pRand.nextGaussian() * 0.005D
        );
    }
}
