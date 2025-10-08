package com.zurrtum.create.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllKeys {
    public static final List<KeyBinding> ALL = new ArrayList<>();
    public static final Category CATEGORY = Category.create(Identifier.of(MOD_ID, MOD_ID));
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

    public static boolean hasControlDown() {
        return SystemKeycodes.IS_MAC_OS ? InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow(),
            GLFW.GLFW_KEY_LEFT_SUPER
        ) || InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow(),
            GLFW.GLFW_KEY_RIGHT_SUPER
        ) : InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean hasShiftDown() {
        return InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow(),
            GLFW.GLFW_KEY_LEFT_SHIFT
        ) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static void register() {
    }
}
