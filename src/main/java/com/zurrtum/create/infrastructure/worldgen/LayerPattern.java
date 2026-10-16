package com.zurrtum.create.infrastructure.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.HeightMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LayerPattern {
    public static final Codec<LayerPattern> CODEC = Codec.list(Layer.CODEC)
        .xmap(LayerPattern::new, pattern -> pattern.layers);

    public final List<Layer> layers;

    public LayerPattern(List<Layer> layers) {
        this.layers = layers;
    }

    @Nullable
    public Layer rollNext(@Nullable Layer previous, RandomSource random) {
        int totalWeight = 0;
        for (Layer layer : layers) {
            if (layer != previous) {
                totalWeight += layer.weight;
            }
        }
        int rolled = random.nextInt(totalWeight);

        for (Layer layer : layers) {
            if (layer == previous) {
                continue;
            }
            rolled -= layer.weight;
            if (rolled < 0) {
                return layer;
            }
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

        public Builder layer(Consumer<Layer.Builder> builder) {
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
            Codec.list(Codec.list(BlockReplacement.CODEC)).fieldOf("targets").forGetter(layer -> layer.targets),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_size").forGetter(layer -> layer.minSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("max_size").forGetter(layer -> layer.maxSize),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(layer -> layer.weight)
        ).apply(instance, Layer::new));

        public final List<List<BlockReplacement>> targets;
        public final int minSize;
        public final int maxSize;
        public final int weight;

        public Layer(List<List<BlockReplacement>> targets, int minSize, int maxSize, int weight) {
            this.targets = targets;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.weight = weight;
        }

        public List<BlockReplacement> rollBlock(RandomSource random) {
            if (targets.size() == 1) {
                return targets.getFirst();
            }
            return targets.get(random.nextInt(targets.size()));
        }

        public static class Builder {
            private static final RuleTest STONE_ORE_REPLACEABLES = RuleTest.either(
                new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES),
                HeightMatchTest.min(0),
                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES)
            );
            private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = RuleTest.either(
                new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES),
                HeightMatchTest.max(8),
                new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
            );
            private static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);

            private final List<List<BlockReplacement>> targets = new ArrayList<>();
            private int minSize = 1;
            private int maxSize = 1;
            private int weight = 1;
            private boolean netherMode;

            public Builder block(Supplier<? extends Block> block) {
                return block(block.get());
            }

            public Builder passiveBlock() {
                return blocks(Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState());
            }

            public Builder block(Block block) {
                if (netherMode) {
                    targets.add(ImmutableList.of(BlockReplacement.replace(
                        NETHER_ORE_REPLACEABLES,
                        block.defaultBlockState()
                    )));
                    return this;
                }
                return blocks(block.defaultBlockState(), block.defaultBlockState());
            }

            public Builder blocks(Block block, Block deepblock) {
                return blocks(block.defaultBlockState(), deepblock.defaultBlockState());
            }

            public Builder blocks(Couple<Supplier<? extends Block>> blocksByDepth) {
                return blocks(
                    blocksByDepth.getFirst().get().defaultBlockState(),
                    blocksByDepth.getSecond().get().defaultBlockState()
                );
            }

            private Builder blocks(BlockState stone, BlockState deepslate) {
                targets.add(ImmutableList.of(
                    BlockReplacement.replace(STONE_ORE_REPLACEABLES, stone),
                    BlockReplacement.replace(DEEPSLATE_ORE_REPLACEABLES, deepslate)
                ));
                return this;
            }

            public Builder weight(int weight) {
                this.weight = weight;
                return this;
            }

            public Builder size(int min, int max) {
                minSize = min;
                maxSize = max;
                return this;
            }

            public Layer build() {
                return new Layer(targets, minSize, maxSize, weight);
            }
        }
    }
}