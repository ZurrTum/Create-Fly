package com.zurrtum.create.client.compat.computercraft;

import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.Mods;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ComputerScreen extends AbstractSimiScreen {

    private final AllGuiTextures background = AllGuiTextures.COMPUTER;

    private final Supplier<Text> displayTitle;
    private final AdditionalRenderer additionalRenderer;
    private final Screen previousScreen;
    private final Supplier<Boolean> hasAttachedComputer;

    private ElementWidget computerWidget;
    private IconButton confirmButton;

    public ComputerScreen(Text title, @Nullable AdditionalRenderer additionalRenderer, Screen previousScreen, Supplier<Boolean> hasAttachedComputer) {
        this(title, () -> title, additionalRenderer, previousScreen, hasAttachedComputer);
    }

    public ComputerScreen(
        Text title,
        Supplier<Text> displayTitle,
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
            client.setScreen(previousScreen);

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
            addDrawableChild(computerWidget);
        }

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        if (additionalRenderer != null) {
            additionalRenderer.addAdditional(this, x, y, background);
        }
    }

    @Override
    public void close() {
        super.close();
        previousScreen.close();
        if (computerWidget != null) {
            computerWidget.getRenderElement().clear();
        }
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        graphics.drawText(
            textRenderer,
            displayTitle.get(),
            Math.round(x + background.getWidth() / 2.0F - textRenderer.getWidth(displayTitle.get()) / 2.0F),
            y + 4,
            0xFF442000,
            false
        );
        graphics.drawWrappedText(
            textRenderer,
            CreateLang.translate("gui.attached_computer.controlled").component(),
            x + 55,
            y + 32,
            111,
            0xFF7A7A7A,
            false
        );

        if (additionalRenderer != null)
            additionalRenderer.updateAdditional(partialTicks);
    }

    public interface AdditionalRenderer {
        void addAdditional(Screen screen, int x, int y, AllGuiTextures background);

        default void updateAdditional(float partialTicks) {
        }
    }
}
