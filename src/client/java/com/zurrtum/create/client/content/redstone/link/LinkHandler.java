package com.zurrtum.create.client.content.redstone.link;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.infrastructure.packet.c2s.LinkSettingsPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class LinkHandler {
    @Nullable
    public static InteractionResult onBlockActivated(Level world, LocalPlayer player, InteractionHand hand, BlockHitResult ray) {
        if (player.isShiftKeyDown() || player.isSpectator())
            return null;

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(AllItems.LINKED_CONTROLLER))
            return null;
        if (heldItem.is(AllItems.WRENCH))
            return null;

        BlockPos pos = ray.getBlockPos();
        LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
        if (behaviour == null)
            return null;

        for (boolean first : Iterate.trueAndFalse) {
            if (behaviour.testHit(first, ray.getLocation())) {
                behaviour.setFrequency(first, heldItem);
                world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
                player.connection.send(new LinkSettingsPacket(pos, first, hand));
                return InteractionResult.SUCCESS;
            }
        }
        return null;
    }
}
