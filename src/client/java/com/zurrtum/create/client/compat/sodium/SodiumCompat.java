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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.HashSet;
import java.util.Set;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
@SuppressWarnings("deprecation")
public class SodiumCompat {
    private static final boolean DISABLE = !FabricLoader.getInstance().isModLoaded("sodium");
    public static final Material SAW_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, Create.asResource("block/saw_reversed"));
    public static final Material FACTORY_PANEL_TEXTURE = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        Create.asResource("block/factory_panel_connections_animated")
    );
    public static final Material SAW_VANILLA_TEXTURE = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        Identifier.withDefaultNamespace("block/stonecutter_saw")
    );

    @SuppressWarnings("UnstableApiUsage")
    public static void markSpriteActive(Minecraft mc) {
        if (DISABLE) {
            return;
        }
        AtlasManager atlasManager = mc.getAtlasManager();
        SpriteUtil.INSTANCE.markSpriteActive(atlasManager.get(SAW_TEXTURE));
        SpriteUtil.INSTANCE.markSpriteActive(atlasManager.get(SAW_VANILLA_TEXTURE));
        SpriteUtil.INSTANCE.markSpriteActive(atlasManager.get(FACTORY_PANEL_TEXTURE));
    }

    @SuppressWarnings("UnstableApiUsage")
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
            if (saw && state.is(AllBlocks.MECHANICAL_SAW)) {
                AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
                SpriteUtil.INSTANCE.markSpriteActive(atlasManager.get(SAW_TEXTURE));
                SpriteUtil.INSTANCE.markSpriteActive(atlasManager.get(SAW_VANILLA_TEXTURE));
                saw = false;
                continue;
            }
            if (panel && state.is(AllBlocks.FACTORY_GAUGE)) {
                SpriteUtil.INSTANCE.markSpriteActive(Minecraft.getInstance().getAtlasManager().get(FACTORY_PANEL_TEXTURE));
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
                Fluid fluid = fluidState.getType();
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