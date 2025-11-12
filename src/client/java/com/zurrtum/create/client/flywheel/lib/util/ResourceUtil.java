package com.zurrtum.create.client.flywheel.lib.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public final class ResourceUtil {
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));

    private ResourceUtil() {
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Same as {@link Identifier#parse(String)}, but defaults to Flywheel namespace.
     */
    public static Identifier parseFlywheelDefault(String location) {
        String namespace = MOD_ID;
        String path = location;
        int i = location.indexOf(58);
        if (i >= 0) {
            path = location.substring(i + 1);
            if (i >= 1) {
                namespace = location.substring(0, i);
            }
        }

        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Same as {@link Identifier#read(StringReader)}, but defaults to Flywheel namespace.
     */
    public static Identifier readFlywheelDefault(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && Identifier.isAllowedInIdentifier(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());

        try {
            return parseFlywheelDefault(s);
        } catch (IdentifierException var4) {
            reader.setCursor(i);
            throw ERROR_INVALID.createWithContext(reader);
        }
    }

    /**
     * Same as {@link Identifier#toDebugFileName()}, but also removes the file extension.
     */
    public static String toDebugFileNameNoExtension(Identifier Identifier) {
        String stringLoc = Identifier.toDebugFileName();
        return stringLoc.substring(0, stringLoc.lastIndexOf('.'));
    }
}
