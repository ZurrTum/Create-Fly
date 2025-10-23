package com.zurrtum.create.client.content.logistics.filter;

import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.logistics.filter.PackageFilterMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PackageFilterScreen extends AbstractFilterScreen<PackageFilterMenu> {

    private AddressEditBox addressBox;
    private boolean deferFocus;

    public PackageFilterScreen(PackageFilterMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title, AllGuiTextures.PACKAGE_FILTER);
    }

    public static PackageFilterScreen create(
        MinecraftClient mc,
        MenuType<ItemStack> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(PackageFilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
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

        addressBox = new AddressEditBox(this, textRenderer, x + 44, y + 28, 129, 9, false);
        addressBox.setEditableColor(0xffffffff);
        addressBox.setText(handler.address);
        addressBox.setChangedListener(this::onAddressEdited);
        addDrawableChild(addressBox);

        setFocused(addressBox);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawItem(PackageStyles.getDefaultBox(), x + 16, y + 23);
    }

    public void onAddressEdited(String s) {
        handler.address = s;
        NbtCompound tag = new NbtCompound();
        tag.putString("Address", s);
        client.player.networkHandler.sendPacket(new FilterScreenPacket(Option.UPDATE_ADDRESS, tag));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER)
            setFocused(null);
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    protected void contentsCleared() {
        addressBox.setText("");
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