package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class FunnelMovementBehaviour extends MovementBehaviour {

    private final boolean hasFilter;

    public static FunnelMovementBehaviour andesite() {
        return new FunnelMovementBehaviour(false);
    }

    public static FunnelMovementBehaviour brass() {
        return new FunnelMovementBehaviour(true);
    }

    private FunnelMovementBehaviour(boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        Direction facing = FunnelBlock.getFunnelFacing(context.state);
        Vec3d vec = Vec3d.of(facing.getVector());
        if (facing != Direction.UP)
            return vec.multiply(context.state.get(FunnelBlock.EXTRACTING) ? .15 : .65);

        return vec.multiply(.65);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);

        if (context.state.get(FunnelBlock.EXTRACTING))
            extract(context, pos);
        else
            succ(context, pos);

    }

    private void extract(MovementContext context, BlockPos pos) {
        World world = context.world;

        Vec3d entityPos = context.position;
        if (context.state.get(FunnelBlock.FACING) != Direction.DOWN)
            entityPos = entityPos.add(0, -.5f, 0);

        if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty())
            return;

        if (!world.getNonSpectatingEntities(ItemEntity.class, new Box(BlockPos.ofFloored(entityPos))).isEmpty())
            return;

        FilterItemStack filter = context.getFilterFromBE();
        int filterAmount = context.blockEntityData.getInt("FilterAmount", 0);
        boolean upTo = context.blockEntityData.getBoolean("UpTo", false);
        filterAmount = hasFilter ? filterAmount : 1;

        Inventory inventory = context.contraption.getStorage().getAllItems();
        ItemStack extract;
        if (upTo) {
            extract = inventory.extract(s -> filter.test(world, s), filterAmount);
        } else {
            extract = inventory.preciseExtract(s -> filter.test(world, s), filterAmount);
        }

        if (extract.isEmpty())
            return;

        if (world.isClient)
            return;

        ItemEntity entity = new ItemEntity(world, entityPos.x, entityPos.y, entityPos.z, extract);
        entity.setVelocity(Vec3d.ZERO);
        entity.setPickupDelay(5);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1 / 16f, .1f);
        world.spawnEntity(entity);
    }

    private void succ(MovementContext context, BlockPos pos) {
        World world = context.world;
        List<Entity> items = world.getOtherEntities(null, new Box(pos), e -> e instanceof ItemEntity || e instanceof PackageEntity);
        FilterItemStack filter = context.getFilterFromBE();

        for (Entity entity : items) {
            if (!entity.isAlive())
                continue;
            ItemStack toInsert = ItemHelper.fromItemEntity(entity);
            if (!filter.test(context.world, toInsert))
                continue;
            Inventory inventory = context.contraption.getStorage().getAllItems();
            int count = toInsert.getCount();
            int insert = inventory.insertExist(toInsert);
            if (insert == count) {
                entity.discard();
            } else if (insert > 0) {
                toInsert.setCount(count - insert);
                if (entity instanceof ItemEntity item) {
                    item.setStack(toInsert);
                }
            }
        }
    }

}
