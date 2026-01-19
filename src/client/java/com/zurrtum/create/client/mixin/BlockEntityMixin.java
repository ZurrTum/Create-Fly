package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Shadow
    @Nullable
    protected Level level;

    @Inject(method = "setRemoved()V", at = @At("TAIL"))
    private void flywheel$removeVisual(CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(level);
        if (manager == null) {
            return;
        }

        manager.blockEntities().queueRemove((BlockEntity) (Object) this);
    }
}
