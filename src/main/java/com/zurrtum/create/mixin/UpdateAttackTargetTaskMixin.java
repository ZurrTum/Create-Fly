package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UpdateAttackTargetTask.class)
public class UpdateAttackTargetTaskMixin {
    @WrapOperation(method = "method_47123(Lnet/minecraft/entity/ai/brain/task/UpdateAttackTargetTask$StartCondition;Lnet/minecraft/entity/ai/brain/task/UpdateAttackTargetTask$TargetGetter;Lnet/minecraft/entity/ai/brain/MemoryQueryResult;Lnet/minecraft/entity/ai/brain/MemoryQueryResult;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;J)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;canTarget(Lnet/minecraft/entity/LivingEntity;)Z"))
    private static boolean ignoreAttack(MobEntity mob, LivingEntity livingEntity, Operation<Boolean> original) {
        if (livingEntity instanceof DeployerPlayer) {
            CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();
            switch (setting) {
                case ALL -> {
                    return false;
                }
                case CREEPERS -> {
                    if (mob instanceof CreeperEntity) {
                        return false;
                    }
                }
            }
        }
        return original.call(mob, livingEntity);
    }
}
