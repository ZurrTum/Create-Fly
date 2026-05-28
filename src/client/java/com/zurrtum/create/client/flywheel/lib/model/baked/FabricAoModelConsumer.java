package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

public class FabricAoModelConsumer extends FabricModelConsumer implements QuadTransform {
    private @UnknownNullability TriState defaultAo;

    public FabricAoModelConsumer(AltModelBlockRenderer renderer) {
        super(renderer);
    }

    @Override
    public void updateOutput(BufferEmitterOutput output) {
        super.updateOutput(output);
        emitter.pushTransform(this);
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
        defaultAo = TriState.of(blockState.getLightEmission() == 0);
        super.tesselateBlock(x, y, z, level, pos, blockState, model, seed);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        if (quad.ambientOcclusion() == TriState.DEFAULT) {
            quad.ambientOcclusion(defaultAo);
        }
        return true;
    }
}
