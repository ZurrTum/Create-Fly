package com.zurrtum.create.client.catnip.gui;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ScreenOpener {

    private static final Deque<Screen> backStack = new ArrayDeque<>();

    public static void open(@Nullable Screen screen) {
        open(Minecraft.getInstance().screen, screen);
    }

    public static void open(@Nullable Screen current, @Nullable Screen toOpen) {
        if (current != null) {
            if (backStack.size() >= 15) // don't go deeper than 15 steps
            {
                backStack.pollLast();
            }

            backStack.push(current);
        } else {
            backStack.clear();
        }

        openScreen(toOpen);
    }

    public static void openPreviousScreen() {
        Screen previousScreen = backStack.pollFirst();
        if (previousScreen == null) {
            return;
        }
        if (previousScreen instanceof NavigatableSimiScreen previousNavScreen) {
            previousNavScreen.transition.startWithValue(-0.001).chase(-1, .3f, LerpedFloat.Chaser.EXP);
        }
        openScreen(previousScreen);
    }

    // transitions are only supported in simiScreens atm. they take care of all the
    // rendering for it
    public static void transitionTo(NavigatableSimiScreen screen) {
        if (tryBackTracking(screen)) {
            return;
        }
        screen.transition.startWithValue(0.001).chase(1, .3f, LerpedFloat.Chaser.EXP);
        open(screen);
    }

    private static boolean tryBackTracking(NavigatableSimiScreen screen) {
        Screen previouslyRenderedScreen = getScreenFirst();
        if (previouslyRenderedScreen == null) {
            return false;
        }
        if (!(previouslyRenderedScreen instanceof NavigatableSimiScreen navigatableSimiScreen)) {
            return false;
        }
        if (!screen.isEquivalentTo(navigatableSimiScreen)) {
            return false;
        }

        screen.shareContextWith(navigatableSimiScreen);
        openPreviousScreen();
        return true;
    }

    public static void clearStack() {
        backStack.clear();
    }

    public static List<Screen> getScreenHistory() {
        return new ArrayList<>(backStack);
    }

    public static @Nullable Screen getScreenFirst() {
        return backStack.peekFirst();
    }

    @Nullable
    public static Screen getBackStepScreen() {
        return backStack.peek();
    }

    private static void openScreen(@Nullable Screen screen) {
        Minecraft.getInstance().schedule(() -> {
            Minecraft.getInstance().setScreen(screen);
        });
    }

}
