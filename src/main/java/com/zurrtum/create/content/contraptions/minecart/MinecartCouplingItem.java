package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;

import java.util.Optional;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MinecartCouplingItem extends Item {

    public MinecartCouplingItem(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    public static InteractionResult handleInteractionWithMinecart(Player player, InteractionHand hand, Entity interacted) {
        if (!(interacted instanceof AbstractMinecart minecart))
            return null;
        Optional<MinecartController> value = AllSynchedDatas.MINECART_CONTROLLER.get(minecart);
        if (value.isEmpty())
            return null;
        MinecartController controller = value.get();
        if (!controller.isPresent())
            return null;

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(AllItems.MINECART_COUPLING)) {
            onCouplingInteractOnMinecart(minecart, player, controller);
        } else if (heldItem.is(AllItems.WRENCH)) {
            if (!onWrenchInteractOnMinecart(player, controller))
                return null;
        } else
            return null;

        return InteractionResult.SUCCESS;
    }

    protected static void onCouplingInteractOnMinecart(AbstractMinecart minecart, Player player, MinecartController controller) {
        Level world = player.level();
        if (controller.isFullyCoupled()) {
            if (!world.isClientSide())
                CouplingHandler.status(player, "two_couplings_max");
        }
        if (world != null && world.isClientSide())
            AllClientHandle.INSTANCE.cartClicked(player, minecart);
    }

    private static boolean onWrenchInteractOnMinecart(Player player, MinecartController controller) {
        Level world = player.level();
        int couplings = (controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
        if (couplings == 0)
            return false;
        if (world.isClientSide())
            return true;

        for (boolean forward : Iterate.trueAndFalse) {
            if (controller.hasContraptionCoupling(forward))
                couplings--;
        }

        CouplingHandler.status(player, "removed");
        controller.decouple();
        if (!player.isCreative())
            player.getInventory().placeItemBackInInventory(new ItemStack(AllItems.MINECART_COUPLING, couplings));
        return true;
    }
}
