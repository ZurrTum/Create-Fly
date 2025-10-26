package com.zurrtum.create.client.ponder.enums;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import org.lwjgl.glfw.GLFW;

public class PonderKeybinds {
    public static final String CATEGORY = "key.categories.ponder";
    public static final KeyBinding PONDER = register("ponder", GLFW.GLFW_KEY_W);

    private static KeyBinding register(String description, int defaultKey) {
        KeyBinding keyBinding = new KeyBinding("key.ponder." + description, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        Key key = InputUtil.Type.KEYSYM.createFromCode(defaultKey);
        keyBinding.defaultKey = key;
        keyBinding.setBoundKey(key);
        return keyBinding;
    }

    public static void register() {
    }
}
