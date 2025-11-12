package com.zurrtum.create.client.content.logistics.packagePort;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.trains.station.NoShadowFontWrapper;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortMenu;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.PackagePortConfigurationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PackagePortScreen extends AbstractSimiContainerScreen<PackagePortMenu> {

    private final boolean frogMode;
    private final AllGuiTextures background;

    private EditBox addressBox;
    private IconButton confirmButton;
    private IconButton dontAcceptPackages;
    private IconButton acceptPackages;
    private ElementWidget renderedItem;

    private final ItemStack icon;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public PackagePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        background = AllGuiTextures.FROGPORT_BG;
        frogMode = container.contentHolder instanceof FrogportBlockEntity;
        icon = new ItemStack(container.contentHolder.getBlockState().getBlock().asItem());
    }

    public static PackagePortScreen create(
        Minecraft mc,
        MenuType<PackagePortBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(PackagePortScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();

        Consumer<String> onTextChanged;
        onTextChanged = s -> addressBox.setX(nameBoxX(s, addressBox));
        addressBox = new EditBox(new NoShadowFontWrapper(font), leftPos + 23, topPos - 11, background.getWidth() - 20, 10, Component.empty());
        addressBox.setBordered(false);
        addressBox.setMaxLength(25);
        addressBox.setTextColor(0xFF3D3C48);
        addressBox.setValue(menu.contentHolder.addressFilter);
        addressBox.setFocused(false);
        addressBox.mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
        addressBox.setResponder(onTextChanged);
        addressBox.setX(nameBoxX(addressBox.getValue(), addressBox));
        addRenderableWidget(addressBox);

        confirmButton = new IconButton(leftPos + background.getWidth() - 33, topPos + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);

        acceptPackages = new IconButton(leftPos + 37, topPos + background.getHeight() - 24, AllIcons.I_SEND_AND_RECEIVE);
        acceptPackages.withCallback(() -> {
            acceptPackages.green = true;
            dontAcceptPackages.green = false;
        });
        acceptPackages.green = menu.contentHolder.acceptsPackages;
        acceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_and_receive"));
        addRenderableWidget(acceptPackages);

        dontAcceptPackages = new IconButton(leftPos + 37 + 18, topPos + background.getHeight() - 24, AllIcons.I_SEND_ONLY);
        dontAcceptPackages.withCallback(() -> {
            acceptPackages.green = false;
            dontAcceptPackages.green = true;
        });
        dontAcceptPackages.green = !menu.contentHolder.acceptsPackages;
        dontAcceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_only"));
        addRenderableWidget(dontAcceptPackages);

        containerTick();

        extraAreas = ImmutableList.of(new Rect2i(leftPos + background.getWidth(), topPos + background.getHeight() - 50, 70, 60));

        renderedItem = new ElementWidget(leftPos + background.getWidth() + 6, topPos + background.getHeight() - 56).showingElement(GuiGameElement.of(
            icon).scale(4));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    private int nameBoxX(String s, EditBox nameBox) {
        return leftPos + background.getWidth() / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
    }

    @Override
    protected void containerTick() {
        acceptPackages.visible = menu.contentHolder.target != null;
        dontAcceptPackages.visible = menu.contentHolder.target != null;
        super.containerTick();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures header = frogMode ? AllGuiTextures.FROGPORT_HEADER : AllGuiTextures.POSTBOX_HEADER;
        int x = this.leftPos;
        int y = this.topPos;
        header.render(graphics, x, y - header.getHeight());
        background.render(graphics, x, y);

        String text = addressBox.getValue();
        if (!addressBox.isFocused()) {
            if (text.isEmpty()) {
                text = icon.getHoverName().getString();
                graphics.drawString(font, text, nameBoxX(text, addressBox), y - 11, 0xFF3D3C48, false);
            }
            AllGuiTextures.FROGPORT_EDIT_NAME.render(graphics, nameBoxX(text, addressBox) + font.width(text) + 5, y - 14);
        }

        int invX = x + 30;
        int invY = y + 8 + imageHeight - AllGuiTextures.PLAYER_INVENTORY.getHeight();
        renderPlayerInventory(graphics, invX, invY);

        if (menu.contentHolder.target == null)
            return;

        x += 13;
        y += 58;
        AllGuiTextures.FROGPORT_SLOT.render(graphics, x, y);
        graphics.renderItem(menu.contentHolder.target.getIcon(), x + 1, y + 1);

        if (addressBox.isHovered()) {
            graphics.setComponentTooltipForNextFrame(
                font, List.of(
                    CreateLang.translate("gui.package_port.catch_packages").color(AbstractSimiWidget.HEADER_RGB).component(),
                    CreateLang.translate("gui.package_port.catch_packages_empty").style(ChatFormatting.GRAY).component(),
                    CreateLang.translate("gui.package_port.catch_packages_wildcard").style(ChatFormatting.GRAY).component()
                ), pMouseX, pMouseY
            );
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int pKeyCode = input.key();
        boolean hitEnter = getFocused() instanceof EditBox && (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER);

        if (hitEnter && addressBox.isFocused()) {
            addressBox.setFocused(false);
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void removed() {
        minecraft.player.connection.send(new PackagePortConfigurationPacket(
            menu.contentHolder.getBlockPos(),
            addressBox.getValue(),
            acceptPackages.green
        ));
        super.removed();
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}