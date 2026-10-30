package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferAoEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferAoPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferAoPoseEmitter.class)
public interface BufferAoPoseEmitterMixin extends BufferAoEmitter, FabricEmitterSupplier {
    @Shadow(remap = false)
    Pose getPose();

    @Override
    default @NonNull QuadEmitter quadEmitter() {
        return getOrCreateEmitter((BufferAoPoseEmitter emitter) -> Renderer.get().quadEmitter(quad -> {
            VertexConsumer buffer = emitter.getBuffer(
                quad.chunkLayer(),
                quad.shadeDirectionOverride(),
                quad.ambientOcclusion().get()
            );
            if (buffer == null) {
                return;
            }
            quad.buffer(OverlayTexture.NO_OVERLAY, emitter.getPose(), buffer);
        }));
    }
}
