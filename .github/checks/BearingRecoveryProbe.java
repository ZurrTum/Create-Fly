package probe;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Requires automatic recovery of a saved Running=true bearing without its Contraption. */
public class BearingRecoveryProbe implements DedicatedServerModInitializer {
    private static final BlockPos ORPHAN_POS = new BlockPos(228, 120, 228);
    private static final BlockPos CONTROL_POS = new BlockPos(234, 120, 228);
    private MechanicalBearingBlockEntity orphan;
    private MechanicalBearingBlockEntity control;
    private CreativeMotorBlockEntity orphanMotor;
    private int ticks;
    private int phase;
    private int phaseStarted;
    private boolean done;
    private boolean reproduced;
    private boolean recovered;
    private boolean assemblyControl;
    private boolean stoppedControl;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
            server.overworld().setChunkForced(ORPHAN_POS.getX() >> 4, ORPHAN_POS.getZ() >> 4, true));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (done) return;
            ticks++;
            try {
                step(server.overworld());
                if (!done && ticks > 600) throw new AssertionError("fixture timed out in phase " + phase);
            } catch (Throwable failure) {
                done = true;
                System.out.println("BEARING_RECOVERY_PROBE_RESULT unexpected=true phase=" + phase);
                failure.printStackTrace(System.out);
            }
        });
    }

    private void step(ServerLevel level) {
        if (phase == 0) {
            if (!level.isPositionEntityTicking(ORPHAN_POS) || !level.isPositionEntityTicking(CONTROL_POS)) return;
            orphan = place(level, ORPHAN_POS);
            control = place(level, CONTROL_POS);
            orphanMotor = (CreativeMotorBlockEntity) level.getBlockEntity(ORPHAN_POS.below());
            nextPhase();
        } else if (phase == 1 && ticks - phaseStarted >= 20) {
            require(orphan.getSpeed() != 0 && control.getSpeed() != 0, "motors did not power bearings");
            require(orphan.getMovedContraption() == null && control.getMovedContraption() == null,
                "empty fixtures unexpectedly assembled");
            level.setBlockAndUpdate(ORPHAN_POS.above(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(CONTROL_POS.above(), Blocks.STONE.defaultBlockState());
            CompoundTag saved = orphan.saveWithFullMetadata(level.registryAccess());
            saved.putBoolean("Running", true);
            // Placement marks the old BE as moved; disk loading creates a fresh instance.
            orphan = (MechanicalBearingBlockEntity) BlockEntity.loadStatic(ORPHAN_POS,
                orphan.getBlockState(), saved, level.registryAccess());
            require(orphan != null, "vanilla block entity loader rejected fixture");
            level.setBlockEntity(orphan);
            require(orphan.isRunning() && orphan.getMovedContraption() == null, "saved orphan state was not loaded");
            control.onSpeedChanged(0);
            System.out.println("BEARING_RECOVERY_SETUP savedRunning=true entity=false speed=" + orphan.getSpeed()
                + " entityTicking=" + level.isPositionEntityTicking(ORPHAN_POS));
            nextPhase();
        } else if (phase == 2 && ticks - phaseStarted >= 120) {
            boolean sourcePresent = level.getBlockState(ORPHAN_POS.above()).is(Blocks.STONE);
            boolean ticking = level.isPositionEntityTicking(ORPHAN_POS);
            reproduced = orphan.isRunning() && orphan.getMovedContraption() == null && sourcePresent
                && orphan.getSpeed() != 0 && ticking;
            recovered = orphan.isRunning() && orphan.getMovedContraption() != null && !sourcePresent
                && orphan.getSpeed() != 0 && ticking;
            assemblyControl = control.isRunning() && control.getMovedContraption() != null
                && level.getBlockState(CONTROL_POS.above()).isAir();
            System.out.println("BEARING_RECOVERY_OBS ticks=120 running=" + orphan.isRunning()
                + " entity=" + (orphan.getMovedContraption() != null) + " frontBlock=" + sourcePresent
                + " speed=" + orphan.getSpeed() + " entityTicking=" + ticking
                + " normalAssemblyControl=" + assemblyControl);
            require(assemblyControl, "ordinary powered bearing failed to assemble its stone control");
            require(recovered, "loaded orphan bearing did not automatically assemble");
            orphanMotor.generatedSpeed.setValue(0);
            nextPhase();
        } else if (phase == 3 && ticks - phaseStarted >= 30) {
            stoppedControl = orphan.getSpeed() == 0 && !orphan.isRunning();
            System.out.println("BEARING_RECOVERY_POWER_OFF speed=" + orphan.getSpeed()
                + " running=" + orphan.isRunning());
            require(stoppedControl, "power-off did not clear running state");
            orphanMotor.generatedSpeed.setValue(16);
            nextPhase();
        } else if (phase == 4 && ticks - phaseStarted >= 40) {
            boolean powerCycleControl = orphan.isRunning() && orphan.getMovedContraption() != null
                && level.getBlockState(ORPHAN_POS.above()).isAir();
            require(powerCycleControl, "orphan fixture did not assemble after power cycle");
            done = true;
            System.out.println("BEARING_RECOVERY_PROBE_RESULT reproduced=" + reproduced
                + " automaticRecovery=" + recovered
                + " ordinaryAssembly=" + assemblyControl + " powerOff=" + stoppedControl
                + " powerCycleAssembly=" + powerCycleControl + " unexpected=false");
        }
    }

    private MechanicalBearingBlockEntity place(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, AllBlocks.MECHANICAL_BEARING.defaultBlockState().setValue(BearingBlock.FACING, Direction.UP));
        level.setBlockAndUpdate(pos.below(), AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(CreativeMotorBlock.FACING, Direction.UP));
        return (MechanicalBearingBlockEntity) level.getBlockEntity(pos);
    }

    private void nextPhase() {
        phase++;
        phaseStarted = ticks;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
