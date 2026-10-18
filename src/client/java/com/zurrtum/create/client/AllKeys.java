package com.zurrtum.create.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllKeys {
    public static final List<KeyMapping> ALL = new ArrayList<>();
    public static final Category CATEGORY = Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "binding"));
    public static final KeyMapping TOOL_MENU = register("toolmenu", InputConstants.KEY_LALT);
    public static final KeyMapping TOOLBELT = register("toolbelt", InputConstants.KEY_LALT);
    public static final KeyMapping ROTATE_MENU = register("rotate_menu", InputConstants.UNKNOWN.getValue());

    private static KeyMapping register(String name, int code) {
        KeyMapping key = new KeyMapping("create.keyinfo." + name, code, CATEGORY);
        ALL.add(key);
        return key;
    }

    public static void register() {
    }
}
