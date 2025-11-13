package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            AllAdvancements.CARDBOARD_ARMOR.trigger(serverPlayer);
        }

        float width = 0.6F * scale;
        float height = 0.8F * scale;
        return new EntityDimensions(width, height, width, EntityAttachments.createDefault(width, height), true);
    }

    public static void playerChangesEquipment(Player player) {
        if (player.getPose() == Pose.CROUCHING && (isCardboardArmor(player.getItemBySlot(EquipmentSlot.HEAD)) || isCardboardArmor(player.getItemBySlot(
            EquipmentSlot.CHEST)) || isCardboardArmor(player.getItemBySlot(EquipmentSlot.LEGS)) || isCardboardArmor(player.getItemBySlot(EquipmentSlot.FEET)))) {
            player.getEntityData().set(Entity.DATA_POSE, Pose.CROUCHING, true);
        }
    }

    public static void mobsMayLoseTargetWhenItIsWearingCardboard(Entity entity) {
        if (!(entity instanceof Mob mob))
            return;
        if (mob.tickCount % 16 != 0)
            return;

        if (testForStealth(mob.getTarget())) {
            mob.setTarget(null);
            if (mob.targetSelector != null)
                for (WrappedGoal goal : mob.targetSelector.getAvailableGoals()) {
                    if (goal.isRunning() && goal.getGoal() instanceof TargetGoal tg)
                        tg.stop();
                }
        }

        if (entity instanceof NeutralMob nMob && entity.level() instanceof ServerLevel sl) {
            LivingEntity target = EntityReference.getLivingEntity(nMob.getPersistentAngerTarget(), sl);
            if (testForStealth(target))
                nMob.stopBeingAngry();
        }

        if (testForStealth(mob.getLastHurtByMob())) {
            mob.setLastHurtByMob(null);
            mob.setLastHurtByPlayer((EntityReference<Player>) null, 0);
        }
    }

    public static boolean testForStealth(Entity entityIn) {
        if (!(entityIn instanceof LivingEntity entity))
            return false;
        if (entity.getPose() != Pose.CROUCHING)
            return false;
        if (entity instanceof Player player && player.getAbilities().flying)
            return false;
        if (!isCardboardArmor(entity.getItemBySlot(EquipmentSlot.HEAD)))
            return false;
        if (!isCardboardArmor(entity.getItemBySlot(EquipmentSlot.CHEST)))
            return false;
        if (!isCardboardArmor(entity.getItemBySlot(EquipmentSlot.LEGS)))
            return false;
        return isCardboardArmor(entity.getItemBySlot(EquipmentSlot.FEET));
    }

    public static boolean isCardboardArmor(ItemStack stack) {
        return stack.getItem() instanceof CardboardArmorItem;
    }
}
