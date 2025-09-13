package com.zurrtum.create.foundation.utility;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.zurrtum.create.Create.MOD_ID;

public class DynamicComponent {
    public static Text parseCustomText(World level, BlockPos pos, Text customText) {
        if (!(level instanceof ServerWorld serverLevel))
            return null;
        try {
            return Texts.parse(getCommandSource(serverLevel, pos), customText, null, 0);
        } catch (JsonParseException | CommandSyntaxException e) {
            return null;
        }
    }

    public static ServerCommandSource getCommandSource(ServerWorld level, BlockPos pos) {
        return new ServerCommandSource(
            CommandOutput.DUMMY,
            Vec3d.ofCenter(pos),
            Vec2f.ZERO,
            level,
            2,
            MOD_ID,
            Text.literal(MOD_ID),
            level.getServer(),
            null
        );
    }

}
