package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllEnchantments;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.List;
import java.util.Optional;

public class BacktankBlock extends HorizontalKineticBlock implements IBE<BacktankBlockEntity>, Waterloggable, SpecialBlockItemRequirement {

    public BacktankBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED);
        super.appendProperties(builder);
    }

    @Override
    public boolean hasComparatorOutput(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return getBlockEntityOptional(world, pos).map(BacktankBlockEntity::getComparatorOutput).orElse(0);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        Random random
    ) {
        if (state.get(Properties.WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        return super.getPlacementState(context).with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        if (worldIn.isClient())
            return;
        if (stack == null)
            return;
        withBlockEntityDo(
            worldIn, pos, be -> {
                be.setCapacityEnchantLevel(stack.getEnchantments().getLevel(worldIn.getRegistryManager().getEntryOrThrow(AllEnchantments.CAPACITY)));
                be.setAirLevel(stack.getOrDefault(AllDataComponents.BACKTANK_AIR, 0));
                if (stack.contains(DataComponentTypes.CUSTOM_NAME))
                    be.setCustomName(stack.getCustomName());

                be.setComponentPatch(stack.getComponentChanges());
            }
        );
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState pState, LootWorldContext.Builder pBuilder) {
        List<ItemStack> lootDrops = super.getDroppedStacks(pState, pBuilder);

        BlockEntity blockEntity = pBuilder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (!(blockEntity instanceof BacktankBlockEntity bbe))
            return lootDrops;

        ComponentChanges components = bbe.getComponentPatch().withRemovedIf(c -> c.equals(AllDataComponents.BACKTANK_AIR));
        if (components.isEmpty())
            return lootDrops;

        return lootDrops.stream().peek(stack -> {
            if (stack.getItem() instanceof BacktankItem)
                stack.applyUnvalidatedChanges(components);
        }).toList();
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
        if (player == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (FakePlayerHandler.has(player))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.getMainHandStack().getItem() instanceof BlockItem)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isEmpty())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!level.isClient()) {
            level.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .75f, 1);
            player.equipStack(EquipmentSlot.CHEST, getPickStack(level, pos, state, true));
            level.breakBlock(pos, false);
        }
        return ActionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    public ItemStack getPickStack(WorldView pLevel, BlockPos pos, BlockState state, boolean includeData) {
        Item item = asItem();
        //        if (item instanceof BacktankItem.BacktankBlockItem placeable)
        //            item = placeable.getActualItem();

        Optional<BacktankBlockEntity> blockEntityOptional = getBlockEntityOptional(pLevel, pos);

        ComponentChanges components = blockEntityOptional.map(BacktankBlockEntity::getComponentPatch).orElse(ComponentChanges.EMPTY);
        int air = blockEntityOptional.map(BacktankBlockEntity::getAirLevel).orElse(0);

        ItemStack stack = new ItemStack(item.getRegistryEntry(), 1, components);
        stack.set(AllDataComponents.BACKTANK_AIR, air);
        return stack;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.BACKTANK;
    }

    @Override
    public Class<BacktankBlockEntity> getBlockEntityClass() {
        return BacktankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BacktankBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BACKTANK;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        Item item = asItem();
        //        if (item instanceof BacktankItem.BacktankBlockItem placeable)
        //            item = placeable.getActualItem();
        return new ItemRequirement(ItemUseType.CONSUME, item);
    }

}
