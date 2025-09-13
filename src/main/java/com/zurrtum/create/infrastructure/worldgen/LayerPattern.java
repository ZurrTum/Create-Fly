package com.zurrtum.create.infrastructure.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LayerPattern {
    public static final Codec<LayerPattern> CODEC = Codec.list(Layer.CODEC).xmap(LayerPattern::new, pattern -> pattern.layers);

    public final List<Layer> layers;

    public LayerPattern(List<Layer> layers) {
        this.layers = layers;
    }

    public Layer rollNext(@Nullable Layer previous, Random random) {
        int totalWeight = 0;
        for (Layer layer : layers)
            if (layer != previous)
                totalWeight += layer.weight;
        int rolled = random.nextInt(totalWeight);

        for (Layer layer : layers) {
            if (layer == previous)
                continue;
            rolled -= layer.weight;
            if (rolled < 0)
                return layer;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Layer> layers = new ArrayList<>();
        private boolean netherMode;

        public Builder inNether() {
            netherMode = true;
            return this;
        }

        public Builder layer(Consumer<Layer.@NotNull Builder> builder) {
            Layer.Builder layerBuilder = new Layer.Builder();
            layerBuilder.netherMode = netherMode;
            builder.accept(layerBuilder);
            layers.add(layerBuilder.build());
            return this;
        }

        public LayerPattern build() {
            return new LayerPattern(layers);
        }
    }

    public static class Layer {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.list(OreFeatureConfig.Target.CODEC)).fieldOf("targets").forGetter(layer -> layer.targets),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_size").forGetter(layer -> layer.minSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_size").forGetter(layer -> layer.maxSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(layer -> layer.weight)
        ).apply(instance, Layer::new));

        public final List<List<OreFeatureConfig.Target>> targets;
        public final int minSize;
        public final int maxSize;
        public final int weight;

        public Layer(List<List<OreFeatureConfig.Target>> targets, int minSize, int maxSize, int weight) {
            this.targets = targets;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.weight = weight;
        }

        public List<OreFeatureConfig.Target> rollBlock(Random random) {
            if (targets.size() == 1)
                return targets.getFirst();
            return targets.get(random.nextInt(targets.size()));
        }

        public static class Builder {
            private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
            private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
            private static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchRuleTest(BlockTags.BASE_STONE_NETHER);

            private final List<List<OreFeatureConfig.Target>> targets = new ArrayList<>();
            private int minSize = 1;
            private int maxSize = 1;
            private int weight = 1;
            private boolean netherMode;

            public Builder block(Supplier<? extends Block> block) {
                return block(block.get());
            }

            public Builder passiveBlock() {
                return blocks(Blocks.STONE.getDefaultState(), Blocks.DEEPSLATE.getDefaultState());
            }

            public Builder block(Block block) {
                if (netherMode) {
                    this.targets.add(ImmutableList.of(OreFeatureConfig.createTarget(NETHER_ORE_REPLACEABLES, block.getDefaultState())));
                    return this;
                }
                return blocks(block.getDefaultState(), block.getDefaultState());
            }

            public Builder blocks(Block block, Block deepblock) {
                return blocks(block.getDefaultState(), deepblock.getDefaultState());
            }

            public Builder blocks(Couple<Supplier<? extends Block>> blocksByDepth) {
                return blocks(blocksByDepth.getFirst().get().getDefaultState(), blocksByDepth.getSecond().get().getDefaultState());
            }

            private Builder blocks(BlockState stone, BlockState deepslate) {
                this.targets.add(ImmutableList.of(
                    OreFeatureConfig.createTarget(STONE_ORE_REPLACEABLES, stone),
                    OreFeatureConfig.createTarget(DEEPSLATE_ORE_REPLACEABLES, deepslate)
                ));
                return this;
            }

            public Builder weight(int weight) {
                this.weight = weight;
                return this;
            }

            public Builder size(int min, int max) {
                this.minSize = min;
                this.maxSize = max;
                return this;
            }

            public Layer build() {
                return new Layer(targets, minSize, maxSize, weight);
            }
        }
    }
}