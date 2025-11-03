package com.zurrtum.create.client;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.content.contraptions.chassis.ChassisCTBehaviour;
import com.zurrtum.create.client.content.decoration.MetalScaffoldingCTBehaviour;
import com.zurrtum.create.client.content.decoration.RoofBlockCTBehaviour;
import com.zurrtum.create.client.content.decoration.TrapdoorCTBehaviour;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.content.decoration.girder.GirderCTBehaviour;
import com.zurrtum.create.client.content.decoration.palettes.CTs;
import com.zurrtum.create.client.content.decoration.palettes.WeatheredIronWindowCTBehaviour;
import com.zurrtum.create.client.content.decoration.palettes.WeatheredIronWindowPaneCTBehaviour;
import com.zurrtum.create.client.content.fluids.tank.FluidTankCTBehaviour;
import com.zurrtum.create.client.content.kinetics.crafter.CrafterCTBehaviour;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogCTBehaviour;
import com.zurrtum.create.client.content.logistics.tunnel.BrassTunnelCTBehaviour;
import com.zurrtum.create.client.content.logistics.vault.ItemVaultCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.GlassPaneCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.HorizontalCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.RotatedPillarCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.block.WoodType;

public class AllCTBehaviours {
    public static final EncasedCTBehaviour ANDESITE_CASING = new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING);
    public static final EncasedCTBehaviour BRASS_CASING = new EncasedCTBehaviour(AllSpriteShifts.BRASS_CASING);
    public static final EncasedCTBehaviour COPPER_CASING = new EncasedCTBehaviour(AllSpriteShifts.COPPER_CASING);
    public static final HorizontalCTBehaviour RAILWAY_CASING = new HorizontalCTBehaviour(
        AllSpriteShifts.RAILWAY_CASING_SIDE,
        AllSpriteShifts.RAILWAY_CASING
    );
    public static final EncasedCTBehaviour SHADOW_STEEL_CASING = new EncasedCTBehaviour(AllSpriteShifts.SHADOW_STEEL_CASING);
    public static final EncasedCTBehaviour REFINED_RADIANCE_CASING = new EncasedCTBehaviour(AllSpriteShifts.REFINED_RADIANCE_CASING);
    public static final EncasedCogCTBehaviour COG_SIDE_ANDESITE_CASING = new EncasedCogCTBehaviour(
        AllSpriteShifts.ANDESITE_CASING,
        Couple.create(AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_SIDE, AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_OTHERSIDE)
    );
    public static final EncasedCogCTBehaviour COG_SIDE_BRASS_CASING = new EncasedCogCTBehaviour(
        AllSpriteShifts.BRASS_CASING,
        Couple.create(AllSpriteShifts.BRASS_ENCASED_COGWHEEL_SIDE, AllSpriteShifts.BRASS_ENCASED_COGWHEEL_OTHERSIDE)
    );
    public static final EncasedCogCTBehaviour COG_ANDESITE_CASING = new EncasedCogCTBehaviour(AllSpriteShifts.ANDESITE_CASING, null);
    public static final EncasedCogCTBehaviour COG_BRASS_CASING = new EncasedCogCTBehaviour(AllSpriteShifts.BRASS_CASING, null);
    public static final ChassisCTBehaviour CHASSIS = new ChassisCTBehaviour();
    public static final FluidTankCTBehaviour FLUID_TANK = new FluidTankCTBehaviour(
        AllSpriteShifts.FLUID_TANK,
        AllSpriteShifts.FLUID_TANK_TOP,
        AllSpriteShifts.FLUID_TANK_INNER
    );
    public static final FluidTankCTBehaviour CREATIVE_FLUID_TANK = new FluidTankCTBehaviour(
        AllSpriteShifts.CREATIVE_FLUID_TANK,
        AllSpriteShifts.CREATIVE_CASING,
        AllSpriteShifts.CREATIVE_CASING
    );
    public static final HorizontalCTBehaviour INDUSTRIAL_IRON_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.INDUSTRIAL_IRON_WINDOW);
    public static final GlassPaneCTBehaviour INDUSTRIAL_IRON_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.INDUSTRIAL_IRON_WINDOW);
    public static final WeatheredIronWindowCTBehaviour WEATHERED_IRON_WINDOW = new WeatheredIronWindowCTBehaviour();
    public static final WeatheredIronWindowPaneCTBehaviour WEATHERED_IRON_WINDOW_PANE = new WeatheredIronWindowPaneCTBehaviour();
    public static final BrassTunnelCTBehaviour BRASS_TUNNEL = new BrassTunnelCTBehaviour();
    public static final GirderCTBehaviour METAL_GIRDER = new GirderCTBehaviour();
    public static final CrafterCTBehaviour CRAFTER = new CrafterCTBehaviour();
    public static final ItemVaultCTBehaviour ITEM_VAULT = new ItemVaultCTBehaviour();
    public static final HorizontalCTBehaviour ORNATE_IRON_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.ORNATE_IRON_WINDOW);
    public static final MetalScaffoldingCTBehaviour ANDESITE_SCAFFOLD = new MetalScaffoldingCTBehaviour(
        AllSpriteShifts.ANDESITE_SCAFFOLD,
        AllSpriteShifts.ANDESITE_SCAFFOLD_INSIDE,
        AllSpriteShifts.ANDESITE_CASING
    );
    public static final MetalScaffoldingCTBehaviour BRASS_SCAFFOLD = new MetalScaffoldingCTBehaviour(
        AllSpriteShifts.BRASS_SCAFFOLD,
        AllSpriteShifts.BRASS_SCAFFOLD_INSIDE,
        AllSpriteShifts.BRASS_CASING
    );
    public static final MetalScaffoldingCTBehaviour COPPER_SCAFFOLD = new MetalScaffoldingCTBehaviour(
        AllSpriteShifts.COPPER_SCAFFOLD,
        AllSpriteShifts.COPPER_SCAFFOLD_INSIDE,
        AllSpriteShifts.COPPER_CASING
    );
    public static final TrapdoorCTBehaviour FRAMED_GLASS_TRAPDOOR = new TrapdoorCTBehaviour();
    public static final RoofBlockCTBehaviour COPPER_SHINGLES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_SHINGLES.get(OxidationLevel.UNAFFECTED));
    public static final RoofBlockCTBehaviour EXPOSED_COPPER_SHINGLES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_SHINGLES.get(OxidationLevel.EXPOSED));
    public static final RoofBlockCTBehaviour WEATHERED_COPPER_SHINGLES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_SHINGLES.get(OxidationLevel.WEATHERED));
    public static final RoofBlockCTBehaviour OXIDIZED_COPPER_SHINGLES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_SHINGLES.get(OxidationLevel.OXIDIZED));
    public static final RoofBlockCTBehaviour COPPER_TILES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_TILES.get(OxidationLevel.UNAFFECTED));
    public static final RoofBlockCTBehaviour EXPOSED_COPPER_TILES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_TILES.get(OxidationLevel.EXPOSED));
    public static final RoofBlockCTBehaviour WEATHERED_COPPER_TILES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_TILES.get(OxidationLevel.WEATHERED));
    public static final RoofBlockCTBehaviour OXIDIZED_COPPER_TILES = new RoofBlockCTBehaviour(AllSpriteShifts.COPPER_TILES.get(OxidationLevel.OXIDIZED));
    public static final SimpleCTBehaviour FRAMED_GLASS = new SimpleCTBehaviour(AllSpriteShifts.FRAMED_GLASS);
    public static final HorizontalCTBehaviour HORIZONTAL_FRAMED_GLASS = new HorizontalCTBehaviour(
        AllSpriteShifts.HORIZONTAL_FRAMED_GLASS,
        AllSpriteShifts.FRAMED_GLASS
    );
    public static final HorizontalCTBehaviour VERTICAL_FRAMED_GLASS = new HorizontalCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS);
    public static final GlassPaneCTBehaviour FRAMED_GLASS_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.FRAMED_GLASS);
    public static final GlassPaneCTBehaviour HORIZONTAL_FRAMED_GLASS_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.HORIZONTAL_FRAMED_GLASS);
    public static final GlassPaneCTBehaviour VERTICAL_FRAMED_GLASS_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS);
    public static final HorizontalCTBehaviour OAK_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.OAK));
    public static final HorizontalCTBehaviour SPRUCE_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.SPRUCE));
    public static final HorizontalCTBehaviour BIRCH_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.BIRCH));
    public static final HorizontalCTBehaviour JUNGLE_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.JUNGLE));
    public static final HorizontalCTBehaviour ACACIA_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.ACACIA));
    public static final HorizontalCTBehaviour DARK_OAK_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.DARK_OAK));
    public static final HorizontalCTBehaviour MANGROVE_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.MANGROVE));
    public static final HorizontalCTBehaviour CRIMSON_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.CRIMSON));
    public static final HorizontalCTBehaviour WARPED_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.WARPED));
    public static final HorizontalCTBehaviour CHERRY_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.CHERRY));
    public static final HorizontalCTBehaviour BAMBOO_WINDOW = new HorizontalCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.BAMBOO));
    public static final GlassPaneCTBehaviour OAK_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.OAK));
    public static final GlassPaneCTBehaviour SPRUCE_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.SPRUCE));
    public static final GlassPaneCTBehaviour BIRCH_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.BIRCH));
    public static final GlassPaneCTBehaviour JUNGLE_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.JUNGLE));
    public static final GlassPaneCTBehaviour ACACIA_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.ACACIA));
    public static final GlassPaneCTBehaviour DARK_OAK_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.DARK_OAK));
    public static final GlassPaneCTBehaviour MANGROVE_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.MANGROVE));
    public static final GlassPaneCTBehaviour CRIMSON_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.CRIMSON));
    public static final GlassPaneCTBehaviour WARPED_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.WARPED));
    public static final GlassPaneCTBehaviour CHERRY_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.CHERRY));
    public static final GlassPaneCTBehaviour BAMBOO_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.getWoodenWindow(WoodType.BAMBOO));
    public static final GlassPaneCTBehaviour ORNATE_IRON_WINDOW_PANE = new GlassPaneCTBehaviour(AllSpriteShifts.ORNATE_IRON_WINDOW);
    public static final HorizontalCTBehaviour LAYERED_GRANITE = new HorizontalCTBehaviour(CTs.LAYERED.get("granite"), CTs.CAP.get("granite"));
    public static final RotatedPillarCTBehaviour GRANITE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("granite"), CTs.CAP.get("granite"));
    public static final HorizontalCTBehaviour LAYERED_DIORITE = new HorizontalCTBehaviour(CTs.LAYERED.get("diorite"), CTs.CAP.get("diorite"));
    public static final RotatedPillarCTBehaviour DIORITE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("diorite"), CTs.CAP.get("diorite"));
    public static final HorizontalCTBehaviour LAYERED_ANDESITE = new HorizontalCTBehaviour(CTs.LAYERED.get("andesite"), CTs.CAP.get("andesite"));
    public static final RotatedPillarCTBehaviour ANDESITE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("andesite"), CTs.CAP.get("andesite"));
    public static final HorizontalCTBehaviour LAYERED_CALCITE = new HorizontalCTBehaviour(CTs.LAYERED.get("calcite"), CTs.CAP.get("calcite"));
    public static final RotatedPillarCTBehaviour CALCITE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("calcite"), CTs.CAP.get("calcite"));
    public static final HorizontalCTBehaviour LAYERED_DRIPSTONE = new HorizontalCTBehaviour(CTs.LAYERED.get("dripstone"), CTs.CAP.get("dripstone"));
    public static final RotatedPillarCTBehaviour DRIPSTONE_PILLAR = new RotatedPillarCTBehaviour(
        CTs.PILLAR.get("dripstone"),
        CTs.CAP.get("dripstone")
    );
    public static final HorizontalCTBehaviour LAYERED_DEEPSLATE = new HorizontalCTBehaviour(CTs.LAYERED.get("deepslate"), CTs.CAP.get("deepslate"));
    public static final RotatedPillarCTBehaviour DEEPSLATE_PILLAR = new RotatedPillarCTBehaviour(
        CTs.PILLAR.get("deepslate"),
        CTs.CAP.get("deepslate")
    );
    public static final HorizontalCTBehaviour LAYERED_TUFF = new HorizontalCTBehaviour(CTs.LAYERED.get("tuff"), CTs.CAP.get("tuff"));
    public static final RotatedPillarCTBehaviour TUFF_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("tuff"), CTs.CAP.get("tuff"));
    public static final HorizontalCTBehaviour LAYERED_ASURINE = new HorizontalCTBehaviour(CTs.LAYERED.get("asurine"), CTs.CAP.get("asurine"));
    public static final RotatedPillarCTBehaviour ASURINE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("asurine"), CTs.CAP.get("asurine"));
    public static final HorizontalCTBehaviour LAYERED_CRIMSITE = new HorizontalCTBehaviour(CTs.LAYERED.get("crimsite"), CTs.CAP.get("crimsite"));
    public static final RotatedPillarCTBehaviour CRIMSITE_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("crimsite"), CTs.CAP.get("crimsite"));
    public static final HorizontalCTBehaviour LAYERED_LIMESTONE = new HorizontalCTBehaviour(CTs.LAYERED.get("limestone"), CTs.CAP.get("limestone"));
    public static final RotatedPillarCTBehaviour LIMESTONE_PILLAR = new RotatedPillarCTBehaviour(
        CTs.PILLAR.get("limestone"),
        CTs.CAP.get("limestone")
    );
    public static final HorizontalCTBehaviour LAYERED_OCHRUM = new HorizontalCTBehaviour(CTs.LAYERED.get("ochrum"), CTs.CAP.get("ochrum"));
    public static final RotatedPillarCTBehaviour OCHRUM_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("ochrum"), CTs.CAP.get("ochrum"));
    public static final HorizontalCTBehaviour LAYERED_SCORIA = new HorizontalCTBehaviour(CTs.LAYERED.get("scoria"), CTs.CAP.get("scoria"));
    public static final RotatedPillarCTBehaviour SCORIA_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("scoria"), CTs.CAP.get("scoria"));
    public static final HorizontalCTBehaviour LAYERED_SCORCHIA = new HorizontalCTBehaviour(CTs.LAYERED.get("scorchia"), CTs.CAP.get("scorchia"));
    public static final RotatedPillarCTBehaviour SCORCHIA_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("scorchia"), CTs.CAP.get("scorchia"));
    public static final HorizontalCTBehaviour LAYERED_VERIDIUM = new HorizontalCTBehaviour(CTs.LAYERED.get("veridium"), CTs.CAP.get("veridium"));
    public static final RotatedPillarCTBehaviour VERIDIUM_PILLAR = new RotatedPillarCTBehaviour(CTs.PILLAR.get("veridium"), CTs.CAP.get("veridium"));

    public static void register() {
    }
}
