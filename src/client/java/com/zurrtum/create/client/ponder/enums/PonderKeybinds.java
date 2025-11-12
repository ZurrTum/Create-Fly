package com.zurrtum.create.client.ponder.enums;

import org.lwjgl.glfw.GLFW;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;

public class PonderKeybinds {
    public static final Category CATEGORY = Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "binding"));
    public static final KeyMapping PONDER = register("ponder", GLFW.GLFW_KEY_W);

    private static KeyMapping register(String description, int defaultKey) {
        KeyMapping keyBinding = new KeyMapping("key.ponder." + description, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
        Key key = InputConstants.Type.KEYSYM.getOrCreate(defaultKey);
        keyBinding.defaultKey = key;
        keyBinding.setKey(key);
        return keyBinding;
    }

    public static void register() {
    }
}
