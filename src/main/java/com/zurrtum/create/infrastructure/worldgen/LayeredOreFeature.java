package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class LayeredOreFeature implements Feature {
    public static final MapCodec<LayeredOreFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.list(LayerPattern.CODEC).fieldOf("layer_patterns").forGetter(LayeredOreFeature::layerPatterns),
        Codec.intRange(0, 64).fieldOf("size").forGetter(LayeredOreFeature::size),
        Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure")
            .forGetter(LayeredOreFeature::discardChanceOnAirExposure)
    ).apply(i, LayeredOreFeature::new));

    private static final float MAX_LAYER_DISPLACEMENT = 1.75f;
    private static final float LAYER_NOISE_FREQUENCY = 0.125f;

    private static final float MAX_RADIAL_THRESHOLD_REDUCTION = 0.25f;
    private static final float RADIAL_NOISE_FREQUENCY = 0.125f;

    public final List<LayerPattern> layerPatterns;
    public final int size;
    public final float discardChanceOnAirExposure;

    public LayeredOreFeature(List<LayerPattern> layerPatterns, int size, float discardChanceOnAirExposure) {
        this.layerPatterns = layerPatterns;
        this.size = size;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
    }

    public List<LayerPattern> layerPatterns() {
        return layerPatterns;
    }

    public int size() {
        return size;
    }

    public float discardChanceOnAirExposure() {
        return discardChanceOnAirExposure;
    }

    @Override
    public MapCodec<LayeredOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(
        WorldGenLevel worldGenLevel,
        ChunkGenerator chunkGenerator,
        RandomSource random,
        BlockPos origin
    ) {
        if (layerPatterns.isEmpty()) {
            return false;
        }

        LayerPattern layerPattern = layerPatterns.get(random.nextInt(layerPatterns.size()));

        int placedAmount = 0;
        int size = this.size + 1;
        float radius = this.size * 0.5f;
        int radiusBound = Mth.ceil(radius) - 1;
        int x0 = origin.getX();
        int y0 = origin.getY();
        int z0 = origin.getZ();

        if (origin.getY() >= worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ())) {
            return false;
        }

        List<TemporaryLayerEntry> tempLayers = new ArrayList<>();
        float layerSizeTotal = 0.0f;
        Layer current = null;
        while (layerSizeTotal < size) {
            Layer next = layerPattern.rollNext(current, random);
            float layerSize = Mth.randomBetweenInclusive(random, next.minSize, next.maxSize);
            tempLayers.add(new TemporaryLayerEntry(next, layerSize));
            layerSizeTotal += layerSize;
            current = next;
        }

        List<ResolvedLayerEntry> resolvedLayers = new ArrayList<>(tempLayers.size());
        float cumulativeLayerSize = -(layerSizeTotal - size) * random.nextFloat();
        for (TemporaryLayerEntry tempLayerEntry : tempLayers) {
            float rampStartValue =
                resolvedLayers.isEmpty() ? Float.NEGATIVE_INFINITY : cumulativeLayerSize * (2.0f / size) - 1.0f;
            cumulativeLayerSize += tempLayerEntry.size();
            if (cumulativeLayerSize < 0) {
                continue;
            }
            float radialThresholdMultiplier = Mth.randomBetween(random, 0.5f, 1.0f);
            resolvedLayers.add(new ResolvedLayerEntry(tempLayerEntry.layer, radialThresholdMultiplier, rampStartValue));
        }

        // Choose stacking direction
        float gy = Mth.randomBetween(random, -1.0f, 1.0f);
        gy = (float) Math.cbrt(gy); // Make layer alignment tend towards horizontal more than vertical
        float xzRescale = Mth.sqrt(1.0f - gy * gy);
        float theta = random.nextFloat() * Mth.TWO_PI;
        float gx = Mth.cos(theta) * xzRescale;
        float gz = Mth.sin(theta) * xzRescale;

        SimplexNoise layerDisplacementNoise = new SimplexNoise(random);
        SimplexNoise radiusNoise = new SimplexNoise(random);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(worldGenLevel);

        try {

            for (int dzBlock = -radiusBound; dzBlock <= radiusBound; dzBlock++) {
                float dz = dzBlock * (1.0f / radius);
                if (dz * dz > 1) {
                    continue;
                }

                for (int dxBlock = -radiusBound; dxBlock <= radiusBound; dxBlock++) {
                    float dx = dxBlock * (1.0f / radius);
                    if (dz * dz + dx * dx > 1) {
                        continue;
                    }

                    for (int dyBlock = -radiusBound; dyBlock <= radiusBound; dyBlock++) {
                        float dy = dyBlock * (1.0f / radius);
                        float distanceSquared = dz * dz + dx * dx + dy * dy;
                        if (distanceSquared > 1) {
                            continue;
                        }
                        if (worldGenLevel.isOutsideBuildHeight(y0 + dyBlock)) {
                            continue;
                        }

                        int currentX = x0 + dxBlock;
                        int currentY = y0 + dyBlock;
                        int currentZ = z0 + dzBlock;

                        float rampValue = gx * dx + gy * dy + gz * dz;
                        rampValue += layerDisplacementNoise.get(
                            currentX * LAYER_NOISE_FREQUENCY,
                            currentY * LAYER_NOISE_FREQUENCY,
                            currentZ * LAYER_NOISE_FREQUENCY
                        ) * (MAX_LAYER_DISPLACEMENT / size);

                        int layerIndex = Collections.binarySearch(
                            resolvedLayers,
                            new ResolvedLayerEntry(null, 0, rampValue)
                        );
                        if (layerIndex < 0) {
                            layerIndex = -2 - layerIndex; // Counter (-insertionIndex - 1) return result, where insertionIndex = layerIndex + 1
                        }
                        ResolvedLayerEntry layerEntry = resolvedLayers.get(layerIndex);

                        if (distanceSquared > layerEntry.radialThresholdMultiplier) {
                            continue;
                        }

                        float thresholdNoiseValue = Mth.map(
                            radiusNoise.get(
                                currentX * RADIAL_NOISE_FREQUENCY,
                                currentY * RADIAL_NOISE_FREQUENCY,
                                currentZ * RADIAL_NOISE_FREQUENCY
                            ), -1.0f, 1.0f, 1.0f - MAX_RADIAL_THRESHOLD_REDUCTION, 1.0f
                        );

                        if (distanceSquared > layerEntry.radialThresholdMultiplier * thresholdNoiseValue) {
                            continue;
                        }

                        Layer layer = layerEntry.layer;
                        List<BlockReplacement> targetBlockStates = layer.rollBlock(random);

                        mutablePos.set(currentX, currentY, currentZ);
                        if (!worldGenLevel.ensureCanWrite(mutablePos)) {
                            continue;
                        }
                        LevelChunkSection levelChunkSection = bulkSectionAccess.getSection(mutablePos);
                        if (levelChunkSection == null) {
                            continue;
                        }

                        int localX = SectionPos.sectionRelative(currentX);
                        int localY = SectionPos.sectionRelative(currentY);
                        int localZ = SectionPos.sectionRelative(currentZ);
                        BlockState blockState = levelChunkSection.getBlockState(localX, localY, localZ);

                        for (BlockReplacement targetBlockState : targetBlockStates) {
                            if (!canPlaceOre(
                                blockState,
                                bulkSectionAccess::getBlockState,
                                random,
                                targetBlockState,
                                mutablePos
                            )) {
                                continue;
                            }
                            BlockState state = targetBlockState.state();
                            if (state.isAir()) {
                                continue;
                            }
                            levelChunkSection.setBlockState(localX, localY, localZ, state, false);
                            ++placedAmount;
                            break;
                        }

                    }
                }
            }

        } catch (Throwable throwable1) {
            try {
                bulkSectionAccess.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        bulkSectionAccess.close();
        return placedAmount > 0;
    }

    public boolean canPlaceOre(
        BlockState pState,
        Function<BlockPos, BlockState> pAdjacentStateAccessor,
        RandomSource pRandom,
        BlockReplacement pTargetState,
        BlockPos.MutableBlockPos pMatablePos
    ) {
        if (!pTargetState.target().test(pState, pMatablePos, pRandom)) {
            return false;
        }
        if (AbstractOreFeature.shouldSkipAirCheck(pRandom, discardChanceOnAirExposure)) {
            return true;
        }

        return !AbstractOreFeature.isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    private record TemporaryLayerEntry(Layer layer, float size) {
    }

    private record ResolvedLayerEntry(Layer layer, float radialThresholdMultiplier,
                                      float rampStartValue) implements Comparable<ResolvedLayerEntry> {
        @Override
        public int compareTo(ResolvedLayerEntry b) {
            return Float.compare(rampStartValue, b.rampStartValue);
        }
    }
}