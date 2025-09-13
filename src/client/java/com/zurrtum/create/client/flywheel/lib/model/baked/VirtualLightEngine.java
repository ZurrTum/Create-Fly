package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public final class VirtualLightEngine extends LightingProvider {
    private final ChunkLightingView blockListener;
    private final ChunkLightingView skyListener;

    public VirtualLightEngine(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc, BlockView level) {
        super(
            new ChunkProvider() {
                @Override
                @Nullable
                public LightSourceView getChunk(int x, int z) {
                    return null;
                }

                @Override
                public BlockView getWorld() {
                    return level;
                }
            }, false, false
        );

        blockListener = new VirtualLayerLightEventListener(blockLightFunc);
        skyListener = new VirtualLayerLightEventListener(skyLightFunc);
    }

    @Override
    public ChunkLightingView get(LightType layer) {
        return layer == LightType.BLOCK ? blockListener : skyListener;
    }

    @Override
    public int getLight(BlockPos pos, int amount) {
        int i = skyListener.getLightLevel(pos) - amount;
        int j = blockListener.getLightLevel(pos);
        return Math.max(j, i);
    }

    private static class VirtualLayerLightEventListener implements ChunkLightingView {
        private final ToIntFunction<BlockPos> lightFunc;

        public VirtualLayerLightEventListener(ToIntFunction<BlockPos> lightFunc) {
            this.lightFunc = lightFunc;
        }

        @Override
        public void checkBlock(BlockPos pos) {
        }

        @Override
        public boolean hasUpdates() {
            return false;
        }

        @Override
        public int doLightUpdates() {
            return 0;
        }

        @Override
        public void setSectionStatus(ChunkSectionPos pos, boolean isSectionEmpty) {
        }

        @Override
        public void setColumnEnabled(ChunkPos pos, boolean lightEnabled) {
        }

        @Override
        public void propagateLight(ChunkPos pos) {
        }

        @Override
        public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
            return null;
        }

        @Override
        public int getLightLevel(BlockPos pos) {
            return lightFunc.applyAsInt(pos);
        }
    }
}
