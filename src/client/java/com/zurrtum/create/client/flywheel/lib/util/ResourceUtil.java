package com.zurrtum.create.client.flywheel.lib.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public final class ResourceUtil {
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Text.translatable("argument.id.invalid"));

    private ResourceUtil() {
    }

    public static Identifier rl(String path) {
        return Identifier.of("flywheel", path);
    }

    public static Identifier parseFlywheelDefault(String location) {
        String namespace = "flywheel";
        String path = location;
        int i = location.indexOf(58);
        if (i >= 0) {
            path = location.substring(i + 1);
            if (i >= 1) {
                namespace = location.substring(0, i);
            }
        }

        return Identifier.of(namespace, path);
    }

    public static Identifier readFlywheelDefault(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());

        try {
            return parseFlywheelDefault(s);
        } catch (InvalidIdentifierException var4) {
            reader.setCursor(i);
            throw ERROR_INVALID.createWithContext(reader);
        }
    }

    public static String toDebugFileNameNoExtension(Identifier resourceLocation) {
        String stringLoc = resourceLocation.toUnderscoreSeparatedString();
        return stringLoc.substring(0, stringLoc.lastIndexOf(46));
    }
}
