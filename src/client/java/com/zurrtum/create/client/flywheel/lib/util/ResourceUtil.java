package com.zurrtum.create.client.flywheel.lib.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public final class ResourceUtil {
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));

    private ResourceUtil() {
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Same as {@link ResourceLocation#parse(String)}, but defaults to Flywheel namespace.
     */
    public static ResourceLocation parseFlywheelDefault(String location) {
        String namespace = MOD_ID;
        String path = location;
        int i = location.indexOf(58);
        if (i >= 0) {
            path = location.substring(i + 1);
            if (i >= 1) {
                namespace = location.substring(0, i);
            }
        }

        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Same as {@link ResourceLocation#read(StringReader)}, but defaults to Flywheel namespace.
     */
    public static ResourceLocation readFlywheelDefault(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());

        try {
            return parseFlywheelDefault(s);
        } catch (ResourceLocationException var4) {
            reader.setCursor(i);
            throw ERROR_INVALID.createWithContext(reader);
        }
    }

    /**
     * Same as {@link ResourceLocation#toDebugFileName()}, but also removes the file extension.
     */
    public static String toDebugFileNameNoExtension(ResourceLocation resourceLocation) {
        String stringLoc = resourceLocation.toDebugFileName();
        return stringLoc.substring(0, stringLoc.lastIndexOf('.'));
    }
}
