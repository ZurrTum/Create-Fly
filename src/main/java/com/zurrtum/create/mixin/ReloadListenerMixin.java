package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(CreateResourceReloader.class)
public abstract class ReloadListenerMixin implements FabricResourceReloader {
    @Shadow
    public abstract Identifier getId();

    @Override
    public @NonNull Identifier fabric$getId() {
        return getId();
    }
}
