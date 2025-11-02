package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.decoration.steamWhistle.WhistleSoundInstance;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WhistleAudioBehaviour extends AudioBehaviour<WhistleBlockEntity> {
    protected WhistleSoundInstance soundInstance;

    public WhistleAudioBehaviour(WhistleBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        FluidTankBlockEntity tank = blockEntity.getTank();
        boolean powered = blockEntity.isPowered() && (tank != null && tank.boiler.isActive() && (tank.boiler.passiveHeat || tank.boiler.activeHeat > 0) || blockEntity.isVirtual());
        if (!powered) {
            if (soundInstance != null) {
                soundInstance.fadeOut();
                soundInstance = null;
            }
            return;
        }

        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();
        float f = (float) Math.pow(2, -blockEntity.pitch / 12.0);
        boolean particle = world.getTime() % 8 == 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d eyePosition = mc.getCameraEntity().getEyePos();
        float maxVolume = (float) MathHelper.clamp((64 - eyePosition.distanceTo(Vec3d.ofCenter(pos))) / 64, 0, 1);

        WhistleSize size = blockEntity.getOctave();
        if (soundInstance == null || soundInstance.isDone() || soundInstance.getOctave() != size) {
            mc.getSoundManager().play(soundInstance = new WhistleSoundInstance(size, pos));
            AllSoundEvents.WHISTLE_CHIFF.playAt(world, pos, maxVolume * .175f, size == WhistleBlock.WhistleSize.SMALL ? f + .75f : f, false);
            particle = true;
        }

        soundInstance.keepAlive();
        soundInstance.setPitch(f);

        if (!particle)
            return;

        Direction facing = blockEntity.getCachedState().getOrEmpty(WhistleBlock.FACING).orElse(Direction.SOUTH);
        float angle = 180 + AngleHelper.horizontalAngle(facing);
        Vec3d sizeOffset = VecHelper.rotate(new Vec3d(0, -0.4f, 1 / 16f * size.ordinal()), angle, Axis.Y);
        Vec3d offset = VecHelper.rotate(new Vec3d(0, 1, 0.75f), angle, Axis.Y);
        Vec3d v = offset.multiply(.45f).add(sizeOffset).add(Vec3d.ofCenter(pos));
        Vec3d m = offset.subtract(Vec3d.of(facing.getVector()).multiply(.75f));
        world.addParticleClient(AllParticleTypes.STEAM_JET, v.x, v.y, v.z, m.x, m.y, m.z);
    }
}
