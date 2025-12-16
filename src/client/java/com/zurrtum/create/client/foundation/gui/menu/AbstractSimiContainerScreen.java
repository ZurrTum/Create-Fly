package com.zurrtum.create.client.foundation.gui.menu;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.TickableGuiEventListener;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSimiContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected int windowXOffset, windowYOffset;

    public AbstractSimiContainerScreen(T container, Inventory inv, Component title, int imageWidth, int imageHeight) {
        super(container, inv, title, imageWidth, imageHeight);
    }

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        super.init();
        leftPos += windowXOffset;
        topPos += windowYOffset;
    }

    @Override
    protected void containerTick() {
        for (GuiEventListener listener : children()) {
            if (listener instanceof TickableGuiEventListener tickable) {
                tickable.tick();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(W... widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    protected void removeWidgets(GuiEventListener... widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

    protected void removeWidgets(Collection<? extends GuiEventListener> widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

	/*@Override
	public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pGuiGraphics, pMouseX, pMouseY));
		renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
	}*/

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(minecraft.getDeltaTracker());

        super.render(graphics, mouseX, mouseY, partialTicks);

        renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // no-op to prevent screen- and inventory-title from being rendered at incorrect
        // location
        // could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper
        // values instead
    }

    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderTooltip(graphics, mouseX, mouseY);
        for (Renderable widget : renderables) {
            if (widget instanceof AbstractSimiWidget simiWidget && simiWidget.isMouseOver(mouseX, mouseY)) {
                List<Component> tooltip = simiWidget.getToolTip();
                if (tooltip.isEmpty())
                    continue;
                int ttx = simiWidget.lockedTooltipX == -1 ? mouseX : simiWidget.lockedTooltipX + simiWidget.getX();
                int tty = simiWidget.lockedTooltipY == -1 ? mouseY : simiWidget.lockedTooltipY + simiWidget.getY();
                graphics.setComponentTooltipForNextFrame(font, tooltip, ttx, tty);
            }
        }
    }

    public int getLeftOfCentered(int textureWidth) {
        return leftPos - windowXOffset + (imageWidth - textureWidth) / 2;
    }

    public void renderPlayerInventory(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.PLAYER_INVENTORY.render(graphics, x, y);
        graphics.drawString(font, playerInventoryTitle, x + 8, y + 6, 0xFF404040, false);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (getFocused() instanceof EditBox && input.key() != GLFW.GLFW_KEY_ESCAPE)
            return getFocused().keyPressed(input);
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (getFocused() != null && !getFocused().isMouseOver(click.x(), click.y()))
            setFocused(null);
        return super.mouseClicked(click, doubled);
    }

    @Override
    public GuiEventListener getFocused() {
        GuiEventListener focused = super.getFocused();
        if (focused instanceof AbstractWidget && !focused.isFocused())
            focused = null;
        setFocused(focused);
        return focused;
    }

    /**
     * Used for moving JEI out of the way of extra things like block renders.
     *
     * @return the space that the GUI takes up outside the normal rectangle defined
     * by {@link ContainerScreen}.
     */
    public List<Rect2i> getExtraAreas() {
        return Collections.emptyList();
    }

    @Deprecated
    protected void debugWindowArea(GuiGraphics graphics) {
        graphics.fill(leftPos + imageWidth, topPos + imageHeight, leftPos, topPos, 0xD3D3D3D3);
    }

    @Deprecated
    protected void debugExtraAreas(GuiGraphics graphics) {
        for (Rect2i area : getExtraAreas()) {
            graphics.fill(area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(), 0xD3D3D3D3);
        }
    }

    protected void playUiSound(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume * 0.25f));
    }

    public static ItemStack getStack(RegistryFriendlyByteBuf extraData) {
        return ItemStack.STREAM_CODEC.decode(extraData);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T getBlockEntity(Minecraft mc, RegistryFriendlyByteBuf extraData) {
        ClientLevel world = mc.level;
        if (world == null) {
            return null;
        }
        return (T) world.getBlockEntity(extraData.readBlockPos());
    }
}
