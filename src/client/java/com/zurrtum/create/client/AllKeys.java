package com.zurrtum.create.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AllKeys {
    public static final List<KeyBinding> ALL = new ArrayList<>();
    public static final String CATEGORY = "itemGroup.create.base";
    public static final KeyBinding TOOL_MENU = register("toolmenu", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyBinding TOOLBELT = register("toolbelt", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyBinding ROTATE_MENU = register("rotate_menu", GLFW.GLFW_KEY_UNKNOWN);

    private static KeyBinding register(String name, int code) {
        KeyBinding key = new KeyBinding("create.keyinfo." + name, code, CATEGORY);
        ALL.add(key);
        return key;
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == 1;
    }

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == 1;
    }

    public static void register() {
    }
}
