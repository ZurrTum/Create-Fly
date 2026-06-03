package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.MysteriousItemConversionView;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class MysteriousItemConversionCategory extends CreateCategory {
    public static final MysteriousItemConversionCategory INSTANCE = new MysteriousItemConversionCategory();

    public static void register(List<ReliableClientRecipe> output) {
        output.add(new MysteriousItemConversionView(
            Identifier.fromNamespaceAndPath(MOD_ID, "to_blaze_burner"),
            AllItems.EMPTY_BLAZE_BURNER,
            AllItems.BLAZE_BURNER
        ));
        output.add(new MysteriousItemConversionView(
            Identifier.fromNamespaceAndPath(MOD_ID, "to_haunted_bell"),
            AllItems.PECULIAR_BELL,
            AllItems.HAUNTED_BELL
        ));
    }

    public MysteriousItemConversionCategory() {
        super("mystery_conversion");
    }

    @Override
    public int getDisplayHeight() {
        return 29;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.PECULIAR_BELL.getDefaultInstance();
    }
}
