package com.zurrtum.create.content.redstone.diodes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BrassDiodeBlock extends AbstractDiodeBlock implements IBE<BrassDiodeBlockEntity>, RedStoneConnectBlock {

    public static final BooleanProperty POWERING = BooleanProperty.of("powering");
    public static final BooleanProperty INVERTED = BooleanProperty.of("inverted");

    public static final MapCodec<BrassDiodeBlock> CODEC = createCodec(BrassDiodeBlock::new);

    public BrassDiodeBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false).with(POWERING, false).with(INVERTED, false));
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        return toggle(level, pos, state, player, hand);
    }

    public ActionResult toggle(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity player, Hand pHand) {
        if (!player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.getStackInHand(pHand).isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (pLevel.isClient())
            return ActionResult.SUCCESS;
        pLevel.setBlockState(pPos, pState.cycle(INVERTED), Block.NOTIFY_ALL);
        float f = !pState.get(INVERTED) ? 0.6F : 0.5F;
        pLevel.playSound(null, pPos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, POWERING, FACING, INVERTED);
        super.appendProperties(builder);
    }

    @Override
    protected int getOutputLevel(BlockView worldIn, BlockPos pos, BlockState state) {
        return state.get(POWERING) ^ state.get(INVERTED) ? 15 : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(FACING) == side ? this.getOutputLevel(blockAccess, pos, blockState) : 0;
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        if (side == null)
            return false;
        return side.getAxis() == state.get(FACING).getAxis();
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
    protected @NotNull MapCodec<? extends AbstractDiodeBlock> getCodec() {
        return CODEC;
    }
}