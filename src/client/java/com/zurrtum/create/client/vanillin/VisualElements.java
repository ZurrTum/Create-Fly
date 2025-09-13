package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.client.vanillin.compose.VisualElement;
import com.zurrtum.create.client.vanillin.elements.FireElement;
import com.zurrtum.create.client.vanillin.elements.HitboxElement;
import com.zurrtum.create.client.vanillin.elements.ShadowElement;
import com.zurrtum.create.client.vanillin.visuals.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;

// TODO: A way to get other elements in a visual, likely using these as keys.
public class VisualElements {
    public static final VisualElement<Entity, Boolean> HITBOX = HitboxElement::new;
    public static final VisualElement<Entity, ShadowElement.Config> SHADOW = ShadowElement::new;
    public static final VisualElement.Unit<Entity> FIRE = FireElement::new;

    public static final VisualElement.Unit<ItemEntity> ITEM_ENTITY = ItemVisual::new;
    public static final VisualElement.Unit<DisplayEntity.ItemDisplayEntity> ITEM_DISPLAY = ItemDisplayVisual::new;
    public static final VisualElement.Unit<DisplayEntity.BlockDisplayEntity> BLOCK_DISPLAY = BlockDisplayVisual::new;
    public static final VisualElement.Unit<ItemFrameEntity> ITEM_FRAME = ItemFrameVisual::new;
    public static final VisualElement<AbstractMinecartEntity, EntityModelLayer> MINECART = MinecartVisual::new;
    public static final VisualElement.Unit<TntMinecartEntity> TNT_MINECART = TntMinecartVisual::new;

}
