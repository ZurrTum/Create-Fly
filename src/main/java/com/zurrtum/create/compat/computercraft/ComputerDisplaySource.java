package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ComputerDisplaySource extends DisplaySource {

	@Override
	public List<MutableText> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		List<MutableText> components = new ArrayList<>();
        NbtList tag = context.sourceConfig().getList("ComputerSourceList").orElse(new NbtList());

		for (int i = 0; i < tag.size(); i++) {
			components.add(Text.literal(tag.getString(i).get()));
		}

		return components;
	}

	@Override
	public boolean shouldPassiveReset() {
		return false;
	}

}
