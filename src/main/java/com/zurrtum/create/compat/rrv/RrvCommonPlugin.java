package com.zurrtum.create.compat.rrv;

import cc.cassian.rrv.api.ReliableRecipeViewerPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableServerRecipeType;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class RrvCommonPlugin implements ReliableRecipeViewerPlugin {
    public static final ReliableServerRecipeType<PotionDisplay> POTION = ReliableServerRecipeType.register(
        Identifier.fromNamespaceAndPath(
            MOD_ID,
            "potion"
        ), PotionDisplay::new
    );

    @Override
    public void onIntegrationInitialize() {
        ItemView.addServerRecipeProvider(PotionDisplay::register);
    }
}
