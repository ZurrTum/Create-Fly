package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CouplingHandler {
    public static boolean preventEntitiesFromMoutingOccupiedCart(Entity entityMounting, Entity entityBeingMounted) {
        if (entityBeingMounted instanceof AbstractMinecart cart) {
            Optional<MinecartController> value = AllSynchedDatas.MINECART_CONTROLLER.get(cart);
            if (value.isPresent()) {
                return !(entityMounting instanceof AbstractContraptionEntity) && value.get().isCoupledThroughContraption();
            }
        }
        return false;
    }

    public static void forEachLoadedCoupling(Level world, Consumer<Couple<MinecartController>> consumer) {
        if (world == null)
            return;
        Set<UUID> cartsWithCoupling = CapabilityMinecartController.loadedMinecartsWithCoupling.get(world);
        if (cartsWithCoupling == null)
            return;

        for (UUID id : cartsWithCoupling) {
            MinecartController controller = CapabilityMinecartController.getIfPresent(world, id);
            if (controller == null)
                return;
            if (!controller.isLeadingCoupling())
                return;
            UUID coupledCart = controller.getCoupledCart(true);
            MinecartController coupledController = CapabilityMinecartController.getIfPresent(world, coupledCart);
            if (coupledController == null)
                return;
            consumer.accept(Couple.create(controller, coupledController));
        }
        ;
    }

    public static boolean tryToCoupleCarts(@Nullable Player player, Level world, int cartId1, int cartId2) {
        Entity entity1 = world.getEntity(cartId1);
        Entity entity2 = world.getEntity(cartId2);

        if (!(entity1 instanceof AbstractMinecart cart1))
            return false;
        if (!(entity2 instanceof AbstractMinecart cart2))
            return false;

        String tooMany = "two_couplings_max";
        String unloaded = "unloaded";
        String noLoops = "no_loops";
        String tooFar = "too_far";

        int distanceTo = (int) entity1.position().distanceTo(entity2.position());
        boolean contraptionCoupling = player == null;

        if (distanceTo < 2) {
            if (contraptionCoupling)
                return false; // dont allow train contraptions with <2 distance
            distanceTo = 2;
        }

        if (distanceTo > AllConfigs.server().kinetics.maxCartCouplingLength.get()) {
            status(player, tooFar);
            return false;
        }

        UUID mainID = cart1.getUUID();
        UUID connectedID = cart2.getUUID();
        MinecartController mainController = CapabilityMinecartController.getIfPresent(world, mainID);
        MinecartController connectedController = CapabilityMinecartController.getIfPresent(world, connectedID);

        if (mainController == null || connectedController == null) {
            status(player, unloaded);
            return false;
        }
        if (mainController.isFullyCoupled() || connectedController.isFullyCoupled()) {
            status(player, tooMany);
            return false;
        }

        if (mainController.isLeadingCoupling() && mainController.getCoupledCart(true)
            .equals(connectedID) || connectedController.isLeadingCoupling() && connectedController.getCoupledCart(true).equals(mainID))
            return false;

        for (boolean main : Iterate.trueAndFalse) {
            MinecartController current = main ? mainController : connectedController;
            boolean forward = current.isLeadingCoupling();
            int safetyCount = 1000;

            while (true) {
                if (safetyCount-- <= 0) {
                    Create.LOGGER.warn("Infinite loop in coupling iteration");
                    return false;
                }

                Optional<MinecartController> next = getNextInCouplingChainLegacy(world, current, forward);
                if (next == null) {
                    status(player, unloaded);
                    return false;
                }
                if (next.isEmpty()) {
                    break;
                }
                current = next.get();
                if (current == connectedController) {
                    status(player, noLoops);
                    return false;
                }
            }
        }

        if (!contraptionCoupling) {
            for (InteractionHand hand : InteractionHand.values()) {
                if (player.isCreative())
                    break;
                ItemStack heldItem = player.getItemInHand(hand);
                if (!heldItem.is(AllItems.MINECART_COUPLING))
                    continue;
                heldItem.shrink(1);
                break;
            }
        }

        mainController.prepareForCoupling(true);
        connectedController.prepareForCoupling(false);

        mainController.coupleWith(true, connectedID, distanceTo, contraptionCoupling);
        connectedController.coupleWith(false, mainID, distanceTo, contraptionCoupling);
        return true;
    }

    /**
     * Optional.EMPTY if none connected, null if not yet loaded
     */
    @Nullable
    public static Optional<MinecartController> getNextInCouplingChainLegacy(Level world, MinecartController controller, boolean forward) {
        UUID coupledCart = controller.getCoupledCart(forward);
        if (coupledCart == null)
            return Optional.empty();
        MinecartController coupledController = CapabilityMinecartController.getIfPresent(world, coupledCart);
        return coupledController == null ? null : Optional.of(coupledController);
    }

    public static Optional<MinecartController> getNextInCouplingChain(Level world, MinecartController controller, boolean forward) {
        UUID coupledCart = controller.getCoupledCart(forward);
        if (coupledCart == null)
            return Optional.empty();
        return Optional.ofNullable(CapabilityMinecartController.getIfPresent(world, coupledCart));
    }

    public static void status(Player player, String key) {
        if (player == null)
            return;
        player.displayClientMessage(Component.translatable("create.minecart_coupling." + key), true);
    }

}