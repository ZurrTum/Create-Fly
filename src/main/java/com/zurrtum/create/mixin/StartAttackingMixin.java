package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StartAttacking.class)
public class StartAttackingMixin {
    @WrapOperation(method = "lambda$create$1(Lnet/minecraft/world/entity/ai/behavior/StartAttacking$StartAttackingCondition;Lnet/minecraft/world/entity/ai/behavior/StartAttacking$TargetFinder;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean ignoreAttack(Mob mob, LivingEntity livingEntity, Operation<Boolean> original) {
        if (livingEntity instanceof DeployerPlayer) {
            CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();
            switch (setting) {
                case ALL -> {
                    return false;
                }
                case CREEPERS -> {
                    if (mob instanceof Creeper) {
                        return false;
                    }
                }
            }
        }
        return original.call(mob, livingEntity);
    }
}
