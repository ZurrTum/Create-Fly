package com.zurrtum.create.client.compat.fabric;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidTintSource;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.component.BottleType;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

public record PotionRenderHandler(
    FluidTintSource source) implements FluidVariantRenderHandler, FluidVariantAttributeHandler {
    public static final PotionRenderHandler INSTANCE = new PotionRenderHandler(AllFluidConfigs.TINT.get(AllFluids.POTION));

    @Override
    public Component getName(FluidVariant fluidVariant) {
        DataComponentMap components = fluidVariant.getComponents();
        PotionContents contents = components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        BottleType bottleType = components.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
        return contents.getName(PotionFluidHandler.itemFromBottleType(bottleType).getDescriptionId() + ".effect.");
    }

    @Override
    public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        return source.get(fluidVariant.getFluid(), fluidVariant.getComponentsPatch());
    }

    public static void register() {
        FluidVariantRendering.register(AllFluids.POTION, INSTANCE);
        FluidVariantAttributes.register(AllFluids.POTION, INSTANCE);
    }
}
