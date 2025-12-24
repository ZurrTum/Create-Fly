package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class ComputerDisplaySource extends DisplaySource {

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        List<MutableComponent> components = new ArrayList<>();
        ListTag tag = context.sourceConfig().getList("ComputerSourceList").orElseGet(ListTag::new);

        for (int i = 0; i < tag.size(); i++) {
            components.add(Component.literal(tag.getString(i).get()));
        }

        return components;
    }

    @Override
    public boolean shouldPassiveReset() {
        return false;
    }

}
