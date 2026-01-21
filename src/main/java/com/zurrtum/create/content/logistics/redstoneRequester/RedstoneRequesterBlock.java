package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.component.AutoRequestData;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class RedstoneRequesterBlock extends Block implements IBE<RedstoneRequesterBlockEntity>, IWrenchable, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public RedstoneRequesterBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POWERED, AXIS));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState stateForPlacement = super.getStateForPlacement(pContext);
        if (stateForPlacement == null)
            return null;
        return stateForPlacement.setValue(AXIS, pContext.getHorizontalDirection().getAxis())
            .setValue(POWERED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos, Direction direction) {
        RedstoneRequesterBlockEntity req = getBlockEntity(pLevel, pPos);
        return req != null && req.lastRequestSucceeded ? 15 : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return onBlockEntityUse(level, pos, be -> be.use(player));
    }

    public static void programRequester(ServerPlayer player, StockTickerBlockEntity be, PackageOrderWithCrafts order, String address) {
        ItemStack stack = player.getMainHandItem();
        boolean isRequester = stack.is(AllItems.REDSTONE_REQUESTER);
        boolean isShopCloth = stack.is(AllItemTags.TABLE_CLOTHS);
        if (!isRequester && !isShopCloth)
            return;

        String targetDim = player.level().dimension().identifier().toString();
        AutoRequestData autoRequestData = new AutoRequestData(order, address, be.getBlockPos(), targetDim, false);

        autoRequestData.writeToItem(BlockPos.ZERO, stack);

        if (isRequester) {
            TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag beTag;
            BlockEntityType<?> type;
            if (data != null) {
                beTag = data.copyTagWithoutId();
                type = data.type();
            } else {
                beTag = new CompoundTag();
                type = AllBlockEntityTypes.PACKAGER_LINK;
            }
            beTag.store("Freq", UUIDUtil.CODEC, be.behaviour.freqId);
            beTag.store("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, AllBlockEntityTypes.REDSTONE_REQUESTER);
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, beTag));
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    public static void appendRequesterTooltip(ItemStack pStack, Consumer<Component> pTooltip) {
        if (!pStack.has(AllDataComponents.AUTO_REQUEST_DATA))
            return;

        AutoRequestData data = pStack.get(AllDataComponents.AUTO_REQUEST_DATA);

        //noinspection DataFlowIssue
        for (BigItemStack entry : data.encodedRequest().stacks()) {
            pTooltip.accept(entry.stack.getHoverName().copy().append(" x").append(String.valueOf(entry.count)).withStyle(ChatFormatting.GRAY));
        }

        pTooltip.accept(Component.translatable("create.logistically_linked.tooltip_clear").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos requesterPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        Player player = pPlacer instanceof Player ? (Player) pPlacer : null;
        withBlockEntityDo(
            pLevel, requesterPos, rrbe -> {
                AutoRequestData data = AutoRequestData.readFromItem(pLevel, player, requesterPos, pStack);
                if (data == null)
                    return;
                rrbe.encodedRequest = data.encodedRequest();
                rrbe.encodedTargetAdress = data.encodedTargetAddress();
            }
        );
    }

    @Override
    public void neighborChanged(
        BlockState pState,
        Level pLevel,
        BlockPos pPos,
        Block pNeighborBlock,
        @Nullable Orientation wireOrientation,
        boolean pMovedByPiston
    ) {
        if (pLevel.isClientSide())
            return;
        pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, pLevel.hasNeighborSignal(pPos)));
        withBlockEntityDo(pLevel, pPos, RedstoneRequesterBlockEntity::onRedstonePowerChanged);
    }

    @Override
    public Class<RedstoneRequesterBlockEntity> getBlockEntityClass() {
        return RedstoneRequesterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneRequesterBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.REDSTONE_REQUESTER;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(AXIS, pRotation.rotate(Direction.get(AxisDirection.POSITIVE, pState.getValue(AXIS))).getAxis());
    }

}
