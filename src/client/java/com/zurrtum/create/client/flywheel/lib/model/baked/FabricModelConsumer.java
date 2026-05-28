package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

public class FabricModelConsumer implements ModelConsumer {
    private final AltModelBlockRenderer renderer;
    protected @UnknownNullability QuadEmitter emitter;

    public FabricModelConsumer(AltModelBlockRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void updateOutput(BufferEmitterOutput output) {
        emitter = ((FabricEmitterSupplier) output).quadEmitter();
    }

    @Override
    public void tesselateBlock(
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState blockState,
        BlockStateModel model,
        long seed
    ) {
        renderer.tesselateBlock(emitter, x, y, z, level, pos, blockState, model, seed);
    }
}
