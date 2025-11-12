package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.storage.DepotMountedStorage;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MountedDepotInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand == InteractionHand.OFF_HAND)
            return false;
        Level world = player.level();
        if (world.isClientSide())
            return true;

        ItemStack itemInHand = player.getItemInHand(activeHand);
        MountedStorageManager manager = contraptionEntity.getContraption().getStorage();

        MountedItemStorage storage = manager.getAllItemStorages().get(localPos);
        if (!(storage instanceof DepotMountedStorage depot))
            return false;

        Optional<TransportedItemStack> itemOnDepot = depot.getHeld();
        if (itemOnDepot.isPresent()) {
            ItemStack heldItem = itemOnDepot.get().stack;
            if (ItemStack.isSameItemSameComponents(heldItem, itemInHand)) {
                int remainder = heldItem.getCount();
                int count = itemInHand.getCount();
                int extract = Math.min(remainder, itemInHand.getMaxStackSize() - count);
                if (extract != 0) {
                    itemInHand.setCount(count + extract);
                    if (extract == remainder) {
                        heldItem = ItemStack.EMPTY;
                    } else {
                        heldItem.setCount(remainder - extract);
                    }
                }
            }
            if (!heldItem.isEmpty()) {
                player.getInventory().placeItemBackInInventory(heldItem);
                world.playSound(
                    null,
                    BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 0)),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    .2f,
                    1f + world.getRandom().nextFloat()
                );
                if (itemInHand.isEmpty()) {
                    depot.removeHeldItem();
                    depot.setChanged();
                    return true;
                }
            }
        }
        if (itemInHand.isEmpty()) {
            return true;
        }

        TransportedItemStack transported = new TransportedItemStack(itemInHand);
        transported.insertedFrom = player.getDirection();
        transported.prevBeltPosition = .25f;
        transported.beltPosition = .25f;
        depot.setHeld(transported);
        depot.setChanged();
        player.setItemInHand(activeHand, ItemStack.EMPTY);
        AllSoundEvents.DEPOT_SLIDE.playOnServer(world, BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 0)));

        return true;
    }

}
