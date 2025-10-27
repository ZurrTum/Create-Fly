package com.zurrtum.create.client.content.contraptions.wrench;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class RadialWrenchHandler {

    public static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !AllKeys.ROTATE_MENU.isPressed())
            COOLDOWN--;
    }

    public static void onKeyInput(MinecraftClient mc, InputUtil.Key key, boolean pressed) {
        if (!pressed)
            return;

        if (key != AllKeys.ROTATE_MENU.boundKey)
            return;

        if (COOLDOWN > 0)
            return;

        if (mc.interactionManager == null || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
            return;

        ClientPlayerEntity player = mc.player;
        if (player == null)
            return;

        World level = player.getWorld();

        ItemStack heldItem = player.getMainHandStack();
        if (!heldItem.isOf(AllItems.WRENCH))
            return;

        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult blockHitResult))
            return;

        BlockState state = level.getBlockState(blockHitResult.getBlockPos());

        RadialWrenchMenu.tryCreateFor(state, blockHitResult.getBlockPos(), level).ifPresent(ScreenOpener::open);
    }

}
