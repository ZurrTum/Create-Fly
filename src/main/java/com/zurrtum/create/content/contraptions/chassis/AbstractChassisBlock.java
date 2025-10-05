package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractChassisBlock extends PillarBlock implements IWrenchable, IBE<ChassisBlockEntity>, TransformableBlock {

    public AbstractChassisBlock(Settings properties) {
        super(properties);
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
        if (!player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        boolean isSlimeBall = stack.isIn(AllItemTags.SLIME_BALLS) || stack.isOf(AllItems.SUPER_GLUE);

        BooleanProperty affectedSide = getGlueableSide(state, hitResult.getSide());
        if (affectedSide == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (isSlimeBall && state.get(affectedSide)) {
            for (Direction face : Iterate.directions) {
                BooleanProperty glueableSide = getGlueableSide(state, face);
                if (glueableSide != null && !state.get(glueableSide) && glueAllowedOnSide(level, pos, state, face)) {
                    if (level.isClient()) {
                        Vec3d vec = hitResult.getPos();
                        level.addParticleClient(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
                        return ActionResult.SUCCESS;
                    }
                    AllSoundEvents.SLIME_ADDED.playOnServer(level, pos, .5f, 1);
                    state = state.with(glueableSide, true);
                }
            }
            if (!level.isClient())
                level.setBlockState(pos, state);
            return ActionResult.SUCCESS;
        }

        if ((!stack.isEmpty() || !player.isSneaking()) && !isSlimeBall)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (state.get(affectedSide) == isSlimeBall)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!glueAllowedOnSide(level, pos, state, hitResult.getSide()))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient()) {
            Vec3d vec = hitResult.getPos();
            level.addParticleClient(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
            return ActionResult.SUCCESS;
        }

        AllSoundEvents.SLIME_ADDED.playOnServer(level, pos, .5f, 1);
        level.setBlockState(pos, state.with(affectedSide, isSlimeBall));
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if (rotation == BlockRotation.NONE)
            return state;

        BlockState rotated = super.rotate(state, rotation);
        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = getGlueableSide(rotated, face);
            if (glueableSide != null)
                rotated = rotated.with(glueableSide, false);
        }

        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = getGlueableSide(state, face);
            if (glueableSide == null || !state.get(glueableSide))
                continue;
            Direction rotatedFacing = rotation.rotate(face);
            BooleanProperty rotatedGlueableSide = getGlueableSide(rotated, rotatedFacing);
            if (rotatedGlueableSide != null)
                rotated = rotated.with(rotatedGlueableSide, true);
        }

        return rotated;
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        if (mirrorIn == BlockMirror.NONE)
            return state;

        BlockState mirrored = state;
        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = getGlueableSide(mirrored, face);
            if (glueableSide != null)
                mirrored = mirrored.with(glueableSide, false);
        }

        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = getGlueableSide(state, face);
            if (glueableSide == null || !state.get(glueableSide))
                continue;
            Direction mirroredFacing = mirrorIn.apply(face);
            BooleanProperty mirroredGlueableSide = getGlueableSide(mirrored, mirroredFacing);
            if (mirroredGlueableSide != null)
                mirrored = mirrored.with(mirroredGlueableSide, true);
        }

        return mirrored;
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Direction.Axis.Y) {
            return rotate(state, transform.rotation);
        }
        return transformInner(state, transform);
    }

    protected BlockState transformInner(BlockState state, StructureTransform transform) {
        if (transform.rotation == BlockRotation.NONE)
            return state;

        BlockState rotated = state.with(AXIS, transform.rotateAxis(state.get(AXIS)));
        AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();

        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = block.getGlueableSide(rotated, face);
            if (glueableSide != null)
                rotated = rotated.with(glueableSide, false);
        }

        for (Direction face : Iterate.directions) {
            BooleanProperty glueableSide = block.getGlueableSide(state, face);
            if (glueableSide == null || !state.get(glueableSide))
                continue;
            Direction rotatedFacing = transform.rotateFacing(face);
            BooleanProperty rotatedGlueableSide = block.getGlueableSide(rotated, rotatedFacing);
            if (rotatedGlueableSide != null)
                rotated = rotated.with(rotatedGlueableSide, true);
        }

        return rotated;
    }

    public abstract BooleanProperty getGlueableSide(BlockState state, Direction face);

    protected boolean glueAllowedOnSide(BlockView world, BlockPos pos, BlockState state, Direction side) {
        return true;
    }

    @Override
    public Class<ChassisBlockEntity> getBlockEntityClass() {
        return ChassisBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChassisBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CHASSIS;
    }

}
