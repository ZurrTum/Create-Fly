package com.zurrtum.create.client.ponder.enums;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderKeybinds {
    public static final Category CATEGORY = Category.create(Identifier.of(MOD_ID, MOD_ID));
    public static final KeyBinding PONDER = register("ponder.keyinfo.ponder", GLFW.GLFW_KEY_W);

    private static KeyBinding register(String name, int code) {
        KeyBinding keyBinding = new KeyBinding(name, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        Key key = InputUtil.Type.KEYSYM.createFromCode(code);
        keyBinding.defaultKey = key;
        keyBinding.setBoundKey(key);
        return keyBinding;
    }
}
