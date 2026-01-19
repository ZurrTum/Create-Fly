package com.zurrtum.create.catnip.nbt;

import com.zurrtum.create.catnip.codecs.CatnipCodecUtils;
import com.zurrtum.create.catnip.components.ComponentProcessors;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class NBTProcessors {

    private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> processors = new HashMap<>();
    private static final Map<BlockEntityType<?>, UnaryOperator<CompoundTag>> survivalProcessors = new HashMap<>();

    public static synchronized void addProcessor(BlockEntityType<?> type, UnaryOperator<CompoundTag> processor) {
        processors.put(type, processor);
    }

    public static synchronized void addSurvivalProcessor(BlockEntityType<?> type, UnaryOperator<CompoundTag> processor) {
        survivalProcessors.put(type, processor);
    }

    // Triggered by block tag, not BE type
    private static final UnaryOperator<CompoundTag> signProcessor = data -> {
        for (String key : List.of("front_text", "back_text")) {
            SignText text = data.getCompound(key).flatMap(k -> CatnipCodecUtils.decode(SignText.DIRECT_CODEC, k)).orElse(null);
            if (text != null) {
                for (Component component : text.getMessages(false)) {
                    if (textComponentHasClickEvent(component))
                        return null;
                }
            }
        }
        if (data.contains("front_item") || data.contains("back_item"))
            return null; // "Amendments" compat: sign data contains itemstacks
        return data;
    };

    public static UnaryOperator<CompoundTag> itemProcessor(String tagKey) {
        return data -> {
            CompoundTag compound = data.getCompoundOrEmpty(tagKey);
            if (!compound.contains("components"))
                return data;
            CompoundTag itemComponents = compound.getCompoundOrEmpty("components");
            HashSet<String> keys = new HashSet<>(itemComponents.keySet());
            for (String key : keys) {
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse(key));
                if (type != null && ComponentProcessors.isUnsafeItemComponent(type))
                    itemComponents.remove(key);
            }
            if (itemComponents.isEmpty())
                compound.remove("components");
            return data;
        };
    }

    public static boolean textComponentHasClickEvent(Component component) {
        for (Component sibling : component.getSiblings()) {
            if (textComponentHasClickEvent(sibling)) {
                return true;
            }
        }
        return component.getStyle().getClickEvent() != null;
    }

    private NBTProcessors() {
    }

    @Nullable
    public static CompoundTag process(BlockState state, BlockEntity blockEntity, @Nullable CompoundTag compound, boolean survival) {
        if (compound == null)
            return null;
        BlockEntityType<?> type = blockEntity.getType();
        if (survival && survivalProcessors.containsKey(type))
            compound = survivalProcessors.get(type).apply(compound);
        if (compound != null && processors.containsKey(type))
            return processors.get(type).apply(compound);
        if (blockEntity instanceof SpawnerBlockEntity)
            return compound;
        if (state.is(BlockTags.ALL_SIGNS))
            return signProcessor.apply(compound);
        return compound;
    }

}
