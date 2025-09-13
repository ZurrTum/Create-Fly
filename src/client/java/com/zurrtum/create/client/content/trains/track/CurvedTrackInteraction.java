package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.infrastructure.packet.c2s.CurvedTrackDestroyPacket;
import com.zurrtum.create.infrastructure.packet.c2s.CurvedTrackSelectionPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CurvedTrackInteraction {
    static int breakTicks;
    static int breakTimeout;
    static float breakProgress;
    static BlockPos breakPos;

    public static void clientTick(MinecraftClient mc) {
        BezierPointSelection result = TrackBlockOutline.result;
        ClientPlayerEntity player = mc.player;
        ClientWorld level = mc.world;

        if (!player.getAbilities().allowModifyWorld)
            return;

        if (mc.options.attackKey.isPressed() && result != null) {
            breakPos = result.blockEntity().getPos();
            BlockState blockState = level.getBlockState(breakPos);
            if (blockState.isAir()) {
                resetBreakProgress(level, player);
                return;
            }

            if (breakTicks % 4.0F == 0.0F) {
                BlockSoundGroup soundtype = blockState.getSoundGroup();
                mc.getSoundManager().play(new PositionedSoundInstance(
                    soundtype.getHitSound(),
                    SoundCategory.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 8.0F,
                    soundtype.getPitch() * 0.5F,
                    level.random,
                    BlockPos.ofFloored(result.vec())
                ));
            }

            boolean creative = player.getAbilities().creativeMode;

            breakTicks++;
            breakTimeout = 2;
            breakProgress += creative ? 0.125f : blockState.calcBlockBreakingDelta(player, level, breakPos) / 8f;

            Vec3d vec = VecHelper.offsetRandomly(result.vec(), level.random, 0.25f);
            level.addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), vec.x, vec.y, vec.z, 0, 0, 0);

            int progress = (int) (breakProgress * 10.0F) - 1;
            level.setBlockBreakingInfo(player.getId(), breakPos, progress);
            player.swingHand(Hand.MAIN_HAND);

            if (breakProgress >= 1) {
                player.networkHandler.sendPacket(new CurvedTrackDestroyPacket(
                    breakPos,
                    result.loc().curveTarget(),
                    BlockPos.ofFloored(result.vec()),
                    false
                ));
                resetBreakProgress(level, player);
            }

            return;
        }

        if (breakTimeout == 0)
            return;
        if (--breakTimeout > 0)
            return;

        resetBreakProgress(level, player);
    }

    private static void resetBreakProgress(ClientWorld level, ClientPlayerEntity player) {
        if (breakPos != null && level != null)
            level.setBlockBreakingInfo(player.getId(), breakPos, -1);

        breakProgress = 0;
        breakTicks = 0;
        breakPos = null;
    }

    public static boolean onClickInput(MinecraftClient mc, boolean attack) {
        BezierPointSelection result = TrackBlockOutline.result;
        if (result == null)
            return false;

        ClientPlayerEntity player = mc.player;
        if (player == null)
            return false;
        ClientWorld level = mc.world;
        if (level == null)
            return false;

        if (attack)
            return true;

        ItemStack heldItem = player.getMainHandStack();
        Item item = heldItem.getItem();
        if (heldItem.isIn(AllItemTags.TRACKS)) {
            player.sendMessage(CreateLang.translateDirect("track.turn_start").formatted(Formatting.RED), true);
            return true;
        }
        if (item instanceof TrackTargetingBlockItem) {
            useOnCurve(player, result);
            return true;
        }
        if (heldItem.isOf(AllItems.WRENCH) && player.isSneaking()) {
            player.networkHandler.sendPacket(new CurvedTrackDestroyPacket(
                result.blockEntity().getPos(),
                result.loc().curveTarget(),
                BlockPos.ofFloored(result.vec()),
                true
            ));
            resetBreakProgress(level, player);
            return true;
        }

        return false;
    }

    public static void useOnCurve(ClientPlayerEntity player, BezierPointSelection selection) {
        TrackBlockEntity be = selection.blockEntity();
        BezierTrackPointLocation loc = selection.loc();
        boolean front = player.getRotationVector().dotProduct(selection.direction()) < 0;

        player.networkHandler.sendPacket(new CurvedTrackSelectionPacket(
            be.getPos(),
            loc.curveTarget(),
            front,
            loc.segment(),
            player.getInventory().getSelectedSlot()
        ));
    }

}