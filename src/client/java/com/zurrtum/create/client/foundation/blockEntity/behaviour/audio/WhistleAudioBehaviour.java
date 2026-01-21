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
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WhistleAudioBehaviour extends AudioBehaviour<WhistleBlockEntity> {
    protected @Nullable WhistleSoundInstance soundInstance;

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

        Level world = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        float f = (float) Math.pow(2, -blockEntity.pitch / 12.0);
        boolean particle = world.getGameTime() % 8 == 0;
        Minecraft mc = Minecraft.getInstance();
        Vec3 eyePosition = mc.getCameraEntity().getEyePosition();
        float maxVolume = (float) Mth.clamp((64 - eyePosition.distanceTo(Vec3.atCenterOf(pos))) / 64, 0, 1);

        WhistleSize size = blockEntity.getOctave();
        if (soundInstance == null || soundInstance.isStopped() || soundInstance.getOctave() != size) {
            mc.getSoundManager().play(soundInstance = new WhistleSoundInstance(size, pos));
            AllSoundEvents.WHISTLE_CHIFF.playAt(world, pos, maxVolume * .175f, size == WhistleBlock.WhistleSize.SMALL ? f + .75f : f, false);
            particle = true;
        }

        soundInstance.keepAlive();
        soundInstance.setPitch(f);

        if (!particle)
            return;

        Direction facing = blockEntity.getBlockState().getOptionalValue(WhistleBlock.FACING).orElse(Direction.SOUTH);
        float angle = 180 + AngleHelper.horizontalAngle(facing);
        Vec3 sizeOffset = VecHelper.rotate(new Vec3(0, -0.4f, 1 / 16f * size.ordinal()), angle, Axis.Y);
        Vec3 offset = VecHelper.rotate(new Vec3(0, 1, 0.75f), angle, Axis.Y);
        Vec3 v = offset.scale(.45f).add(sizeOffset).add(Vec3.atCenterOf(pos));
        Vec3 m = offset.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(.75f));
        world.addParticle(AllParticleTypes.STEAM_JET, v.x, v.y, v.z, m.x, m.y, m.z);
    }
}
