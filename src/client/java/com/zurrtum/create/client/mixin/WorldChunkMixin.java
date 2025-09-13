package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Shadow
    @Final
    World world;

    @Shadow
    public abstract Map<BlockPos, BlockEntity> getBlockEntities();

    @Inject(method = "setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void flywheel$onBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
        VisualizationManager manager = VisualizationManager.get(world);
        if (manager == null) {
            return;
        }

        manager.blockEntities().queueAdd(blockEntity);
    }

    @Inject(method = "clear()V", at = @At("HEAD"))
    private void clear(CallbackInfo ci) {
        getBlockEntities().values().forEach(blockEntity -> {
            if (blockEntity instanceof SmartBlockEntity sbe) {
                sbe.onChunkUnloaded();
            }
        });
    }
}
