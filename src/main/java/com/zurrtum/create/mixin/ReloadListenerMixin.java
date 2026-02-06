package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.fabricmc.fabric.impl.resource.v1.FabricResourceReloader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(CreateResourceReloader.class)
public abstract class ReloadListenerMixin implements FabricResourceReloader {
    @Shadow
    public abstract Identifier getId();

    @Override
    public Identifier fabric$getId() {
        return getId();
    }
}