package com.zurrtum.create.content.contraptions.actors.plough;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.zurrtum.create.content.trains.track.FakeTrackBlock;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;

import java.util.Objects;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.get(PloughBlock.FACING).getOpposite()
        );
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);
        World world = context.world;
        if (world.isClient())
            return;
        BlockPos below = pos.down();
        if (!world.isPosLoaded(below))
            return;

        Vec3d vec = VecHelper.getCenterOf(pos);

        BlockHitResult ray = world.raycast(new RaycastContext(vec, vec.add(0, -1, 0), ShapeType.OUTLINE, FluidHandling.NONE, ShapeContext.absent()));
        if (ray.getType() != HitResult.Type.BLOCK)
            return;

        ItemUsageContext ctx = new ItemUsageContext(world, null, Hand.MAIN_HAND, Items.DIAMOND_HOE.getDefaultStack(), ray);
        Items.DIAMOND_HOE.useOnBlock(ctx);
    }

    @Override
    protected void throwEntity(MovementContext context, Entity entity) {
        super.throwEntity(context, entity);
        if (!(entity instanceof FallingBlockEntity fbe))
            return;
        if (!(fbe.getBlockState().getBlock() instanceof AnvilBlock))
            return;
        if (entity.getVelocity().length() < 0.25f)
            return;
        entity.getEntityWorld().getNonSpectatingEntities(PlayerEntity.class, new Box(entity.getBlockPos()).expand(32)).stream()
            .map(player -> player instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null).filter(Objects::nonNull)
            .forEach(AllAdvancements.ANVIL_PLOUGH::trigger);
    }

    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(PloughBlock.FACING).getVector()).multiply(.45);
    }

    @Override
    protected boolean throwsEntities(World level) {
        return true;
    }

    @Override
    public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
        if (state.isAir())
            return false;
        if (world.getBlockState(breakingPos.down()).getBlock() instanceof FarmlandBlock)
            return false;
        if (state.getBlock() instanceof FluidBlock)
            return false;
        if (state.getBlock() instanceof BubbleColumnBlock)
            return false;
        if (state.getBlock() instanceof NetherPortalBlock)
            return false;
        if (state.getBlock() instanceof ITrackBlock)
            return true;
        if (state.getBlock() instanceof FakeTrackBlock)
            return false;
        return state.getCollisionShape(world, breakingPos).isEmpty();
    }

    @Override
    protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
        super.onBlockBroken(context, pos, brokenState);

        if (brokenState.getBlock() == Blocks.SNOW && context.world instanceof ServerWorld world) {
            brokenState.getDroppedStacks(new LootWorldContext.Builder(world).add(LootContextParameters.BLOCK_STATE, brokenState)
                .add(LootContextParameters.THIS_ENTITY, context.contraption.entity).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .add(LootContextParameters.TOOL, new ItemStack(Items.IRON_SHOVEL))).forEach(s -> collectOrDropItem(context, s));
        }
    }
}
