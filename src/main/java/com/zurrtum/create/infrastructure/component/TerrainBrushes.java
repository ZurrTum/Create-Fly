package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.*;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum TerrainBrushes implements StringRepresentable {
    Cuboid(new CuboidBrush()),
    Sphere(new SphereBrush()),
    Cylinder(new CylinderBrush()),
    Surface(new DynamicBrush(true)),
    Cluster(new DynamicBrush(false));

    public static final Codec<TerrainBrushes> CODEC = StringRepresentable.fromEnum(TerrainBrushes::values);
    public static final StreamCodec<ByteBuf, TerrainBrushes> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(TerrainBrushes.class);

    private final Brush brush;

    TerrainBrushes(Brush brush) {
        this.brush = brush;
    }

    public Brush get() {
        return brush;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
