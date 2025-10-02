package com.zurrtum.create.compat.rei;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.compat.rei.display.*;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SmeltingRecipe;

import static com.zurrtum.create.Create.MOD_ID;

public class ReiCommonPlugin implements REICommonPlugin {
    public static final CategoryIdentifier<CraftingDisplay> AUTOMATIC_PACKING = CategoryIdentifier.of(MOD_ID, "automatic_packing");
    public static final CategoryIdentifier<CompactingDisplay> PACKING = CategoryIdentifier.of(MOD_ID, "packing");
    public static final CategoryIdentifier<PressingDisplay> PRESSING = CategoryIdentifier.of(MOD_ID, "pressing");
    public static final CategoryIdentifier<CraftingDisplay> AUTOMATIC_SHAPELESS = CategoryIdentifier.of(MOD_ID, "automatic_shapeless");
    public static final CategoryIdentifier<MixingDisplay> MIXING = CategoryIdentifier.of(MOD_ID, "mixing");
    public static final CategoryIdentifier<MillingDisplay> MILLING = CategoryIdentifier.of(MOD_ID, "milling");
    public static final CategoryIdentifier<SawingDisplay> SAWING = CategoryIdentifier.of(MOD_ID, "sawing");
    public static final CategoryIdentifier<CrushingDisplay> CRUSHING = CategoryIdentifier.of(MOD_ID, "crushing");
    public static final CategoryIdentifier<ManualApplicationDisplay> ITEM_APPLICATION = CategoryIdentifier.of(MOD_ID, "item_application");
    public static final CategoryIdentifier<DeployingDisplay> DEPLOYING = CategoryIdentifier.of(MOD_ID, "deploying");
    public static final CategoryIdentifier<DrainingDisplay> DRAINING = CategoryIdentifier.of(MOD_ID, "draining");
    public static final CategoryIdentifier<MechanicalCraftingDisplay> MECHANICAL_CRAFTING = CategoryIdentifier.of(MOD_ID, "mechanical_crafting");
    public static final CategoryIdentifier<SpoutFillingDisplay> SPOUT_FILLING = CategoryIdentifier.of(MOD_ID, "spout_filling");
    public static final CategoryIdentifier<SandpaperPolishingDisplay> SANDPAPER_POLISHING = CategoryIdentifier.of(MOD_ID, "sandpaper_polishing");
    public static final CategoryIdentifier<SequencedAssemblyDisplay> SEQUENCED_ASSEMBLY = CategoryIdentifier.of(MOD_ID, "sequenced_assembly");
    public static final CategoryIdentifier<BlockCuttingDisplay> BLOCK_CUTTING = CategoryIdentifier.of(MOD_ID, "block_cutting");
    public static final CategoryIdentifier<FanBlastingDisplay> FAN_BLASTING = CategoryIdentifier.of(MOD_ID, "fan_blasting");

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CraftingRecipe.class).fill(AutoCompactingDisplay::of);
        registry.beginRecipeFiller(CompactingRecipe.class).filterType(AllRecipeTypes.COMPACTING).fill(CompactingDisplay::new);
        registry.beginRecipeFiller(PressingRecipe.class).filterType(AllRecipeTypes.PRESSING).fill(PressingDisplay::new);
        registry.beginRecipeFiller(ShapelessRecipe.class).fill(AutoMixingDisplay::of);
        registry.beginRecipeFiller(MixingRecipe.class).fill(MixingDisplay::new);
        registry.beginRecipeFiller(MillingRecipe.class).fill(MillingDisplay::new);
        registry.beginRecipeFiller(CuttingRecipe.class).fill(SawingDisplay::new);
        registry.beginRecipeFiller(CrushingRecipe.class).fill(CrushingDisplay::of);
        registry.beginRecipeFiller(MillingRecipe.class).fill(CrushingDisplay::of);
        registry.beginRecipeFiller(ManualApplicationRecipe.class).fill(ManualApplicationDisplay::new);
        registry.beginRecipeFiller(ItemApplicationRecipe.class).fill(DeployingDisplay::new);
        registry.beginRecipeFiller(EmptyingRecipe.class).fill(DrainingDisplay::new);
        registry.beginRecipeFiller(MechanicalCraftingRecipe.class).fill(MechanicalCraftingDisplay::new);
        registry.beginRecipeFiller(FillingRecipe.class).fill(SpoutFillingDisplay::new);
        registry.beginRecipeFiller(SandPaperPolishingRecipe.class).fill(SandpaperPolishingDisplay::new);
        registry.beginRecipeFiller(SequencedAssemblyRecipe.class).fill(SequencedAssemblyDisplay::new);
        registry.beginRecipeFiller(BlastingRecipe.class).fill(FanBlastingDisplay::of);
        registry.beginRecipeFiller(SmeltingRecipe.class).fill(FanBlastingDisplay::of);
        BlockCuttingDisplay.register(registry);
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shapeless"),
            AutoCompactingDisplay.ShapelessDisplay.SERIALIZER
        );
        registry.register(AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/default/shaped"), AutoCompactingDisplay.ShapedDisplay.SERIALIZER);
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/client/shaped"),
            AutoCompactingDisplay.CraftingDisplayShaped.SERIALIZER
        );
        registry.register(
            AUTOMATIC_PACKING.getIdentifier().withSuffixedPath("/client/shapeless"),
            AutoCompactingDisplay.CraftingDisplayShapeless.SERIALIZER
        );
        registry.register(PACKING.getIdentifier(), CompactingDisplay.SERIALIZER);
        registry.register(PRESSING.getIdentifier(), PressingDisplay.SERIALIZER);
        registry.register(AUTOMATIC_SHAPELESS.getIdentifier().withSuffixedPath("/default/shapeless"), AutoMixingDisplay.ShapelessDisplay.SERIALIZER);
        registry.register(
            AUTOMATIC_SHAPELESS.getIdentifier().withSuffixedPath("/client/shapeless"),
            AutoMixingDisplay.CraftingDisplayShapeless.SERIALIZER
        );
        registry.register(MIXING.getIdentifier(), MixingDisplay.SERIALIZER);
        registry.register(MILLING.getIdentifier(), MillingDisplay.SERIALIZER);
        registry.register(SAWING.getIdentifier(), SawingDisplay.SERIALIZER);
        registry.register(CRUSHING.getIdentifier(), CrushingDisplay.SERIALIZER);
        registry.register(ITEM_APPLICATION.getIdentifier(), ManualApplicationDisplay.SERIALIZER);
        registry.register(DEPLOYING.getIdentifier(), DeployingDisplay.SERIALIZER);
        registry.register(DRAINING.getIdentifier(), DrainingDisplay.SERIALIZER);
        registry.register(MECHANICAL_CRAFTING.getIdentifier(), MechanicalCraftingDisplay.SERIALIZER);
        registry.register(SPOUT_FILLING.getIdentifier(), SpoutFillingDisplay.SERIALIZER);
        registry.register(SANDPAPER_POLISHING.getIdentifier(), SandpaperPolishingDisplay.SERIALIZER);
        registry.register(SEQUENCED_ASSEMBLY.getIdentifier(), SequencedAssemblyDisplay.SERIALIZER);
        registry.register(BLOCK_CUTTING.getIdentifier(), BlockCuttingDisplay.SERIALIZER);
        registry.register(FAN_BLASTING.getIdentifier(), FanBlastingDisplay.SERIALIZER);
    }
}
