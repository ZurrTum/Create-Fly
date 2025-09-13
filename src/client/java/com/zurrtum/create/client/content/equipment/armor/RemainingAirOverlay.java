package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.equipment.armor.BacktankUtil;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class RemainingAirOverlay {
    public static void render(MinecraftClient mc, DrawContext guiGraphics) {
        ClientPlayerEntity player = mc.player;
        if (player == null)
            return;
        if (player.isCreative())
            return;
        int timeLeft = AllSynchedDatas.VISUAL_BACKTANK_AIR.get(player);
        if (timeLeft == 0) {
            return;
        }
        boolean isAir = !player.isSubmergedIn(FluidTags.WATER) || player.getWorld()
            .getBlockState(BlockPos.ofFloored(player.getX(), player.getEyeY(), player.getZ())).isOf(Blocks.BUBBLE_COLUMN);
        boolean canBreathe = StatusEffectUtil.hasWaterBreathing(player) || player.getAbilities().invulnerable;
        if ((isAir || canBreathe) && !player.isInLava())
            return;

        Matrix3x2fStack poseStack = guiGraphics.getMatrices();
        poseStack.pushMatrix();

        ItemStack backtank = getDisplayedBacktank(player);
        poseStack.translate(
            guiGraphics.getScaledWindowWidth() / 2 + 90,
            guiGraphics.getScaledWindowHeight() - 53 + (backtank.takesDamageFrom(mc.world.getDamageSources().lava()) ? 0 : 9)
        );

        Text text = Text.literal(StringHelper.formatTicks(Math.max(0, timeLeft - 1) * 20, mc.world.getTickManager().getTickRate()));
        guiGraphics.drawItem(backtank, 0, 0);
        int color = 0xFF_FFFFFF;
        if (timeLeft < 60 && timeLeft % 2 == 0) {
            color = Color.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
        }
        guiGraphics.drawText(mc.textRenderer, text, 16, 5, color, true);

        poseStack.popMatrix();
    }

    public static ItemStack getDisplayedBacktank(ClientPlayerEntity player) {
        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (!backtanks.isEmpty()) {
            return backtanks.getFirst();
        }
        return AllItems.COPPER_BACKTANK.getDefaultStack();
    }

    private static void resetAirData(PlayerEntity player) {
        int old = AllSynchedDatas.VISUAL_BACKTANK_AIR.get(player);
        if (old == 0) {
            return;
        }
        AllSynchedDatas.VISUAL_BACKTANK_AIR.set(player, 0);
    }

    public static void update(ClientPlayerEntity player, World world) {
        if (player.getAbilities().invulnerable) {
            resetAirData(player);
            return;
        }
        boolean lavaDiving = player.isInLava();
        if (!lavaDiving && (!player.isSubmergedIn(FluidTags.WATER) || world.getBlockState(BlockPos.ofFloored(
            player.getX(),
            player.getEyeY(),
            player.getZ()
        )).isOf(Blocks.BUBBLE_COLUMN) || player.canBreatheInWater() || StatusEffectUtil.hasWaterBreathing(player))) {
            resetAirData(player);
            return;
        }

        ItemStack helmet = DivingHelmetItem.getWornItem(player);
        if (helmet.isEmpty()) {
            resetAirData(player);
            return;
        }

        if (lavaDiving && helmet.takesDamageFrom(world.getDamageSources().lava())) {
            resetAirData(player);
            return;
        }

        List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
        if (backtanks.isEmpty() || (lavaDiving && backtanks.stream()
            .allMatch(backtank -> backtank.takesDamageFrom(world.getDamageSources().lava())))) {
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
