package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.packet.s2c.ShootGadgetPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;
import java.util.function.Predicate;

public class ShootableGadgetItemMethods {

    public static void applyCooldown(PlayerEntity player, ItemStack item, Hand hand, Predicate<ItemStack> predicate, int cooldown) {
        if (cooldown <= 0)
            return;

        boolean gunInOtherHand = predicate.test(player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
        player.getItemCooldownManager().set(item, gunInOtherHand ? cooldown * 2 / 3 : cooldown);
    }

    public static void sendPackets(PlayerEntity player, Function<Boolean, ? extends ShootGadgetPacket> factory) {
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return;
        serverPlayer.getEntityWorld().getChunkManager().sendToOtherNearbyPlayers(player, factory.apply(false));
        serverPlayer.networkHandler.sendPacket(factory.apply(true));
    }

    public static boolean shouldSwap(PlayerEntity player, ItemStack item, Hand hand, Predicate<ItemStack> predicate) {
        boolean isSwap = item.contains(AllDataComponents.SHAPER_SWAP);
        boolean mainHand = hand == Hand.MAIN_HAND;
        boolean gunInOtherHand = predicate.test(player.getStackInHand(mainHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

        // Pass To Offhand
        if (mainHand && isSwap && gunInOtherHand)
            return true;
        if (mainHand && !isSwap && gunInOtherHand)
            item.set(AllDataComponents.SHAPER_SWAP, true);
        if (!mainHand && isSwap)
            item.remove(AllDataComponents.SHAPER_SWAP);
        if (!mainHand && gunInOtherHand)
            player.getStackInHand(Hand.MAIN_HAND).remove(AllDataComponents.SHAPER_SWAP);
        player.setCurrentHand(hand);
        return false;
    }

    public static Vec3d getGunBarrelVec(PlayerEntity player, boolean mainHand, Vec3d rightHandForward) {
        Vec3d start = player.getPos().add(0, player.getStandingEyeHeight(), 0);
        float yaw = (float) ((player.getYaw()) / -180 * Math.PI);
        float pitch = (float) ((player.getPitch()) / -180 * Math.PI);
        int flip = mainHand == (player.getMainArm() == Arm.RIGHT) ? -1 : 1;
        Vec3d barrelPosNoTransform = new Vec3d(flip * rightHandForward.x, rightHandForward.y, rightHandForward.z);
        Vec3d barrelPos = start.add(barrelPosNoTransform.rotateX(pitch).rotateY(yaw));
        return barrelPos;
    }

}
