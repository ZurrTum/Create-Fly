package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;

public class DivingBootsItem extends Item {
    public static final EquipmentSlot SLOT = EquipmentSlot.FEET;

    public DivingBootsItem(Settings settings) {
        super(settings);
    }

    public static boolean isWornBy(Entity entity) {
        return !getWornItem(entity).isEmpty();
    }

    public static ItemStack getWornItem(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = livingEntity.getEquippedStack(SLOT);
        if (!(stack.getItem() instanceof DivingBootsItem)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static void accelerateDescentUnderwater(Entity entity) {
        if (!(entity instanceof PlayerEntity player))
            return;

        if (!affects(player))
            return;

        Vec3d motion = player.getVelocity();
        player.setOnGround(player.isOnGround() || player.verticalCollision);

        if (player.isJumping() && player.isOnGround()) {
            motion = motion.add(0, .5f, 0);
            player.setOnGround(false);
        } else {
            motion = motion.add(0, -0.05f, 0);
        }

        float multiplier = 1.3f;
        if (motion.multiply(1, 0, 1).length() < 0.145f && (player.forwardSpeed > 0 || player.sidewaysSpeed != 0) && !player.isSneaking())
            motion = motion.multiply(multiplier, 1, multiplier);
        player.setVelocity(motion);
    }

    protected static boolean affects(PlayerEntity player) {
        boolean old = AllSynchedDatas.HEAVY_BOOTS.get(player);
        if (!isWornBy(player)) {
            if (old) {
                AllSynchedDatas.HEAVY_BOOTS.set(player, false);
            }
            return false;
        }
        if (!old) {
            AllSynchedDatas.HEAVY_BOOTS.set(player, true);
        }
        if (!player.isTouchingWater())
            return false;
        if (player.getPose() == EntityPose.SWIMMING)
            return false;
        return !player.getAbilities().flying;
    }

    public static void onLavaTravel(PlayerEntity player, boolean onGround) {
        ItemStack bootsStack = DivingBootsItem.getWornItem(player);
        if (!bootsStack.isOf(AllItems.NETHERITE_DIVING_BOOTS)) {
            return;
        }
        Vec3d playerVelocity = player.getVelocity();
        double yMotion = playerVelocity.y;
        double vMultiplier = yMotion < 0 ? Math.max(0, 2.5 - Math.abs(yMotion) * 2) : 1;
        Vec3d velocity;
        if (onGround) {
            if (player.isJumping()) {
                boolean eyeInFluid = player.isSubmergedIn(FluidTags.LAVA);
                vMultiplier = yMotion == 0 ? 0 : (eyeInFluid ? 1 : 0.5) / yMotion;
            }
            double hMultiplier = player.isSprinting() ? 1.85 : 1.75;
            velocity = new Vec3d(hMultiplier, vMultiplier, hMultiplier);
        } else {
            if (yMotion > 0) {
                vMultiplier = 1.3;
            }
            velocity = new Vec3d(1.75, vMultiplier, 1.75);
        }
        //        if (!player.isOnGround()) {
        //            if (player.isJumping() && player.getPersistentData().contains("LavaGrounded")) {
        //                boolean eyeInFluid = player.isSubmergedIn(FluidTags.LAVA);
        //                vMultiplier = yMotion == 0 ? 0 : (eyeInFluid ? 1 : 0.5) / yMotion;
        //            } else if (yMotion > 0) {
        //                vMultiplier = 1.3;
        //            }
        //
        //            player.getPersistentData().remove("LavaGrounded");
        //            velocity = new Vec3d(1.75, vMultiplier, 1.75);
        //        } else {
        //            player.getPersistentData().putBoolean("LavaGrounded", true);
        //            double hMultiplier = player.isSprinting() ? 1.85 : 1.75;
        //            velocity = new Vec3d(hMultiplier, vMultiplier, hMultiplier);
        //        }

        player.setVelocity(playerVelocity.multiply(velocity));
    }
}
