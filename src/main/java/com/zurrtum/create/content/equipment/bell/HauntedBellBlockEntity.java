package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HauntedBellBlockEntity extends AbstractBellBlockEntity {

    public static final int DISTANCE = 10;
    public static final int RECHARGE_TICKS = 65;
    public static final int EFFECT_TICKS = 20;

    public int effectTicks = 0;

    public HauntedBellBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HAUNTED_BELL, pos, state);
    }

    @Override
    public boolean ring(World world, BlockPos pos, Direction direction) {
        if (isRinging && ringingTicks < RECHARGE_TICKS)
            return false;
        if (world instanceof ServerWorld serverLevel)
            HauntedBellPulser.sendPulse(serverLevel, pos, DISTANCE, false);
        effectTicks = EFFECT_TICKS;
        return super.ring(world, pos, direction);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("EffectTicks", effectTicks);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        effectTicks = view.getInt("EffectTicks", 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (effectTicks <= 0)
            return;
        effectTicks--;

        if (!world.isClient())
            return;

        Random rand = world.getRandom();
        if (rand.nextFloat() > 0.25f)
            return;

        spawnParticle(rand);
        playSound(rand);
    }

    protected void spawnParticle(Random rand) {
        double x = pos.getX() + rand.nextDouble();
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + rand.nextDouble();
        double vx = rand.nextDouble() * 0.04 - 0.02;
        double vy = 0.1;
        double vz = rand.nextDouble() * 0.04 - 0.02;
        world.addParticleClient(ParticleTypes.SOUL, x, y, z, vx, vy, vz);
    }

    protected void playSound(Random rand) {
        float vol = rand.nextFloat() * 0.4F + rand.nextFloat() > 0.9F ? 0.6F : 0.0F;
        float pitch = 0.6F + rand.nextFloat() * 0.4F;
        world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.BLOCKS, vol, pitch);
    }

}
