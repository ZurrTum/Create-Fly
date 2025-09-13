package com.zurrtum.create.content.kinetics.chainConveyor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChainConveyorBlock extends KineticBlock implements IBE<ChainConveyorBlockEntity> {

    public ChainConveyorBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return AllShapes.CHAIN_CONVEYOR_INTERACTION;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CHAIN_CONVEYOR_INTERACTION;
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
        if (!level.isClient() && stack.isOf(Items.CHAIN))
            return ActionResult.SUCCESS;
        if (stack.isOf(AllItems.PACKAGE_FROGPORT))
            return ActionResult.SUCCESS;
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public BlockState onBreak(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
        super.onBreak(pLevel, pPos, pState, pPlayer);
        if (pLevel.isClient())
            return pState;
        if (!pPlayer.isCreative())
            return pState;
        withBlockEntityDo(pLevel, pPos, be -> be.cancelDrops = true);
        return pState;
    }

    @Override
    public void afterBreak(World level, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null)
            return super.onSneakWrenched(state, context);

        withBlockEntityDo(
            context.getWorld(), context.getBlockPos(), be -> {
                be.cancelDrops = true;
                if (player.isCreative())
                    return;
                for (BlockPos targetPos : be.connections) {
                    int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos);
                    while (chainCost > 0) {
                        player.getInventory().offerOrDrop(new ItemStack(Items.CHAIN, Math.min(chainCost, 64)));
                        chainCost -= 64;
                    }
                }
            }
        );

        return super.onSneakWrenched(state, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++)
                if (pContext.getWorld().getBlockState(pContext.getBlockPos().add(x, 0, z)).getBlock() == this)
                    return null;

        return super.getPlacementState(pContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return VoxelShapes.fullCube();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<ChainConveyorBlockEntity> getBlockEntityClass() {
        return ChainConveyorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChainConveyorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CHAIN_CONVEYOR;
    }

}
