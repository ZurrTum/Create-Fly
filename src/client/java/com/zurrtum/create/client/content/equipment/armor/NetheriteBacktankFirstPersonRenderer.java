package com.zurrtum.create.client.content.equipment.armor;


import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class NetheriteBacktankFirstPersonRenderer {
    private static final Identifier BACKTANK_ARMOR_LOCATION = Create.asResource("textures/models/armor/netherite_diving_arm.png");

    @Nullable
    public static Identifier getHandTexture(@Nullable ClientPlayerEntity player) {
        if (player != null && player.getEquippedStack(EquipmentSlot.CHEST).isOf(AllItems.NETHERITE_BACKTANK)) {
            return BACKTANK_ARMOR_LOCATION;
        }
        return null;
    }
}
