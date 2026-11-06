package com.zurrtum.create.compat.rrv;

import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ItemView.StackSensitive;
import cc.cassian.rrv.api.recipe.ReliableServerRecipe;
import cc.cassian.rrv.api.recipe.ReliableServerRecipeType;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import cc.cassian.rrv.common.recipe.ServerRecipeManager;
import com.mojang.serialization.Codec;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;

public class PotionDisplay implements ReliableServerRecipe {
    public int id;
    public ItemStack item;
    public FluidStack fluid;

    public PotionDisplay() {
    }

    public PotionDisplay(int id, ItemStack item, FluidStack fluid) {
        this.id = id;
        this.item = item;
        this.fluid = fluid;
    }

    public static void register(List<ReliableServerRecipe> output) {
        HashMap<Item, List<StackSensitive>> map = ItemView.getStackSensitive();
        int i = 0;
        for (StackSensitive stackSensitive : map.get(Items.POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
        for (StackSensitive stackSensitive : map.get(Items.SPLASH_POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
        for (StackSensitive stackSensitive : map.get(Items.LINGERING_POTION)) {
            registerPotionItem(i++, stackSensitive.stack(), output);
        }
    }

    private static void registerPotionItem(int i, ItemStack stack, List<ReliableServerRecipe> output) {
        output.add(new PotionDisplay(i, stack, PotionFluidHandler.getFluidFromPotionItem(stack)));
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = ServerRecipeManager.INSTANCE.createSerializationContext();
        tag.store("id", Codec.INT, id);
        tag.store("item", ItemStack.CODEC, ops, item);
        tag.store("fluid", FluidStack.CODEC, ops, fluid);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = ClientRecipeManager.INSTANCE.createSerializationContext();
        id = tag.read("id", Codec.INT).orElseThrow();
        item = tag.read("item", ItemStack.CODEC, ops).orElseThrow();
        fluid = tag.read("fluid", FluidStack.CODEC, ops).orElseThrow();
    }

    @Override
    public ReliableServerRecipeType<PotionDisplay> getRecipeType() {
        return RrvCommonPlugin.POTION;
    }
}
