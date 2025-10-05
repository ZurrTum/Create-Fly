package com.zurrtum.create.content.kinetics.turntable;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class TurntableBlock extends KineticBlock implements IBE<TurntableBlockEntity> {

    public TurntableBlock(Settings properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.TURNTABLE_SHAPE;
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity e, EntityCollisionHandler handler) {
        if (!e.isOnGround())
            return;
        if (e.getVelocity().y > 0)
            return;
        if (e.getY() < pos.getY() + .5f)
            return;

        withBlockEntityDo(
            worldIn, pos, be -> {
                float speed = be.getSpeed() * 3 / 10;
                if (speed == 0)
                    return;

                World world = e.getEntityWorld();
                if (world.isClient() && (e instanceof PlayerEntity)) {
                    if (worldIn.getBlockState(e.getBlockPos()) != state) {
                        Vec3d origin = VecHelper.getCenterOf(pos);
                        Vec3d offset = e.getPos().subtract(origin);
                        offset = VecHelper.rotate(offset, MathHelper.clamp(speed, -16, 16) / 1f, Axis.Y);
                        Vec3d movement = origin.add(offset).subtract(e.getPos());
                        e.setVelocity(e.getVelocity().add(movement));
                        e.velocityModified = true;
                    }
                }

                if ((e instanceof PlayerEntity))
                    return;
                if (world.isClient())
                    return;

                if ((e instanceof LivingEntity livingEntity)) {
                    float diff = e.getHeadYaw() - speed;
                    livingEntity.setDespawnCounter(20);
                    e.setBodyYaw(diff);
                    e.setHeadYaw(diff);
                    e.setOnGround(false);
                    e.velocityModified = true;
                }

                e.setYaw(e.getYaw() - speed);
            }
        );
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<TurntableBlockEntity> getBlockEntityClass() {
        return TurntableBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TurntableBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TURNTABLE;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
