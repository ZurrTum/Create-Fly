package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class PortableStorageInterfaceBlock extends WrenchableDirectionalBlock implements IBE<PortableStorageInterfaceBlockEntity>, ItemInventoryProvider<PortableStorageInterfaceBlockEntity>, FluidInventoryProvider<PortableStorageInterfaceBlockEntity> {
    @Override
    public Inventory getInventory(
        WorldAccess world,
        BlockPos pos,
        BlockState state,
        PortableStorageInterfaceBlockEntity blockEntity,
        Direction context
    ) {
        return getInventory(blockEntity);
    }

    @Override
    public Inventory getInventory(@Nullable BlockState state, WorldAccess world, BlockPos pos, @Nullable BlockEntity blockEntity, Direction context) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        return getInventory(blockEntity);
    }

    private Inventory getInventory(BlockEntity blockEntity) {
        if (blockEntity instanceof PortableItemInterfaceBlockEntity be) {
            return be.capability;
        }
        return null;
    }

    @Override
    public FluidInventory getFluidInventory(
        WorldAccess world,
        BlockPos pos,
        BlockState state,
        PortableStorageInterfaceBlockEntity blockEntity,
        Direction context
    ) {
        return getFluidInventory(blockEntity);
    }

    @Override
    public FluidInventory getFluidInventory(
        @Nullable BlockState state,
        WorldAccess world,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        Direction context
    ) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        return getFluidInventory(blockEntity);
    }

    private FluidInventory getFluidInventory(BlockEntity blockEntity) {
        if (blockEntity instanceof PortableFluidInterfaceBlockEntity be) {
            return be.capability;
        }
        return null;
    }

    boolean fluids;

    public static PortableStorageInterfaceBlock forItems(Settings p_i48415_1_) {
        return new PortableStorageInterfaceBlock(p_i48415_1_, false);
    }

    public static PortableStorageInterfaceBlock forFluids(Settings p_i48415_1_) {
        return new PortableStorageInterfaceBlock(p_i48415_1_, true);
    }

    private PortableStorageInterfaceBlock(Settings p_i48415_1_, boolean fluids) {
        super(p_i48415_1_);
        this.fluids = fluids;
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable WireOrientation WireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, PortableStorageInterfaceBlockEntity::neighbourChanged);
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction direction = context.getPlayerLookDirection();
        if (context.getPlayer() != null && context.getPlayer().isSneaking())
            direction = direction.getOpposite();
        return getDefaultState().with(FACING, direction.getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.get(FACING));
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos, Direction direction) {
        return getBlockEntityOptional(worldIn, pos).map(be -> be.isConnected() ? 15 : 0).orElse(0);
    }

    @Override
    public Class<PortableStorageInterfaceBlockEntity> getBlockEntityClass() {
        return PortableStorageInterfaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PortableStorageInterfaceBlockEntity> getBlockEntityType() {
        return fluids ? AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE : AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE;
    }

}
