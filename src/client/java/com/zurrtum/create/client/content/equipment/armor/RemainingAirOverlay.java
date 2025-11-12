package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.equipment.armor.BacktankUtil;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class RemainingAirOverlay {
    public static void render(Minecraft mc, GuiGraphics guiGraphics) {
        LocalPlayer player = mc.player;
        if (player == null)
            return;
        if (player.isCreative())
            return;
        int timeLeft = AllSynchedDatas.VISUAL_BACKTANK_AIR.get(player);
        if (timeLeft == 0) {
            return;
        }
        boolean isAir = !player.isEyeInFluid(FluidTags.WATER) || player.level()
            .getBlockState(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ())).is(Blocks.BUBBLE_COLUMN);
        boolean canBreathe = MobEffectUtil.hasWaterBreathing(player) || player.getAbilities().invulnerable;
        if ((isAir || canBreathe) && !player.isInLava())
            return;

        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();

        ItemStack backtank = getDisplayedBacktank(player);
        poseStack.translate(
            guiGraphics.guiWidth() / 2 + 90,
            guiGraphics.guiHeight() - 53 + (backtank.canBeHurtBy(mc.level.damageSources().lava()) ? 0 : 9)
        );

        Component text = Component.literal(StringUtil.formatTickDuration(Math.max(0, timeLeft - 1) * 20, mc.level.tickRateManager().tickrate()));
        guiGraphics.renderItem(backtank, 0, 0);
        int color = 0xFF_FFFFFF;
        if (timeLeft < 60 && timeLeft % 2 == 0) {
            color = Color.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
        }
        guiGraphics.drawString(mc.font, text, 16, 5, color, true);

        poseStack.popMatrix();
    }

    public static ItemStack getDisplayedBacktank(LocalPlayer player) {
        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (!backtanks.isEmpty()) {
            return backtanks.getFirst();
        }
        return AllItems.COPPER_BACKTANK.getDefaultInstance();
    }

    private static void resetAirData(Player player) {
        int old = AllSynchedDatas.VISUAL_BACKTANK_AIR.get(player);
        if (old == 0) {
            return;
        }
        AllSynchedDatas.VISUAL_BACKTANK_AIR.set(player, 0);
    }

    public static void update(LocalPlayer player, Level world) {
        if (player.getAbilities().invulnerable) {
            resetAirData(player);
            return;
        }
        boolean lavaDiving = player.isInLava();
        if (!lavaDiving && (!player.isEyeInFluid(FluidTags.WATER) || world.getBlockState(BlockPos.containing(
            player.getX(),
            player.getEyeY(),
            player.getZ()
        )).is(Blocks.BUBBLE_COLUMN) || player.canBreatheUnderwater() || MobEffectUtil.hasWaterBreathing(player))) {
            resetAirData(player);
            return;
        }

        ItemStack helmet = DivingHelmetItem.getWornItem(player);
        if (helmet.isEmpty()) {
            resetAirData(player);
            return;
        }

        if (lavaDiving && helmet.canBeHurtBy(world.damageSources().lava())) {
            resetAirData(player);
            return;
        }

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty() || (lavaDiving && backtanks.stream().allMatch(backtank -> backtank.canBeHurtBy(world.damageSources().lava())))) {
            resetAirData(player);
            return;
        }

        int visualBacktankAir = 0;
        for (ItemStack stack : backtanks)
            visualBacktankAir += BacktankUtil.getAir(stack);

        if (AllSynchedDatas.VISUAL_BACKTANK_AIR.get(player) == visualBacktankAir) {
            return;
        }
        AllSynchedDatas.VISUAL_BACKTANK_AIR.set(player, visualBacktankAir);
    }
}
