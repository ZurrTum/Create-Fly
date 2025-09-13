package com.zurrtum.create.infrastructure.component;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public enum PlacementPatterns implements StringIdentifiable {
    Solid,
    Checkered,
    InverseCheckered,
    Chance25,
    Chance50,
    Chance75;

    public static final Codec<PlacementPatterns> CODEC = StringIdentifiable.createCodec(PlacementPatterns::values);
    public static final PacketCodec<ByteBuf, PlacementPatterns> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PlacementPatterns.class);

    public final String translationKey;

    PlacementPatterns() {
        this.translationKey = name().toLowerCase(Locale.ROOT);
    }

    public static void applyPattern(List<BlockPos> blocksIn, ItemStack stack, Random random) {
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
    public @NotNull String asString() {
        return translationKey;
    }
}
