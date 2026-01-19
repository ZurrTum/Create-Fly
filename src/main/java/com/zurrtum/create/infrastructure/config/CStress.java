package com.zurrtum.create.infrastructure.config;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class CStress extends ConfigBase {
    // bump this version to reset configured values.
    private static final int VERSION = 2;

    // IDs need to be used since configs load before registration

    private static final Object2DoubleMap<Identifier> DEFAULT_IMPACTS = new Object2DoubleOpenHashMap<>();
    private static final Object2DoubleMap<Identifier> DEFAULT_CAPACITIES = new Object2DoubleOpenHashMap<>();

    protected final Map<Identifier, DoubleRawValue> capacities = new HashMap<>();
    protected final Map<Identifier, DoubleRawValue> impacts = new HashMap<>();

    public static void setNoImpact(Block block) {
        setImpact(block, 0);
    }

    public static void setImpact(Block block, double value) {
        DEFAULT_IMPACTS.put(BuiltInRegistries.BLOCK.getKey(block), value);
    }

    public static void setCapacity(Block block, double value) {
        DEFAULT_CAPACITIES.put(BuiltInRegistries.BLOCK.getKey(block), value);
    }

    @Override
    public void registerAll(Builder builder) {
        builder.comment(Comments.su, Comments.impact).push("impact");
        DEFAULT_IMPACTS.forEach((id, value) -> this.impacts.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        builder.comment(Comments.su, Comments.capacity).push("capacity");
        DEFAULT_CAPACITIES.forEach((id, value) -> this.capacities.put(id, builder.define(id.getPath(), value)));
        builder.pop();
    }

    @Override
    public String getName() {
        return "stressValues.v" + VERSION;
    }

    @Nullable
    public DoubleSupplier getImpact(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        DoubleRawValue value = this.impacts.get(id);
        return value == null ? null : value::get;
    }

    @Nullable
    public DoubleSupplier getCapacity(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        DoubleRawValue value = this.capacities.get(id);
        return value == null ? null : value::get;
    }

    private static class Comments {
        static String su = "[in Stress Units]";
        static String impact = "Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
        static String capacity = "Configure how much stress a source can accommodate for.";
    }

}
