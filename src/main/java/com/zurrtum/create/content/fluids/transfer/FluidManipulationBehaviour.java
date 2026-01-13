package com.zurrtum.create.content.fluids.transfer;

import com.zurrtum.create.AllFluidTags;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.packet.s2c.FluidSplashPacket;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class FluidManipulationBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public record BlockPosEntry(BlockPos pos, int distance) {
    }

    public static class ChunkNotLoadedException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    BlockBox affectedArea;
    BlockPos rootPos;
    boolean infinite;
    protected boolean counterpartActed;

    // Search
    static final int searchedPerTick = 1024;
    static final int validationTimerMin = 160;
    List<BlockPosEntry> frontier;
    Set<BlockPos> visited;

    int revalidateIn;

    public FluidManipulationBehaviour(SmartBlockEntity be) {
        super(be);
        setValidationTimer();
        infinite = false;
        visited = new HashSet<>();
        frontier = new ArrayList<>();
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void counterpartActed() {
        counterpartActed = true;
    }

    protected int validationTimer() {
        int maxBlocks = maxBlocks();
        // Allow enough time for the server's infinite block threshold to be reached
        return maxBlocks < 0 ? validationTimerMin : Math.max(validationTimerMin, maxBlocks / searchedPerTick + 1);
    }

    protected int setValidationTimer() {
        return revalidateIn = validationTimer();
    }

    protected int setLongValidationTimer() {
        return revalidateIn = validationTimer() * 2;
    }

    protected int maxRange() {
        return AllConfigs.server().fluids.hosePulleyRange.get();
    }

    protected int maxBlocks() {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get();
    }

    protected boolean fillInfinite() {
        return AllConfigs.server().fluids.fillInfinite.get();
    }

    public void reset() {
        if (affectedArea != null)
            scheduleUpdatesInAffectedArea();
        affectedArea = null;
        setValidationTimer();
        frontier.clear();
        visited.clear();
        infinite = false;
    }

    @Override
    public void destroy() {
        reset();
        super.destroy();
    }

    protected void scheduleUpdatesInAffectedArea() {
        World world = getWorld();
        BlockPos.stream(
            new BlockPos(affectedArea.getMinX() - 1, affectedArea.getMinY() - 1, affectedArea.getMinZ() - 1),
            new BlockPos(affectedArea.getMaxX() + 1, affectedArea.getMaxY() + 1, affectedArea.getMaxZ() + 1)
        ).forEach(pos -> {
            FluidState nextFluidState = world.getFluidState(pos);
            if (nextFluidState.isEmpty())
                return;
            world.scheduleFluidTick(pos, nextFluidState.getFluid(), world.getRandom().nextInt(5));
        });
    }

    protected int comparePositions(BlockPosEntry e1, BlockPosEntry e2) {
        Vec3d centerOfRoot = VecHelper.getCenterOf(rootPos);
        BlockPos pos2 = e2.pos;
        BlockPos pos1 = e1.pos;
        if (pos1.getY() != pos2.getY())
            return Integer.compare(pos2.getY(), pos1.getY());
        int compareDistance = Integer.compare(e2.distance, e1.distance);
        if (compareDistance != 0)
            return compareDistance;
        return Double.compare(
            VecHelper.getCenterOf(pos2).squaredDistanceTo(centerOfRoot),
            VecHelper.getCenterOf(pos1).squaredDistanceTo(centerOfRoot)
        );
    }

    protected Fluid search(
        Fluid fluid,
        List<BlockPosEntry> frontier,
        Set<BlockPos> visited,
        BiConsumer<BlockPos, Integer> add,
        boolean searchDownward
    ) throws ChunkNotLoadedException {
        World world = getWorld();
        int maxBlocks = maxBlocks();
        int maxRange = maxRange();
        int maxRangeSq = maxRange * maxRange;
        int i;

        for (i = 0; i < searchedPerTick && !frontier.isEmpty() && (visited.size() <= maxBlocks || !canDrainInfinitely(fluid)); i++) {
            BlockPosEntry entry = frontier.remove(0);
            BlockPos currentPos = entry.pos;
            if (visited.contains(currentPos))
                continue;
            visited.add(currentPos);

            if (!world.isPosLoaded(currentPos))
                throw new ChunkNotLoadedException();

            FluidState fluidState = world.getFluidState(currentPos);
            if (fluidState.isEmpty())
                continue;

            Fluid currentFluid = FluidHelper.convertToStill(fluidState.getFluid());
            if (fluid == null)
                fluid = currentFluid;
            if (!currentFluid.matchesType(fluid))
                continue;

            add.accept(currentPos, entry.distance);

            for (Direction side : Iterate.directions) {
                if (!searchDownward && side == Direction.DOWN)
                    continue;

                BlockPos offsetPos = currentPos.offset(side);
                if (!world.isPosLoaded(offsetPos))
                    throw new ChunkNotLoadedException();
                if (visited.contains(offsetPos))
                    continue;
                if (offsetPos.getSquaredDistance(rootPos) > maxRangeSq)
                    continue;

                FluidState nextFluidState = world.getFluidState(offsetPos);
                if (nextFluidState.isEmpty())
                    continue;
                Fluid nextFluid = nextFluidState.getFluid();
                if (nextFluid == FluidHelper.convertToFlowing(nextFluid) && side == Direction.UP && !VecHelper.onSameAxis(rootPos, offsetPos, Axis.Y))
                    continue;

                frontier.add(new BlockPosEntry(offsetPos, entry.distance + 1));
            }
        }

        return fluid;
    }

    protected void playEffect(World world, BlockPos pos, Fluid fluid, boolean fillSound) {
        if (fluid == null)
            return;

        BlockPos splooshPos = pos == null ? blockEntity.getPos() : pos;
        FluidStack stack = new FluidStack(fluid, 1);

        SoundEvent soundevent = fillSound ? FluidHelper.getFillSound(stack) : FluidHelper.getEmptySound(stack);
        world.playSound(null, splooshPos, soundevent, SoundCategory.BLOCKS, 0.3F, 1.0F);
        if (world instanceof ServerWorld serverLevel) {
            serverLevel.getServer().getPlayerManager().sendToAround(
                null,
                splooshPos.getX(),
                splooshPos.getY(),
                splooshPos.getZ(),
                10,
                serverLevel.getRegistryKey(),
                new FluidSplashPacket(splooshPos, stack.getFluid())
            );
        }
    }

    protected boolean canDrainInfinitely(Fluid fluid) {
        if (fluid == null)
            return false;
        return maxBlocks() != -1 && AllConfigs.server().fluids.bottomlessFluidMode.get().test(fluid);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (infinite)
            view.putBoolean("Infinite", true);
        if (rootPos != null)
            view.put("LastPos", BlockPos.CODEC, rootPos);
        if (affectedArea != null) {
            view.put("AffectedAreaFrom", BlockPos.CODEC, new BlockPos(affectedArea.getMinX(), affectedArea.getMinY(), affectedArea.getMinZ()));
            view.put("AffectedAreaTo", BlockPos.CODEC, new BlockPos(affectedArea.getMaxX(), affectedArea.getMaxY(), affectedArea.getMaxZ()));
        }
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        infinite = view.getBoolean("Infinite", false);
        rootPos = view.read("LastPos", BlockPos.CODEC).orElse(null);
        view.read("AffectedAreaFrom", BlockPos.CODEC).ifPresent(from -> view.read("AffectedAreaTo", BlockPos.CODEC).ifPresent(to -> {
            affectedArea = BlockBox.create(from, to);
        }));
        super.read(view, clientPacket);
    }

    @SuppressWarnings("deprecation")
    public enum BottomlessFluidMode implements Predicate<Fluid> {
        ALLOW_ALL(fluid -> true),
        DENY_ALL(fluid -> false),
        ALLOW_BY_TAG(fluid -> fluid.isIn(AllFluidTags.BOTTOMLESS_ALLOW)),
        DENY_BY_TAG(fluid -> !fluid.isIn(AllFluidTags.BOTTOMLESS_DENY));

        private final Predicate<Fluid> predicate;

        BottomlessFluidMode(Predicate<Fluid> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(Fluid fluid) {
            return predicate.test(fluid);
        }
    }

}
