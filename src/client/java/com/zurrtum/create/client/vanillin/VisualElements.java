package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.client.vanillin.compose.VisualElement;
import com.zurrtum.create.client.vanillin.elements.FireElement;
import com.zurrtum.create.client.vanillin.elements.HitboxElement;
import com.zurrtum.create.client.vanillin.elements.ShadowElement;
import com.zurrtum.create.client.vanillin.visuals.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;

// TODO: A way to get other elements in a visual, likely using these as keys.
public class VisualElements {
    public static final VisualElement<Entity, Boolean> HITBOX = HitboxElement::new;
    public static final VisualElement<Entity, ShadowElement.Config> SHADOW = ShadowElement::new;
    public static final VisualElement.Unit<Entity> FIRE = FireElement::new;

    public static final VisualElement.Unit<ItemEntity> ITEM_ENTITY = ItemVisual::new;
    public static final VisualElement.Unit<Display.ItemDisplay> ITEM_DISPLAY = ItemDisplayVisual::new;
    public static final VisualElement.Unit<Display.BlockDisplay> BLOCK_DISPLAY = BlockDisplayVisual::new;
    public static final VisualElement.Unit<ItemFrame> ITEM_FRAME = ItemFrameVisual::new;
    public static final VisualElement<AbstractMinecart, ModelLayerLocation> MINECART = MinecartVisual::new;
    public static final VisualElement.Unit<MinecartTNT> TNT_MINECART = TntMinecartVisual::new;

}
