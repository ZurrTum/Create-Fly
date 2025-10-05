package com.zurrtum.create.content.trains.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArrivalSoundQueue {
    public int offset;
    int min, max;
    Multimap<Integer, BlockPos> sources;

    public ArrivalSoundQueue() {
        sources = Multimaps.newMultimap(new HashMap<>(), ArrayList::new);
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
    }

    @Nullable
    public Integer firstTick() {
        return sources.isEmpty() ? null : min + offset;
    }

    @Nullable
    public Integer lastTick() {
        return sources.isEmpty() ? null : max + offset;
    }

    public boolean tick(CarriageContraptionEntity entity, int tick, boolean backwards) {
        tick = tick - offset;
        if (!sources.containsKey(tick))
            return backwards ? tick > min : tick < max;
        Contraption contraption = entity.getContraption();
        for (BlockPos blockPos : sources.get(tick))
            play(entity, contraption.getBlocks().get(blockPos));
        return backwards ? tick > min : tick < max;
    }

    public Pair<Boolean, Integer> getFirstWhistle(CarriageContraptionEntity entity) {
        Integer firstTick = firstTick();
        Integer lastTick = lastTick();
        if (firstTick == null || lastTick == null || firstTick > lastTick)
            return null;
        for (int i = firstTick; i <= lastTick; i++) {
            if (!sources.containsKey(i - offset))
                continue;
            Contraption contraption = entity.getContraption();
            for (BlockPos blockPos : sources.get(i - offset)) {
                StructureBlockInfo info = contraption.getBlocks().get(blockPos);
                if (info == null)
                    continue;
                BlockState state = info.state();
                if (state.getBlock() instanceof WhistleBlock && info.nbt() != null) {
                    int pitch = info.nbt().getInt("Pitch", 0);
                    WhistleSize size = state.get(WhistleBlock.SIZE);
                    return Pair.of(size == WhistleSize.LARGE, (size == WhistleSize.SMALL ? 12 : 0) - pitch);
                }
            }
        }
        return null;
    }

    public void write(WriteView view) {
        WriteView tag = view.get("SoundQueue");
        tag.putInt("Offset", offset);
        WriteView.ListView list = tag.getList("Sources");
        for (Map.Entry<Integer, BlockPos> entry : sources.entries()) {
            WriteView item = list.add();
            item.putInt("Tick", entry.getKey());
            item.put("Pos", BlockPos.CODEC, entry.getValue());
        }
    }

    public void read(ReadView view) {
        ReadView tag = view.getReadView("SoundQueue");
        offset = tag.getInt("Offset", 0);
        ReadView.ListReadView list = tag.getListReadView("Sources");
        list.forEach(item -> add(item.getInt("Tick", 0), item.read("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN)));
    }

    public void add(int offset, BlockPos localPos) {
        sources.put(offset, localPos);
        min = Math.min(offset, min);
        max = Math.max(offset, max);
    }

    public static boolean isPlayable(BlockState state) {
        if (state.getBlock() instanceof BellBlock)
            return true;
        if (state.getBlock() instanceof NoteBlock)
            return true;
        return state.getBlock() instanceof WhistleBlock;
    }

    public static void play(CarriageContraptionEntity entity, StructureBlockInfo info) {
        if (info == null)
            return;
        BlockState state = info.state();

        if (state.getBlock() instanceof BellBlock) {
            if (state.isOf(AllBlocks.HAUNTED_BELL))
                playSimple(entity, AllSoundEvents.HAUNTED_BELL_USE.getMainEvent(), 1, 1);
            else
                playSimple(entity, SoundEvents.BLOCK_BELL_USE, 1, 1);
        }

        if (state.getBlock() instanceof NoteBlock nb) {
            float f = (float) Math.pow(2, (state.get(NoteBlock.NOTE) - 12) / 12.0);
            playSimple(entity, state.get(NoteBlock.INSTRUMENT).getSound().value(), 1, f);
        }

        if (state.getBlock() instanceof WhistleBlock && info.nbt() != null) {
            int pitch = info.nbt().getInt("Pitch", 0);
            WhistleSize size = state.get(WhistleBlock.SIZE);
            float f = (float) Math.pow(2, ((size == WhistleSize.SMALL ? 12 : 0) - pitch) / 12.0);
            playSimple(entity, (size == WhistleSize.LARGE ? AllSoundEvents.WHISTLE_TRAIN_LOW : AllSoundEvents.WHISTLE_TRAIN).getMainEvent(), 1, f);
            //			playSimple(entity, AllSoundEvents.WHISTLE_CHIFF.getMainEvent(), .75f,
            //				size == WhistleSize.SMALL ? f + .75f : f);
        }

    }

    private static void playSimple(CarriageContraptionEntity entity, SoundEvent event, float volume, float pitch) {
        entity.getEntityWorld().playSoundFromEntity(null, entity, event, SoundCategory.NEUTRAL, 5 * volume, pitch);
    }

}
