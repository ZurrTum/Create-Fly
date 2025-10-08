package com.zurrtum.create.client.foundation.gui.menu;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.TickableGuiEventListener;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSimiContainerScreen<T extends ScreenHandler> extends HandledScreen<T> {
    public static GpuBufferSlice projectionMatrixBuffer;
    public static ProjectionType projectionType;

    public static void backupProjectionMatrix() {
        projectionMatrixBuffer = RenderSystem.getProjectionMatrixBuffer();
        projectionType = RenderSystem.getProjectionType();
    }

    public static void restoreProjectionMatrix() {
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer, projectionType);
    }

    protected int windowXOffset, windowYOffset;

    public AbstractSimiContainerScreen(T container, PlayerInventory inv, Text title) {
        super(container, inv, title);
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowSize(int width, int height) {
        backgroundWidth = width;
        backgroundHeight = height;
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
        x += windowXOffset;
        y += windowYOffset;
    }

    @Override
    protected void handledScreenTick() {
        for (Element listener : children()) {
            if (listener instanceof TickableGuiEventListener tickable) {
                tickable.tick();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <W extends Element & Drawable & Selectable> void addRenderableWidgets(W... widgets) {
        for (W widget : widgets) {
            addDrawableChild(widget);
        }
    }

    protected <W extends Element & Drawable & Selectable> void addRenderableWidgets(Collection<W> widgets) {
        for (W widget : widgets) {
            addDrawableChild(widget);
        }
    }

    protected void removeWidgets(Element... widgets) {
        for (Element widget : widgets) {
            remove(widget);
        }
    }

    protected void removeWidgets(Collection<? extends Element> widgets) {
        for (Element widget : widgets) {
            remove(widget);
        }
    }

	/*@Override
	public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pGuiGraphics, pMouseX, pMouseY));
		renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
	}*/

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());

        super.render(graphics, mouseX, mouseY, partialTicks);

        renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawForeground(DrawContext graphics, int mouseX, int mouseY) {
        // no-op to prevent screen- and inventory-title from being rendered at incorrect
        // location
        // could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper
        // values instead
    }

    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        drawMouseoverTooltip(graphics, mouseX, mouseY);
        for (Drawable widget : drawables) {
            if (widget instanceof AbstractSimiWidget simiWidget && simiWidget.isMouseOver(mouseX, mouseY)) {
                List<Text> tooltip = simiWidget.getToolTip();
                if (tooltip.isEmpty())
                    continue;
                int ttx = simiWidget.lockedTooltipX == -1 ? mouseX : simiWidget.lockedTooltipX + simiWidget.getX();
                int tty = simiWidget.lockedTooltipY == -1 ? mouseY : simiWidget.lockedTooltipY + simiWidget.getY();
                graphics.drawTooltip(textRenderer, tooltip, ttx, tty);
            }
        }
    }

    public int getLeftOfCentered(int textureWidth) {
        return x - windowXOffset + (backgroundWidth - textureWidth) / 2;
    }

    public void renderPlayerInventory(DrawContext graphics, int x, int y) {
        AllGuiTextures.PLAYER_INVENTORY.render(graphics, x, y);
        graphics.drawText(textRenderer, playerInventoryTitle, x + 8, y + 6, 0xFF404040, false);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (getFocused() instanceof TextFieldWidget && pKeyCode != GLFW.GLFW_KEY_ESCAPE)
            return getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (getFocused() != null && !getFocused().isMouseOver(click.x(), click.y()))
            setFocused(null);
        return super.mouseClicked(click, doubled);
    }

    @Override
    public Element getFocused() {
        Element focused = super.getFocused();
        if (focused instanceof ClickableWidget && !focused.isFocused())
            focused = null;
        setFocused(focused);
        return focused;
    }

    /**
     * Used for moving JEI out of the way of extra things like block renders.
     *
     * @return the space that the GUI takes up outside the normal rectangle defined
     * by {@link GenericContainerScreen}.
     */
    public List<Rect2i> getExtraAreas() {
        return Collections.emptyList();
    }

    @Deprecated
    protected void debugWindowArea(DrawContext graphics) {
        graphics.fill(x + backgroundWidth, y + backgroundHeight, x, y, 0xD3D3D3D3);
    }

    @Deprecated
    protected void debugExtraAreas(DrawContext graphics) {
        for (Rect2i area : getExtraAreas()) {
            graphics.fill(area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(), 0xD3D3D3D3);
        }
    }

    protected void playUiSound(SoundEvent sound, float volume, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, pitch, volume * 0.25f));
    }

    public static ItemStack getStack(RegistryByteBuf extraData) {
        return ItemStack.PACKET_CODEC.decode(extraData);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T getBlockEntity(MinecraftClient mc, RegistryByteBuf extraData) {
        ClientWorld world = mc.world;
        if (world == null) {
            return null;
        }
        return (T) world.getBlockEntity(extraData.readBlockPos());
    }
}
