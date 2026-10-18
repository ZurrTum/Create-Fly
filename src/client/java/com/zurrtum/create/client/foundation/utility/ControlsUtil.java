package com.zurrtum.create.client.foundation.utility;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLMouse;

import java.util.ArrayList;
import java.util.List;

public class ControlsUtil {

    private static @Nullable List<KeyMapping> standardControls;

    public static List<KeyMapping> getControls() {
        if (standardControls == null) {
            Options gameSettings = Minecraft.getInstance().options;
            standardControls = new ArrayList<>(6);
            standardControls.add(gameSettings.keyUp);
            standardControls.add(gameSettings.keyDown);
            standardControls.add(gameSettings.keyLeft);
            standardControls.add(gameSettings.keyRight);
            standardControls.add(gameSettings.keyJump);
            standardControls.add(gameSettings.keyShift);
        }
        return standardControls;
    }

    public static boolean isActuallyPressed(KeyMapping kb) {
        InputConstants.Key key = kb.key;
        int button = key.getValue();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return (SDLMouse.SDL_GetMouseState(null, null) & 1 << button - 1) != 0;
        }
        return InputConstants.isKeyDown(button);
    }

}
