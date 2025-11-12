package com.zurrtum.create.client.content.contraptions.wrench;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class RadialWrenchHandler {

    public static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !AllKeys.ROTATE_MENU.isDown())
            COOLDOWN--;
    }

    public static void onKeyInput(Minecraft mc, KeyEvent input, boolean pressed) {
        if (!pressed)
            return;

        if (!AllKeys.ROTATE_MENU.matches(input))
            return;

        if (COOLDOWN > 0)
            return;

        if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        LocalPlayer player = mc.player;
        if (player == null)
            return;

        Level level = player.level();

        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(AllItems.WRENCH))
            return;

        HitResult objectMouseOver = mc.hitResult;
        if (!(objectMouseOver instanceof BlockHitResult blockHitResult))
            return;

        BlockState state = level.getBlockState(blockHitResult.getBlockPos());

        RadialWrenchMenu.tryCreateFor(state, blockHitResult.getBlockPos(), level).ifPresent(ScreenOpener::open);
    }

}
