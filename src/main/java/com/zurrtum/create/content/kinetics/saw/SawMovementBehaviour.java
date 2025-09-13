package com.zurrtum.create.content.kinetics.saw;

import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(SawBlock.FACING).getOpposite());
    }

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(SawBlock.FACING).getVector()).multiply(.65f);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);
        Vec3d facingVec = Vec3d.of(context.state.get(SawBlock.FACING).getVector());
        facingVec = context.rotation.apply(facingVec);

        Direction closestToFacing = Direction.getFacing(facingVec.x, facingVec.y, facingVec.z);
        if (closestToFacing.getAxis().isVertical() && context.data.contains("BreakingPos")) {
            context.data.remove("BreakingPos");
            context.stall = false;
        }
    }

    @Override
    public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
        return super.canBreak(world, breakingPos, state) && SawBlockEntity.isSawable(state);
    }

    @Override
    protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
        if (brokenState.isIn(BlockTags.LEAVES))
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
        Inventory inventory = context.contraption.getStorage().getAllItems();
        int count = stack.getCount();
        int insert = inventory.insert(stack);
        if (insert == count)
            return;

        World world = context.world;
        Vec3d dropPos = VecHelper.getCenterOf(pos);
        float distance = context.position == null ? 1 : (float) dropPos.distanceTo(context.position);
        stack.setCount(count - insert);
        ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setVelocity(context.relativeMotion.multiply(distance / 20f));
        world.spawnEntity(entity);
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
    protected DamageSource getDamageSource(World level) {
        return AllDamageSources.get(level).saw;
    }
}
