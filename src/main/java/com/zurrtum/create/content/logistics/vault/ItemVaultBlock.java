package com.zurrtum.create.content.logistics.vault;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ItemVaultBlock extends Block implements IWrenchable, IBE<ItemVaultBlockEntity>, ItemInventoryProvider<ItemVaultBlockEntity> {

    public static final EnumProperty<Axis> HORIZONTAL_AXIS = Properties.HORIZONTAL_AXIS;
    public static final BooleanProperty LARGE = BooleanProperty.of("large");

    public ItemVaultBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
        setDefaultState(getDefaultState().with(LARGE, false));
    }

    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, ItemVaultBlockEntity blockEntity, Direction context) {
        if (blockEntity.itemCapability != null) {
            Inventory inventory = blockEntity.itemCapability.get();
            if (inventory != null) {
                return inventory;
            }
        }
        blockEntity.initCapability();
        return blockEntity.itemCapability != null ? blockEntity.itemCapability.get() : null;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_AXIS, LARGE);
        super.appendProperties(pBuilder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        if (pContext.getPlayer() == null || !pContext.getPlayer().isSneaking()) {
            BlockState placedOn = pContext.getWorld().getBlockState(pContext.getBlockPos().offset(pContext.getSide().getOpposite()));
            Axis preferredAxis = getVaultBlockAxis(placedOn);
            if (preferredAxis != null)
                return this.getDefaultState().with(HORIZONTAL_AXIS, preferredAxis);
        }
        return this.getDefaultState().with(HORIZONTAL_AXIS, pContext.getHorizontalPlayerFacing().getAxis());
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() == pState.getBlock())
            return;
        if (pIsMoving)
            return;
        withBlockEntityDo(pLevel, pPos, ItemVaultBlockEntity::updateConnectivity);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (context.getSide().getAxis().isVertical()) {
            BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
            if (be instanceof ItemVaultBlockEntity vault) {
                ConnectivityHandler.splitMulti(vault);
                vault.removeController(true);
            }
            state = state.with(LARGE, false);
        }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean pIsMoving) {
        if (state.hasBlockEntity()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof ItemVaultBlockEntity vaultBE))
                return;
            ItemScatterer.spawn(world, pos, vaultBE.inventory);
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(vaultBE);
        }
    }

    public static boolean isVault(BlockState state) {
        return state.isOf(AllBlocks.ITEM_VAULT);
    }

    @Nullable
    public static Axis getVaultBlockAxis(BlockState state) {
        if (!isVault(state))
            return null;
        return state.get(HORIZONTAL_AXIS);
    }

    public static boolean isLarge(BlockState state) {
        if (!isVault(state))
            return false;
        return state.get(LARGE);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        Axis axis = state.get(HORIZONTAL_AXIS);
        return state.with(HORIZONTAL_AXIS, rot.rotate(Direction.from(axis, AxisDirection.POSITIVE)).getAxis());
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state;
    }

    @Override
    public boolean hasComparatorOutput(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos) {
        return ItemHelper.calcRedstoneFromBlockEntity(this, pLevel, pPos);
    }

    @Override
    public BlockEntityType<? extends ItemVaultBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ITEM_VAULT;
    }

    @Override
    public Class<ItemVaultBlockEntity> getBlockEntityClass() {
        return ItemVaultBlockEntity.class;
    }
}
