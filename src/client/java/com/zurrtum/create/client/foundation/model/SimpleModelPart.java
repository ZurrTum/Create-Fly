package com.zurrtum.create.client.foundation.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record SimpleModelPart(QuadCollection quads, boolean useAmbientOcclusion,
                              Baked particleMaterial) implements BlockStateModelPart {
    @Override
    public List<BakedQuad> getQuads(final @Nullable Direction direction) {
        return quads.getQuads(direction);
    }

    @Override
    public int materialFlags() {
        return quads.materialFlags();
    }
}
