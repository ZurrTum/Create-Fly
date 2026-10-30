package com.zurrtum.create.client;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.content.equipment.armor.DivingLavaFogModifier;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidFogModifier;
import com.zurrtum.create.client.infrastructure.fluid.FluidTintSource;
import com.zurrtum.create.client.infrastructure.fluid.FogDistanceSupplier;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllFluidConfigs {
    public static final Map<FlowingFluid, FluidModel.Unbaked> MODEL = new IdentityHashMap<>();
    public static final Map<Fluid, FogDistanceSupplier> FOG_DISTANCE = new IdentityHashMap<>();
    public static final Map<Fluid, Vector3fc> FOG_COLOR = new IdentityHashMap<>();
    public static final Map<Fluid, FluidTintSource> TINT = new IdentityHashMap<>();
    public static final FluidModel.Unbaked POTION = model(AllFluids.POTION);
    public static final FluidModel.Unbaked TEA = model(AllFluids.TEA);
    public static final FluidModel.Unbaked MILK = model(AllFluids.MILK);
    public static final FluidModel.Unbaked HONEY = model(AllFluids.HONEY);
    public static final FluidModel.Unbaked CHOCOLATE = model(AllFluids.CHOCOLATE);
    public static final boolean HAS_RENDER = FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1");

    public static FluidModel.Unbaked model(FlowingFluid fluid) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
        Material still = new Material(id.withPath(path -> "block/" + path + "_still"));
        Material flow = new Material(id.withPath(path -> "block/" + path + "_flow"));
        FluidModel.Unbaked model = new FluidModel.Unbaked(still, flow, null, null);
        MODEL.put(fluid, model);
        return model;
    }

    public static void fog(FlowableFluid fluid, Vector3fc fogColor, FogDistanceSupplier fogDistance) {
        FOG_COLOR.put(fluid, fogColor);
        FOG_DISTANCE.put(fluid, fogDistance);
        Fluid flowing = fluid.getFlowing();
        FOG_COLOR.put(flowing, fogColor);
        FOG_DISTANCE.put(flowing, fogDistance);
    }

    public static void tint(FlowableFluid fluid, FluidTintSource tint) {
        TINT.put(fluid, tint);
        TINT.put(fluid.getFlowing(), tint);
    }

    public static int getTint(
        @Nullable BlockAndTintGetter level,
        double x,
        double y,
        double z,
        FluidState state,
        FluidModel model,
        Fluid fluid,
        DataComponentPatch components
    ) {
        FluidTintSource fluidTintSource = TINT.get(fluid);
        if (fluidTintSource != null) {
            return fluidTintSource.get(fluid, components);
        }
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        if (HAS_RENDER) {
            return FluidVariantRendering.getColor(FluidVariant.of(fluid, components), level, pos);
        }
        BlockTintSource blockTintSource = model.tintSource();
        if (blockTintSource == null) {
            return -1;
        }
        BlockState blockState = state.createLegacyBlock();
        if (level != null) {
            return blockTintSource.colorInWorld(blockState, level, pos);
        }
        return blockTintSource.color(blockState);
    }

    public static int getTint(
        @Nullable BlockAndTintGetter level,
        double x,
        double y,
        double z,
        BlockState blockState,
        FluidModel model,
        Fluid fluid,
        DataComponentPatch components
    ) {
        FluidTintSource fluidTintSource = TINT.get(fluid);
        if (fluidTintSource != null) {
            return fluidTintSource.get(fluid, components);
        }
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        if (HAS_RENDER) {
            return FluidVariantRendering.getColor(FluidVariant.of(fluid, components), level, pos);
        }
        return getBlockTint(level, pos, blockState, model.tintSource());
    }

    public static int getTint(
        @Nullable BlockAndTintGetter level,
        @Nullable BlockPos pos,
        BlockState blockState,
        FluidModel model,
        Fluid fluid,
        DataComponentPatch components
    ) {
        FluidTintSource fluidTintSource = TINT.get(fluid);
        if (fluidTintSource != null) {
            return fluidTintSource.get(fluid, components);
        }
        if (HAS_RENDER) {
            return FluidVariantRendering.getColor(FluidVariant.of(fluid, components), level, pos);
        }
        return getBlockTint(level, pos, blockState, model.tintSource());
    }

    private static int getBlockTint(
        @Nullable BlockAndTintGetter level,
        @Nullable BlockPos pos,
        BlockState blockState,
        @Nullable BlockTintSource blockTintSource
    ) {
        if (blockTintSource == null) {
            return -1;
        }
        if (level != null && pos != null) {
            return blockTintSource.colorInWorld(blockState, level, pos);
        }
        return blockTintSource.color(blockState);
    }

    public static void register() {
        FogRenderer.FOG_ENVIRONMENTS.addFirst(new DivingLavaFogModifier());
        FogRenderer.FOG_ENVIRONMENTS.addFirst(new FluidFogModifier());
        fog(
            AllFluids.HONEY,
            ARGB.vector3fFromRGB24(0xEAAE2F),
            () -> 96.0f * (1.0f / 8.0f * AllConfigs.client().honeyTransparencyMultiplier.getF())
        );
        fog(
            AllFluids.CHOCOLATE,
            ARGB.vector3fFromRGB24(0x622020),
            () -> 96.0f * (1.0f / 32.0f * AllConfigs.client().chocolateTransparencyMultiplier.getF())
        );
        tint(
            AllFluids.POTION, (fluid, component) -> {
                PotionContents potion = component.get(DataComponentMap.EMPTY, DataComponents.POTION_CONTENTS);
                if (potion == null) {
                    potion = PotionContents.EMPTY;
                }
                return potion.getColor() | 0xFF000000;
            }
        );
    }
}
