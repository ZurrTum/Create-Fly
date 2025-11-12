package com.zurrtum.create.content.trains.schedule.condition;

import com.mojang.serialization.Codec;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RedstoneLinkCondition extends ScheduleWaitCondition {
    private static final Codec<Couple<Frequency>> FREQUENCY_CODEC = Couple.codec(Frequency.CODEC);

    public Couple<Frequency> freq;

    public RedstoneLinkCondition(Identifier id) {
        super(id);
        freq = Couple.create(() -> Frequency.EMPTY);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        int lastChecked = context.contains("LastChecked") ? context.getIntOr("LastChecked", 0) : -1;
        int status = Create.REDSTONE_LINK_NETWORK_HANDLER.globalPowerVersion.get();
        if (status == lastChecked)
            return false;
        context.putInt("LastChecked", status);
        return Create.REDSTONE_LINK_NETWORK_HANDLER.hasAnyLoadedPower(freq) != lowActivation();
    }

    @Override
    protected void writeAdditional(ValueOutput view) {
        view.store("Frequency", FREQUENCY_CODEC, freq);
    }

    public boolean lowActivation() {
        return intData("Inverted") == 1;
    }

    @Override
    protected void readAdditional(ValueInput view) {
        view.read("Frequency", FREQUENCY_CODEC).ifPresent(freq -> this.freq = freq);
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
        return Component.translatable("create.schedule.condition.redstone_link.status");
    }
}
