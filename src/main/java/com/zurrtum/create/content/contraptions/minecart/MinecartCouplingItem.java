package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;

public class MinecartCouplingItem extends Item {

    public MinecartCouplingItem(Settings p_i48487_1_) {
        super(p_i48487_1_);
    }

    public static ActionResult handleInteractionWithMinecart(PlayerEntity player, Hand hand, Entity interacted) {
        if (!(interacted instanceof AbstractMinecartEntity minecart))
            return null;
        Optional<MinecartController> value = AllSynchedDatas.MINECART_CONTROLLER.get(minecart);
        if (value.isEmpty())
            return null;
        MinecartController controller = value.get();
        if (!controller.isPresent())
            return null;

        ItemStack heldItem = player.getStackInHand(hand);
        if (heldItem.isOf(AllItems.MINECART_COUPLING)) {
            onCouplingInteractOnMinecart(minecart, player, controller);
        } else if (heldItem.isOf(AllItems.WRENCH)) {
            if (!onWrenchInteractOnMinecart(player, controller))
                return null;
        } else
            return null;

        return ActionResult.SUCCESS;
    }

    protected static void onCouplingInteractOnMinecart(AbstractMinecartEntity minecart, PlayerEntity player, MinecartController controller) {
        World world = player.getWorld();
        if (controller.isFullyCoupled()) {
            if (!world.isClient)
                CouplingHandler.status(player, "two_couplings_max");
        }
        if (world != null && world.isClient)
            AllClientHandle.INSTANCE.cartClicked(player, minecart);
    }

    private static boolean onWrenchInteractOnMinecart(PlayerEntity player, MinecartController controller) {
        World world = player.getWorld();
        int couplings = (controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
        if (couplings == 0)
            return false;
        if (world.isClient)
            return true;

        for (boolean forward : Iterate.trueAndFalse) {
            if (controller.hasContraptionCoupling(forward))
                couplings--;
        }

        CouplingHandler.status(player, "removed");
        controller.decouple();
        if (!player.isCreative())
            player.getInventory().offerOrDrop(new ItemStack(AllItems.MINECART_COUPLING, couplings));
        return true;
    }
}
