package com.zurrtum.create.client.compat.sodium;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
    private static final boolean DISABLE = !FabricLoader.getInstance().isModLoaded("sodium");
    public static final Identifier SAW_TEXTURE = Create.asResource("block/saw_reversed");
    public static final Identifier FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");
    public static final Identifier SAW_VANILLA_TEXTURE = Identifier.ofVanilla("block/stonecutter_saw");

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static void markSpriteActive(MinecraftClient mc) {
        if (DISABLE) {
            return;
        }
        Function<Identifier, Sprite> atlas = mc.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        SpriteUtil.INSTANCE.markSpriteActive(atlas.apply(SAW_TEXTURE));
        SpriteUtil.INSTANCE.markSpriteActive(atlas.apply(SAW_VANILLA_TEXTURE));
        SpriteUtil.INSTANCE.markSpriteActive(atlas.apply(FACTORY_PANEL_TEXTURE));
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static void markPonderSpriteActive(PonderLevel world, Selection section) {
        if (DISABLE) {
            return;
        }
        boolean saw = true;
        boolean panel = true;
        Set<Fluid> fluids = new HashSet<>();
        for (BlockPos pos : section) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (saw && state.isOf(AllBlocks.MECHANICAL_SAW)) {
                Function<Identifier, Sprite> atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
                SpriteUtil.INSTANCE.markSpriteActive(atlas.apply(SAW_TEXTURE));
                SpriteUtil.INSTANCE.markSpriteActive(atlas.apply(SAW_VANILLA_TEXTURE));
                saw = false;
                continue;
            }
            if (panel && state.isOf(AllBlocks.FACTORY_GAUGE)) {
                SpriteUtil.INSTANCE.markSpriteActive(MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                    .apply(FACTORY_PANEL_TEXTURE));
                panel = false;
                continue;
            }
            if (state.getBlock() instanceof FluidTankBlock && world.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
                FluidStack stack = tank.getTankInventory().getFluid();
                if (stack.isEmpty()) {
                    continue;
                }
                Fluid fluid = stack.getFluid();
                if (fluids.add(fluid)) {
                    markFluidSpriteActive(fluid);
                }
                continue;
            }
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                Fluid fluid = fluidState.getFluid();
                if (fluids.add(fluid)) {
                    markFluidSpriteActive(fluid);
                }
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void markFluidSpriteActive(Fluid fluid) {
        if (DISABLE) {
            return;
        }
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            SpriteUtil.INSTANCE.markSpriteActive(config.still().get());
            SpriteUtil.INSTANCE.markSpriteActive(config.flowing().get());
        }
    }
}