package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HauntedBellBlockEntity extends AbstractBellBlockEntity {

    public static final int DISTANCE = 10;
    public static final int RECHARGE_TICKS = 65;
    public static final int EFFECT_TICKS = 20;

    public int effectTicks = 0;

    public HauntedBellBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HAUNTED_BELL, pos, state);
    }

    @Override
    public boolean ring(Level world, BlockPos pos, Direction direction) {
        if (isRinging && ringingTicks < RECHARGE_TICKS)
            return false;
        if (world instanceof ServerLevel serverLevel)
            HauntedBellPulser.sendPulse(serverLevel, pos, DISTANCE, false);
        effectTicks = EFFECT_TICKS;
        return super.ring(world, pos, direction);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("EffectTicks", effectTicks);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        effectTicks = view.getIntOr("EffectTicks", 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (effectTicks <= 0)
            return;
        effectTicks--;

        if (!level.isClientSide())
            return;

        RandomSource rand = level.getRandom();
        if (rand.nextFloat() > 0.25f)
            return;

        spawnParticle(rand);
        playSound(rand);
    }

    protected void spawnParticle(RandomSource rand) {
        double x = worldPosition.getX() + rand.nextDouble();
        double y = worldPosition.getY() + 0.5;
        double z = worldPosition.getZ() + rand.nextDouble();
        double vx = rand.nextDouble() * 0.04 - 0.02;
        double vy = 0.1;
        double vz = rand.nextDouble() * 0.04 - 0.02;
        level.addParticle(ParticleTypes.SOUL, x, y, z, vx, vy, vz);
    }

    protected void playSound(RandomSource rand) {
        float vol = rand.nextFloat() * 0.4F + rand.nextFloat() > 0.9F ? 0.6F : 0.0F;
        float pitch = 0.6F + rand.nextFloat() * 0.4F;
        level.playSound(null, worldPosition, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, vol, pitch);
    }

}
