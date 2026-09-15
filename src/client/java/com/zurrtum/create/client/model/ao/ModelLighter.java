package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class ModelLighter extends BlockModelLighter {
    private final AoFaceDataCache cache = new AoFaceDataCache(super.cache, scratchPos);

    public void prepare() {
        cache.clear();
    }

    @Override
    public void prepareQuadAmbientOcclusion(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        BakedQuad quad,
        QuadInstance output
    ) {
        Vector3fc p0 = quad.position0(), p1 = quad.position1(), p2 = quad.position2(), p3 = quad.position3();
        switch ((quad.direction().ordinal() << 1) + (quad.materialInfo().shade() ? 1 : 0)) {
            case 0 -> Down.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 1 -> Down.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 2 -> Up.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 3 -> Up.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 4 -> North.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 5 -> North.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 6 -> South.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 7 -> South.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 8 -> West.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 9 -> West.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 10 -> East.prepareQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
            case 11 -> East.prepareShadeQuad(level, state, pos, quad, p0, p1, p2, p3, output, cache);
        }
    }
}
