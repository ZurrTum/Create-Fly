package com.zurrtum.create.foundation.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemPlacementSoundContext extends ItemPlacementContext {
    private final float volume;
    private final float pitch;
    private final SoundEvent sound;

    public ItemPlacementSoundContext(
        World world,
        @Nullable PlayerEntity playerEntity,
        Hand hand,
        ItemStack itemStack,
        BlockHitResult blockHitResult,
        float volume,
        float pitch,
        SoundEvent sound
    ) {
        super(world, playerEntity, hand, itemStack, blockHitResult);
        this.volume = volume;
        this.pitch = pitch;
        this.sound = sound;
    }

    public ItemPlacementSoundContext(ItemPlacementContext context, float volume, float pitch, SoundEvent sound) {
        super(context);
        this.volume = volume;
        this.pitch = pitch;
        this.sound = sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public SoundEvent getSound() {
        return sound;
    }

    public ItemPlacementSoundContext offset(BlockPos pos, Direction side) {
        return new ItemPlacementSoundContext(
            getWorld(), getPlayer(), getHand(), getStack(), new BlockHitResult(
            new Vec3d(
                pos.getX() + 0.5 + side.getOffsetX() * 0.5,
                pos.getY() + 0.5 + side.getOffsetY() * 0.5,
                pos.getZ() + 0.5 + side.getOffsetZ() * 0.5
            ),
            side,
            pos,
            false
        ), volume, pitch, sound
        );
    }
}
