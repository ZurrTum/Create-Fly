package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.item.TooltipWorldContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.item.Item$TooltipContext$2")
public class TooltipContextMixin implements TooltipWorldContext {
    @Shadow
    @Final
    Level field_51354;

    @Override
    public Level create$getWorld() {
        return field_51354;
    }
}
