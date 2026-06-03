package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.DrainingView;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
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

import java.util.HashMap;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class DrainingCategory extends CreateCategory {
    public static final DrainingCategory INSTANCE = new DrainingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        for (RecipeHolder<EmptyingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.EMPTYING)) {
            output.add(new DrainingView(entry.id().identifier(), entry.value()));
        }
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid.isSource(fluid.defaultFluidState())) {
                Item bucket = fluid.getBucket();
                if (bucket == Items.AIR) {
                    continue;
                }
                registerGenericItem(bucket.getDefaultInstance(), output);
            }
        }
        registerGenericItem(Items.MILK_BUCKET.getDefaultInstance(), output);
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

    private static void registerGenericItem(ItemStack stack, List<ReliableClientRecipe> output) {
        try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack)) {
            if (capability == null) {
                return;
            }
            FluidStack fluid = capability.extractAny(BucketFluidInventory.CAPACITY);
            if (fluid.isEmpty()) {
                return;
            }
            Identifier itemName = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
            Identifier id = Identifier.fromNamespaceAndPath(
                MOD_ID,
                "empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
            );
            output.add(new DrainingView(id, capability.getContainer(), fluid, stack));
        }
    }

    private static void registerPotionItem(int i, ItemStack item, List<ReliableClientRecipe> output) {
        output.add(new DrainingView(
            Identifier.fromNamespaceAndPath(MOD_ID, "draining_potions_" + i),
            Items.GLASS_BOTTLE.getDefaultInstance(),
            PotionFluidHandler.getFluidFromPotionItem(item),
            item
        ));
    }

    public DrainingCategory() {
        super("draining");
    }

    @Override
    public int getDisplayHeight() {
        return 44;
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.ITEM_DRAIN.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return Items.WATER_BUCKET.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.ITEM_DRAIN.getDefaultInstance());
    }
}
