package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.hats.HatState;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfo;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements HatState {
    @Unique
    private PartialModel hat;
    @Unique
    private TrainHatInfo hatInfo;

    @Override
    public void create$setHat(PartialModel hat) {
        this.hat = hat;
    }

    @Override
    public PartialModel create$getHat() {
        return hat;
    }

    @Override
    public void create$updateHatInfo(Entity entity) {
        hatInfo = TrainHatInfoReloadListener.getHatInfoFor(entity);
    }

    public TrainHatInfo create$getHatInfo() {
        return hatInfo;
    }
}
