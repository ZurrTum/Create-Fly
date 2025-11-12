package com.zurrtum.create.client.ponder.foundation;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.NavigatableSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.enums.PonderKeybinds;
import com.zurrtum.create.client.ponder.foundation.registration.PonderLocalization;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class PonderTooltipHandler {

    private static final Color borderA = new Color(0x5000ff, false).setImmutable();
    private static final Color borderB = new Color(0x5555ff, false).setImmutable();
    private static final Color borderC = new Color(0xffffff, false).setImmutable();

    public static boolean enable = true;

    static LerpedFloat holdKeyProgress = LerpedFloat.linear().startWithValue(0);
    static ItemStack hoveredStack = ItemStack.EMPTY;
    static ItemStack trackingStack = ItemStack.EMPTY;
    static boolean subject = false;
    static boolean deferTick = false;

    static final List<Consumer<ItemStack>> hoveredStackCallbacks = new ArrayList<>();

    public static final String HOLD_TO_PONDER = PonderLocalization.UI_PREFIX + "hold_to_ponder";
    public static final String SUBJECT = PonderLocalization.UI_PREFIX + "subject";

    public static void tick() {
        deferTick = true;
    }

    public static void deferredTick() {
        deferTick = false;
        Minecraft instance = Minecraft.getInstance();
        Screen currentScreen = instance.screen;

        if (hoveredStack.isEmpty() || trackingStack.isEmpty()) {
            trackingStack = ItemStack.EMPTY;
            holdKeyProgress.startWithValue(0);
            return;
        }

        float value = holdKeyProgress.getValue();

        if (RenderSystem.isOnRenderThread() && !subject && !PonderKeybinds.PONDER.isUnbound() && InputConstants.isKeyDown(
            instance.getWindow(),
            PonderKeybinds.PONDER.key.getValue()
        ) && currentScreen != null) {
            if (value >= 1) {
                if (currentScreen instanceof NavigatableSimiScreen)
                    ((NavigatableSimiScreen) currentScreen).centerScalingOnMouse();
                ScreenOpener.transitionTo(PonderUI.of(trackingStack));
                holdKeyProgress.startWithValue(0);
                return;
            }
            holdKeyProgress.setValue(Math.min(1, value + Math.max(.25f, value) * .25f));
        } else
            holdKeyProgress.setValue(Math.max(0, value - .05f));

        hoveredStack = ItemStack.EMPTY;
    }

    public static void addToTooltip(List<Component> toolTip, ItemStack stack) {
        if (!enable)
            return;

        if (NavigatableSimiScreen.isCurrentlyRenderingPreviousScreen())
            return;

        Minecraft mc = Minecraft.getInstance();
        updateHovered(mc, stack);

        if (deferTick)
            deferredTick();

        if (trackingStack != stack)
            return;

        // TODO - Checkover
        float renderPartialTicks = AnimationTickHolder.getPartialTicksUI(mc.getDeltaTracker());
        Component component = subject ? Ponder.lang().translate(SUBJECT).component().withStyle(ChatFormatting.GREEN) : makeProgressBar(Math.min(
            1,
            holdKeyProgress.getValue(renderPartialTicks) * 8 / 7f
        ));
        if (toolTip.size() < 2)
            toolTip.add(component);
        else
            toolTip.add(1, component);
    }

    protected static void updateHovered(Minecraft instance, ItemStack stack) {
        Screen currentScreen = instance.screen;
        boolean inPonderUI = currentScreen instanceof PonderUI;

        ItemStack prevStack = trackingStack;
        hoveredStack = ItemStack.EMPTY;
        subject = false;

        if (inPonderUI) {
            PonderUI ponderUI = (PonderUI) currentScreen;
            ItemStack uiSubject = ponderUI.getSubject();
            if (!uiSubject.isEmpty() && stack.is(uiSubject.getItem()))
                subject = true;
        }

        if (stack.isEmpty())
            return;
        if (!PonderIndex.getSceneAccess().doScenesExistForId(RegisteredObjectsHelper.getKeyOrThrow(stack.getItem())))
            return;

        if (prevStack.isEmpty() || !prevStack.is(stack.getItem()))
            holdKeyProgress.startWithValue(0);

        hoveredStack = stack;
        trackingStack = stack;

        for (Consumer<ItemStack> hoveredStackCallback : hoveredStackCallbacks)
            hoveredStackCallback.accept(hoveredStack.copy());
    }

    public static Optional<Couple<Color>> handleTooltipColor(ItemStack stack) {
        if (trackingStack != stack)
            return Optional.empty();

        if (holdKeyProgress.getValue() == 0)
            return Optional.empty();

        // TODO - Checkover
        float renderPartialTicks = AnimationTickHolder.getPartialTicksUI(Minecraft.getInstance().getDeltaTracker());

        Color startC;
        Color endC;
        float progress = Math.min(1, holdKeyProgress.getValue(renderPartialTicks) * 8 / 7f);

        startC = getSmoothColorForProgress(progress);
        endC = getSmoothColorForProgress(progress);

        return Optional.of(Couple.create(startC, endC));

    }

    private static Color getSmoothColorForProgress(float progress) {
        if (progress < 0.5)
            return borderA.mixWith(borderB, progress * 2);
        return borderB.mixWith(borderC, (progress - .5f) * 2);
    }

    private static Component makeProgressBar(float progress) {
        MutableComponent holdW = Ponder.lang()
            .translate(HOLD_TO_PONDER, PonderKeybinds.PONDER.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY)).style(ChatFormatting.DARK_GRAY)
            .component();

        if (progress > 0) {
            Font fontRenderer = Minecraft.getInstance().font;
            float charWidth = fontRenderer.width("|");
            float tipWidth = fontRenderer.width(holdW);

            int total = (int) (tipWidth / charWidth);
            int current = (int) (progress * total);

            String bars = "";
            bars += ChatFormatting.GRAY + Strings.repeat("|", current);
            if (progress < 1)
                bars += ChatFormatting.DARK_GRAY + Strings.repeat("|", total - current);
            return Component.literal(bars);
        }

        return holdW;
    }

    public synchronized static void registerHoveredPonderStackCallback(Consumer<ItemStack> consumer) {
        hoveredStackCallbacks.add(consumer);
    }

    public synchronized static void removeHoveredPonderStackCallback(Consumer<ItemStack> consumer) {
        hoveredStackCallbacks.remove(consumer);
    }
}