package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllAdvancements;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class CardboardArmorHandler {
    public static EntityDimensions playerHitboxChangesWhenHidingAsBox(Entity entity) {
        if (!testForStealth(entity))
            return null;
        if (!testForStealth(entity))
            return null;
        float scale;
        if (entity instanceof LivingEntity le) {
            scale = le.getScale();
        } else {
            scale = 1.0F;
        }

        if (!entity.getEntityWorld().isClient() && entity instanceof ServerPlayerEntity serverPlayer) {
            AllAdvancements.CARDBOARD_ARMOR.trigger(serverPlayer);
        }

        float width = 0.6F * scale;
        float height = 0.8F * scale;
        return new EntityDimensions(width, height, width, EntityAttachments.of(width, height), true);
    }

    public static void playerChangesEquipment(PlayerEntity player) {
        if (player.getPose() == EntityPose.CROUCHING && (isCardboardArmor(player.getEquippedStack(EquipmentSlot.HEAD)) || isCardboardArmor(player.getEquippedStack(
            EquipmentSlot.CHEST)) || isCardboardArmor(player.getEquippedStack(EquipmentSlot.LEGS)) || isCardboardArmor(player.getEquippedStack(
            EquipmentSlot.FEET)))) {
            player.getDataTracker().set(Entity.POSE, EntityPose.CROUCHING, true);
        }
    }

    public static void mobsMayLoseTargetWhenItIsWearingCardboard(Entity entity) {
        if (!(entity instanceof MobEntity mob))
            return;
        if (mob.age % 16 != 0)
            return;

        if (testForStealth(mob.getTarget())) {
            mob.setTarget(null);
            if (mob.targetSelector != null)
                for (PrioritizedGoal goal : mob.targetSelector.getGoals()) {
                    if (goal.isRunning() && goal.getGoal() instanceof TrackTargetGoal tg)
                        tg.stop();
                }
        }

        if (entity instanceof Angerable nMob && entity.getEntityWorld() instanceof ServerWorld sl) {
            UUID uuid = nMob.getAngryAt();
            if (uuid != null && testForStealth(sl.getEntity(uuid)))
                nMob.stopAnger();
        }

        if (testForStealth(mob.getAttacker())) {
            mob.setAttacker(null);
            mob.setAttacking((LazyEntityReference<PlayerEntity>) null, 0);
        }
    }

    public static boolean testForStealth(Entity entityIn) {
        if (!(entityIn instanceof LivingEntity entity))
            return false;
        if (entity.getPose() != EntityPose.CROUCHING)
            return false;
        if (entity instanceof PlayerEntity player && player.getAbilities().flying)
            return false;
        if (!isCardboardArmor(entity.getEquippedStack(EquipmentSlot.HEAD)))
            return false;
        if (!isCardboardArmor(entity.getEquippedStack(EquipmentSlot.CHEST)))
            return false;
        if (!isCardboardArmor(entity.getEquippedStack(EquipmentSlot.LEGS)))
            return false;
        return isCardboardArmor(entity.getEquippedStack(EquipmentSlot.FEET));
    }

    public static boolean isCardboardArmor(ItemStack stack) {
        return stack.getItem() instanceof CardboardArmorItem;
    }
}
