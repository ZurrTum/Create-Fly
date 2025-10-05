package com.zurrtum.create.content.processing.burner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BlazeBurnerBlock extends HorizontalFacingBlock implements IBE<BlazeBurnerBlockEntity>, IWrenchable, SpecialBlockItemRequirement {
    public static final MapCodec<BlazeBurnerBlock> CODEC = createCodec(BlazeBurnerBlock::new);
    public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.of("blaze", HeatLevel.class);

    public BlazeBurnerBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HEAT_LEVEL, HeatLevel.NONE));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HEAT_LEVEL, FACING);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        if (world.isClient())
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos.up());
        if (!(blockEntity instanceof BasinBlockEntity basin))
            return;
        basin.notifyChangeOfContents();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HEAT_LEVEL) == HeatLevel.NONE)
            return null;
        return IBE.super.createBlockEntity(pos, state);
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
        HeatLevel heat = state.get(HEAT_LEVEL);

        if (stack.isOf(AllItems.GOGGLES) && heat != HeatLevel.NONE)
            return onBlockEntityUseItemOn(
                level, pos, bbte -> {
                    if (bbte.goggles)
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    bbte.goggles = true;
                    bbte.notifyUpdate();
                    return ActionResult.SUCCESS;
                }
            );

        BlazeBurnerBlockEntity be = getBlockEntity(level, pos);
        if (be != null && be.stockKeeper) {
            StockTickerBlockEntity stockTicker = BlazeBurnerBlockEntity.getStockTicker(level, pos);
            if (stockTicker != null)
                StockTickerInteractionHandler.interactWithLogisticsManagerAt(player, level, stockTicker.getPos());
            return ActionResult.SUCCESS;
        }

        if (stack.isEmpty() && heat != HeatLevel.NONE)
            return onBlockEntityUseItemOn(
                level, pos, bbte -> {
                    if (!bbte.goggles)
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    bbte.goggles = false;
                    bbte.notifyUpdate();
                    return ActionResult.SUCCESS;
                }
            );

        if (heat == HeatLevel.NONE) {
            if (stack.getItem() instanceof FlintAndSteelItem) {
                level.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
                if (level.isClient())
                    return ActionResult.SUCCESS;
                stack.damage(1, player, hand.getEquipmentSlot());
                level.setBlockState(pos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        boolean doNotConsume = player.isCreative();
        boolean forceOverflow = !FakePlayerHandler.has(player);

        ActionResult res = tryInsert(state, level, pos, stack, doNotConsume, forceOverflow, false);
        if (res instanceof ActionResult.Success success) {
            ItemStack leftover = success.getNewHandStack();
            if (!level.isClient() && !doNotConsume && leftover != null && !leftover.isEmpty()) {
                if (stack.isEmpty()) {
                    player.setStackInHand(hand, leftover);
                } else if (!player.getInventory().insertStack(leftover)) {
                    player.dropItem(leftover, false);
                }
            }
        }

        if (res.isAccepted())
            return res;
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    public static ActionResult tryInsert(
        BlockState state,
        World world,
        BlockPos pos,
        ItemStack stack,
        boolean doNotConsume,
        boolean forceOverflow,
        boolean simulate
    ) {
        if (!state.hasBlockEntity())
            return ActionResult.FAIL;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof BlazeBurnerBlockEntity burnerBE))
            return ActionResult.FAIL;

        if (burnerBE.isCreativeFuel(stack)) {
            if (!simulate)
                burnerBE.applyCreativeFuel();
            return ActionResult.SUCCESS.withNewHandStack(ItemStack.EMPTY);
        }
        if (!burnerBE.tryUpdateFuel(stack, forceOverflow, simulate))
            return ActionResult.FAIL;

        if (!doNotConsume) {
            ItemStack container = stack.getItem().getRecipeRemainder();
            if (!world.isClient()) {
                stack.decrement(1);
            }
            return ActionResult.SUCCESS.withNewHandStack(container);
        }
        return ActionResult.SUCCESS.withNewHandStack(ItemStack.EMPTY);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        ItemStack stack = context.getStack();
        Item item = stack.getItem();
        BlockState defaultState = getDefaultState();
        if (!(item instanceof BlazeBurnerBlockItem))
            return defaultState;
        HeatLevel initialHeat = ((BlazeBurnerBlockItem) item).hasCapturedBlaze() ? HeatLevel.SMOULDERING : HeatLevel.NONE;
        return defaultState.with(HEAT_LEVEL, initialHeat).with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView reader, BlockPos pos, ShapeContext context) {
        return AllShapes.HEATER_BLOCK_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockView p_220071_2_, BlockPos p_220071_3_, ShapeContext p_220071_4_) {
        if (p_220071_4_ == ShapeContext.absent())
            return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
        return getOutlineShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
    }

    @Override
    public boolean hasComparatorOutput(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World p_180641_2_, BlockPos p_180641_3_, Direction direction) {
        return Math.max(0, state.get(HEAT_LEVEL).ordinal() - 1);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(10) != 0)
            return;
        if (!state.get(HEAT_LEVEL).isAtLeast(HeatLevel.SMOULDERING))
            return;
        world.playSoundClient(
            pos.getX() + 0.5F,
            pos.getY() + 0.5F,
            pos.getZ() + 0.5F,
            SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
            SoundCategory.BLOCKS,
            0.5F + random.nextFloat(),
            random.nextFloat() * 0.7F + 0.6F,
            false
        );
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    public static HeatLevel getHeatLevelOf(BlockState blockState) {
        return blockState.contains(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.get(BlazeBurnerBlock.HEAT_LEVEL) : HeatLevel.NONE;
    }

    public static int getLight(BlockState state) {
        HeatLevel level = state.get(HEAT_LEVEL);
        return switch (level) {
            case NONE -> 0;
            case SMOULDERING -> 8;
            default -> 15;
        };
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return getLitOrUnlitStack(state);
    }

    @Override
    public Class<BlazeBurnerBlockEntity> getBlockEntityClass() {
        return BlazeBurnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeBurnerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HEATER;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(ItemUseType.CONSUME, getLitOrUnlitStack(state));
    }

    private static ItemStack getLitOrUnlitStack(BlockState state) {
        boolean isLit = state.get(HEAT_LEVEL) != HeatLevel.NONE;
        return (isLit ? AllItems.BLAZE_BURNER : AllItems.EMPTY_BLAZE_BURNER).getDefaultStack();
    }

    public enum HeatLevel implements StringIdentifiable {
        NONE,
        SMOULDERING,
        FADING,
        KINDLED,
        SEETHING;

        public static final Codec<HeatLevel> CODEC = StringIdentifiable.createCodec(HeatLevel::values);

        public static HeatLevel byIndex(int index) {
            return values()[index];
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean isAtLeast(HeatLevel heatLevel) {
            return this.ordinal() >= heatLevel.ordinal();
        }

        public HeatLevel nextActiveLevel() {
            return byIndex(ordinal() % (values().length - 1) + 1);
        }
    }

}
