package com.zurrtum.create.client.foundation.utility;

import com.zurrtum.create.client.AllKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class ControlsUtil {

    private static List<KeyBinding> standardControls;

    public static List<KeyBinding> getControls() {
        if (standardControls == null) {
            GameOptions gameSettings = MinecraftClient.getInstance().options;
            standardControls = new ArrayList<>(6);
            standardControls.add(gameSettings.forwardKey);
            standardControls.add(gameSettings.backKey);
            standardControls.add(gameSettings.leftKey);
            standardControls.add(gameSettings.rightKey);
            standardControls.add(gameSettings.jumpKey);
            standardControls.add(gameSettings.sneakKey);
        }
        return standardControls;
    }

    public static boolean isActuallyPressed(KeyBinding kb) {
        InputUtil.Key key = kb.boundKey;
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return AllKeys.isMouseButtonDown(key.getCode());
        } else {
            return AllKeys.isKeyDown(key.getCode());
        }
    }

}
