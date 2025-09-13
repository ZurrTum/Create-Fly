package com.zurrtum.create.content.trains.schedule.condition;

import com.mojang.serialization.Codec;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RedstoneLinkCondition extends ScheduleWaitCondition {
    private static final Codec<Couple<Frequency>> FREQUENCY_CODEC = Couple.codec(Frequency.CODEC);

    public Couple<Frequency> freq;

    public RedstoneLinkCondition(Identifier id) {
        super(id);
        freq = Couple.create(() -> Frequency.EMPTY);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        int lastChecked = context.contains("LastChecked") ? context.getInt("LastChecked", 0) : -1;
        int status = Create.REDSTONE_LINK_NETWORK_HANDLER.globalPowerVersion.get();
        if (status == lastChecked)
            return false;
        context.putInt("LastChecked", status);
        return Create.REDSTONE_LINK_NETWORK_HANDLER.hasAnyLoadedPower(freq) != lowActivation();
    }

    @Override
    protected void writeAdditional(WriteView view) {
        view.put("Frequency", FREQUENCY_CODEC, freq);
    }

    public boolean lowActivation() {
        return intData("Inverted") == 1;
    }

    @Override
    protected void readAdditional(ReadView view) {
        view.read("Frequency", FREQUENCY_CODEC).ifPresent(freq -> this.freq = freq);
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        return Text.translatable("create.schedule.condition.redstone_link.status");
    }
}
