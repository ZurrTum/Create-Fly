package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.storage.DepotMountedStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class MountedDepotInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand == Hand.OFF_HAND)
            return false;
        World world = player.getWorld();
        if (world.isClient)
            return true;

        ItemStack itemInHand = player.getStackInHand(activeHand);
        MountedStorageManager manager = contraptionEntity.getContraption().getStorage();

        MountedItemStorage storage = manager.getAllItemStorages().get(localPos);
        if (!(storage instanceof DepotMountedStorage depot))
            return false;

        Optional<TransportedItemStack> itemOnDepot = depot.getHeld();
        if (itemOnDepot.isPresent()) {
            ItemStack heldItem = itemOnDepot.get().stack;
            if (ItemStack.areItemsAndComponentsEqual(heldItem, itemInHand)) {
                int remainder = heldItem.getCount();
                int count = itemInHand.getCount();
                int extract = Math.min(remainder, itemInHand.getMaxCount() - count);
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
                player.getInventory().offerOrDrop(heldItem);
                world.playSound(
                    null,
                    BlockPos.ofFloored(contraptionEntity.toGlobalVector(Vec3d.ofCenter(localPos), 0)),
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    .2f,
                    1f + world.getRandom().nextFloat()
                );
                if (itemInHand.isEmpty()) {
                    depot.removeHeldItem();
                    depot.markDirty();
                    return true;
                }
            }
        }
        if (itemInHand.isEmpty()) {
            return true;
        }

        TransportedItemStack transported = new TransportedItemStack(itemInHand);
        transported.insertedFrom = player.getHorizontalFacing();
        transported.prevBeltPosition = .25f;
        transported.beltPosition = .25f;
        depot.setHeld(transported);
        depot.markDirty();
        player.setStackInHand(activeHand, ItemStack.EMPTY);
        AllSoundEvents.DEPOT_SLIDE.playOnServer(world, BlockPos.ofFloored(contraptionEntity.toGlobalVector(Vec3d.ofCenter(localPos), 0)));

        return true;
    }

}
