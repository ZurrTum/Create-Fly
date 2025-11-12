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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TableClothBlock extends Block implements IWrenchable, IBE<TableClothBlockEntity> {

    public static final BooleanProperty HAS_BE = BooleanProperty.create("entity");

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    private DyeColor colour;

    public TableClothBlock(Properties pProperties, DyeColor colour) {
        super(pProperties);
        this.colour = colour;
        registerDefaultState(defaultBlockState().setValue(HAS_BE, false));
    }

    public TableClothBlock(Properties pProperties, String type) {
        super(pProperties);
    }

    public static Function<Properties, TableClothBlock> dyed(DyeColor color) {
        return settings -> new TableClothBlock(settings, color);
    }

    public static Function<Properties, TableClothBlock> styled(String type) {
        return settings -> new TableClothBlock(settings, type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(HAS_BE));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!(pPlacer instanceof Player player))
            return;

        AutoRequestData requestData = AutoRequestData.readFromItem(pLevel, player, pPos, pStack);
        if (requestData == null)
            return;

        pLevel.setBlockAndUpdate(pPos, pState.setValue(HAS_BE, true));
        withBlockEntityDo(
            pLevel, pPos, dcbe -> {
                dcbe.requestData = requestData;
                dcbe.owner = player.getUUID();
                dcbe.facing = player.getDirection().getOpposite();
                if (player instanceof ServerPlayer serverPlayer) {
                    AllAdvancements.TABLE_CLOTH_SHOP.trigger(serverPlayer);
                }
            }
        );
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
        if (hitResult.getDirection() == Direction.DOWN)
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        ItemStack heldItem = player.getItemInHand(hand);
        boolean shiftKeyDown = player.isShiftKeyDown();
        if (!player.mayBuild())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem)) {
            if (shiftKeyDown)
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) heldItem.getItem(), player, hand);
            return InteractionResult.SUCCESS;
        }

        if ((shiftKeyDown || heldItem.isEmpty()) && !state.getValue(HAS_BE))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!level.isClientSide() && !state.getValue(HAS_BE))
            level.setBlockAndUpdate(pos, state.cycle(HAS_BE));

        return onBlockEntityUseItemOn(level, pos, dcbe -> dcbe.use(player, hitResult));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);

        if (!(pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TableClothBlockEntity dcbe))
            return drops;
        if (!dcbe.isShop())
            return drops;

        for (ItemStack stack : drops) {
            if (stack.is(AllItemTags.TABLE_CLOTHS)) {
                ItemStack drop = new ItemStack(this);
                dcbe.requestData.writeToItem(dcbe.getBlockPos(), drop);
                return List.of(drop);
            }
        }

        return drops;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.TABLE_CLOTH;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return AllShapes.TABLE_CLOTH;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return AllShapes.TABLE_CLOTH_OCCLUSION;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.TABLE_CLOTH_OCCLUSION;
    }

    @Nullable
    public DyeColor getColor() {
        return colour;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HAS_BE) ? IBE.super.newBlockEntity(pos, state) : null;
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
            return i -> i.is(AllItemTags.TABLE_CLOTHS);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof TableClothBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getLocation(),
                Axis.Y,
                dir -> world.getBlockState(pos.relative(dir)).canBeReplaced()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else
                return PlacementOffset.success(pos.relative(directions.getFirst()), s -> s);
        }
    }
}
