package com.zurrtum.create.content.redstone.diodes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BrassDiodeBlock extends AbstractDiodeBlock implements IBE<BrassDiodeBlockEntity>, RedStoneConnectBlock {

    public static final BooleanProperty POWERING = BooleanProperty.create("powering");
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public static final MapCodec<BrassDiodeBlock> CODEC = simpleCodec(BrassDiodeBlock::new);

    public BrassDiodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(POWERING, false).setValue(INVERTED, false));
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        return toggle(level, pos, state, player, hand);
    }

    public InteractionResult toggle(Level pLevel, BlockPos pPos, BlockState pState, Player player, InteractionHand pHand) {
        if (!player.mayBuild())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (player.isShiftKeyDown())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (player.getItemInHand(pHand).is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (pLevel.isClientSide())
            return InteractionResult.SUCCESS;
        pLevel.setBlock(pPos, pState.cycle(INVERTED), Block.UPDATE_ALL);
        float f = !pState.getValue(INVERTED) ? 0.6F : 0.5F;
        pLevel.playSound(null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, POWERING, FACING, INVERTED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
        return state.getValue(POWERING) ^ state.getValue(INVERTED) ? 15 : 0;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
    }

    @Override
    protected int getDelay(BlockState state) {
        return 2;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        if (side == null)
            return false;
        return side.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Class<BrassDiodeBlockEntity> getBlockEntityClass() {
        return BrassDiodeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BrassDiodeBlockEntity> getBlockEntityType() {
        return this == AllBlocks.PULSE_TIMER ? AllBlockEntityTypes.PULSE_TIMER : this == AllBlocks.PULSE_EXTENDER ? AllBlockEntityTypes.PULSE_EXTENDER : AllBlockEntityTypes.PULSE_REPEATER;
    }

    @Override
    protected MapCodec<? extends AbstractDiodeBlock> codec() {
        return CODEC;
    }
}