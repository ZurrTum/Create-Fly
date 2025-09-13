package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.logistics.chute.AbstractChuteBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class EncasedFanBlock extends DirectionalKineticBlock implements IBE<EncasedFanBlockEntity> {

    public EncasedFanBlock(Settings properties) {
        super(properties);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        blockUpdate(state, worldIn, pos);
    }

    @Override
    public void prepare(BlockState stateIn, WorldAccess worldIn, BlockPos pos, int flags, int count) {
        super.prepare(stateIn, worldIn, pos, flags, count);
        blockUpdate(stateIn, worldIn, pos);
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
        blockUpdate(state, worldIn, pos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction face = context.getSide();

        BlockState placedOn = world.getBlockState(pos.offset(face.getOpposite()));
        BlockState placedOnOpposite = world.getBlockState(pos.offset(face));
        if (AbstractChuteBlock.isChute(placedOn))
            return getDefaultState().with(FACING, face.getOpposite());
        if (AbstractChuteBlock.isChute(placedOnOpposite))
            return getDefaultState().with(FACING, face);

        Direction preferredFacing = getPreferredFacing(context);
        if (preferredFacing == null)
            preferredFacing = context.getPlayerLookDirection();
        return getDefaultState().with(
            FACING,
            context.getPlayer() != null && context.getPlayer().isSneaking() ? preferredFacing : preferredFacing.getOpposite()
        );
    }

    protected void blockUpdate(BlockState state, WorldAccess worldIn, BlockPos pos) {
        if (worldIn instanceof WrappedLevel)
            return;
        notifyFanBlockEntity(worldIn, pos);
    }

    protected void notifyFanBlockEntity(WorldAccess world, BlockPos pos) {
        withBlockEntityDo(world, pos, EncasedFanBlockEntity::blockInFrontChanged);
    }

    @Override
    public BlockState updateAfterWrenched(BlockState newState, ItemUsageContext context) {
        blockUpdate(newState, context.getWorld(), context.getBlockPos());
        return newState;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(FACING).getOpposite();
    }

    @Override
    public Class<EncasedFanBlockEntity> getBlockEntityClass() {
        return EncasedFanBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EncasedFanBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ENCASED_FAN;
    }

}
