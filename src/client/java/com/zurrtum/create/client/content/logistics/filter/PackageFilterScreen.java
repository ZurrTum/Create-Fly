package com.zurrtum.create.client.content.logistics.filter;

import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.logistics.filter.PackageFilterMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class PackageFilterScreen extends AbstractFilterScreen<PackageFilterMenu> {

    private AddressEditBox addressBox;
    private boolean deferFocus;

    public PackageFilterScreen(PackageFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, AllGuiTextures.PACKAGE_FILTER);
    }

    @Nullable
    public static PackageFilterScreen create(
        Minecraft mc,
        MenuType<ItemStack> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(PackageFilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (deferFocus) {
            deferFocus = false;
            setFocused(addressBox);
        }
        addressBox.tick();
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        super.init();

        addressBox = new AddressEditBox(this, font, leftPos + 44, topPos + 28, 129, 9, false);
        addressBox.setTextColor(0xffffffff);
        addressBox.setValue(menu.address);
        addressBox.setResponder(this::onAddressEdited);
        addRenderableWidget(addressBox);

        setFocused(addressBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.renderItem(PackageStyles.getDefaultBox(), leftPos + 16, topPos + 23);
    }

    public void onAddressEdited(String s) {
        menu.address = s;
        CompoundTag tag = new CompoundTag();
        tag.putString("Address", s);
        minecraft.player.connection.send(new FilterScreenPacket(Option.UPDATE_ADDRESS, tag));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ENTER)
            setFocused(null);
        return super.keyPressed(input);
    }

    @Override
    protected void contentsCleared() {
        addressBox.setValue("");
        deferFocus = true;
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        return false;
    }

    @Override
    protected int getTitleColor() {
        return 0xFF3D3C48;
    }
}