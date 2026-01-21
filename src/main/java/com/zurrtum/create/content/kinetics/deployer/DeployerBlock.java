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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DeployerBlock extends DirectionalAxisKineticBlock implements IBE<DeployerBlockEntity>, ItemInventoryProvider<DeployerBlockEntity> {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public DeployerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, DeployerBlockEntity blockEntity, @Nullable Direction context) {
        if (blockEntity.invHandler == null)
            blockEntity.initHandler();
        return blockEntity.invHandler;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.DEPLOYER_INTERACTION.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_12PX.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (isHand(state, context)) {
            if (!context.getLevel().isClientSide())
                withBlockEntityDo(context.getLevel(), context.getClickedPos(), DeployerBlockEntity::changeMode);
            return InteractionResult.SUCCESS;
        }
        return super.onWrenched(state, context);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && isHand(state, context)) {
            if (!context.getLevel().isClientSide())
                withBlockEntityDo(
                    context.getLevel(), context.getClickedPos(), be -> {
                        ServerPlayer serverPlayer = be.player.cast();
                        ItemStack heldByDeployer = serverPlayer.getMainHandItem();
                        ItemStack heldByPlayer = context.getItemInHand();
                        if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
                            return;

                        player.setItemInHand(context.getHand(), heldByDeployer.copy());
                        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, heldByPlayer);
                        be.notifyUpdate();
                    }
                );
            return InteractionResult.SUCCESS;
        }
        return super.onSneakWrenched(state, context);
    }

    private static boolean isHand(BlockState state, UseOnContext context) {
        Vec3 normal = Vec3.atLowerCornerOf(state.getValue(FACING).getUnitVec3i());
        Vec3 location = context.getClickLocation().subtract(Vec3.atCenterOf(context.getClickedPos()).subtract(normal.scale(.5))).multiply(normal);
        return location.length() > .75f;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (placer instanceof ServerPlayer serverPlayer) {
            withBlockEntityDo(
                worldIn, pos, dbe -> {
                    dbe.owner = serverPlayer.getUUID();
                    dbe.ownerName = serverPlayer.getGameProfile().name();
                }
            );
        }
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
        ItemStack heldByPlayer = stack.copy();

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(heldByPlayer) && placementHelper.getOffset(player, level, state, pos, hitResult)
                .placeInWorld(level, (BlockItem) heldByPlayer.getItem(), player, hand).consumesAction())
                return InteractionResult.SUCCESS;
        }

        if (heldByPlayer.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        Vec3 normal = Vec3.atLowerCornerOf(state.getValue(FACING).getUnitVec3i());
        Vec3 location = hitResult.getLocation().subtract(Vec3.atCenterOf(pos).subtract(normal.scale(.5))).multiply(normal);
        if (location.length() < .75f)
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        withBlockEntityDo(
            level, pos, be -> {
                ServerPlayer serverPlayer = be.player.cast();
                ItemStack heldByDeployer = serverPlayer.getMainHandItem().copy();
                if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
                    return;

                player.setItemInHand(hand, heldByDeployer);
                serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, heldByPlayer);
                be.notifyUpdate();
            }
        );

        return InteractionResult.SUCCESS;
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
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable Orientation wireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, DeployerBlockEntity::redstoneUpdate);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        if (context instanceof AssemblyOperatorUseContext)
            return Direction.DOWN;
        else
            return super.getFacingForPlacement(context);
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.is(AllItems.DEPLOYER);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.is(AllBlocks.DEPLOYER);
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getLocation(),
                state.getValue(FACING).getAxis(),
                dir -> world.getBlockState(pos.relative(dir)).canBeReplaced()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(
                    pos.relative(directions.getFirst()),
                    s -> s.setValue(FACING, state.getValue(FACING)).setValue(AXIS_ALONG_FIRST_COORDINATE, state.getValue(AXIS_ALONG_FIRST_COORDINATE))
                );
            }
        }

    }

}
