package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.foundation.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StickerBlock extends WrenchableDirectionalBlock implements IBE<StickerBlockEntity>, WeakPowerControlBlock, LandingEffectControlBlock, RunningEffectControlBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

    public StickerBlock(Properties p_i48415_1_) {
        super(p_i48415_1_);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(EXTENDED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        boolean shouldPower = context.getLevel().hasNeighborSignal(context.getClickedPos());
        Direction facing = context.getPlayer() != null && context.getPlayer()
            .isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite();

        return defaultBlockState().setValue(FACING, facing).setValue(POWERED, shouldPower);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POWERED, EXTENDED));
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable Orientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClientSide())
            return;

        boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != worldIn.hasNeighborSignal(pos)) {
            state = state.cycle(POWERED);
            if (state.getValue(POWERED))
                state = state.cycle(EXTENDED);
            worldIn.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
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

    private boolean isUprightSticker(BlockGetter world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.is(AllBlocks.STICKER) && blockState.getValue(FACING) == Direction.UP;
    }

    @Override
    public void fallOn(Level p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, double p_152430_) {
        if (!isUprightSticker(p_152426_, p_152428_) || p_152429_.isSuppressingBounce())
            super.fallOn(p_152426_, p_152427_, p_152428_, p_152429_, p_152430_);
        p_152429_.causeFallDamage(p_152430_, 1.0F, p_152426_.damageSources().fall());
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter p_176216_1_, Entity p_176216_2_) {
        if (!isUprightSticker(p_176216_1_, p_176216_2_.blockPosition().below()) || p_176216_2_.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(p_176216_1_, p_176216_2_);
        } else {
            this.bounceUp(p_176216_2_);
        }
    }

    private void bounceUp(Entity p_226946_1_) {
        Vec3 Vector3d = p_226946_1_.getDeltaMovement();
        if (Vector3d.y < 0.0D) {
            double d0 = p_226946_1_ instanceof LivingEntity ? 1.0D : 0.8D;
            p_226946_1_.setDeltaMovement(Vector3d.x, -Vector3d.y * d0, Vector3d.z);
        }
    }

    @Override
    public void stepOn(Level p_152431_, BlockPos p_152432_, BlockState p_152433_, Entity p_152434_) {
        double d0 = Math.abs(p_152434_.getDeltaMovement().y);
        if (d0 < 0.1D && !p_152434_.isSteppingCarefully() && isUprightSticker(p_152431_, p_152432_)) {
            double d1 = 0.4D + d0 * 0.2D;
            p_152434_.setDeltaMovement(p_152434_.getDeltaMovement().multiply(d1, 1.0D, d1));
        }
        super.stepOn(p_152431_, p_152432_, p_152433_, p_152434_);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel world, BlockPos pos, LivingEntity entity, double distance) {
        if (state.getValue(FACING) == Direction.UP) {
            double e = entity.getX();
            double f = entity.getY();
            double g = entity.getZ();
            BlockPos blockPos = entity.blockPosition();
            if (pos.getX() != blockPos.getX() || pos.getZ() != blockPos.getZ()) {
                double h = e - pos.getX() - 0.5;
                double i = g - pos.getZ() - 0.5;
                double j = Math.max(Math.abs(h), Math.abs(i));
                e = pos.getX() + 0.5 + h / j * 0.5;
                g = pos.getZ() + 0.5 + i / j * 0.5;
            }

            double h = Math.min(0.2F + distance / 15.0, 2.5);
            int k = (int) (150.0 * h);
            world.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()),
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
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (state.getValue(FACING) == Direction.UP) {
            Vec3 vec3d = entity.getDeltaMovement();
            BlockPos blockPos = entity.blockPosition();
            double d = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
            double e = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
            int x = pos.getX();
            if (blockPos.getX() != x) {
                d = Mth.clamp(d, x, x + 1.0);
            }

            int z = pos.getZ();
            if (blockPos.getZ() != z) {
                e = Mth.clamp(e, z, z + 1.0);
            }
            world.addParticle(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()),
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
