package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.renderer.block.BlockQuadOutput;

import java.util.function.Function;

/**
 * Implemented via Mixin
 * The output needs to implement FabricEmitterSupplier.
 */
public interface BufferEmitterOutput extends BlockQuadOutput {
    <T extends BufferEmitterOutput, Emitter> Emitter getOrCreateEmitter(Function<T, Emitter> func);
}
