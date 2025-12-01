package com.zurrtum.create.client.content.kinetics.transmission.sequencer;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiItemRenderBuilder;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.compat.computercraft.ComputerScreen;
import com.zurrtum.create.client.compat.computercraft.ComputerScreen.AdditionalRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.transmission.sequencer.Instruction;
import com.zurrtum.create.content.kinetics.transmission.sequencer.InstructionSpeedModifiers;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSequencedGearshiftPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SequencedGearshiftScreen extends AbstractSimiScreen implements AdditionalRenderer {
    private final AllGuiTextures background = AllGuiTextures.SEQUENCER;
    private IconButton confirmButton;
    private GuiItemRenderBuilder renderedItem;
    private SequencedGearshiftBlockEntity be;

    private Vector<Instruction> instructions;

    private Vector<Vector<ScrollInput>> inputs;

    public SequencedGearshiftScreen(SequencedGearshiftBlockEntity be) {
        super(CreateLang.translateDirect("gui.sequenced_gearshift.title"));
        this.instructions = be.getInstructions();
        this.be = be;
    }

    @Override
    protected void init() {
        renderedItem = GuiGameElement.of(AllItems.SEQUENCED_GEARSHIFT.getDefaultStack()).scale(5);
        if (be.computerBehaviour.hasAttachedComputer())
            ScreenOpener.open(
                new ComputerScreen(title, this, this, be.computerBehaviour::hasAttachedComputer));

        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        inputs = new Vector<>(5);
        for (int row = 0; row < inputs.capacity(); row++)
            inputs.add(new Vector<>(3));

        for (int row = 0; row < instructions.size(); row++)
            initInputsOfRow(row, x, y);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);
        addAdditional(this, x, y, background);
    }

    @Override
    public void addAdditional(Screen screen, int x, int y, AllGuiTextures background) {
        screen.addDrawableChild(new ElementWidget(x + background.getWidth() + 6, y + background.getHeight() - 56).showingElement(renderedItem));
    }

    @Override
    public void close() {
        super.close();
        renderedItem.clear();
    }

    private static String translationKey(SequencerInstructions def) {
        return "gui.sequenced_gearshift.instruction." + Lang.asId(def.name());
    }

    private static String descriptiveTranslationKey(SequencerInstructions def) {
        return translationKey(def) + ".descriptive";
    }

    private static String translationKey(InstructionSpeedModifiers def) {
        return "gui.sequenced_gearshift.speed." + Lang.asId(def.name());
    }

    private static List<Text> getSpeedOptions() {
        List<Text> options = new ArrayList<>();
        for (InstructionSpeedModifiers entry : InstructionSpeedModifiers.values())
            options.add(CreateLang.translateDirect(translationKey(entry)));
        return options;
    }

    private static List<Text> getSequencerOptions() {
        List<Text> options = new ArrayList<>();
        for (SequencerInstructions entry : SequencerInstructions.values())
            options.add(CreateLang.translateDirect(descriptiveTranslationKey(entry)));
        return options;
    }

    public void initInputsOfRow(int row, int backgroundX, int backgroundY) {
        int x = backgroundX + 30;
        int y = backgroundY + 20;
        int rowHeight = 22;

        Vector<ScrollInput> rowInputs = inputs.get(row);
        removeWidgets(rowInputs);
        rowInputs.clear();
        int index = row;
        Instruction instruction = instructions.get(row);

        ScrollInput type = new SelectionScrollInput(x, y + rowHeight * row, 50, 18).forOptions(getSequencerOptions())
            .calling(state -> instructionUpdated(index, state)).setState(instruction.instruction.ordinal())
            .titled(CreateLang.translateDirect("gui.sequenced_gearshift.instruction"));
        ScrollInput value = new ScrollInput(x + 58, y + rowHeight * row, 28, 18).calling(state -> instruction.value = state);
        ScrollInput direction = new SelectionScrollInput(x + 88, y + rowHeight * row, 28, 18).forOptions(getSpeedOptions())
            .calling(state -> instruction.speedModifier = InstructionSpeedModifiers.values()[state])
            .titled(CreateLang.translateDirect("gui.sequenced_gearshift.speed"));

        rowInputs.add(type);
        rowInputs.add(value);
        rowInputs.add(direction);

        addRenderableWidgets(rowInputs);
        updateParamsOfRow(row);
    }

    private static boolean hasValueParameter(SequencerInstructions def) {
        return switch (def) {
            case DELAY, TURN_DISTANCE, TURN_ANGLE -> true;
            default -> false;
        };
    }

    private static boolean hasSpeedParameter(SequencerInstructions def) {
        return switch (def) {
            case TURN_DISTANCE, TURN_ANGLE -> true;
            default -> false;
        };
    }

    private static int maxValue(SequencerInstructions def) {
        return switch (def) {
            case TURN_ANGLE -> 360;
            case TURN_DISTANCE -> 128;
            case DELAY -> 600;
            default -> -1;
        };
    }

    private static String parameterKey(SequencerInstructions def) {
        return translationKey(def) + switch (def) {
            case TURN_ANGLE -> ".angle";
            case TURN_DISTANCE -> ".distance";
            case DELAY -> "";
            default -> "";
        };
    }

    private static AllGuiTextures background(SequencerInstructions def) {
        return switch (def) {
            case TURN_ANGLE, TURN_DISTANCE -> AllGuiTextures.SEQUENCER_INSTRUCTION;
            case DELAY -> AllGuiTextures.SEQUENCER_DELAY;
            case AWAIT -> AllGuiTextures.SEQUENCER_AWAIT;
            case END -> AllGuiTextures.SEQUENCER_END;
        };
    }

    private static int shiftStep(SequencerInstructions def) {
        return switch (def) {
            case TURN_ANGLE -> 45;
            case TURN_DISTANCE -> 5;
            case DELAY -> 20;
            default -> -1;
        };
    }

    public void updateParamsOfRow(int row) {
        Instruction instruction = instructions.get(row);
        Vector<ScrollInput> rowInputs = inputs.get(row);
        SequencerInstructions def = instruction.instruction;
        boolean hasValue = hasValueParameter(def);
        boolean hasModifier = hasSpeedParameter(def);

        ScrollInput value = rowInputs.get(1);
        value.active = value.visible = hasValue;
        if (hasValue)
            value.withRange(1, maxValue(def) + 1).titled(CreateLang.translateDirect(parameterKey(def))).withShiftStep(shiftStep(def))
                .setState(instruction.value).onChanged();
        if (def == SequencerInstructions.DELAY) {
            value.withStepFunction(context -> {
                int v = context.currentValue;
                if (!context.forward)
                    v--;
                if (v < 20)
                    return context.shift ? 20 : 1;
                return context.shift ? 100 : 20;
            });
        } else
            value.withStepFunction(value.standardStep());

        ScrollInput modifier = rowInputs.get(2);
        modifier.active = modifier.visible = hasModifier;
        if (hasModifier)
            modifier.setState(instruction.speedModifier.ordinal());
    }

    @Override
    public void tick() {
        super.tick();

        if (be.computerBehaviour.hasAttachedComputer())
            ScreenOpener.open(new ComputerScreen(title, this, this, be.computerBehaviour::hasAttachedComputer));
    }

    private static String formatValue(SequencerInstructions def, int value) {
        return switch (def) {
            case TURN_ANGLE -> value + CreateLang.translateDirect("generic.unit.degrees").getString();
            case TURN_DISTANCE -> value + "m";
            case DELAY -> value >= 20 ? (value / 20) + "s" : value + "t";
            default -> String.valueOf(value);
        };
    }

    private static Text label(InstructionSpeedModifiers def) {
        return Text.literal(switch (def) {
            case FORWARD_FAST -> ">>";
            case FORWARD -> "->";
            case BACK -> "<-";
            case BACK_FAST -> "<<";
        });
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        for (int row = 0; row < instructions.capacity(); row++) {
            AllGuiTextures toDraw = AllGuiTextures.SEQUENCER_EMPTY;
            int yOffset = toDraw.getHeight() * row;

            toDraw.render(graphics, x, y + 16 + yOffset);
        }

        for (int row = 0; row < instructions.capacity(); row++) {
            AllGuiTextures toDraw = AllGuiTextures.SEQUENCER_EMPTY;
            int yOffset = toDraw.getHeight() * row;
            if (row >= instructions.size()) {
                toDraw.render(graphics, x, y + 16 + yOffset);
                continue;
            }

            Instruction instruction = instructions.get(row);
            SequencerInstructions def = instruction.instruction;
            background(def).render(graphics, x, y + 16 + yOffset);

            label(graphics, 36, yOffset - 1, CreateLang.translateDirect(translationKey(def)));
            if (hasValueParameter(def)) {
                String text = formatValue(def, instruction.value);
                int stringWidth = textRenderer.getWidth(text);
                label(graphics, 90 + (12 - stringWidth / 2), yOffset - 1, Text.literal(text));
            }
            if (hasSpeedParameter(def))
                label(graphics, 127, yOffset - 1, label(instruction.speedModifier));
        }

        graphics.drawText(textRenderer, title, x + (background.getWidth() - 8) / 2 - textRenderer.getWidth(title) / 2, y + 4, 0xFF592424, false);
    }

    private void label(DrawContext graphics, int x, int y, Text text) {
        graphics.drawText(textRenderer, text, guiLeft + x, guiTop + 26 + y, 0xFFFFFFEE, true);
    }

    public void sendPacket() {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ConfigureSequencedGearshiftPacket(be.getPos(), instructions));
    }

    @Override
    public void removed() {
        sendPacket();
    }

    private static int defaultValue(SequencerInstructions def) {
        return switch (def) {
            case TURN_ANGLE -> 90;
            case TURN_DISTANCE -> 5;
            case DELAY -> 10;
            default -> -1;
        };
    }

    private void instructionUpdated(int index, int state) {
        SequencerInstructions newValue = SequencerInstructions.values()[state];
        instructions.get(index).instruction = newValue;
        instructions.get(index).value = defaultValue(newValue);
        updateParamsOfRow(index);
        if (newValue == SequencerInstructions.END) {
            for (int i = instructions.size() - 1; i > index; i--) {
                instructions.remove(i);
                Vector<ScrollInput> rowInputs = inputs.get(i);
                removeWidgets(rowInputs);
                rowInputs.clear();
            }
        } else {
            if (index + 1 < instructions.capacity() && index + 1 == instructions.size()) {
                instructions.add(new Instruction(SequencerInstructions.END));
                initInputsOfRow(index + 1, guiLeft, guiTop);
            }
        }
    }

}
