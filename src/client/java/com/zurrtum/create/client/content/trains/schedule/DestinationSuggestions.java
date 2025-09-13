package com.zurrtum.create.client.content.trains.schedule;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.zurrtum.create.catnip.data.IntAttached;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class DestinationSuggestions extends ChatInputSuggestor {

    private final TextFieldWidget textBox;
    private final List<IntAttached<String>> viableStations;
    private String previous = "<>";
    private final TextRenderer font;
    private boolean active;

    List<Suggestion> currentSuggestions;
    private final int yOffset;

    public DestinationSuggestions(
        MinecraftClient pMinecraft,
        Screen pScreen,
        TextFieldWidget pInput,
        TextRenderer pFont,
        List<IntAttached<String>> viableStations,
        boolean anchorToBottom,
        int yOffset
    ) {
        super(pMinecraft, pScreen, pInput, pFont, true, true, 0, 7, anchorToBottom, 0xee_303030);
        this.textBox = pInput;
        this.font = pFont;
        this.viableStations = viableStations;
        this.yOffset = yOffset;
        currentSuggestions = new ArrayList<>();
        active = false;
    }

    public void tick() {
        if (window == null)
            textBox.setSuggestion("");
        if (active == textBox.isFocused())
            return;
        active = textBox.isFocused();
        refresh();
    }

    @Override
    public void refresh() {
        if (textBox.getText().length() < textBox.getCursor())
            return;

        String trimmed = textBox.getText().substring(0, textBox.getCursor());

        if (!textBox.getSelectedText().isBlank())
            trimmed = trimmed.replace(textBox.getSelectedText(), "");

        final String value = trimmed;

        if (value.equals(previous))
            return;
        if (!active) {
            window = null;
            return;
        }

        previous = value;
        currentSuggestions = viableStations.stream()
            .filter(ia -> !ia.getValue().equals(value) && ia.getValue().toLowerCase().startsWith(value.toLowerCase()))
            .sorted((ia1, ia2) -> Integer.compare(ia1.getFirst(), ia2.getFirst())).map(IntAttached::getValue)
            .map(s -> new Suggestion(new StringRange(0, 1000), s)).toList();

        showSuggestions(false);
    }

    public void showSuggestions(boolean pNarrateFirstSuggestion) {
        if (currentSuggestions.isEmpty()) {
            window = null;
            return;
        }

        int width = 0;
        for (Suggestion suggestion : currentSuggestions)
            width = Math.max(width, font.getWidth(suggestion.getText()));
        int x = MathHelper.clamp(textBox.getCharacterX(0), 0, textBox.getCharacterX(0) + textBox.getInnerWidth() - width);
        window = new ChatInputSuggestor.SuggestionWindow(x, 72 + yOffset, width, currentSuggestions, false);
    }

    public boolean isEmpty() {
        return viableStations.isEmpty();
    }

}
