package com.zurrtum.create.content.kinetics.saw;

import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.getValue(SawBlock.FACING).getOpposite()
        );
    }

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(SawBlock.FACING).getUnitVec3i()).scale(.65f);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);
        Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(SawBlock.FACING).getUnitVec3i());
        facingVec = context.rotation.apply(facingVec);

        Direction closestToFacing = Direction.getApproximateNearest(facingVec.x, facingVec.y, facingVec.z);
        if (closestToFacing.getAxis().isVertical() && context.data.contains("BreakingPos")) {
            context.data.remove("BreakingPos");
            context.stall = false;
        }
    }

    @Override
    public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
        return super.canBreak(world, breakingPos, state) && SawBlockEntity.isSawable(state);
    }

    @Override
    protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
        if (brokenState.is(BlockTags.LEAVES))
            return;

        //TODO
        //        Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(brokenState.getBlock(), pos);
        //        if (dynamicTree.isPresent()) {
        //            dynamicTree.get().destroyBlocks(context.world, null, (stack, dropPos) -> dropItemFromCutTree(context, stack, dropPos));
        //            return;
        //        }

        TreeCutter.findTree(context.world, pos, brokenState)
            .destroyBlocks(context.world, null, (stack, dropPos) -> dropItemFromCutTree(context, stack, dropPos));
    }

    public void dropItemFromCutTree(MovementContext context, BlockPos pos, ItemStack stack) {
        Container inventory = context.contraption.getStorage().getAllItems();
        int count = stack.getCount();
        int insert = inventory.insert(stack);
        if (insert == count)
            return;

        Level world = context.world;
        Vec3 dropPos = VecHelper.getCenterOf(pos);
        float distance = context.position == null ? 1 : (float) dropPos.distanceTo(context.position);
        stack.setCount(count - insert);
        ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setDeltaMovement(context.relativeMotion.scale(distance / 20f));
        world.addFreshEntity(entity);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    protected boolean shouldDestroyStartBlock(BlockState stateToBreak) {
        //TODO
        //        return !TreeCutter.canDynamicTreeCutFrom(stateToBreak.getBlock());
        return true;
    }

    @Override
    protected DamageSource getDamageSource(Level level) {
        return AllDamageSources.get(level).saw;
    }
}
