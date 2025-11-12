package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.Create;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class ScheduleDataEntry {
    protected Identifier id;
    protected CompoundTag data;

    public ScheduleDataEntry(Identifier id) {
        this.id = id;
        data = new CompoundTag();
    }

    public Identifier getId() {
        return id;
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(HolderLookup.Provider registries, CompoundTag data) {
        this.data = data;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleDataEntry", Create.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, registries, data);
            readAdditional(view);
        }
    }

    protected void writeAdditional(ValueOutput view) {
    }

    protected void readAdditional(ValueInput view) {
    }

    public <T> T enumData(String key, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return enumConstants[data.getIntOr(key, 0) % enumConstants.length];
    }

    protected String textData(String key) {
        return data.getStringOr(key, "");
    }

    public int intData(String key) {
        return data.getIntOr(key, 0);
    }

}
