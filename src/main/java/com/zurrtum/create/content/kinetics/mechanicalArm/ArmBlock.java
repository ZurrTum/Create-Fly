package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public class ArmBlock extends KineticBlock implements IBE<ArmBlockEntity>, ICogWheel {

    public static final BooleanProperty CEILING = BooleanProperty.of("ceiling");

    public ArmBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(CEILING, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> p_206840_1_) {
        super.appendProperties(p_206840_1_.add(CEILING));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(CEILING, ctx.getSide() == Direction.DOWN);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return state.get(CEILING) ? AllShapes.MECHANICAL_ARM_CEILING : AllShapes.MECHANICAL_ARM;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);
        withBlockEntityDo(world, pos, ArmBlockEntity::redstoneUpdate);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable WireOrientation wireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, ArmBlockEntity::redstoneUpdate);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<ArmBlockEntity> getBlockEntityClass() {
        return ArmBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ArmBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MECHANICAL_ARM;
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
        if (stack.isOf(AllItems.GOGGLES)) {
            ActionResult gogglesResult = onBlockEntityUseItemOn(
                level, pos, ate -> {
                    if (ate.goggles)
                        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                    ate.goggles = true;
                    ate.notifyUpdate();
                    return ActionResult.SUCCESS;
                }
            );
            if (gogglesResult.isAccepted())
                return gogglesResult;
        }

        MutableBoolean success = new MutableBoolean(false);
        withBlockEntityDo(
            level, pos, be -> {
                if (be.heldItem.isEmpty())
                    return;
                success.setTrue();
                if (level.isClient)
                    return;
                player.getInventory().offerOrDrop(be.heldItem);
                be.heldItem = ItemStack.EMPTY;
                be.phase = Phase.SEARCH_INPUTS;
                be.markDirty();
                be.sendData();
            }
        );

        return success.booleanValue() ? ActionResult.SUCCESS : ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

}
