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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CurvedTrackInteraction {
    static int breakTicks;
    static int breakTimeout;
    static float breakProgress;
    static BlockPos breakPos;

    public static void clientTick(Minecraft mc) {
        BezierPointSelection result = TrackBlockOutline.result;
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;

        if (!player.getAbilities().mayBuild)
            return;

        if (mc.options.keyAttack.isDown() && result != null) {
            breakPos = result.blockEntity().getBlockPos();
            BlockState blockState = level.getBlockState(breakPos);
            if (blockState.isAir()) {
                resetBreakProgress(level, player);
                return;
            }

            if (breakTicks % 4.0F == 0.0F) {
                SoundType soundtype = blockState.getSoundType();
                mc.getSoundManager().play(new SimpleSoundInstance(
                    soundtype.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 8.0F,
                    soundtype.getPitch() * 0.5F,
                    level.getRandom(),
                    BlockPos.containing(result.vec())
                ));
            }

            boolean creative = player.getAbilities().instabuild;

            breakTicks++;
            breakTimeout = 2;
            breakProgress += creative ? 0.125f : blockState.getDestroyProgress(player, level, breakPos) / 8f;

            Vec3 vec = VecHelper.offsetRandomly(result.vec(), level.getRandom(), 0.25f);
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec.x, vec.y, vec.z, 0, 0, 0);

            int progress = (int) (breakProgress * 10.0F) - 1;
            level.destroyBlockProgress(player.getId(), breakPos, progress);
            player.swing(InteractionHand.MAIN_HAND);

            if (breakProgress >= 1) {
                player.connection.send(new CurvedTrackDestroyPacket(breakPos, result.loc().curveTarget(), BlockPos.containing(result.vec()), false));
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

    private static void resetBreakProgress(ClientLevel level, LocalPlayer player) {
        if (breakPos != null && level != null)
            level.destroyBlockProgress(player.getId(), breakPos, -1);

        breakProgress = 0;
        breakTicks = 0;
        breakPos = null;
    }

    public static boolean onClickInput(Minecraft mc, boolean attack) {
        BezierPointSelection result = TrackBlockOutline.result;
        if (result == null)
            return false;

        LocalPlayer player = mc.player;
        if (player == null)
            return false;
        ClientLevel level = mc.level;
        if (level == null)
            return false;

        if (attack)
            return true;

        ItemStack heldItem = player.getMainHandItem();
        Item item = heldItem.getItem();
        if (heldItem.is(AllItemTags.TRACKS)) {
            player.displayClientMessage(CreateLang.translateDirect("track.turn_start").withStyle(ChatFormatting.RED), true);
            return true;
        }
        if (item instanceof TrackTargetingBlockItem) {
            useOnCurve(player, result);
            return true;
        }
        if (heldItem.is(AllItems.WRENCH) && player.isShiftKeyDown()) {
            player.connection.send(new CurvedTrackDestroyPacket(
                result.blockEntity().getBlockPos(),
                result.loc().curveTarget(),
                BlockPos.containing(result.vec()),
                true
            ));
            resetBreakProgress(level, player);
            return true;
        }

        return false;
    }

    public static void useOnCurve(LocalPlayer player, BezierPointSelection selection) {
        TrackBlockEntity be = selection.blockEntity();
        BezierTrackPointLocation loc = selection.loc();
        boolean front = player.getLookAngle().dot(selection.direction()) < 0;

        player.connection.send(new CurvedTrackSelectionPacket(
            be.getBlockPos(),
            loc.curveTarget(),
            front,
            loc.segment(),
            player.getInventory().getSelectedSlot()
        ));
    }

}