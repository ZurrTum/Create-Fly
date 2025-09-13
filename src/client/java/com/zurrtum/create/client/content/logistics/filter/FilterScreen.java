package com.zurrtum.create.client.content.logistics.filter;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.filter.FilterMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class FilterScreen extends AbstractFilterScreen<FilterMenu> {

    private static final String PREFIX = "gui.filter.";

    private final Text allowN = CreateLang.translateDirect(PREFIX + "allow_list");
    private final Text allowDESC = CreateLang.translateDirect(PREFIX + "allow_list.description");
    private final Text denyN = CreateLang.translateDirect(PREFIX + "deny_list");
    private final Text denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

    private final Text respectDataN = CreateLang.translateDirect(PREFIX + "respect_data");
    private final Text respectDataDESC = CreateLang.translateDirect(PREFIX + "respect_data.description");
    private final Text ignoreDataN = CreateLang.translateDirect(PREFIX + "ignore_data");
    private final Text ignoreDataDESC = CreateLang.translateDirect(PREFIX + "ignore_data.description");

    private IconButton whitelist, blacklist;
    private IconButton respectNBT, ignoreNBT;

    public FilterScreen(FilterMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title, AllGuiTextures.FILTER);
    }

    public static FilterScreen create(
        MinecraftClient mc,
        MenuType<ItemStack> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(FilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 5);
        super.init();

        blacklist = new IconButton(x + 18, y + 75, AllIcons.I_BLACKLIST);
        blacklist.withCallback(() -> {
            handler.blacklist = true;
            sendOptionUpdate(Option.BLACKLIST);
        });
        blacklist.setToolTip(denyN);
        whitelist = new IconButton(x + 36, y + 75, AllIcons.I_WHITELIST);
        whitelist.withCallback(() -> {
            handler.blacklist = false;
            sendOptionUpdate(Option.WHITELIST);
        });
        whitelist.setToolTip(allowN);
        addRenderableWidgets(blacklist, whitelist);

        respectNBT = new IconButton(x + 60, y + 75, AllIcons.I_RESPECT_NBT);
        respectNBT.withCallback(() -> {
            handler.respectNBT = true;
            sendOptionUpdate(Option.RESPECT_DATA);
        });
        respectNBT.setToolTip(respectDataN);
        ignoreNBT = new IconButton(x + 78, y + 75, AllIcons.I_IGNORE_NBT);
        ignoreNBT.withCallback(() -> {
            handler.respectNBT = false;
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
    protected List<MutableText> getTooltipDescriptions() {
        return Arrays.asList(
            denyDESC.copyContentOnly(),
            allowDESC.copyContentOnly(),
            respectDataDESC.copyContentOnly(),
            ignoreDataDESC.copyContentOnly()
        );
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == blacklist)
            return !handler.blacklist;
        if (button == whitelist)
            return handler.blacklist;
        if (button == respectNBT)
            return !handler.respectNBT;
        if (button == ignoreNBT)
            return handler.respectNBT;
        return true;
    }

}
