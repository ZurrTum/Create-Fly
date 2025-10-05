package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.foundation.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class StickerBlock extends WrenchableDirectionalBlock implements IBE<StickerBlockEntity>, WeakPowerControlBlock, LandingEffectControlBlock, RunningEffectControlBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty EXTENDED = Properties.EXTENDED;

    public StickerBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
        setDefaultState(getDefaultState().with(POWERED, false).with(EXTENDED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction nearestLookingDirection = context.getPlayerLookDirection();
        boolean shouldPower = context.getWorld().isReceivingRedstonePower(context.getBlockPos());
        Direction facing = context.getPlayer() != null && context.getPlayer()
            .isSneaking() ? nearestLookingDirection : nearestLookingDirection.getOpposite();

        return getDefaultState().with(FACING, facing).with(POWERED, shouldPower);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, EXTENDED));
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient())
            return;

        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos)) {
            state = state.cycle(POWERED);
            if (state.get(POWERED))
                state = state.cycle(EXTENDED);
            worldIn.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public Class<StickerBlockEntity> getBlockEntityClass() {
        return StickerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StickerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.STICKER;
    }

    // Slime block stuff

    private boolean isUprightSticker(BlockView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isOf(AllBlocks.STICKER) && blockState.get(FACING) == Direction.UP;
    }

    @Override
    public void onLandedUpon(World p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        if (!isUprightSticker(p_152426_, p_152428_) || p_152429_.bypassesLandingEffects())
            super.onLandedUpon(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_);
        p_152429_.handleFallDamage(p_152430_, 1.0F, p_152426_.getDamageSources().fall());
    }

    @Override
    public void onEntityLand(BlockView p_176216_1_, Entity p_176216_2_) {
        if (!isUprightSticker(p_176216_1_, p_176216_2_.getBlockPos().down()) || p_176216_2_.bypassesLandingEffects()) {
            super.onEntityLand(p_176216_1_, p_176216_2_);
        } else {
            this.bounceUp(p_176216_2_);
        }
    }

    private void bounceUp(Entity p_226946_1_) {
        Vec3d Vector3d = p_226946_1_.getVelocity();
        if (Vector3d.y < 0.0D) {
            double d0 = p_226946_1_ instanceof LivingEntity ? 1.0D : 0.8D;
            p_226946_1_.setVelocity(Vector3d.x, -Vector3d.y * d0, Vector3d.z);
        }
    }

    @Override
    public void onSteppedOn(World p_152431_, BlockPos p_152432_, BlockState p_152433_, Entity p_152434_) {
        double d0 = Math.abs(p_152434_.getVelocity().y);
        if (d0 < 0.1D && !p_152434_.bypassesSteppingEffects() && isUprightSticker(p_152431_, p_152432_)) {
            double d1 = 0.4D + d0 * 0.2D;
            p_152434_.setVelocity(p_152434_.getVelocity().multiply(d1, 1.0D, d1));
        }
        super.onSteppedOn(p_152431_, p_152432_, p_152433_, p_152434_);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos, LivingEntity entity, double distance) {
        if (state.get(FACING) == Direction.UP) {
            double e = entity.getX();
            double f = entity.getY();
            double g = entity.getZ();
            BlockPos blockPos = entity.getBlockPos();
            if (pos.getX() != blockPos.getX() || pos.getZ() != blockPos.getZ()) {
                double h = e - pos.getX() - 0.5;
                double i = g - pos.getZ() - 0.5;
                double j = Math.max(Math.abs(h), Math.abs(i));
                e = pos.getX() + 0.5 + h / j * 0.5;
                g = pos.getZ() + 0.5 + i / j * 0.5;
            }

            double h = Math.min(0.2F + distance / 15.0, 2.5);
            int k = (int) (150.0 * h);
            world.spawnParticles(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.getDefaultState()),
                e,
                f,
                g,
                k,
                0.0D,
                0.0D,
                0.0D,
                0.15F
            );
        }
        return false;
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(FACING) == Direction.UP) {
            Vec3d vec3d = entity.getVelocity();
            BlockPos blockPos = entity.getBlockPos();
            double d = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            double e = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            int x = pos.getX();
            if (blockPos.getX() != x) {
                d = MathHelper.clamp(d, x, x + 1.0);
            }

            int z = pos.getZ();
            if (blockPos.getZ() != z) {
                e = MathHelper.clamp(e, z, z + 1.0);
            }
            world.addParticleClient(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.getDefaultState()),
                d,
                entity.getY() + 0.1,
                e,
                vec3d.x * -4.0,
                1.5,
                vec3d.z * -4.0
            );
            return true;
        }
        return false;
    }

}
