package com.zurrtum.create.foundation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ItemPlacementSoundContext extends BlockPlaceContext {
    private final float volume;
    private final float pitch;
    private final SoundEvent sound;

    public ItemPlacementSoundContext(
        Level world,
        @Nullable Player playerEntity,
        InteractionHand hand,
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

    public ItemPlacementSoundContext(BlockPlaceContext context, float volume, float pitch, SoundEvent sound) {
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
            getLevel(), getPlayer(), getHand(), getItemInHand(), new BlockHitResult(
            new Vec3(pos.getX() + 0.5 + side.getStepX() * 0.5, pos.getY() + 0.5 + side.getStepY() * 0.5, pos.getZ() + 0.5 + side.getStepZ() * 0.5),
            side,
            pos,
            false
        ), volume, pitch, sound
        );
    }
}
