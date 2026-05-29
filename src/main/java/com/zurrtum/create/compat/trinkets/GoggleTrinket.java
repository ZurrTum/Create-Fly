package com.zurrtum.create.compat.trinkets;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import eu.pb4.trinkets.api.TrinketsApi;

public class GoggleTrinket {
    public static void register() {
        GogglesItem.addIsWearingPredicate(player -> TrinketsApi.getAttachment(player).isEquipped(AllItems.GOGGLES));
    }
}
