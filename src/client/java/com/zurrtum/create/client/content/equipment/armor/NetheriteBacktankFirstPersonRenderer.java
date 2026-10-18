package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class NetheriteBacktankFirstPersonRenderer {
    private static final Identifier BACKTANK_ARMOR_LOCATION = Create.asResource(
        "textures/models/armor/netherite_diving_arm.png");

    @Nullable
    public static Identifier getHandTexture(AvatarRenderState state) {
        if (state.chestEquipment.is(AllItems.NETHERITE_BACKTANK)) {
            return BACKTANK_ARMOR_LOCATION;
        }
        return null;
    }
}
