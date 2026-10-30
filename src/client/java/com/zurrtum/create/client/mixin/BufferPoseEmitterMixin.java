package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferPoseEmitter.class)
public interface BufferPoseEmitterMixin extends BufferEmitter, FabricEmitterSupplier {
    @Shadow(remap = false)
    Pose getPose();

    @Override
    default @NonNull QuadEmitter quadEmitter() {
        return getOrCreateEmitter((BufferPoseEmitter emitter) -> Renderer.get().quadEmitter(quad -> quad.buffer(
            OverlayTexture.NO_OVERLAY,
            emitter.getPose(),
            emitter.getBuffer(quad.shadeDirectionOverride(), quad.chunkLayer())
        )));
    }
}
