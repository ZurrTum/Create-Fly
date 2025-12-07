package com.zurrtum.create.content.equipment.blueprint;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlueprintItem extends Item {

    public BlueprintItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        Direction face = ctx.getSide();
        PlayerEntity player = ctx.getPlayer();
        ItemStack stack = ctx.getStack();
        BlockPos pos = ctx.getBlockPos().offset(face);

        if (player != null && !player.canPlaceOn(pos, face, stack))
            return ActionResult.FAIL;

        World world = ctx.getWorld();
        AbstractDecorationEntity hangingentity = new BlueprintEntity(
            world,
            pos,
            face,
            face.getAxis().isHorizontal() ? Direction.DOWN : ctx.getHorizontalPlayerFacing()
        );
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customData != null)
            EntityType.loadFromEntityNbt(world, player, hangingentity, customData);
        if (!hangingentity.canStayAttached())
            return ActionResult.CONSUME;
        if (!world.isClient) {
            hangingentity.onPlace();
            world.spawnEntity(hangingentity);
        }

        stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}