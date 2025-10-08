package com.zurrtum.create.client.content.logistics.packagePort;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PackagePortScreen extends AbstractSimiContainerScreen<PackagePortMenu> {

    private final boolean frogMode;
    private final AllGuiTextures background;

    private TextFieldWidget addressBox;
    private IconButton confirmButton;
    private IconButton dontAcceptPackages;
    private IconButton acceptPackages;
    private ElementWidget renderedItem;

    private final ItemStack icon;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public PackagePortScreen(PackagePortMenu container, PlayerInventory inv, Text title) {
        super(container, inv, title);
        background = AllGuiTextures.FROGPORT_BG;
        frogMode = container.contentHolder instanceof FrogportBlockEntity;
        icon = new ItemStack(container.contentHolder.getCachedState().getBlock().asItem());
    }

    public static PackagePortScreen create(
        MinecraftClient mc,
        MenuType<PackagePortBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(PackagePortScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearChildren();

        Consumer<String> onTextChanged;
        onTextChanged = s -> addressBox.setX(nameBoxX(s, addressBox));
        addressBox = new TextFieldWidget(new NoShadowFontWrapper(textRenderer), x + 23, y - 11, background.getWidth() - 20, 10, Text.empty());
        addressBox.setDrawsBackground(false);
        addressBox.setMaxLength(25);
        addressBox.setEditableColor(0xFF3D3C48);
        addressBox.setText(handler.contentHolder.addressFilter);
        addressBox.setFocused(false);
        addressBox.mouseClicked(new Click(0, 0, new MouseInput(0, 0)), false);
        addressBox.setChangedListener(onTextChanged);
        addressBox.setX(nameBoxX(addressBox.getText(), addressBox));
        addDrawableChild(addressBox);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.player.closeHandledScreen());
        addDrawableChild(confirmButton);

        acceptPackages = new IconButton(x + 37, y + background.getHeight() - 24, AllIcons.I_SEND_AND_RECEIVE);
        acceptPackages.withCallback(() -> {
            acceptPackages.green = true;
            dontAcceptPackages.green = false;
        });
        acceptPackages.green = handler.contentHolder.acceptsPackages;
        acceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_and_receive"));
        addDrawableChild(acceptPackages);

        dontAcceptPackages = new IconButton(x + 37 + 18, y + background.getHeight() - 24, AllIcons.I_SEND_ONLY);
        dontAcceptPackages.withCallback(() -> {
            acceptPackages.green = false;
            dontAcceptPackages.green = true;
        });
        dontAcceptPackages.green = !handler.contentHolder.acceptsPackages;
        dontAcceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_only"));
        addDrawableChild(dontAcceptPackages);

        handledScreenTick();

        extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 50, 70, 60));

        renderedItem = new ElementWidget(x + background.getWidth() + 6, y + background.getHeight() - 56).showingElement(GuiGameElement.of(icon)
            .scale(4));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    private int nameBoxX(String s, TextFieldWidget nameBox) {
        return x + background.getWidth() / 2 - (Math.min(textRenderer.getWidth(s), nameBox.getWidth()) + 10) / 2;
    }

    @Override
    protected void handledScreenTick() {
        acceptPackages.visible = handler.contentHolder.target != null;
        dontAcceptPackages.visible = handler.contentHolder.target != null;
        super.handledScreenTick();
    }

    @Override
    protected void drawBackground(DrawContext graphics, float pPartialTick, int pMouseX, int pMouseY) {
        AllGuiTextures header = frogMode ? AllGuiTextures.FROGPORT_HEADER : AllGuiTextures.POSTBOX_HEADER;
        int x = this.x;
        int y = this.y;
        header.render(graphics, x, y - header.getHeight());
        background.render(graphics, x, y);

        String text = addressBox.getText();
        if (!addressBox.isFocused()) {
            if (text.isEmpty()) {
                text = icon.getName().getString();
                graphics.drawText(textRenderer, text, nameBoxX(text, addressBox), y - 11, 0xFF3D3C48, false);
            }
            AllGuiTextures.FROGPORT_EDIT_NAME.render(graphics, nameBoxX(text, addressBox) + textRenderer.getWidth(text) + 5, y - 14);
        }

        int invX = x + 30;
        int invY = y + 8 + backgroundHeight - AllGuiTextures.PLAYER_INVENTORY.getHeight();
        renderPlayerInventory(graphics, invX, invY);

        if (handler.contentHolder.target == null)
            return;

        x += 13;
        y += 58;
        AllGuiTextures.FROGPORT_SLOT.render(graphics, x, y);
        graphics.drawItem(handler.contentHolder.target.getIcon(), x + 1, y + 1);

        if (addressBox.isHovered()) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.package_port.catch_packages").color(AbstractSimiWidget.HEADER_RGB).component(),
                    CreateLang.translate("gui.package_port.catch_packages_empty").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.package_port.catch_packages_wildcard").style(Formatting.GRAY).component()
                ), pMouseX, pMouseY
            );
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int pKeyCode = input.key();
        boolean hitEnter = getFocused() instanceof TextFieldWidget && (pKeyCode == InputUtil.GLFW_KEY_ENTER || pKeyCode == InputUtil.GLFW_KEY_KP_ENTER);

        if (hitEnter && addressBox.isFocused()) {
            addressBox.setFocused(false);
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void removed() {
        client.player.networkHandler.sendPacket(new PackagePortConfigurationPacket(
            handler.contentHolder.getPos(),
            addressBox.getText(),
            acceptPackages.green
        ));
        super.removed();
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}