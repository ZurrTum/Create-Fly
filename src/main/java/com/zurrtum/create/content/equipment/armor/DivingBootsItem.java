package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DivingBootsItem extends Item {
    public static final EquipmentSlot SLOT = EquipmentSlot.FEET;

    public DivingBootsItem(Properties settings) {
        super(settings);
    }

    public static boolean isWornBy(Entity entity) {
        return !getWornItem(entity).isEmpty();
    }

    public static ItemStack getWornItem(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = livingEntity.getItemBySlot(SLOT);
        if (!(stack.getItem() instanceof DivingBootsItem)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static void accelerateDescentUnderwater(Entity entity) {
        if (!(entity instanceof Player player))
            return;

        if (!affects(player))
            return;

        Vec3 motion = player.getDeltaMovement();
        player.setOnGround(player.onGround() || player.verticalCollision);

        if (player.isJumping() && player.onGround()) {
            motion = motion.add(0, .5f, 0);
            player.setOnGround(false);
        } else {
            motion = motion.add(0, -0.05f, 0);
        }

        float multiplier = 1.3f;
        if (motion.multiply(1, 0, 1).length() < 0.145f && (player.zza > 0 || player.xxa != 0) && !player.isShiftKeyDown())
            motion = motion.multiply(multiplier, 1, multiplier);
        player.setDeltaMovement(motion);
    }

    protected static boolean affects(Player player) {
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
        if (!player.isInWater())
            return false;
        if (player.getPose() == Pose.SWIMMING)
            return false;
        return !player.getAbilities().flying;
    }

    public static void onLavaTravel(Player player, boolean onGround) {
        ItemStack bootsStack = DivingBootsItem.getWornItem(player);
        if (!bootsStack.is(AllItems.NETHERITE_DIVING_BOOTS)) {
            return;
        }
        Vec3 playerVelocity = player.getDeltaMovement();
        double yMotion = playerVelocity.y;
        double vMultiplier = yMotion < 0 ? Math.max(0, 2.5 - Math.abs(yMotion) * 2) : 1;
        Vec3 velocity;
        if (onGround) {
            if (player.isJumping()) {
                boolean eyeInFluid = player.isEyeInFluid(FluidTags.LAVA);
                vMultiplier = yMotion == 0 ? 0 : (eyeInFluid ? 1 : 0.5) / yMotion;
            }
            double hMultiplier = player.isSprinting() ? 1.85 : 1.75;
            velocity = new Vec3(hMultiplier, vMultiplier, hMultiplier);
        } else {
            if (yMotion > 0) {
                vMultiplier = 1.3;
            }
            velocity = new Vec3(1.75, vMultiplier, 1.75);
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

        player.setDeltaMovement(playerVelocity.multiply(velocity));
    }
}
