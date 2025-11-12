package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContraptionControlsBlock extends ControlsBlock implements IBE<ContraptionControlsBlockEntity> {

    public static final MapCodec<ContraptionControlsBlock> CODEC = simpleCodec(ContraptionControlsBlock::new);

    public ContraptionControlsBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return onBlockEntityUse(
            level, pos, cte -> {
                cte.pressButton();
                if (!level.isClientSide()) {
                    cte.disabled = !cte.disabled;
                    cte.notifyUpdate();
                    ContraptionControlsBlockEntity.sendStatus(player, cte.filtering.getFilter(), !cte.disabled);
                    AllSoundEvents.CONTROLLER_CLICK.play(cte.getLevel(), null, cte.getBlockPos(), 1, cte.disabled ? 0.8f : 1.5f);
                }
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public void neighborChanged(
        BlockState pState,
        Level pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable Orientation wireOrientation,
        boolean pIsMoving
    ) {
        withBlockEntityDo(pLevel, pPos, ContraptionControlsBlockEntity::updatePoweredState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.CONTRAPTION_CONTROLS.get(pState.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.CONTRAPTION_CONTROLS_COLLISION.get(pState.getValue(FACING));
    }

    @Override
    public Class<ContraptionControlsBlockEntity> getBlockEntityClass() {
        return ContraptionControlsBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ContraptionControlsBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CONTRAPTION_CONTROLS;
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}