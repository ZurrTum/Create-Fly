package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.component.AutoRequestData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class TableClothBlock extends Block implements IWrenchable, IBE<TableClothBlockEntity> {

    public static final BooleanProperty HAS_BE = BooleanProperty.of("entity");

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    private DyeColor colour;

    public TableClothBlock(Settings pProperties, DyeColor colour) {
        super(pProperties);
        this.colour = colour;
        setDefaultState(getDefaultState().with(HAS_BE, false));
    }

    public TableClothBlock(Settings pProperties, String type) {
        super(pProperties);
    }

    public static Function<Settings, TableClothBlock> dyed(DyeColor color) {
        return settings -> new TableClothBlock(settings, color);
    }

    public static Function<Settings, TableClothBlock> styled(String type) {
        return settings -> new TableClothBlock(settings, type);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(HAS_BE));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        if (!(pPlacer instanceof PlayerEntity player))
            return;

        AutoRequestData requestData = AutoRequestData.readFromItem(pLevel, player, pPos, pStack);
        if (requestData == null)
            return;

        pLevel.setBlockState(pPos, pState.with(HAS_BE, true));
        withBlockEntityDo(
            pLevel, pPos, dcbe -> {
                dcbe.requestData = requestData;
                dcbe.owner = player.getUuid();
                dcbe.facing = player.getHorizontalFacing().getOpposite();
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    AllAdvancements.TABLE_CLOTH_SHOP.trigger(serverPlayer);
                }
            }
        );
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
        if (hitResult.getSide() == Direction.DOWN)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient())
            return ActionResult.SUCCESS;

        ItemStack heldItem = player.getStackInHand(hand);
        boolean shiftKeyDown = player.isSneaking();
        if (!player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem)) {
            if (shiftKeyDown)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) heldItem.getItem(), player, hand);
            return ActionResult.SUCCESS;
        }

        if ((shiftKeyDown || heldItem.isEmpty()) && !state.get(HAS_BE))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (!level.isClient() && !state.get(HAS_BE))
            level.setBlockState(pos, state.cycle(HAS_BE));

        return onBlockEntityUseItemOn(level, pos, dcbe -> dcbe.use(player, hitResult));
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState pState, LootWorldContext.Builder pParams) {
        List<ItemStack> drops = super.getDroppedStacks(pState, pParams);

        if (!(pParams.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof TableClothBlockEntity dcbe))
            return drops;
        if (!dcbe.isShop())
            return drops;

        for (ItemStack stack : drops) {
            if (stack.isIn(AllItemTags.TABLE_CLOTHS)) {
                ItemStack drop = new ItemStack(this);
                dcbe.requestData.writeToItem(dcbe.getPos(), drop);
                return List.of(drop);
            }
        }

        return drops;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.TABLE_CLOTH;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return AllShapes.TABLE_CLOTH;
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return AllShapes.TABLE_CLOTH_OCCLUSION;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.TABLE_CLOTH_OCCLUSION;
    }

    @Nullable
    public DyeColor getColor() {
        return colour;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(HAS_BE) ? IBE.super.createBlockEntity(pos, state) : null;
    }

    @Override
    public Class<TableClothBlockEntity> getBlockEntityClass() {
        return TableClothBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TableClothBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TABLE_CLOTH;
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.isIn(AllItemTags.TABLE_CLOTHS);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof TableClothBlock;
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                Axis.Y,
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else
                return PlacementOffset.success(pos.offset(directions.getFirst()), s -> s);
        }
    }
}
