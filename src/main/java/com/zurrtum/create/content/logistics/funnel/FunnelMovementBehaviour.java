package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
    public Vec3 getActiveAreaOffset(MovementContext context) {
        Direction facing = FunnelBlock.getFunnelFacing(context.state);
        Vec3 vec = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        if (facing != Direction.UP)
            return vec.scale(context.state.getValue(FunnelBlock.EXTRACTING) ? .15 : .65);

        return vec.scale(.65);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);

        if (context.state.getValue(FunnelBlock.EXTRACTING))
            extract(context, pos);
        else
            succ(context, pos);

    }

    private void extract(MovementContext context, BlockPos pos) {
        Level world = context.world;

        Vec3 entityPos = context.position;
        if (context.state.getValue(FunnelBlock.FACING) != Direction.DOWN)
            entityPos = entityPos.add(0, -.5f, 0);

        if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty())
            return;

        if (!world.getEntitiesOfClass(ItemEntity.class, new AABB(BlockPos.containing(entityPos))).isEmpty())
            return;

        FilterItemStack filter = context.getFilterFromBE();
        int filterAmount = context.blockEntityData.getIntOr("FilterAmount", 0);
        boolean upTo = context.blockEntityData.getBooleanOr("UpTo", false);
        filterAmount = hasFilter ? filterAmount : 1;

        Container inventory = context.contraption.getStorage().getAllItems();
        ItemStack extract;
        if (upTo) {
            extract = inventory.extract(s -> filter.test(world, s), filterAmount);
        } else {
            extract = inventory.preciseExtract(s -> filter.test(world, s), filterAmount);
        }

        if (extract.isEmpty())
            return;

        if (world.isClientSide())
            return;

        ItemEntity entity = new ItemEntity(world, entityPos.x, entityPos.y, entityPos.z, extract);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPickUpDelay(5);
        world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1 / 16f, .1f);
        world.addFreshEntity(entity);
    }

    private void succ(MovementContext context, BlockPos pos) {
        Level world = context.world;
        List<Entity> items = world.getEntities((Entity) null, new AABB(pos), e -> e instanceof ItemEntity || e instanceof PackageEntity);
        FilterItemStack filter = context.getFilterFromBE();

        for (Entity entity : items) {
            if (!entity.isAlive())
                continue;
            ItemStack toInsert = ItemHelper.fromItemEntity(entity);
            if (!filter.test(context.world, toInsert))
                continue;
            Container inventory = context.contraption.getStorage().getAllItems();
            int count = toInsert.getCount();
            int insert = inventory.insertExist(toInsert);
            if (insert == count) {
                entity.discard();
            } else if (insert > 0) {
                toInsert.setCount(count - insert);
                if (entity instanceof ItemEntity item) {
                    item.setItem(toInsert);
                }
            }
        }
    }

}
