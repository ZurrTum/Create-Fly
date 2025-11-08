package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.utility.ServerSpeedProvider;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.infrastructure.packet.c2s.ServerboundChainConveyorRidingPacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ChainConveyorRidingHandler {

    public static BlockPos ridingChainConveyor;
    public static float chainPosition;
    public static BlockPos ridingConnection;
    public static boolean flipped;
    public static int catchingUp;

    public static void embark(MinecraftClient mc, BlockPos lift, float position, BlockPos connection) {
        ridingChainConveyor = lift;
        chainPosition = position;
        ridingConnection = connection;
        catchingUp = 20;
        if (mc.world.getBlockEntity(ridingChainConveyor) instanceof ChainConveyorBlockEntity clbe)
            flipped = clbe.getSpeed() < 0;

        Text component = Text.translatable("mount.onboard", mc.options.sneakKey.getBoundKeyLocalizedText());
        mc.inGameHud.setOverlayMessage(component, false);
        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_CHAIN_HIT, 1f, 0.5f));
    }

    public static void clientTick(MinecraftClient mc) {
        if (ridingChainConveyor == null)
            return;
        if (mc.isPaused())
            return;
        if (!mc.player.isHolding(i -> i.isIn(AllItemTags.CHAIN_RIDEABLE))) {
            stopRiding(mc);
            return;
        }
        BlockEntity blockEntity = mc.world.getBlockEntity(ridingChainConveyor);
        if (mc.player.isSneaking() || !(blockEntity instanceof ChainConveyorBlockEntity clbe)) {
            stopRiding(mc);
            return;
        }
        if (ridingConnection != null && !clbe.connections.contains(ridingConnection)) {
            stopRiding(mc);
            return;
        }

        clbe.prepareStats();

        float chainYOffset = 0.5f * mc.player.getScale();
        Vec3d playerPosition = mc.player.getPos().add(0, mc.player.getBoundingBox().getLengthY() + chainYOffset, 0);

        updateTargetPosition(mc, clbe);

        blockEntity = mc.world.getBlockEntity(ridingChainConveyor);
        if (!(blockEntity instanceof ChainConveyorBlockEntity))
            return;

        clbe = (ChainConveyorBlockEntity) blockEntity;
        clbe.prepareStats();

        Vec3d targetPosition;

        if (ridingConnection != null) {
            ConnectionStats stats = clbe.connectionStats.get(ridingConnection);
            targetPosition = stats.start()
                .add((stats.end().subtract(stats.start())).normalize().multiply(Math.min(stats.chainLength(), chainPosition)));
        } else {
            targetPosition = Vec3d.ofBottomCenter(ridingChainConveyor).add(VecHelper.rotate(new Vec3d(0, 0.25, 1), chainPosition, Axis.Y));
        }

        if (catchingUp > 0)
            catchingUp--;

        Vec3d diff = targetPosition.subtract(playerPosition);
        if (catchingUp == 0 && (diff.length() > 3 || diff.y < -1)) {
            stopRiding(mc);
            return;
        }

        mc.player.setVelocity(mc.player.getVelocity().multiply(0.75).add(diff.multiply(0.25)));
        if (AnimationTickHolder.getTicks() % 10 == 0)
            mc.getNetworkHandler().sendPacket(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, false));
    }

    private static void stopRiding(MinecraftClient mc) {
        if (ridingChainConveyor != null)
            mc.getNetworkHandler().sendPacket(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, true));
        ridingChainConveyor = null;
        ridingConnection = null;
        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_CHAIN_HIT, 0.75f, 0.35f));
    }

    private static void updateTargetPosition(MinecraftClient mc, ChainConveyorBlockEntity clbe) {
        float serverSpeed = ServerSpeedProvider.get();
        float speed = clbe.getSpeed() / 360f;
        float radius = 1.5f;
        float distancePerTick = Math.abs(speed);
        float degreesPerTick = (speed / (MathHelper.PI * radius)) * 360f;

        if (ridingConnection != null) {
            ConnectionStats stats = clbe.connectionStats.get(ridingConnection);

            if (flipped != clbe.getSpeed() < 0) {
                flipped = clbe.getSpeed() < 0;
                ridingChainConveyor = clbe.getPos().add(ridingConnection);
                chainPosition = stats.chainLength() - chainPosition;
                ridingConnection = ridingConnection.multiply(-1);
                return;
            }

            chainPosition += serverSpeed * distancePerTick;
            chainPosition = Math.min(stats.chainLength(), chainPosition);
            if (chainPosition < stats.chainLength())
                return;

            // transfer to other
            if (mc.world.getBlockEntity(clbe.getPos().add(ridingConnection)) instanceof ChainConveyorBlockEntity clbe2) {
                chainPosition = clbe.wrapAngle(stats.tangentAngle() + 180 + 2 * 35 * (clbe.reversed ? -1 : 1));
                ridingChainConveyor = clbe2.getPos();
                ridingConnection = null;
            }

            return;
        }

        float prevChainPosition = chainPosition;
        chainPosition += serverSpeed * degreesPerTick;
        chainPosition = clbe.wrapAngle(chainPosition);

        BlockPos nearestLooking = BlockPos.ORIGIN;
        double bestDiff = Double.MAX_VALUE;
        for (BlockPos connection : clbe.connections) {
            double diff = Vec3d.of(connection).normalize().squaredDistanceTo(mc.player.getRotationVector().normalize());
            if (diff > bestDiff)
                continue;
            nearestLooking = connection;
            bestDiff = diff;
        }

        if (nearestLooking == BlockPos.ORIGIN)
            return;

        float offBranchAngle = clbe.connectionStats.get(nearestLooking).tangentAngle();
        if (!clbe.loopThresholdCrossed(chainPosition, prevChainPosition, offBranchAngle))
            return;

        chainPosition = 0;
        ridingConnection = nearestLooking;
    }

}
