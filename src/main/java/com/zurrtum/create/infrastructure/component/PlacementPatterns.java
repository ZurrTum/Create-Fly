package com.zurrtum.create.infrastructure.component;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum PlacementPatterns implements StringRepresentable {
    Solid,
    Checkered,
    InverseCheckered,
    Chance25,
    Chance50,
    Chance75;

    public static final Codec<PlacementPatterns> CODEC = StringRepresentable.fromEnum(PlacementPatterns::values);
    public static final StreamCodec<ByteBuf, PlacementPatterns> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PlacementPatterns.class);

    public final String translationKey;

    PlacementPatterns() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    public static void applyPattern(List<BlockPos> blocksIn, ItemStack stack, RandomSource random) {
        PlacementPatterns pattern = stack.getOrDefault(AllDataComponents.PLACEMENT_PATTERN, Solid);
        Predicate<BlockPos> filter = Predicates.alwaysFalse();

        switch (pattern) {
            case Chance25:
                filter = pos -> random.nextBoolean() || random.nextBoolean();
                break;
            case Chance50:
                filter = pos -> random.nextBoolean();
                break;
            case Chance75:
                filter = pos -> random.nextBoolean() && random.nextBoolean();
                break;
            case Checkered:
                filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 == 0;
                break;
            case InverseCheckered:
                filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
                break;
            case Solid:
            default:
                break;
        }

        blocksIn.removeIf(filter);
    }

    @Override
    public @NotNull String getSerializedName() {
        return translationKey;
    }
}
