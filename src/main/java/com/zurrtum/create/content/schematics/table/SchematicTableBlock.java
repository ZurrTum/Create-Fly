package com.zurrtum.create.content.schematics.table;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SchematicTableBlock extends HorizontalFacingBlock implements IBE<SchematicTableBlockEntity> {

    public static final MapCodec<SchematicTableBlock> CODEC = createCodec(SchematicTableBlock::new);

    public SchematicTableBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.TABLE_POLE_SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.SCHEMATICS_TABLE.get(state.get(FACING));
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (level.isClient)
            return ActionResult.SUCCESS;
        withBlockEntityDo(level, pos, be -> be.openHandledScreen((ServerPlayerEntity) player));
        return ActionResult.SUCCESS;
    }

    @Override
    public Class<SchematicTableBlockEntity> getBlockEntityClass() {
        return SchematicTableBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SchematicTableBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SCHEMATIC_TABLE;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

}
