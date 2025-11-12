package com.zurrtum.create.client.content.logistics.filter;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.filter.FilterMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FilterScreen extends AbstractFilterScreen<FilterMenu> {

    private static final String PREFIX = "gui.filter.";

    private final Component allowN = CreateLang.translateDirect(PREFIX + "allow_list");
    private final Component allowDESC = CreateLang.translateDirect(PREFIX + "allow_list.description");
    private final Component denyN = CreateLang.translateDirect(PREFIX + "deny_list");
    private final Component denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

    private final Component respectDataN = CreateLang.translateDirect(PREFIX + "respect_data");
    private final Component respectDataDESC = CreateLang.translateDirect(PREFIX + "respect_data.description");
    private final Component ignoreDataN = CreateLang.translateDirect(PREFIX + "ignore_data");
    private final Component ignoreDataDESC = CreateLang.translateDirect(PREFIX + "ignore_data.description");

    private IconButton whitelist, blacklist;
    private IconButton respectNBT, ignoreNBT;

    public FilterScreen(FilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, AllGuiTextures.FILTER);
    }

    public static FilterScreen create(
        Minecraft mc,
        MenuType<ItemStack> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(FilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 5);
        super.init();

        blacklist = new IconButton(leftPos + 18, topPos + 75, AllIcons.I_BLACKLIST);
        blacklist.withCallback(() -> {
            menu.blacklist = true;
            sendOptionUpdate(Option.BLACKLIST);
        });
        blacklist.setToolTip(denyN);
        whitelist = new IconButton(leftPos + 36, topPos + 75, AllIcons.I_WHITELIST);
        whitelist.withCallback(() -> {
            menu.blacklist = false;
            sendOptionUpdate(Option.WHITELIST);
        });
        whitelist.setToolTip(allowN);
        addRenderableWidgets(blacklist, whitelist);

        respectNBT = new IconButton(leftPos + 60, topPos + 75, AllIcons.I_RESPECT_NBT);
        respectNBT.withCallback(() -> {
            menu.respectNBT = true;
            sendOptionUpdate(Option.RESPECT_DATA);
        });
        respectNBT.setToolTip(respectDataN);
        ignoreNBT = new IconButton(leftPos + 78, topPos + 75, AllIcons.I_IGNORE_NBT);
        ignoreNBT.withCallback(() -> {
            menu.respectNBT = false;
            sendOptionUpdate(Option.IGNORE_DATA);
        });
        ignoreNBT.setToolTip(ignoreDataN);
        addRenderableWidgets(respectNBT, ignoreNBT);

        handleIndicators();
    }

    @Override
    protected List<IconButton> getTooltipButtons() {
        return Arrays.asList(blacklist, whitelist, respectNBT, ignoreNBT);
    }

    @Override
    protected List<MutableComponent> getTooltipDescriptions() {
        return Arrays.asList(denyDESC.plainCopy(), allowDESC.plainCopy(), respectDataDESC.plainCopy(), ignoreDataDESC.plainCopy());
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == blacklist)
            return !menu.blacklist;
        if (button == whitelist)
            return menu.blacklist;
        if (button == respectNBT)
            return !menu.respectNBT;
        if (button == ignoreNBT)
            return menu.respectNBT;
        return true;
    }

    @Override
    protected int getTitleColor() {
        return 0xFF303030;
    }
}
