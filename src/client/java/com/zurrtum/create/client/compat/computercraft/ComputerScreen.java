package com.zurrtum.create.client.compat.computercraft;

import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.Mods;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class ComputerScreen extends AbstractSimiScreen {

    private final AllGuiTextures background = AllGuiTextures.COMPUTER;

    private final Supplier<Component> displayTitle;
    private final AdditionalRenderer additionalRenderer;
    private final Screen previousScreen;
    private final Supplier<Boolean> hasAttachedComputer;

    private ElementWidget computerWidget;
    private IconButton confirmButton;

    public ComputerScreen(
        Component title,
        @Nullable AdditionalRenderer additionalRenderer,
        Screen previousScreen,
        Supplier<Boolean> hasAttachedComputer
    ) {
        this(title, () -> title, additionalRenderer, previousScreen, hasAttachedComputer);
    }

    public ComputerScreen(
        Component title,
        Supplier<Component> displayTitle,
        @Nullable AdditionalRenderer additionalRenderer,
        Screen previousScreen,
        Supplier<Boolean> hasAttachedComputer
    ) {
        super(title);
        this.displayTitle = displayTitle;
        this.additionalRenderer = additionalRenderer;
        this.previousScreen = previousScreen;
        this.hasAttachedComputer = hasAttachedComputer;
    }

    @Override
    public void tick() {
        if (!hasAttachedComputer.get())
            minecraft.setScreen(previousScreen);

        super.tick();
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        super.init();

        int x = guiLeft;
        int y = guiTop;

        if (Mods.COMPUTERCRAFT.isLoaded()) {
            computerWidget = new ElementWidget(x + 33, y + 38).showingElement(GuiGameElement.of(Mods.COMPUTERCRAFT.getItem("computer_advanced")));
            computerWidget.getToolTip().add(CreateLang.translate("gui.attached_computer.hint").component());
            addRenderableWidget(computerWidget);
        }

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        if (additionalRenderer != null) {
            additionalRenderer.addAdditional(this, x, y, background);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        previousScreen.onClose();
        if (computerWidget != null) {
            computerWidget.getRenderElement().clear();
        }
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        graphics.drawString(
            font,
            displayTitle.get(),
            Math.round(x + background.getWidth() / 2.0F - font.width(displayTitle.get()) / 2.0F),
            y + 4,
            0xFF442000,
            false
        );
        graphics.drawWordWrap(font, CreateLang.translate("gui.attached_computer.controlled").component(), x + 55, y + 32, 111, 0xFF7A7A7A, false);

        if (additionalRenderer != null)
            additionalRenderer.updateAdditional(partialTicks);
    }

    public interface AdditionalRenderer {
        void addAdditional(Screen screen, int x, int y, AllGuiTextures background);

        default void updateAdditional(float partialTicks) {
        }
    }
}
