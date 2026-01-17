package com.zurrtum.create.compat.accessories;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import io.wispforest.accessories.api.AccessoriesCapability;

public class GoggleAccessory {
    public static void register() {
        GogglesItem.addIsWearingPredicate(player -> AccessoriesCapability.getOptionally(player)
            .map(capability -> capability.isEquipped(AllItems.GOGGLES)).orElse(false));
    }
}
