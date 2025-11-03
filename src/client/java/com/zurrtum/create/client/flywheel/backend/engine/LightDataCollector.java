package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.impl.compat.CompatMod;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightStorage;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.util.BitSet;
import java.util.Objects;

import static com.zurrtum.create.client.flywheel.backend.engine.LightStorage.BLOCKS_PER_SECTION;
import static com.zurrtum.create.client.flywheel.backend.engine.LightStorage.SOLID_SIZE_BYTES;

public abstract class LightDataCollector {
    private static final ConstantDataLayer ALWAYS_0 = new ConstantDataLayer(0);
    private static final ConstantDataLayer ALWAYS_15 = new ConstantDataLayer(15);

    protected final WorldAccess level;
    protected final ChunkLightingView skyLayerListener;
    protected final ChunkLightingView blockLayerListener;

    protected LightDataCollector(WorldAccess level, ChunkLightingView skyLayerListener, ChunkLightingView blockLayerListener) {
        this.level = level;
        this.skyLayerListener = skyLayerListener;
        this.blockLayerListener = blockLayerListener;
    }

    public static LightDataCollector of(WorldAccess level) {
        ChunkLightingView skyLayerListener = level.getLightingProvider().get(LightType.SKY);
        ChunkLightingView blockLayerListener = level.getLightingProvider().get(LightType.BLOCK);

        Long2ObjectFunction<ChunkNibbleArray> fastSkyDataGetter = createFastSkyDataGetter(skyLayerListener);
        Long2ObjectFunction<ChunkNibbleArray> fastBlockDataGetter = createFastBlockDataGetter(blockLayerListener);

        if (fastSkyDataGetter != null && fastBlockDataGetter != null) {
            return new Fast(level, skyLayerListener, blockLayerListener, fastSkyDataGetter, fastBlockDataGetter);
        } else {
            return new Slow(level, skyLayerListener, blockLayerListener);
        }
    }

    private static ChunkNibbleArray getSkyDataLayer(SkyLightStorage skyStorage, long section) {
        long l = section;
        int i = ChunkSectionPos.unpackY(l);
        SkyLightStorage.Data skyDataLayerStorageMap = skyStorage.uncachedStorage;
        int j = skyDataLayerStorageMap.columnToTopSection.get(ChunkSectionPos.withZeroY(l));
        if (j != skyDataLayerStorageMap.minSectionY && i < j) {
            ChunkNibbleArray dataLayer = skyStorage.getLightSection(l);
            if (dataLayer == null) {
                for (; dataLayer == null; dataLayer = skyStorage.getLightSection(l)) {
                    if (++i >= j) {
                        return null;
                    }

                    l = ChunkSectionPos.offset(l, Direction.UP);
                }
            }

            return dataLayer;
        } else {
            return null;
        }
    }

    @Nullable
    private static Long2ObjectFunction<ChunkNibbleArray> createFastSkyDataGetter(ChunkLightingView layerListener) {
        if (layerListener == ChunkLightingView.Empty.INSTANCE) {
            // The dummy listener always returns 0.
            // In vanilla this happens in the nether and end,
            // and the light texture is simply updated
            // to be invariant on sky light.
            return section -> ALWAYS_0;
        }

        if (layerListener instanceof ChunkLightProvider<?, ?> accessor) {
            // Sky storage has a fancy way to get the sky light at a given block position, but the logic is not
            // implemented in vanilla for fetching data layers directly. We need to re-implement it here. The simplest
            // way to do it was to expose the same logic via an extension method. Re-implementing it external to the
            // SkyLightSectionStorage class would require many more accessors.
            if (accessor.lightStorage instanceof SkyLightStorage skyStorage) {
                return section -> {
                    var out = getSkyDataLayer(skyStorage, section);

                    // Null section here means there are no blocks above us to darken this section.
                    return Objects.requireNonNullElse(out, ALWAYS_15);
                };
            }
        }

        if (CompatMod.SCALABLELUX.isLoaded) {
            return section -> Objects.requireNonNullElse(layerListener.getLightSection(ChunkSectionPos.from(section)), ALWAYS_15);
        }

        return null;
    }

    @Nullable
    private static Long2ObjectFunction<ChunkNibbleArray> createFastBlockDataGetter(ChunkLightingView layerListener) {
        if (layerListener == ChunkLightingView.Empty.INSTANCE) {
            return section -> ALWAYS_0;
        }

        if (layerListener instanceof ChunkLightProvider<?, ?> accessor) {
            LightStorage<?> storage = accessor.lightStorage;
            return section -> {
                var out = storage.getLightSection(section, false);
                return Objects.requireNonNullElse(out, ALWAYS_0);
            };
        }

        if (CompatMod.SCALABLELUX.isLoaded) {
            return section -> Objects.requireNonNullElse(layerListener.getLightSection(ChunkSectionPos.from(section)), ALWAYS_0);
        }

        return null;
    }

    public void collectSection(long ptr, long section) {
        collectSolidData(ptr, section);
        collectLightData(ptr, section);
    }

    private void collectSolidData(long ptr, long section) {
        var blockPos = new BlockPos.Mutable();
        int xMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(section));
        int yMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(section));
        int zMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(section));

        var bitSet = new BitSet(BLOCKS_PER_SECTION);
        int index = 0;
        for (int y = -1; y < 17; y++) {
            for (int z = -1; z < 17; z++) {
                for (int x = -1; x < 17; x++) {
                    blockPos.set(xMin + x, yMin + y, zMin + z);

                    var blockState = level.getBlockState(blockPos);

                    if (blockState.isOpaque() && blockState.isFullCube(level, blockPos)) {
                        bitSet.set(index);
                    }

                    index++;
                }
            }
        }

        var longArray = bitSet.toLongArray();
        for (long l : longArray) {
            MemoryUtil.memPutLong(ptr, l);
            ptr += Long.BYTES;
        }
    }

    protected abstract void collectLightData(long ptr, long section);

    /**
     * Write to the given section.
     *
     * @param ptr   Pointer to the base of a section's data.
     * @param x     X coordinate in the section, from [-1, 16].
     * @param y     Y coordinate in the section, from [-1, 16].
     * @param z     Z coordinate in the section, from [-1, 16].
     * @param block The block light level, from [0, 15].
     * @param sky   The sky light level, from [0, 15].
     */
    protected static void write(long ptr, int x, int y, int z, int block, int sky) {
        int x1 = x + 1;
        int y1 = y + 1;
        int z1 = z + 1;

        int offset = x1 + z1 * 18 + y1 * 18 * 18;

        long packedByte = (block & 0xF) | ((sky & 0xF) << 4);

        MemoryUtil.memPutByte(ptr + SOLID_SIZE_BYTES + offset, (byte) packedByte);
    }

    private static class Fast extends LightDataCollector {
        private final Long2ObjectFunction<ChunkNibbleArray> skyDataGetter;
        private final Long2ObjectFunction<ChunkNibbleArray> blockDataGetter;

        public Fast(
            WorldAccess level,
            ChunkLightingView skyLayerListener,
            ChunkLightingView blockLayerListener,
            Long2ObjectFunction<ChunkNibbleArray> skyDataGetter,
            Long2ObjectFunction<ChunkNibbleArray> blockDataGetter
        ) {
            super(level, skyLayerListener, blockLayerListener);
            this.skyDataGetter = skyDataGetter;
            this.blockDataGetter = blockDataGetter;
        }

        @Override
        protected void collectLightData(long ptr, long section) {
            collectCenter(ptr, section);

            for (SectionEdge i : SectionEdge.VALUES) {
                collectYZPlane(ptr, ChunkSectionPos.offset(section, i.sectionOffset, 0, 0), i);
                collectXZPlane(ptr, ChunkSectionPos.offset(section, 0, i.sectionOffset, 0), i);
                collectXYPlane(ptr, ChunkSectionPos.offset(section, 0, 0, i.sectionOffset), i);

                for (SectionEdge j : SectionEdge.VALUES) {
                    collectXStrip(ptr, ChunkSectionPos.offset(section, 0, i.sectionOffset, j.sectionOffset), i, j);
                    collectYStrip(ptr, ChunkSectionPos.offset(section, i.sectionOffset, 0, j.sectionOffset), i, j);
                    collectZStrip(ptr, ChunkSectionPos.offset(section, i.sectionOffset, j.sectionOffset, 0), i, j);
                }
            }

            collectCorners(ptr, section);
        }

        private void collectXStrip(long ptr, long section, SectionEdge y, SectionEdge z) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int x = 0; x < 16; x++) {
                write(ptr, x, y.relative, z.relative, blockData.get(x, y.pos, z.pos), skyData.get(x, y.pos, z.pos));
            }
        }

        private void collectYStrip(long ptr, long section, SectionEdge x, SectionEdge z) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int y = 0; y < 16; y++) {
                write(ptr, x.relative, y, z.relative, blockData.get(x.pos, y, z.pos), skyData.get(x.pos, y, z.pos));
            }
        }

        private void collectZStrip(long ptr, long section, SectionEdge x, SectionEdge y) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int z = 0; z < 16; z++) {
                write(ptr, x.relative, y.relative, z, blockData.get(x.pos, y.pos, z), skyData.get(x.pos, y.pos, z));
            }
        }

        private void collectYZPlane(long ptr, long section, SectionEdge x) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    write(ptr, x.relative, y, z, blockData.get(x.pos, y, z), skyData.get(x.pos, y, z));
                }
            }
        }

        private void collectXZPlane(long ptr, long section, SectionEdge y) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    write(ptr, x, y.relative, z, blockData.get(x, y.pos, z), skyData.get(x, y.pos, z));
                }
            }
        }

        private void collectXYPlane(long ptr, long section, SectionEdge z) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    write(ptr, x, y, z.relative, blockData.get(x, y, z.pos), skyData.get(x, y, z.pos));
                }
            }
        }

        private void collectCenter(long ptr, long section) {
            var blockData = blockDataGetter.get(section);
            var skyData = skyDataGetter.get(section);
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        write(ptr, x, y, z, blockData.get(x, y, z), skyData.get(x, y, z));
                    }
                }
            }
        }

        private void collectCorners(long ptr, long section) {
            var blockLayerListener = this.blockLayerListener;
            var skyLayerListener = this.skyLayerListener;

            var blockPos = new BlockPos.Mutable();
            int xMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(section));
            int yMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(section));
            int zMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(section));

            for (SectionEdge y : SectionEdge.VALUES) {
                for (SectionEdge z : SectionEdge.VALUES) {
                    for (SectionEdge x : SectionEdge.VALUES) {
                        blockPos.set(x.relative + xMin, y.relative + yMin, z.relative + zMin);
                        write(
                            ptr,
                            x.relative,
                            y.relative,
                            z.relative,
                            blockLayerListener.getLightLevel(blockPos),
                            skyLayerListener.getLightLevel(blockPos)
                        );
                    }
                }
            }
        }

        private enum SectionEdge {
            LOW(15, -1, -1),
            HIGH(0, 16, 1),
            ;

            public static final SectionEdge[] VALUES = values();

            /**
             * The position in the section to collect.
             */
            private final int pos;
            /**
             * The position relative to the main section.
             */
            private final int relative;
            /**
             * The offset to the neighboring section.
             */
            private final int sectionOffset;

            SectionEdge(int pos, int relative, int sectionOffset) {
                this.pos = pos;
                this.relative = relative;
                this.sectionOffset = sectionOffset;
            }
        }
    }

    private static class Slow extends LightDataCollector {
        public Slow(WorldAccess level, ChunkLightingView skyLayerListener, ChunkLightingView blockLayerListener) {
            super(level, skyLayerListener, blockLayerListener);
        }

        @Override
        protected void collectLightData(long ptr, long section) {
            var blockLayerListener = this.blockLayerListener;
            var skyLayerListener = this.skyLayerListener;

            var blockPos = new BlockPos.Mutable();
            int xMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(section));
            int yMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(section));
            int zMin = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(section));

            for (int y = -1; y < 17; y++) {
                for (int z = -1; z < 17; z++) {
                    for (int x = -1; x < 17; x++) {
                        blockPos.set(xMin + x, yMin + y, zMin + z);
                        write(ptr, x, y, z, blockLayerListener.getLightLevel(blockPos), skyLayerListener.getLightLevel(blockPos));
                    }
                }
            }
        }
    }

    private static class ConstantDataLayer extends ChunkNibbleArray {
        private final int value;

        private ConstantDataLayer(int value) {
            this.value = value;
        }

        @Override
        public int get(int x, int y, int z) {
            return value;
        }
    }
}