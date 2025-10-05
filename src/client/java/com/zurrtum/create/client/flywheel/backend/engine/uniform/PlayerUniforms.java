package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.backend.FlwBackendXplat;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class PlayerUniforms extends UniformWriter {
    private static final int SIZE = 16 * 2 + 8 + 4 * 9;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.PLAYER_INDEX, SIZE);

    private PlayerUniforms() {
    }

    public static void update(RenderContext context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            BUFFER.clear();
            return;
        }

        long ptr = BUFFER.ptr();

        PlayerListEntry info = player.getPlayerListEntry();

        Vec3d eyePos = player.getCameraPosVec(context.partialTick());
        ptr = writeVec3(ptr, (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);

        ptr = writeTeamColor(ptr, info == null ? null : info.getScoreboardTeam());

        ptr = writeEyeBrightness(ptr, player);

        ptr = writeHeldLight(ptr, player);
        ptr = writeEyeIn(ptr, player);

        ptr = writeInt(ptr, player.isInSneakingPose() ? 1 : 0);
        ptr = writeInt(ptr, player.isSleeping() ? 1 : 0);
        ptr = writeInt(ptr, player.isSwimming() ? 1 : 0);
        ptr = writeInt(ptr, player.isGliding() ? 1 : 0);

        ptr = writeInt(ptr, player.isSneaking() ? 1 : 0);

        ptr = writeInt(ptr, info == null ? 0 : info.getGameMode().getIndex());

        BUFFER.markDirty();
    }

    private static long writeTeamColor(long ptr, @Nullable Team team) {
        if (team != null) {
            Integer color = team.getColor().getColorValue();

            if (color != null) {
                int red = ColorHelper.getRed(color);
                int green = ColorHelper.getGreen(color);
                int blue = ColorHelper.getBlue(color);
                return writeVec4(ptr, red / 255f, green / 255f, blue / 255f, 1f);
            } else {
                return writeVec4(ptr, 1f, 1f, 1f, 1f);
            }
        } else {
            return writeVec4(ptr, 1f, 1f, 1f, 0f);
        }
    }

    private static long writeEyeBrightness(long ptr, ClientPlayerEntity player) {
        World level = player.getEntityWorld();
        int blockBrightness = level.getLightLevel(LightType.BLOCK, player.getBlockPos());
        int skyBrightness = level.getLightLevel(LightType.SKY, player.getBlockPos());
        int maxBrightness = 15;

        return writeVec2(ptr, (float) blockBrightness / (float) maxBrightness, (float) skyBrightness / (float) maxBrightness);
    }

    private static long writeHeldLight(long ptr, ClientPlayerEntity player) {
        int heldLight = 0;

        for (Hand hand : Hand.values()) {
            Item handItem = player.getStackInHand(hand).getItem();
            if (handItem instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                int blockLight = FlwBackendXplat.INSTANCE.getLightEmission(block.getDefaultState(), player.getEntityWorld(), player.getBlockPos());
                if (heldLight < blockLight) {
                    heldLight = blockLight;
                }
            }
        }

        return writeFloat(ptr, (float) heldLight / 15);
    }

    private static long writeEyeIn(long ptr, ClientPlayerEntity player) {
        World level = player.getEntityWorld();
        Vec3d eyePos = player.getEyePos();
        BlockPos blockPos = BlockPos.ofFloored(eyePos);
        return writeInFluidAndBlock(ptr, level, blockPos, eyePos);
    }
}
