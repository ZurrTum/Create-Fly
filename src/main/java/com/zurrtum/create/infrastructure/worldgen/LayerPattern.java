package com.zurrtum.create.infrastructure.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Couple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class LayerPattern {
    public static final Codec<LayerPattern> CODEC = Codec.list(Layer.CODEC).xmap(LayerPattern::new, pattern -> pattern.layers);

    public final List<Layer> layers;

    public LayerPattern(List<Layer> layers) {
        this.layers = layers;
    }

    public Layer rollNext(@Nullable Layer previous, RandomSource random) {
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
            Codec.list(Codec.list(OreConfiguration.TargetBlockState.CODEC)).fieldOf("targets").forGetter(layer -> layer.targets),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_size").forGetter(layer -> layer.minSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_size").forGetter(layer -> layer.maxSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(layer -> layer.weight)
        ).apply(instance, Layer::new));

        public final List<List<OreConfiguration.TargetBlockState>> targets;
        public final int minSize;
        public final int maxSize;
        public final int weight;

        public Layer(List<List<OreConfiguration.TargetBlockState>> targets, int minSize, int maxSize, int weight) {
            this.targets = targets;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.weight = weight;
        }

        public List<OreConfiguration.TargetBlockState> rollBlock(RandomSource random) {
            if (targets.size() == 1)
                return targets.getFirst();
            return targets.get(random.nextInt(targets.size()));
        }

        public static class Builder {
            private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
            private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
            private static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);

            private final List<List<OreConfiguration.TargetBlockState>> targets = new ArrayList<>();
            private int minSize = 1;
            private int maxSize = 1;
            private int weight = 1;
            private boolean netherMode;

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder block(Supplier<? extends Block> block) {
                return block(block.get());
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder passiveBlock() {
                return blocks(Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState());
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder block(Block block) {
                if (netherMode) {
                    this.targets.add(ImmutableList.of(OreConfiguration.target(NETHER_ORE_REPLACEABLES, block.defaultBlockState())));
                    return this;
                }
                return blocks(block.defaultBlockState(), block.defaultBlockState());
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder blocks(Block block, Block deepblock) {
                return blocks(block.defaultBlockState(), deepblock.defaultBlockState());
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder blocks(Couple<Supplier<? extends Block>> blocksByDepth) {
                return blocks(blocksByDepth.getFirst().get().defaultBlockState(), blocksByDepth.getSecond().get().defaultBlockState());
            }

            private com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder blocks(BlockState stone, BlockState deepslate) {
                this.targets.add(ImmutableList.of(
                    OreConfiguration.target(STONE_ORE_REPLACEABLES, stone),
                    OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, deepslate)
                ));
                return this;
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder weight(int weight) {
                this.weight = weight;
                return this;
            }

            public com.zurrtum.create.infrastructure.worldgen.LayerPattern.Layer.Builder size(int min, int max) {
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