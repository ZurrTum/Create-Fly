package com.zurrtum.create;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.equipment.toolbox.ToolboxDyeingRecipe;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.HauntingRecipe;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRecipeSerializers {
    public static final RecipeSerializer<CrushingRecipe> CRUSHING = register("crushing", new CrushingRecipe.Serializer());
    public static final RecipeSerializer<CuttingRecipe> CUTTING = register("cutting", new CuttingRecipe.Serializer());
    public static final RecipeSerializer<MillingRecipe> MILLING = register("milling", new MillingRecipe.Serializer());
    public static final RecipeSerializer<MixingRecipe> MIXING = register("mixing", new MixingRecipe.Serializer());
    public static final RecipeSerializer<CompactingRecipe> COMPACTING = register("compacting", new CompactingRecipe.Serializer());
    public static final RecipeSerializer<PressingRecipe> PRESSING = register("pressing", new PressingRecipe.Serializer());
    public static final RecipeSerializer<SandPaperPolishingRecipe> SANDPAPER_POLISHING = register(
        "sandpaper_polishing",
        new SandPaperPolishingRecipe.Serializer()
    );
    public static final RecipeSerializer<SplashingRecipe> SPLASHING = register("splashing", new SplashingRecipe.Serializer());
    public static final RecipeSerializer<HauntingRecipe> HAUNTING = register("haunting", new HauntingRecipe.Serializer());
    public static final RecipeSerializer<DeployerApplicationRecipe> DEPLOYING = register(
        "deploying",
        new ItemApplicationRecipe.Serializer<>(DeployerApplicationRecipe::new)
    );
    public static final RecipeSerializer<FillingRecipe> FILLING = register("filling", new FillingRecipe.Serializer());
    public static final RecipeSerializer<EmptyingRecipe> EMPTYING = register("emptying", new EmptyingRecipe.Serializer());
    public static final RecipeSerializer<ManualApplicationRecipe> ITEM_APPLICATION = register(
        "item_application",
        new ItemApplicationRecipe.Serializer<>(ManualApplicationRecipe::new)
    );
    public static final RecipeSerializer<MechanicalCraftingRecipe> MECHANICAL_CRAFTING = register(
        "mechanical_crafting",
        new MechanicalCraftingRecipe.Serializer()
    );
    public static final RecipeSerializer<SequencedAssemblyRecipe> SEQUENCED_ASSEMBLY = register(
        "sequenced_assembly",
        new SequencedAssemblyRecipe.Serializer()
    );
    public static final RecipeSerializer<ItemCopyingRecipe> ITEM_COPYING = register(
        "item_copying",
        new SpecialCraftingRecipe.SpecialRecipeSerializer<>(ItemCopyingRecipe::new)
    );
    public static final RecipeSerializer<ToolboxDyeingRecipe> TOOLBOX_DYEING = register(
        "toolbox_dyeing",
        new SpecialCraftingRecipe.SpecialRecipeSerializer<>(ToolboxDyeingRecipe::new)
    );

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, id), serializer);
    }

    public static void register() {
    }
}
