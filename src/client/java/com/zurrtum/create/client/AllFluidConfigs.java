package com.zurrtum.create.client;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.content.equipment.armor.DivingLavaFogModifier;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.client.infrastructure.fluid.FluidFogModifier;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllFluidConfigs {
    public static final Map<Fluid, FluidConfig> ALL = new IdentityHashMap<>();
    public static final Map<Fluid, FluidConfig> CACHE = new IdentityHashMap<>();
    private static final FluidConfig LAVA = new FluidConfig(
        () -> Minecraft.getInstance().getBlockRenderer().liquidBlockRenderer.lavaStill,
        () -> Minecraft.getInstance().getBlockRenderer().liquidBlockRenderer.lavaFlowing,
        component -> -1
    );
    private static final FluidConfig WATER = new FluidConfig(
        () -> Minecraft.getInstance().getBlockRenderer().liquidBlockRenderer.waterStill,
        () -> Minecraft.getInstance().getBlockRenderer().liquidBlockRenderer.waterFlowing,
        component -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                return 0x3f76e4;
            }
            return BiomeColors.getAverageWaterColor(mc.level, mc.player.blockPosition());
        }
    );
    public static final boolean HAS_RENDER = FabricLoader.getInstance().isModLoaded("fabric-rendering-fluids-v1");

    private static void config(FlowableFluid fluid) {
        config(fluid, -1, () -> 96.0f);
    }

    private static void config(FlowableFluid fluid, int fogColor, Supplier<Float> fogDistance) {
        config(fluid, fogColor, fogDistance, component -> -1);
    }

    @SuppressWarnings("deprecation")
    private static void config(FlowableFluid fluid, int fogColor, Supplier<Float> fogDistance, Function<DataComponentPatch, Integer> tint) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid).withPrefix("fluid/");
        FluidConfig config = new FluidConfig(
            () -> Minecraft.getInstance().getAtlasManager().get(new Material(TextureAtlas.LOCATION_BLOCKS, id.withSuffix("_still"))),
            () -> Minecraft.getInstance().getAtlasManager().get(new Material(TextureAtlas.LOCATION_BLOCKS, id.withSuffix("_flow"))),
            tint,
            fogDistance,
            fogColor
        );
        ALL.put(fluid, config);
        ALL.put(fluid.getFlowing(), config);
    }

    public static FluidConfig get(Fluid fluid) {
        FluidConfig config = ALL.get(fluid);
        if (config != null) {
            return config;
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return LAVA;
        }
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return WATER;
        }
        if (!HAS_RENDER) {
            return null;
        }
        config = CACHE.get(fluid);
        if (config != null) {
            return config;
        }
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (handler == null) {
            return null;
        }
        Minecraft client = Minecraft.getInstance();
        FluidState state = fluid.defaultFluidState();
        config = new FluidConfig(
            () -> handler.getFluidSprites(client.level, client.player != null ? client.player.blockPosition() : null, state)[0],
            () -> handler.getFluidSprites(client.level, client.player != null ? client.player.blockPosition() : null, state)[1],
            component -> handler.getFluidColor(client.level, client.player != null ? client.player.blockPosition() : null, state)
        );
        CACHE.put(fluid, config);
        return config;
    }

    public static void register() {
        FogRenderer.FOG_ENVIRONMENTS.addFirst(new DivingLavaFogModifier());
        FogRenderer.FOG_ENVIRONMENTS.addFirst(new FluidFogModifier());
        config(
            AllFluids.POTION, -1, () -> 96.0f, component -> {
                Optional<? extends PotionContents> potion = component.get(DataComponents.POTION_CONTENTS);
                if (potion != null && potion.isPresent()) {
                    return potion.get().getColor() | 0xFF000000;
                }
                return PotionContents.EMPTY.getColor() | 0xFF000000;
            }
        );
        config(AllFluids.TEA);
        config(AllFluids.MILK);
        config(AllFluids.HONEY, 0xEAAE2F, () -> 96.0f * (1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()));
        config(AllFluids.CHOCOLATE, 0x622020, () -> 96.0f * (1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF()));
    }
}
