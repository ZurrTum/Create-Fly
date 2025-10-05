package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BlockBreakingKineticBlockEntity extends KineticBlockEntity {

    public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
    protected int ticksUntilNextProgress;
    protected int destroyProgress;
    protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
    protected BlockPos breakingPos;

    public BlockBreakingKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (destroyProgress == -1)
            destroyNextTick();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (ticksUntilNextProgress == -1)
            destroyNextTick();
    }

    public void destroyNextTick() {
        ticksUntilNextProgress = 1;
    }

    protected abstract BlockPos getBreakingPos();

    protected boolean shouldRun() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("Progress", destroyProgress);
        view.putInt("NextTick", ticksUntilNextProgress);
        if (breakingPos != null)
            view.put("Breaking", BlockPos.CODEC, breakingPos);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        destroyProgress = view.getInt("Progress", 0);
        ticksUntilNextProgress = view.getInt("NextTick", 0);
        breakingPos = view.read("Breaking", BlockPos.CODEC).orElse(null);
        super.read(view, clientPacket);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isClient() && destroyProgress != 0)
            world.setBlockBreakingInfo(breakerId, breakingPos, -1);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient())
            return;
        if (!shouldRun())
            return;
        if (getSpeed() == 0)
            return;

        breakingPos = getBreakingPos();

        if (ticksUntilNextProgress < 0)
            return;
        if (ticksUntilNextProgress-- > 0)
            return;

        BlockState stateToBreak = world.getBlockState(breakingPos);
        float blockHardness = stateToBreak.getHardness(world, breakingPos);

        if (!canBreak(stateToBreak, blockHardness)) {
            if (destroyProgress != 0) {
                destroyProgress = 0;
                world.setBlockBreakingInfo(breakerId, breakingPos, -1);
            }
            return;
        }

        float breakSpeed = getBreakSpeed();
        destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
        world.playSound(null, pos, stateToBreak.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, .25f, 1);

        if (destroyProgress >= 10) {
            onBlockBroken(stateToBreak);
            destroyProgress = 0;
            ticksUntilNextProgress = -1;
            world.setBlockBreakingInfo(breakerId, breakingPos, -1);
            return;
        }

        ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
        world.setBlockBreakingInfo(breakerId, breakingPos, (int) destroyProgress);
    }

    public boolean canBreak(BlockState stateToBreak, float blockHardness) {
        return isBreakable(stateToBreak, blockHardness);
    }

    public static boolean isBreakable(BlockState stateToBreak, float blockHardness) {
        return !(stateToBreak.isLiquid() || stateToBreak.getBlock() instanceof AirBlock || blockHardness == -1 || stateToBreak.isIn(AllBlockTags.NON_BREAKABLE));
    }

    public void onBlockBroken(BlockState stateToBreak) {
        Vec3d vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), world.random, .125f);
        BlockHelper.destroyBlock(
            world, breakingPos, 1f, (stack) -> {
                if (stack.isEmpty())
                    return;
                if (!((ServerWorld) world).getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
                    return;

                ItemEntity itementity = new ItemEntity(world, vec.x, vec.y, vec.z, stack);
                itementity.setToDefaultPickupDelay();
                itementity.setVelocity(Vec3d.ZERO);
                world.spawnEntity(itementity);
            }
        );
    }

    protected float getBreakSpeed() {
        return Math.abs(getSpeed() / 100f);
    }

}
