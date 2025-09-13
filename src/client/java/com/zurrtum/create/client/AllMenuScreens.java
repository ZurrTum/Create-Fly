package com.zurrtum.create.client;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintScreen;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxScreen;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.zurrtum.create.client.content.logistics.filter.AttributeFilterScreen;
import com.zurrtum.create.client.content.logistics.filter.FilterScreen;
import com.zurrtum.create.client.content.logistics.filter.PackageFilterScreen;
import com.zurrtum.create.client.content.logistics.packagePort.PackagePortScreen;
import com.zurrtum.create.client.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperCategoryScreen;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerScreen;
import com.zurrtum.create.client.content.schematics.cannon.SchematicannonScreen;
import com.zurrtum.create.client.content.schematics.table.SchematicTableScreen;
import com.zurrtum.create.client.content.trains.schedule.ScheduleScreen;
import com.zurrtum.create.client.foundation.gui.menu.ScreenFactory;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllMenuScreens {
    public static final Map<MenuType<?>, ScreenFactory<?, ?, ?>> ALL = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>, H> void open(
        MinecraftClient client,
        MenuType<H> type,
        int id,
        Text name,
        RegistryByteBuf extraData
    ) {
        PlayerInventory inventory = client.player.getInventory();
        ScreenFactory<T, U, H> factory = (ScreenFactory<T, U, H>) ALL.get(type);
        if (factory != null) {
            U screen = factory.create(client, type, id, inventory, name, extraData);
            if (screen != null) {
                client.player.currentScreenHandler = screen.getScreenHandler();
                client.setScreen(screen);
                return;
            }
        }
        Create.LOGGER.warn("Failed to create screen");
    }

    public static <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>, H> void register(
        MenuType<H> type,
        ScreenFactory<T, U, H> factory
    ) {
        ALL.put(type, factory);
    }

    public static void register() {
        register(AllMenuTypes.SCHEDULE, ScheduleScreen::create);
        register(AllMenuTypes.LINKED_CONTROLLER, LinkedControllerScreen::create);
        register(AllMenuTypes.FILTER, FilterScreen::create);
        register(AllMenuTypes.ATTRIBUTE_FILTER, AttributeFilterScreen::create);
        register(AllMenuTypes.PACKAGE_FILTER, PackageFilterScreen::create);
        register(AllMenuTypes.REDSTONE_REQUESTER, RedstoneRequesterScreen::create);
        register(AllMenuTypes.STOCK_KEEPER_CATEGORY, StockKeeperCategoryScreen::create);
        register(AllMenuTypes.STOCK_KEEPER_REQUEST, StockKeeperRequestScreen::create);
        register(AllMenuTypes.PACKAGE_PORT, PackagePortScreen::create);
        register(AllMenuTypes.FACTORY_PANEL_SET_ITEM, FactoryPanelSetItemScreen::create);
        register(AllMenuTypes.CRAFTING_BLUEPRINT, BlueprintScreen::create);
        register(AllMenuTypes.TOOLBOX, ToolboxScreen::create);
        register(AllMenuTypes.SCHEMATIC_TABLE, SchematicTableScreen::create);
        register(AllMenuTypes.SCHEMATICANNON, SchematicannonScreen::create);
    }
}
