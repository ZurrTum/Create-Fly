package com.zurrtum.create.client.foundation.ponder.instruction;

import com.zurrtum.create.client.content.kinetics.deployer.DeployerRenderer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateBlockEntityInstruction extends TickingInstruction {

    protected double deltaPerTick;
    protected double totalDelta;
    protected double target;
    protected final BlockPos location;

    private final BiConsumer<PonderLevel, Float> setter;
    private final Function<PonderLevel, Float> getter;

    public static AnimateBlockEntityInstruction bearing(BlockPos location, float totalDelta, int ticks) {
        return new AnimateBlockEntityInstruction(
            location,
            totalDelta,
            ticks,
            (w, f) -> castIfPresent(w, location, IBearingBlockEntity.class).ifPresent(bte -> bte.setAngle(f)),
            (w) -> castIfPresent(w, location, IBearingBlockEntity.class).map(bte -> bte.getInterpolatedAngle(0)).orElse(0f)
        );
    }

    public static AnimateBlockEntityInstruction bogey(BlockPos location, float totalDelta, int ticks) {
        float movedPerTick = totalDelta / ticks;
        return new AnimateBlockEntityInstruction(
            location,
            totalDelta,
            ticks,
            (w, f) -> castIfPresent(
                w,
                location,
                AbstractBogeyBlockEntity.class
            ).ifPresent(bte -> bte.animate(f.equals(totalDelta) ? 0 : movedPerTick)),
            (w) -> 0f
        );
    }

    public static AnimateBlockEntityInstruction pulley(BlockPos location, float totalDelta, int ticks) {
        return new AnimateBlockEntityInstruction(
            location,
            totalDelta,
            ticks,
            (w, f) -> castIfPresent(w, location, PulleyBlockEntity.class).ifPresent(pulley -> pulley.animateOffset(f)),
            (w) -> castIfPresent(w, location, PulleyBlockEntity.class).map(pulley -> pulley.offset).orElse(0f)
        );
    }

    public static AnimateBlockEntityInstruction deployer(BlockPos location, float totalDelta, int ticks) {
        return new AnimateBlockEntityInstruction(
            location,
            totalDelta,
            ticks,
            (w, f) -> castIfPresent(w, location, DeployerBlockEntity.class).ifPresent(deployer -> deployer.setAnimatedOffset(f)),
            (w) -> castIfPresent(w, location, DeployerBlockEntity.class).map(deployer -> DeployerRenderer.getHandOffset(deployer, 1)).orElse(0f)
        );
    }

    protected AnimateBlockEntityInstruction(
        BlockPos location,
        float totalDelta,
        int ticks,
        BiConsumer<PonderLevel, Float> setter,
        Function<PonderLevel, Float> getter
    ) {
        super(false, ticks);
        this.location = location;
        this.setter = setter;
        this.getter = getter;
        this.deltaPerTick = totalDelta * (1d / ticks);
        this.totalDelta = totalDelta;
        this.target = totalDelta;
    }

    @Override
    protected final void firstTick(PonderScene scene) {
        super.firstTick(scene);
        target = getter.apply(scene.getWorld()) + totalDelta;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        PonderLevel world = scene.getWorld();
        float current = getter.apply(world);
        float next = (float) (remainingTicks == 0 ? target : current + deltaPerTick);
        setter.accept(world, next);
        if (remainingTicks == 0) // lock interpolation
            setter.accept(world, next);
    }

    private static <T> Optional<T> castIfPresent(PonderLevel world, BlockPos pos, Class<T> beType) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (beType.isInstance(blockEntity))
            return Optional.of(beType.cast(blockEntity));
        return Optional.empty();
    }

}