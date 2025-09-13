package com.zurrtum.create.content.equipment.bell;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class PeculiarBellBlock extends AbstractBellBlock<PeculiarBellBlockEntity> {

    public PeculiarBellBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends PeculiarBellBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.PECULIAR_BELL;
    }

    @Override
    public Class<PeculiarBellBlockEntity> getBlockEntityClass() {
        return PeculiarBellBlockEntity.class;
    }

    @Override
    public void playSound(World world, BlockPos pos) {
        AllSoundEvents.PECULIAR_BELL_USE.playOnServer(world, pos, 2f, 0.94f);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState newState = super.getPlacementState(ctx);
        if (newState == null)
            return null;

        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return tryConvert(world, pos, newState, world.getBlockState(pos.offset(Direction.DOWN)));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        BlockState newState = super.getStateForNeighborUpdate(state, world, tickView, currentPos, facing, facingPos, facingState, random);
        if (facing != Direction.DOWN)
            return newState;

        return tryConvert((WorldAccess) world, currentPos, newState, facingState);
    }

    protected BlockState tryConvert(WorldAccess world, BlockPos pos, BlockState state, BlockState underState) {
        if (!state.isOf(AllBlocks.PECULIAR_BELL))
            return state;

        Block underBlock = underState.getBlock();
        if (!(Blocks.SOUL_FIRE.equals(underBlock) || Blocks.SOUL_CAMPFIRE.equals(underBlock)))
            return state;

        if (world.isClient()) {
            spawnConversionParticles(world, pos);
        } else if (world instanceof World worldIn) {
            AllSoundEvents.HAUNTED_BELL_CONVERT.playOnServer(worldIn, pos);
        }

        return AllBlocks.HAUNTED_BELL.getDefaultState().with(HauntedBellBlock.FACING, state.get(FACING))
            .with(HauntedBellBlock.ATTACHMENT, state.get(ATTACHMENT)).with(HauntedBellBlock.POWERED, state.get(POWERED));
    }

    public void spawnConversionParticles(WorldAccess world, BlockPos blockPos) {
        Random random = world.getRandom();
        int num = random.nextInt(10) + 15;
        for (int i = 0; i < num; i++) {
            float pitch = random.nextFloat() * 120 - 90;
            float yaw = random.nextFloat() * 360;
            Vec3d vel = Vec3d.fromPolar(pitch, yaw).multiply(random.nextDouble() * 0.1 + 0.1);
            Vec3d pos = Vec3d.ofCenter(blockPos);
            world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }
    }

}