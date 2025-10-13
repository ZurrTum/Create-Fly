package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Shadow
    @Final
    World world;

    @Inject(method = "setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void flywheel$onBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(world);
        if (manager == null) {
            return;
        }

        manager.blockEntities().queueAdd(blockEntity);
    }
}
