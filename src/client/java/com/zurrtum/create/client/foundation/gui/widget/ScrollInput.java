package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.StepContext;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScrollInput extends AbstractSimiWidget {

    protected Consumer<Integer> onScroll;
    protected int state;
    protected Text title = CreateLang.translateDirect("gui.scrollInput.defaultTitle");
    protected final Text scrollToModify = CreateLang.translateDirect("gui.scrollInput.scrollToModify");
    protected final Text shiftScrollsFaster = CreateLang.translateDirect("gui.scrollInput.shiftScrollsFaster");
    protected Text hint = null;
    protected Label displayLabel;
    protected boolean inverted;
    protected boolean soundPlayed;
    protected Function<Integer, Text> formatter;

    protected int min, max;
    protected int shiftStep;
    Function<StepContext, Integer> step;

    public ScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn);
        state = 0;
        min = 0;
        max = 1;
        shiftStep = 5;
        step = standardStep();
        formatter = i -> {
            return Text.literal(String.valueOf(i));
        };
        soundPlayed = false;
    }

    public Function<StepContext, Integer> standardStep() {
        return c -> c.shift ? shiftStep : 1;
    }

    public ScrollInput inverted() {
        inverted = true;
        return this;
    }

    public ScrollInput withRange(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ScrollInput calling(Consumer<Integer> onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    public ScrollInput format(Function<Integer, Text> formatter) {
        this.formatter = formatter;
        return this;
    }

    public ScrollInput removeCallback() {
        this.onScroll = null;
        return this;
    }

    public ScrollInput titled(MutableText title) {
        this.title = title;
        updateTooltip();
        return this;
    }

    public ScrollInput addHint(MutableText hint) {
        this.hint = hint;
        updateTooltip();
        return this;
    }

    public ScrollInput withStepFunction(Function<StepContext, Integer> step) {
        this.step = step;
        return this;
    }

    public ScrollInput writingTo(Label label) {
        this.displayLabel = label;
        if (label != null)
            writeToLabel();
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        soundPlayed = false;
    }

    public int getState() {
        return state;
    }

    public ScrollInput setState(int state) {
        this.state = state;
        clampState();
        updateTooltip();
        if (displayLabel != null)
            writeToLabel();
        return this;
    }

    public ScrollInput withShiftStep(int step) {
        shiftStep = step;
        return this;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (inverted)
            pScrollY *= -1;

        StepContext context = new StepContext();
        context.control = AllKeys.hasControlDown();
        context.shift = AllKeys.hasShiftDown();
        context.currentValue = state;
        context.forward = pScrollY > 0;

        int priorState = state;
        boolean shifted = context.shift;
        int step = (int) Math.signum(pScrollY) * this.step.apply(context);

        state += step;
        if (shifted)
            state -= state % shiftStep;

        clampState();

        if (priorState != state) {
            if (!soundPlayed)
                MinecraftClient.getInstance().getSoundManager()
                    .play(PositionedSoundInstance.master(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 1.5f + 0.1f * (state - min) / (max - min)));
            soundPlayed = true;
            onChanged();
        }

        return priorState != state;
    }

    protected void clampState() {
        if (state >= max)
            state = max - 1;
        if (state < min)
            state = min;
    }

    public void onChanged() {
        if (displayLabel != null)
            writeToLabel();
        if (onScroll != null)
            onScroll.accept(state);
        updateTooltip();
    }

    protected void writeToLabel() {
        displayLabel.text = formatter.apply(state);
    }

    protected void updateTooltip() {
        toolTip.clear();
        if (title == null)
            return;
        toolTip.add(title.copyContentOnly().styled(s -> s.withColor(HEADER_RGB.getRGB())));
        if (hint != null)
            toolTip.add(hint.copyContentOnly().styled(s -> s.withColor(HINT_RGB.getRGB())));
        toolTip.add(scrollToModify.copyContentOnly().formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
        toolTip.add(shiftScrollsFaster.copyContentOnly().formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
    }

}
