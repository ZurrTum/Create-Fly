package com.zurrtum.create.client.content.redstone.link;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.LinkSettingsPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkHandler {
    public static ActionResult onBlockActivated(World world, ClientPlayerEntity player, Hand hand, BlockHitResult ray) {
        if (player.isSneaking() || player.isSpectator())
            return null;

        ItemStack heldItem = player.getStackInHand(hand);
        if (heldItem.isOf(AllItems.LINKED_CONTROLLER))
            return null;
        if (heldItem.isOf(AllItems.WRENCH))
            return null;

        BlockPos pos = ray.getBlockPos();
        LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
        if (behaviour == null)
            return null;

        for (boolean first : Iterate.trueAndFalse) {
            if (behaviour.testHit(first, ray.getPos())) {
                behaviour.setFrequency(first, heldItem);
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
                player.networkHandler.sendPacket(new LinkSettingsPacket(pos, first, hand));
                return ActionResult.SUCCESS;
            }
        }
        return null;
    }
}
