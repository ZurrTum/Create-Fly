package com.zurrtum.create.content.trains.schedule;

import com.zurrtum.create.Create;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;

public abstract class ScheduleDataEntry {
    protected Identifier id;
    protected NbtCompound data;

    public ScheduleDataEntry(Identifier id) {
        this.id = id;
        data = new NbtCompound();
    }

    public Identifier getId() {
        return id;
    }

    public NbtCompound getData() {
        return data;
    }

    public void setData(RegistryWrapper.WrapperLookup registries, NbtCompound data) {
        this.data = data;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleDataEntry", Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, registries, data);
            readAdditional(view);
        }
    }

    protected void writeAdditional(WriteView view) {
    }

    protected void readAdditional(ReadView view) {
    }

    public <T> T enumData(String key, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return enumConstants[data.getInt(key, 0) % enumConstants.length];
    }

    protected String textData(String key) {
        return data.getString(key, "");
    }

    public int intData(String key) {
        return data.getInt(key, 0);
    }

}
