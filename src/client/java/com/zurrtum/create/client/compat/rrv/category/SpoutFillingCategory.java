package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.SpoutFillingView;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class SpoutFillingCategory extends CreateCategory {
    public static final SpoutFillingCategory INSTANCE = new SpoutFillingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<FillingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.FILLING)) {
            output.add(new SpoutFillingView(entry.id().identifier(), entry.value()));
        }
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            FluidState fluidState = fluid.defaultFluidState();
            if (fluid.isSource(fluidState)) {
                registerGenericItem(fluid, output);
            }
        }
        HashMap<Item, List<ItemView.StackSensitive>> map = ItemView.getStackSensitive();
        int i = 0;
        for (ItemView.StackSensitive stackSensitive : map.get(Items.POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
        for (ItemView.StackSensitive stackSensitive : map.get(Items.SPLASH_POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
        for (ItemView.StackSensitive stackSensitive : map.get(Items.LINGERING_POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
    }

    private static void registerGenericItem(Fluid fluid, List<ReliableClientRecipe> output) {
        ItemStack item = Items.BUCKET.getDefaultInstance();
        try (FluidItemInventory capability = FluidHelper.getFluidInventory(item)) {
            if (capability == null) {
                return;
            }
            int insert = capability.insert(new FluidStack(fluid, BucketFluidInventory.CAPACITY));
            if (insert == 0) {
                return;
            }
            ItemStack result = capability.getContainer();
            if (result.isEmpty()) {
                return;
            }
            if (result.is(Items.BUCKET)) {
                return;
            }
            Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid);
            Identifier id = Identifier.fromNamespaceAndPath(
                MOD_ID,
                "fill_minecraft_bucket_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
            );
            FluidStack fluidStack = new FluidStack(fluid, BucketFluidInventory.CAPACITY);
            output.add(new SpoutFillingView(id, result, fluidStack, item));
        }
    }

    private static void registerPotionItem(int i, ItemStack stack, List<ReliableClientRecipe> output) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "filling_potions_" + i);
        FluidStack fluidStack = PotionFluidHandler.getFluidFromPotionItem(stack);
        output.add(new SpoutFillingView(id, stack, fluidStack, Items.GLASS_BOTTLE.getDefaultInstance()));
    }

    public SpoutFillingCategory() {
        super("spout_filling");
    }

    @Override
    public int getDisplayHeight() {
        return 66;
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.SPOUT.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.WATER_BUCKET.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.SPOUT.getDefaultInstance());
    }
}

