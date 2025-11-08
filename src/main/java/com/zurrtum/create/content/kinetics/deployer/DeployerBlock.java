package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.processing.AssemblyOperatorUseContext;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DeployerBlock extends DirectionalAxisKineticBlock implements IBE<DeployerBlockEntity>, ItemInventoryProvider<DeployerBlockEntity> {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public DeployerBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, DeployerBlockEntity blockEntity, Direction context) {
        if (blockEntity.invHandler == null)
            blockEntity.initHandler();
        return blockEntity.invHandler;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.DEPLOYER_INTERACTION.get(state.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.CASING_12PX.get(state.get(FACING));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        Vec3d normal = Vec3d.of(state.get(FACING).getVector());
        Vec3d location = context.getHitPos().subtract(Vec3d.ofCenter(context.getBlockPos()).subtract(normal.multiply(.5))).multiply(normal);
        if (location.length() > .75f) {
            if (!context.getWorld().isClient)
                withBlockEntityDo(context.getWorld(), context.getBlockPos(), DeployerBlockEntity::changeMode);
            return ActionResult.SUCCESS;
        }
        return super.onWrenched(state, context);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        if (placer instanceof ServerPlayerEntity serverPlayer) {
            withBlockEntityDo(
                worldIn, pos, dbe -> {
                    dbe.owner = serverPlayer.getUuid();
                    dbe.ownerName = serverPlayer.getGameProfile().getName();
                }
            );
        }
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
        ItemStack heldByPlayer = stack.copy();

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isSneaking() && player.canModifyBlocks()) {
            if (placementHelper.matchesItem(heldByPlayer) && placementHelper.getOffset(player, level, state, pos, hitResult)
                .placeInWorld(level, (BlockItem) heldByPlayer.getItem(), player, hand).isAccepted())
                return ActionResult.SUCCESS;
        }

        if (heldByPlayer.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        Vec3d normal = Vec3d.of(state.get(FACING).getVector());
        Vec3d location = hitResult.getPos().subtract(Vec3d.ofCenter(pos).subtract(normal.multiply(.5))).multiply(normal);
        if (location.length() < .75f)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient)
            return ActionResult.SUCCESS;

        withBlockEntityDo(
            level, pos, be -> {
                ServerPlayerEntity serverPlayer = be.player.cast();
                ItemStack heldByDeployer = serverPlayer.getMainHandStack().copy();
                if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
                    return;

                player.setStackInHand(hand, heldByDeployer);
                serverPlayer.setStackInHand(Hand.MAIN_HAND, heldByPlayer);
                be.notifyUpdate();
            }
        );

        return ActionResult.SUCCESS;
    }

    @Override
    public Class<DeployerBlockEntity> getBlockEntityClass() {
        return DeployerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DeployerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.DEPLOYER;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);
        withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable WireOrientation wireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected Direction getFacingForPlacement(ItemPlacementContext context) {
        if (context instanceof AssemblyOperatorUseContext)
            return Direction.DOWN;
        else
            return super.getFacingForPlacement(context);
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.DEPLOYER);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.isOf(AllBlocks.DEPLOYER);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                state.get(FACING).getAxis(),
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(
                    pos.offset(directions.getFirst()),
                    s -> s.with(FACING, state.get(FACING)).with(AXIS_ALONG_FIRST_COORDINATE, state.get(AXIS_ALONG_FIRST_COORDINATE))
                );
            }
        }

    }

}
