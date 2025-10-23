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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RedstoneRequesterBlock extends Block implements IBE<RedstoneRequesterBlockEntity>, IWrenchable, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final EnumProperty<Axis> AXIS = Properties.HORIZONTAL_AXIS;

    public RedstoneRequesterBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, AXIS));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        if (stateForPlacement == null)
            return null;
        return stateForPlacement.with(AXIS, pContext.getHorizontalPlayerFacing().getAxis())
            .with(POWERED, pContext.getWorld().isReceivingRedstonePower(pContext.getBlockPos()));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pBlockState, World pLevel, BlockPos pPos) {
        RedstoneRequesterBlockEntity req = getBlockEntity(pLevel, pPos);
        return req != null && req.lastRequestSucceeded ? 15 : 0;
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        return onBlockEntityUse(level, pos, be -> be.use(player));
    }

    public static void programRequester(ServerPlayerEntity player, StockTickerBlockEntity be, PackageOrderWithCrafts order, String address) {
        ItemStack stack = player.getMainHandStack();
        boolean isRequester = stack.isOf(AllItems.REDSTONE_REQUESTER);
        boolean isShopCloth = stack.isIn(AllItemTags.TABLE_CLOTHS);
        if (!isRequester && !isShopCloth)
            return;

        String targetDim = player.getWorld().getRegistryKey().getValue().toString();
        AutoRequestData autoRequestData = new AutoRequestData(order, address, be.getPos(), targetDim, false);

        autoRequestData.writeToItem(BlockPos.ORIGIN, stack);

        if (isRequester) {
            NbtCompound beTag = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
            beTag.put("Freq", Uuids.INT_STREAM_CODEC, be.behaviour.freqId);
            beTag.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, AllBlockEntityTypes.REDSTONE_REQUESTER);
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(beTag));
        }

        player.setStackInHand(Hand.MAIN_HAND, stack);
    }

    public static void appendRequesterTooltip(ItemStack pStack, Consumer<Text> pTooltip) {
        if (!pStack.contains(AllDataComponents.AUTO_REQUEST_DATA))
            return;

        AutoRequestData data = pStack.get(AllDataComponents.AUTO_REQUEST_DATA);

        //noinspection DataFlowIssue
        for (BigItemStack entry : data.encodedRequest().stacks()) {
            pTooltip.accept(entry.stack.getName().copy().append(" x").append(String.valueOf(entry.count)).formatted(Formatting.GRAY));
        }

        pTooltip.accept(Text.translatable("create.logistically_linked.tooltip_clear").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos requesterPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        PlayerEntity player = pPlacer instanceof PlayerEntity ? (PlayerEntity) pPlacer : null;
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
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pNeighborBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pMovedByPiston
    ) {
        if (pLevel.isClient())
            return;
        pLevel.setBlockState(pPos, pState.with(POWERED, pLevel.isReceivingRedstonePower(pPos)));
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState pState, BlockRotation pRotation) {
        return pState.with(AXIS, pRotation.rotate(Direction.get(AxisDirection.POSITIVE, pState.get(AXIS))).getAxis());
    }

}
