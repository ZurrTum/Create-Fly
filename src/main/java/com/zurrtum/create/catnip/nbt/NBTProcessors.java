package com.zurrtum.create.catnip.nbt;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.catnip.components.ComponentProcessors;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class NBTProcessors {

    private static final Map<BlockEntityType<?>, UnaryOperator<NbtCompound>> processors = new HashMap<>();
    private static final Map<BlockEntityType<?>, UnaryOperator<NbtCompound>> survivalProcessors = new HashMap<>();

    public static synchronized void addProcessor(BlockEntityType<?> type, UnaryOperator<NbtCompound> processor) {
        processors.put(type, processor);
    }

    public static synchronized void addSurvivalProcessor(BlockEntityType<?> type, UnaryOperator<NbtCompound> processor) {
        survivalProcessors.put(type, processor);
    }

    // Triggered by block tag, not BE type
    private static final UnaryOperator<NbtCompound> signProcessor = data -> {
        for (String key : List.of("front_text", "back_text")) {
            NbtCompound textTag = data.getCompoundOrEmpty(key);
            if (!textTag.contains("messages"))
                continue;
            for (NbtElement tag : textTag.getListOrEmpty("messages"))
                if (tag instanceof NbtString stringTag)
                    if (textComponentHasClickEvent(stringTag.asString().orElse("")))
                        return null;
        }
        if (data.contains("front_item") || data.contains("back_item"))
            return null; // "Amendments" compat: sign data contains itemstacks
        return data;
    };

    // TODO - Checkover and test
    public static UnaryOperator<NbtCompound> itemProcessor(String tagKey) {
        return data -> {
            NbtCompound compound = data.getCompoundOrEmpty(tagKey);
            if (!compound.contains("components"))
                return data;
            NbtCompound itemComponents = compound.getCompoundOrEmpty("components");
            HashSet<String> keys = new HashSet<>(itemComponents.getKeys());
            for (String key : keys) {
                ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(Identifier.of(key));
                if (type != null && ComponentProcessors.isUnsafeItemComponent(type))
                    itemComponents.remove(key);
            }
            if (itemComponents.isEmpty())
                compound.remove("components");
            return data;
        };
    }

    public static boolean textComponentHasClickEvent(String string) {
        JsonElement json = JsonParser.parseString(string.isEmpty() ? "\"\"" : string);
        Text text = TextCodecs.CODEC.parse(DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
        return textComponentHasClickEvent(text);
    }

    public static boolean textComponentHasClickEvent(Text component) {
        for (Text sibling : component.getSiblings())
            if (textComponentHasClickEvent(sibling))
                return true;
        return component.getStyle() != null && component.getStyle().getClickEvent() != null;
    }

    private NBTProcessors() {
    }

    @Nullable
    public static NbtCompound process(BlockState state, BlockEntity blockEntity, @Nullable NbtCompound compound, boolean survival) {
        if (compound == null)
            return null;
        BlockEntityType<?> type = blockEntity.getType();
        if (survival && survivalProcessors.containsKey(type))
            compound = survivalProcessors.get(type).apply(compound);
        if (compound != null && processors.containsKey(type))
            return processors.get(type).apply(compound);
        if (blockEntity instanceof MobSpawnerBlockEntity)
            return compound;
        if (state.isIn(BlockTags.ALL_SIGNS))
            return signProcessor.apply(compound);
        return compound;
    }

}
