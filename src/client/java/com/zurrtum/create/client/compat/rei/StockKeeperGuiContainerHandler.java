package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public class StockKeeperGuiContainerHandler implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {
        if (screen instanceof StockKeeperRequestScreen stockKeeperRequestScreen) {
            ItemStack stack = stockKeeperRequestScreen.getHoveredItemStack(mouse.x, mouse.y);
            if (stack != null) {
                return CompoundEventResult.interruptTrue(EntryStacks.of(stack));
            }
        }
        return CompoundEventResult.pass();
    }
}
