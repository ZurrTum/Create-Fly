package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferAoEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitterOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferAoEmitter.class)
public interface BufferAoEmitterMixin extends BufferEmitterOutput, FabricEmitterSupplier {
    @Shadow(remap = false)
    @Nullable VertexConsumer getBuffer(
        ChunkSectionLayer layer,
        @Nullable Direction shadeDirectionOverride,
        boolean ambientOcclusion
    );

    @Override
    default @NonNull QuadEmitter quadEmitter() {
        return getOrCreateEmitter((BufferAoEmitter emitter) -> Renderer.get().quadEmitter(quad -> {
            VertexConsumer buffer = emitter.getBuffer(
                quad.chunkLayer(),
                quad.shadeDirectionOverride(),
                quad.ambientOcclusion().get()
            );
            if (buffer == null) {
                return;
            }
            quad.buffer(OverlayTexture.NO_OVERLAY, buffer);
        }));
    }
}
