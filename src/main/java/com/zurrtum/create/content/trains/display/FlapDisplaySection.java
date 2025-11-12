package com.zurrtum.create.content.trains.display;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FlapDisplaySection {
    static final Map<String, String[]> LOADED_FLAP_CYCLES = new HashMap<>();

    public static final float MONOSPACE = 7;
    public static final float WIDE_MONOSPACE = 9;

    float size;
    boolean singleFlap;
    public boolean hasGap;
    boolean rightAligned;
    public boolean wideFlaps;
    boolean sendTransition;
    String cycle;
    Component component;

    // Client
    public String[] cyclingOptions;
    public boolean[] spinning;
    public int spinningTicks;
    public String text;

    public FlapDisplaySection(float width, String cycle, boolean singleFlap, boolean hasGap) {
        this.size = width;
        this.cycle = cycle;
        this.hasGap = hasGap;
        this.singleFlap = singleFlap;
        this.spinning = new boolean[singleFlap ? 1 : Math.max(0, (int) (width / FlapDisplaySection.MONOSPACE))];
        this.text = Strings.repeat(" ", spinning.length);
        this.component = null;
    }

    public FlapDisplaySection rightAligned() {
        rightAligned = true;
        return this;
    }

    public FlapDisplaySection wideFlaps() {
        wideFlaps = true;
        return this;
    }

    public void setText(Component component) {
        this.component = component;
        sendTransition = true;
    }

    public void refresh(boolean transition) {
        if (component == null)
            return;

        String newText = component.getString();

        if (!singleFlap) {
            if (rightAligned)
                newText = newText.trim();
            newText = newText.toUpperCase(Locale.ROOT);
            newText = newText.substring(0, Math.min(spinning.length, newText.length()));
            String whitespace = Strings.repeat(" ", spinning.length - newText.length());
            newText = rightAligned ? whitespace + newText : newText + whitespace;
            if (!text.isEmpty())
                for (int i = 0; i < spinning.length; i++)
                    spinning[i] |= transition && text.charAt(i) != newText.charAt(i);
        } else if (!text.isEmpty())
            spinning[0] |= transition && !newText.equals(text);

        text = newText;
        spinningTicks = 0;
    }

    public int tick(boolean instant, RandomSource random) {
        if (cyclingOptions == null)
            return 0;
        int max = Math.max(4, (int) (cyclingOptions.length * 1.75f));
        if (spinningTicks > max)
            return 0;

        spinningTicks++;
        if (spinningTicks <= max && spinningTicks < 2)
            return spinningTicks == 1 ? 0 : spinning.length;

        int spinningFlaps = 0;
        for (int i = 0; i < spinning.length; i++) {
            int increasingChance = Mth.clamp(8 - spinningTicks, 1, 10);
            boolean continueSpin = !instant && random.nextInt(increasingChance * max / 4) != 0;
            continueSpin &= max > 5 || spinningTicks < 2;
            spinning[i] &= continueSpin;

            if (i > 0 && random.nextInt(3) > 0)
                spinning[i - 1] &= continueSpin;
            if (i < spinning.length - 1 && random.nextInt(3) > 0)
                spinning[i + 1] &= continueSpin;
            if (spinningTicks > max)
                spinning[i] = false;

            if (spinning[i])
                spinningFlaps++;
        }

        return spinningFlaps;
    }

    public float getSize() {
        return size;
    }

    public void write(ValueOutput view) {
        view.putFloat("Width", size);
        view.putString("Cycle", cycle);
        if (rightAligned)
            view.putBoolean("RightAligned", true);
        if (singleFlap)
            view.putBoolean("SingleFlap", true);
        if (hasGap)
            view.putBoolean("Gap", true);
        if (wideFlaps)
            view.putBoolean("Wide", true);
        if (component != null)
            view.store("Text", ComponentSerialization.CODEC, component);
        if (sendTransition)
            view.putBoolean("Transition", true);
        sendTransition = false;
    }

    public static FlapDisplaySection load(ValueInput view) {
        float width = view.getFloatOr("Width", 0);
        String cycle = view.getStringOr("Cycle", "");
        boolean singleFlap = view.getBooleanOr("SingleFlap", false);
        boolean hasGap = view.getBooleanOr("Gap", false);

        FlapDisplaySection section = new FlapDisplaySection(width, cycle, singleFlap, hasGap);
        section.cyclingOptions = getFlapCycle(cycle);
        section.rightAligned = view.getBooleanOr("RightAligned", false);
        section.wideFlaps = view.getBooleanOr("Wide", false);

        view.read("Text", ComponentSerialization.CODEC).ifPresent(text -> {
            section.component = text;
            section.refresh(view.getBooleanOr("Transition", false));
        });
        return section;
    }

    public void update(ValueInput view) {
        view.read("Text", ComponentSerialization.CODEC).ifPresent(text -> component = text);
        if (cyclingOptions == null)
            cyclingOptions = getFlapCycle(cycle);
        refresh(view.getBooleanOr("Transition", false));
    }

    public boolean renderCharsIndividually() {
        return !singleFlap;
    }

    public Component getText() {
        return component;
    }

    public static String[] getFlapCycle(String key) {
        return LOADED_FLAP_CYCLES.computeIfAbsent(key, k -> Component.translatable("create.flap_display.cycles." + key).getString().split(";"));
    }

}
