package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PortableStorageInterfaceBlock extends WrenchableDirectionalBlock implements IBE<PortableStorageInterfaceBlockEntity>, ItemInventoryProvider<PortableStorageInterfaceBlockEntity>, FluidInventoryProvider<PortableStorageInterfaceBlockEntity> {
    @Override
    @Nullable
    public Container getInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        PortableStorageInterfaceBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return getInventory(blockEntity);
    }

    @Override
    @Nullable
    public Container getInventory(
        @Nullable BlockState state,
        LevelAccessor world,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction context
    ) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        return getInventory(blockEntity);
    }

    @Nullable
    private Container getInventory(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof PortableItemInterfaceBlockEntity be) {
            return be.capability;
        }
        return null;
    }

    @Override
    @Nullable
    public FluidInventory getFluidInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        PortableStorageInterfaceBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return getFluidInventory(blockEntity);
    }

    @Override
    @Nullable
    public FluidInventory getFluidInventory(
        @Nullable BlockState state,
        LevelAccessor world,
        BlockPos pos,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction context
    ) {
        if (blockEntity == null) {
            blockEntity = world.getBlockEntity(pos);
        }
        return getFluidInventory(blockEntity);
    }

    @Nullable
    private FluidInventory getFluidInventory(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof PortableFluidInterfaceBlockEntity be) {
            return be.capability;
        }
        return null;
    }

    boolean fluids;

    public static PortableStorageInterfaceBlock forItems(Properties p_i48415_1_) {
        return new PortableStorageInterfaceBlock(p_i48415_1_, false);
    }

    public static PortableStorageInterfaceBlock forFluids(Properties p_i48415_1_) {
        return new PortableStorageInterfaceBlock(p_i48415_1_, true);
    }

    private PortableStorageInterfaceBlock(Properties p_i48415_1_, boolean fluids) {
        super(p_i48415_1_);
        this.fluids = fluids;
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable Orientation WireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, PortableStorageInterfaceBlockEntity::neighbourChanged);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getNearestLookingDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            direction = direction.getOpposite();
        return defaultBlockState().setValue(FACING, direction.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.getValue(FACING));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
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
