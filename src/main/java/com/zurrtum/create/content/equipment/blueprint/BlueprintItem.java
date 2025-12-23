package com.zurrtum.create.content.equipment.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction face = ctx.getClickedFace();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockPos pos = ctx.getClickedPos().relative(face);

        if (player != null && !player.mayUseItemAt(pos, face, stack))
            return InteractionResult.FAIL;

        Level world = ctx.getLevel();
        HangingEntity hangingentity = new BlueprintEntity(
            world,
            pos,
            face,
            face.getAxis().isHorizontal() ? Direction.DOWN : ctx.getHorizontalDirection()
        );
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData != null)
            EntityType.updateCustomEntityTag(world, player, hangingentity, TypedEntityData.of(hangingentity.getType(), customData.copyTag()));
        if (!hangingentity.survives())
            return InteractionResult.CONSUME;
        if (!world.isClientSide()) {
            hangingentity.playPlacementSound();
            world.addFreshEntity(hangingentity);
        }

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}