package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.item.TooltipWorldContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.item.Item$TooltipContext$2")
public class TooltipContextMixin implements TooltipWorldContext {
    @Shadow
    @Final
    World field_51354;

    @Override
    public World create$getWorld() {
        return field_51354;
    }
}
