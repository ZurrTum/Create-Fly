package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.world.level.block.Block;

public class DisplayLinkBlockItem extends ClickToLinkBlockItem {

    public DisplayLinkBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public int getMaxDistanceFromSelection() {
        return AllConfigs.server().logistics.displayLinkRange.get();
    }

    @Override
    public String getMessageTranslationKey() {
        return "display_link";
    }

}
