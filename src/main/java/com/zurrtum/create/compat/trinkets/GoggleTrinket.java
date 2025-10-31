package com.zurrtum.create.compat.trinkets;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import dev.emi.trinkets.api.TrinketsApi;

public class GoggleTrinket {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GogglesItem.addIsWearingPredicate(player -> TrinketsApi.getTrinketComponent(player)
                .map(component -> component.isEquipped(AllItems.GOGGLES)).orElse(false));
        }
    }
}
