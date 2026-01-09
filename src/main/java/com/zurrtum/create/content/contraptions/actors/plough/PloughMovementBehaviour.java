package com.zurrtum.create.content.contraptions.actors.plough;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.zurrtum.create.content.trains.track.FakeTrackBlock;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Objects;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return super.isActive(context) && !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            context.state.getValue(PloughBlock.FACING).getOpposite()
        );
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);
        Level world = context.world;
        if (world.isClientSide())
            return;
        BlockPos below = pos.below();
        if (!world.isLoaded(below))
            return;

        Vec3 vec = VecHelper.getCenterOf(pos);

        BlockHitResult ray = world.clip(new ClipContext(vec, vec.add(0, -1, 0), Block.OUTLINE, Fluid.NONE, CollisionContext.empty()));
        if (ray.getType() != HitResult.Type.BLOCK)
            return;

        UseOnContext ctx = new UseOnContext(world, null, InteractionHand.MAIN_HAND, Items.DIAMOND_HOE.getDefaultInstance(), ray);
        Items.DIAMOND_HOE.useOn(ctx);
    }

    @Override
    protected void throwEntity(MovementContext context, Entity entity) {
        super.throwEntity(context, entity);
        if (!(entity instanceof FallingBlockEntity fbe))
            return;
        if (!(fbe.getBlockState().getBlock() instanceof AnvilBlock))
            return;
        if (entity.getDeltaMovement().length() < 0.25f)
            return;
        entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(32)).stream()
            .map(player -> player instanceof ServerPlayer serverPlayer ? serverPlayer : null).filter(Objects::nonNull)
            .forEach(AllAdvancements.ANVIL_PLOUGH::trigger);
    }

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(PloughBlock.FACING).getUnitVec3i()).scale(.45);
    }

    @Override
    protected boolean throwsEntities(Level level) {
        return true;
    }

    @Override
    public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
        if (state.isAir())
            return false;
        if (world.getBlockState(breakingPos.below()).getBlock() instanceof FarmBlock)
            return false;
        if (state.getBlock() instanceof LiquidBlock)
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

        if (brokenState.getBlock() == Blocks.SNOW && context.world instanceof ServerLevel world) {
            brokenState.getDrops(new LootParams.Builder(world).withParameter(LootContextParams.BLOCK_STATE, brokenState)
                .withParameter(LootContextParams.THIS_ENTITY, context.contraption.entity)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_SHOVEL))).forEach(s -> collectOrDropItem(context, s));
        }
    }
}
