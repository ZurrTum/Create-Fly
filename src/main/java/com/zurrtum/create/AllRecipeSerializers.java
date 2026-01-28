package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.equipment.toolbox.ToolboxDyeingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRecipeSerializers {
    public static final RecipeSerializer<CrushingRecipe> CRUSHING = register("crushing", CrushingRecipe.SERIALIZER);
    public static final RecipeSerializer<CuttingRecipe> CUTTING = register("cutting", CuttingRecipe.SERIALIZER);
    public static final RecipeSerializer<MillingRecipe> MILLING = register("milling", MillingRecipe.SERIALIZER);
    public static final RecipeSerializer<MixingRecipe> MIXING = register("mixing", MixingRecipe.SERIALIZER);
    public static final RecipeSerializer<CompactingRecipe> COMPACTING = register("compacting", CompactingRecipe.SERIALIZER);
    public static final RecipeSerializer<PressingRecipe> PRESSING = register("pressing", PressingRecipe.SERIALIZER);
    public static final RecipeSerializer<SandPaperPolishingRecipe> SANDPAPER_POLISHING = register(
        "sandpaper_polishing",
        SandPaperPolishingRecipe.SERIALIZER
    );
    public static final RecipeSerializer<SplashingRecipe> SPLASHING = register("splashing", SplashingRecipe.SERIALIZER);
    public static final RecipeSerializer<HauntingRecipe> HAUNTING = register("haunting", HauntingRecipe.SERIALIZER);
    public static final RecipeSerializer<DeployerApplicationRecipe> DEPLOYING = register("deploying", DeployerApplicationRecipe.SERIALIZER);
    public static final RecipeSerializer<FillingRecipe> FILLING = register("filling", FillingRecipe.SERIALIZER);
    public static final RecipeSerializer<EmptyingRecipe> EMPTYING = register("emptying", EmptyingRecipe.SERIALIZER);
    public static final RecipeSerializer<ManualApplicationRecipe> ITEM_APPLICATION = register("item_application", ManualApplicationRecipe.SERIALIZER);
    public static final RecipeSerializer<MechanicalCraftingRecipe> MECHANICAL_CRAFTING = register(
        "mechanical_crafting",
        MechanicalCraftingRecipe.SERIALIZER
    );
    public static final RecipeSerializer<SequencedAssemblyRecipe> SEQUENCED_ASSEMBLY = register(
        "sequenced_assembly",
        SequencedAssemblyRecipe.SERIALIZER
    );
    public static final RecipeSerializer<ItemCopyingRecipe> ITEM_COPYING = register("item_copying", ItemCopyingRecipe.SERIALIZER);
    public static final RecipeSerializer<ToolboxDyeingRecipe> TOOLBOX_DYEING = register("toolbox_dyeing", ToolboxDyeingRecipe.SERIALIZER);
    public static final RecipeSerializer<PotionRecipe> POTION = register("potion", PotionRecipe.SERIALIZER);

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, id), serializer);
    }

    public static void register() {
    }
}
