package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SignalBlock extends Block implements IBE<SignalBlockEntity>, IWrenchable, WeakPowerControlBlock {

    public static final EnumProperty<SignalType> TYPE = EnumProperty.of("type", SignalType.class);
    public static final BooleanProperty POWERED = Properties.POWERED;

    public enum SignalType implements StringIdentifiable {
        ENTRY_SIGNAL,
        CROSS_SIGNAL;
        public static final Codec<SignalType> CODEC = StringIdentifiable.createCodec(SignalType::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public SignalBlock(Settings p_53182_) {
        super(p_53182_);
        setDefaultState(getDefaultState().with(TYPE, SignalType.ENTRY_SIGNAL).with(POWERED, false));
    }

    @Override
    public Class<SignalBlockEntity> getBlockEntityClass() {
        return SignalBlockEntity.class;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(TYPE, POWERED));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return getDefaultState().with(POWERED, pContext.getWorld().isReceivingRedstonePower(pContext.getBlockPos()));
    }

    @Override
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pIsMoving
    ) {
        if (pLevel.isClient())
            return;
        boolean powered = pState.get(POWERED);
        if (powered == pLevel.isReceivingRedstonePower(pPos))
            return;
        if (powered) {
            pLevel.scheduleBlockTick(pPos, this, 4);
        } else {
            pLevel.setBlockState(pPos, pState.cycle(POWERED), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
        if (pState.get(POWERED) && !pLevel.isReceivingRedstonePower(pPos))
            pLevel.setBlockState(pPos, pState.cycle(POWERED), Block.NOTIFY_LISTENERS);
    }

    @Override
    public BlockEntityType<? extends SignalBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK_SIGNAL;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        if (level.isClient())
            return ActionResult.SUCCESS;
        withBlockEntityDo(
            level, pos, ste -> {
                SignalBoundary signal = ste.getSignal();
                PlayerEntity player = context.getPlayer();
                if (signal != null) {
                    signal.cycleSignalType(pos);
                    if (player != null)
                        player.sendMessage(Text.translatable("create.track_signal.mode_change." + signal.getTypeFor(pos).asString()), true);
                } else if (player != null)
                    player.sendMessage(Text.translatable("create.track_signal.cannot_change_mode"), true);
            }
        );
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World blockAccess, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(blockAccess, pPos).filter(SignalBlockEntity::isPowered).map($ -> 15).orElse(0);
    }

}
