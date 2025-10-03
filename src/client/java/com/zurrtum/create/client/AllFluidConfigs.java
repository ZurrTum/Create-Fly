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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllFluidConfigs {
    public static final Map<Fluid, FluidConfig> ALL = new IdentityHashMap<>();
    public static final Map<Fluid, FluidConfig> CACHE = new IdentityHashMap<>();
    private static final FluidConfig LAVA = new FluidConfig(
        () -> MinecraftClient.getInstance().getBlockRenderManager().fluidRenderer.lavaSprites[0],
        () -> MinecraftClient.getInstance().getBlockRenderManager().fluidRenderer.lavaSprites[1],
        component -> -1
    );
    private static final FluidConfig WATER = new FluidConfig(
        () -> MinecraftClient.getInstance().getBlockRenderManager().fluidRenderer.waterSprites[0],
        () -> MinecraftClient.getInstance().getBlockRenderManager().fluidRenderer.waterSprites[1],
        component -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null) {
                return 0x3f76e4;
            }
            return BiomeColors.getWaterColor(mc.world, mc.player.getBlockPos());
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
    private static void config(FlowableFluid fluid, int fogColor, Supplier<Float> fogDistance, Function<ComponentChanges, Integer> tint) {
        Identifier id = Registries.FLUID.getId(fluid).withPrefixedPath("fluid/");
        FluidConfig config = new FluidConfig(
            () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id.withSuffixedPath("_still")),
            () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id.withSuffixedPath("_flow")),
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
        MinecraftClient client = MinecraftClient.getInstance();
        FluidState state = fluid.getDefaultState();
        config = new FluidConfig(
            () -> handler.getFluidSprites(client.world, client.player != null ? client.player.getBlockPos() : null, state)[0],
            () -> handler.getFluidSprites(client.world, client.player != null ? client.player.getBlockPos() : null, state)[1],
            component -> handler.getFluidColor(client.world, client.player != null ? client.player.getBlockPos() : null, state)
        );
        CACHE.put(fluid, config);
        return config;
    }

    public static void register() {
        FogRenderer.FOG_MODIFIERS.addFirst(new DivingLavaFogModifier());
        FogRenderer.FOG_MODIFIERS.addFirst(new FluidFogModifier());
        config(
            AllFluids.POTION, -1, () -> 96.0f, component -> {
                Optional<? extends PotionContentsComponent> potion = component.get(DataComponentTypes.POTION_CONTENTS);
                if (potion != null && potion.isPresent()) {
                    return potion.get().getColor() | 0xFF000000;
                }
                return PotionContentsComponent.DEFAULT.getColor() | 0xFF000000;
            }
        );
        config(AllFluids.TEA);
        config(AllFluids.MILK);
        config(AllFluids.HONEY, 0xEAAE2F, () -> 96.0f * (1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()));
        config(AllFluids.CHOCOLATE, 0x622020, () -> 96.0f * (1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF()));
    }
}
