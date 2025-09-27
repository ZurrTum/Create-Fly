package com.zurrtum.create.client.ponder.enums;

import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import org.lwjgl.glfw.GLFW;

public class PonderKeybinds {
    public static final String CATEGORY = Ponder.MOD_NAME;
    public static final KeyBinding PONDER = register("ponder.keyinfo.ponder", GLFW.GLFW_KEY_W);

    private static KeyBinding register(String name, int code) {
        KeyBinding keyBinding = new KeyBinding(name, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        Key key = InputUtil.Type.KEYSYM.createFromCode(code);
        keyBinding.setBoundKey(key);
        return keyBinding;
    }
}
