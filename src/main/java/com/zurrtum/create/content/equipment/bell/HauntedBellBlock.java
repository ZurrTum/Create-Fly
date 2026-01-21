package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HauntedBellBlock extends AbstractBellBlock<HauntedBellBlockEntity> {

    public HauntedBellBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends HauntedBellBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HAUNTED_BELL;
    }

    @Override
    protected boolean ring(Level world, BlockPos pos, Direction direction, @Nullable Player player) {
        boolean ring = super.ring(world, pos, direction, player);
        if (ring && player instanceof ServerPlayer serverPlayer)
            AllAdvancements.HAUNTED_BELL.trigger(serverPlayer);
        return ring;
    }

    @Override
    public Class<HauntedBellBlockEntity> getBlockEntityClass() {
        return HauntedBellBlockEntity.class;
    }

    @Override
    public void playSound(Level world, BlockPos pos) {
        AllSoundEvents.HAUNTED_BELL_USE.playOnServer(world, pos, 4f, 1f);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != this && !world.isClientSide())
            withBlockEntityDo(
                world, pos, hbte -> {
                    hbte.effectTicks = HauntedBellBlockEntity.EFFECT_TICKS;
                    hbte.sendData();
                }
            );
    }

}
