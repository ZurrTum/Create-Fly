package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class SawAudioBehaviour extends KineticAudioBehaviour<SawBlockEntity> {
    public SawAudioBehaviour(SawBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        super.tickAudio();
        if (blockEntity.getSpeed() == 0)
            return;

        if (!blockEntity.playEvent.isEmpty()) {
            boolean isWood = false;
            Item item = blockEntity.playEvent.getItem();
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                isWood = block.defaultBlockState().getSoundType() == SoundType.WOOD;
            }
            blockEntity.spawnEventParticles(blockEntity.playEvent);
            blockEntity.playEvent = ItemStack.EMPTY;
            if (!isWood)
                AllSoundEvents.SAW_ACTIVATE_STONE.playAt(getLevel(), getPos(), 3, 1, true);
            else
                AllSoundEvents.SAW_ACTIVATE_WOOD.playAt(getLevel(), getPos(), 3, 1, true);
        }
    }
}
