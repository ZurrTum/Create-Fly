package com.zurrtum.create.infrastructure.worldgen;

import com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class LayeredOreFeature extends Feature<LayeredOreConfiguration> {
    public LayeredOreFeature() {
        super(LayeredOreConfiguration.CODEC);
    }

    private static final float MAX_LAYER_DISPLACEMENT = 1.75f;
    private static final float LAYER_NOISE_FREQUENCY = 0.125f;

    private static final float MAX_RADIAL_THRESHOLD_REDUCTION = 0.25f;
    private static final float RADIAL_NOISE_FREQUENCY = 0.125f;

    @Override
    public boolean generate(FeatureContext<LayeredOreConfiguration> pContext) {
        Random random = pContext.getRandom();
        BlockPos origin = pContext.getOrigin();
        StructureWorldAccess worldGenLevel = pContext.getWorld();
        LayeredOreConfiguration config = pContext.getConfig();
        List<LayerPattern> patternPool = config.layerPatterns;

        if (patternPool.isEmpty())
            return false;

        LayerPattern layerPattern = patternPool.get(random.nextInt(patternPool.size()));

        int placedAmount = 0;
        int size = config.size + 1;
        float radius = config.size * 0.5f;
        int radiusBound = MathHelper.ceil(radius) - 1;
        int x0 = origin.getX();
        int y0 = origin.getY();
        int z0 = origin.getZ();

        if (origin.getY() >= worldGenLevel.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()))
            return false;

        List<TemporaryLayerEntry> tempLayers = new ArrayList<>();
        float layerSizeTotal = 0.0f;
        Layer current = null;
        while (layerSizeTotal < size) {
            Layer next = layerPattern.rollNext(current, random);
            float layerSize = MathHelper.nextBetween(random, next.minSize, next.maxSize);
            tempLayers.add(new TemporaryLayerEntry(next, layerSize));
            layerSizeTotal += layerSize;
            current = next;
        }

        List<ResolvedLayerEntry> resolvedLayers = new ArrayList<>(tempLayers.size());
        float cumulativeLayerSize = -(layerSizeTotal - size) * random.nextFloat();
        for (TemporaryLayerEntry tempLayerEntry : tempLayers) {
            float rampStartValue = resolvedLayers.size() == 0 ? Float.NEGATIVE_INFINITY : cumulativeLayerSize * (2.0f / size) - 1.0f;
            cumulativeLayerSize += tempLayerEntry.size();
            if (cumulativeLayerSize < 0)
                continue;
            float radialThresholdMultiplier = MathHelper.nextBetween(random, 0.5f, 1.0f);
            resolvedLayers.add(new ResolvedLayerEntry(tempLayerEntry.layer, radialThresholdMultiplier, rampStartValue));
        }

        // Choose stacking direction
        float gy = MathHelper.nextBetween(random, -1.0f, 1.0f);
        gy = (float) Math.cbrt(gy); // Make layer alignment tend towards horizontal more than vertical
        float xzRescale = MathHelper.sqrt(1.0f - gy * gy);
        float theta = random.nextFloat() * MathHelper.TAU;
        float gx = MathHelper.cos(theta) * xzRescale;
        float gz = MathHelper.sin(theta) * xzRescale;

        SimplexNoiseSampler layerDisplacementNoise = new SimplexNoiseSampler(random);
        SimplexNoiseSampler radiusNoise = new SimplexNoiseSampler(random);

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        ChunkSectionCache bulkSectionAccess = new ChunkSectionCache(worldGenLevel);

        try {

            for (int dzBlock = -radiusBound; dzBlock <= radiusBound; dzBlock++) {
                float dz = dzBlock * (1.0f / radius);
                if (dz * dz > 1)
                    continue;

                for (int dxBlock = -radiusBound; dxBlock <= radiusBound; dxBlock++) {
                    float dx = dxBlock * (1.0f / radius);
                    if (dz * dz + dx * dx > 1)
                        continue;

                    for (int dyBlock = -radiusBound; dyBlock <= radiusBound; dyBlock++) {
                        float dy = dyBlock * (1.0f / radius);
                        float distanceSquared = dz * dz + dx * dx + dy * dy;
                        if (distanceSquared > 1)
                            continue;
                        if (worldGenLevel.isOutOfHeightLimit(y0 + dyBlock))
                            continue;

                        int currentX = x0 + dxBlock;
                        int currentY = y0 + dyBlock;
                        int currentZ = z0 + dzBlock;

                        float rampValue = gx * dx + gy * dy + gz * dz;
                        rampValue += layerDisplacementNoise.sample(
                            currentX * LAYER_NOISE_FREQUENCY,
                            currentY * LAYER_NOISE_FREQUENCY,
                            currentZ * LAYER_NOISE_FREQUENCY
                        ) * (MAX_LAYER_DISPLACEMENT / size);

                        int layerIndex = Collections.binarySearch(resolvedLayers, new ResolvedLayerEntry(null, 0, rampValue));
                        if (layerIndex < 0)
                            layerIndex = -2 - layerIndex; // Counter (-insertionIndex - 1) return result, where insertionIndex = layerIndex + 1
                        ResolvedLayerEntry layerEntry = resolvedLayers.get(layerIndex);

                        if (distanceSquared > layerEntry.radialThresholdMultiplier)
                            continue;

                        float thresholdNoiseValue = MathHelper.map(
                            (float) radiusNoise.sample(
                                currentX * RADIAL_NOISE_FREQUENCY,
                                currentY * RADIAL_NOISE_FREQUENCY,
                                currentZ * RADIAL_NOISE_FREQUENCY
                            ),
                            -1.0f,
                            1.0f,
                            1.0f - MAX_RADIAL_THRESHOLD_REDUCTION,
                            1.0f
                        );

                        if (distanceSquared > layerEntry.radialThresholdMultiplier * thresholdNoiseValue)
                            continue;

                        Layer layer = layerEntry.layer;
                        List<OreFeatureConfig.Target> targetBlockStates = layer.rollBlock(random);

                        mutablePos.set(currentX, currentY, currentZ);
                        if (!worldGenLevel.isValidForSetBlock(mutablePos))
                            continue;
                        ChunkSection levelChunkSection = bulkSectionAccess.getSection(mutablePos);
                        if (levelChunkSection == null)
                            continue;

                        int localX = ChunkSectionPos.getLocalCoord(currentX);
                        int localY = ChunkSectionPos.getLocalCoord(currentY);
                        int localZ = ChunkSectionPos.getLocalCoord(currentZ);
                        BlockState blockState = levelChunkSection.getBlockState(localX, localY, localZ);

                        for (OreFeatureConfig.Target targetBlockState : targetBlockStates) {
                            if (!canPlaceOre(blockState, bulkSectionAccess::getBlockState, random, config, targetBlockState, mutablePos))
                                continue;
                            if (targetBlockState.state.isAir())
                                continue;
                            levelChunkSection.setBlockState(localX, localY, localZ, targetBlockState.state, false);
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
        Random pRandom,
        LayeredOreConfiguration pConfig,
        OreFeatureConfig.Target pTargetState,
        BlockPos.Mutable pMatablePos
    ) {
        if (!pTargetState.target.test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, pConfig.discardChanceOnAirExposure))
            return true;

        return !isExposedToAir(pAdjacentStateAccessor, pMatablePos);
    }

    protected boolean shouldSkipAirCheck(Random pRandom, float pChance) {
        return pChance <= 0 ? true : pChance >= 1 ? false : pRandom.nextFloat() >= pChance;
    }

    private record TemporaryLayerEntry(Layer layer, float size) {
    }

    private record ResolvedLayerEntry(Layer layer, float radialThresholdMultiplier, float rampStartValue) implements Comparable<ResolvedLayerEntry> {
        @Override
        public int compareTo(LayeredOreFeature.ResolvedLayerEntry b) {
            return Float.compare(rampStartValue, b.rampStartValue);
        }
    }
}