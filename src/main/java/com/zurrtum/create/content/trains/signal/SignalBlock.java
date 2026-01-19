package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class SignalBlock extends Block implements IBE<SignalBlockEntity>, IWrenchable, WeakPowerControlBlock {

    public static final EnumProperty<SignalType> TYPE = EnumProperty.create("type", SignalType.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public enum SignalType implements StringRepresentable {
        ENTRY_SIGNAL,
        CROSS_SIGNAL;
        public static final Codec<SignalType> CODEC = StringRepresentable.fromEnum(SignalType::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public SignalBlock(Properties p_53182_) {
        super(p_53182_);
        registerDefaultState(defaultBlockState().setValue(TYPE, SignalType.ENTRY_SIGNAL).setValue(POWERED, false));
    }

    @Override
    public Class<SignalBlockEntity> getBlockEntityClass() {
        return SignalBlockEntity.class;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(TYPE, POWERED));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return defaultBlockState().setValue(POWERED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()));
    }

    @Override
    public void neighborChanged(
        BlockState pState,
        Level pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable Orientation wireOrientation,
        boolean pIsMoving
    ) {
        if (pLevel.isClientSide())
            return;
        boolean powered = pState.getValue(POWERED);
        if (powered == pLevel.hasNeighborSignal(pPos))
            return;
        if (powered) {
            pLevel.scheduleTick(pPos, this, 4);
        } else {
            pLevel.setBlock(pPos, pState.cycle(POWERED), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        if (pState.getValue(POWERED) && !pLevel.hasNeighborSignal(pPos))
            pLevel.setBlock(pPos, pState.cycle(POWERED), Block.UPDATE_CLIENTS);
    }

    @Override
    public BlockEntityType<? extends SignalBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK_SIGNAL;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        withBlockEntityDo(
            level, pos, ste -> {
                SignalBoundary signal = ste.getSignal();
                Player player = context.getPlayer();
                if (signal != null) {
                    signal.cycleSignalType(pos);
                    if (player != null)
                        player.displayClientMessage(
                            Component.translatable("create.track_signal.mode_change." + signal.getTypeFor(pos).getSerializedName()), true);
                } else if (player != null)
                    player.displayClientMessage(Component.translatable("create.track_signal.cannot_change_mode"), true);
            }
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level blockAccess, BlockPos pPos, Direction direction) {
        return getBlockEntityOptional(blockAccess, pPos).filter(SignalBlockEntity::isPowered).map($ -> 15).orElse(0);
    }

}
