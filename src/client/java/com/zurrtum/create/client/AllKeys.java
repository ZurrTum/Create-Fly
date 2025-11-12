package com.zurrtum.create.client;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

import com.mojang.blaze3d.platform.InputConstants;

public class AllKeys {
    public static final List<KeyMapping> ALL = new ArrayList<>();
    public static final Category CATEGORY = Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "binding"));
    public static final KeyMapping TOOL_MENU = register("toolmenu", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping TOOLBELT = register("toolbelt", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping ROTATE_MENU = register("rotate_menu", GLFW.GLFW_KEY_UNKNOWN);

    private static KeyMapping register(String name, int code) {
        KeyMapping key = new KeyMapping("create.keyinfo." + name, code, CATEGORY);
        ALL.add(key);
        return key;
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), button) == 1;
    }

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), key) == 1;
    }

    public static boolean hasControlDown() {
        return InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow(),
            GLFW.GLFW_KEY_LEFT_SUPER
        ) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SUPER) : InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow(),
            GLFW.GLFW_KEY_LEFT_CONTROL
        ) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow(),
            GLFW.GLFW_KEY_RIGHT_SHIFT
        );
    }

    public static void register() {
    }
}
