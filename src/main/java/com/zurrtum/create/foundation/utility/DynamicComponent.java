package com.zurrtum.create.foundation.utility;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public class DynamicComponent {
    @Nullable
    public static Component parseCustomText(Level level, BlockPos pos, Component customText) {
        if (!(level instanceof ServerLevel serverLevel))
            return null;
        try {
            return ComponentUtils.updateForEntity(getCommandSource(serverLevel, pos), customText, null, 0);
        } catch (JsonParseException | CommandSyntaxException e) {
            return null;
        }
    }

    public static CommandSourceStack getCommandSource(ServerLevel level, BlockPos pos) {
        return new CommandSourceStack(
            CommandSource.NULL,
            Vec3.atCenterOf(pos),
            Vec2.ZERO,
            level,
            LevelBasedPermissionSet.GAMEMASTER,
            MOD_ID,
            Component.literal(MOD_ID),
            level.getServer(),
            null
        );
    }

}
