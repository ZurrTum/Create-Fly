package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContraptionControlsBlock extends ControlsBlock implements IBE<ContraptionControlsBlockEntity> {

    public static final MapCodec<ContraptionControlsBlock> CODEC = createCodec(ContraptionControlsBlock::new);

    public ContraptionControlsBlock(Settings pProperties) {
        super(pProperties);
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        return onBlockEntityUse(
            level, pos, cte -> {
                cte.pressButton();
                if (!level.isClient()) {
                    cte.disabled = !cte.disabled;
                    cte.notifyUpdate();
                    ContraptionControlsBlockEntity.sendStatus(player, cte.filtering.getFilter(), !cte.disabled);
                    AllSoundEvents.CONTROLLER_CLICK.play(cte.getWorld(), null, cte.getPos(), 1, cte.disabled ? 0.8f : 1.5f);
                }
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pIsMoving
    ) {
        withBlockEntityDo(pLevel, pPos, ContraptionControlsBlockEntity::updatePoweredState);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CONTRAPTION_CONTROLS.get(pState.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CONTRAPTION_CONTROLS_COLLISION.get(pState.get(FACING));
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
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}