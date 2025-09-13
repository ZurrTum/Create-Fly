package com.zurrtum.create.mixin;

import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void ignoreAttack(LivingEntity target, CallbackInfo ci) {
        if (target instanceof DeployerPlayer) {
            CKinetics.DeployerAggroSetting setting = AllConfigs.server().kinetics.ignoreDeployerAttacks.get();
            switch (setting) {
                case ALL -> ci.cancel();
                case CREEPERS -> {
                    if (((MobEntity) (Object) this) instanceof CreeperEntity) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
