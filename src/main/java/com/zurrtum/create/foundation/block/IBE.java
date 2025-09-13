package com.zurrtum.create.foundation.block;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IBE<T extends BlockEntity> extends BlockEntityProvider {

    Class<T> getBlockEntityClass();

    BlockEntityType<? extends T> getBlockEntityType();

    default void withBlockEntityDo(BlockView world, BlockPos pos, Consumer<T> action) {
        getBlockEntityOptional(world, pos).ifPresent(action);
    }

    default ActionResult onBlockEntityUse(BlockView world, BlockPos pos, Function<T, ActionResult> action) {
        return getBlockEntityOptional(world, pos).map(action).orElse(ActionResult.PASS);
    }

    default ActionResult onBlockEntityUseItemOn(BlockView world, BlockPos pos, Function<T, ActionResult> action) {
        return getBlockEntityOptional(world, pos).map(action).orElse(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
    }

    default Optional<T> getBlockEntityOptional(BlockView world, BlockPos pos) {
        return Optional.ofNullable(getBlockEntity(world, pos));
    }

    @Override
    default BlockEntity createBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return getBlockEntityType().instantiate(p_153215_, p_153216_);
    }

    @Override
    default <S extends BlockEntity> BlockEntityTicker<S> getTicker(World p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        if (SmartBlockEntity.class.isAssignableFrom(getBlockEntityClass()))
            return new SmartBlockEntityTicker<>();
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default T getBlockEntity(BlockView worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        Class<T> expectedClass = getBlockEntityClass();

        if (blockEntity == null)
            return null;
        if (!expectedClass.isInstance(blockEntity))
            return null;

        return (T) blockEntity;
    }

}