package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HauntedBellBlock extends AbstractBellBlock<HauntedBellBlockEntity> {

    public HauntedBellBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends HauntedBellBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HAUNTED_BELL;
    }

    @Override
    protected boolean ring(World world, BlockPos pos, Direction direction, PlayerEntity player) {
        boolean ring = super.ring(world, pos, direction, player);
        if (ring && player instanceof ServerPlayerEntity serverPlayer)
            AllAdvancements.HAUNTED_BELL.trigger(serverPlayer);
        return ring;
    }

    @Override
    public Class<HauntedBellBlockEntity> getBlockEntityClass() {
        return HauntedBellBlockEntity.class;
    }

    @Override
    public void playSound(World world, BlockPos pos) {
        AllSoundEvents.HAUNTED_BELL_USE.playOnServer(world, pos, 4f, 1f);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != this && !world.isClient())
            withBlockEntityDo(
                world, pos, hbte -> {
                    hbte.effectTicks = HauntedBellBlockEntity.EFFECT_TICKS;
                    hbte.sendData();
                }
            );
    }

}
