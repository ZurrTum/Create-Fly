package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.google.common.cache.Cache;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBehaviour;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChainConveyorClientBehaviour extends ChainConveyorBehaviour {
    // Client tracks physics data by id so it can travel between BEs
    private static final int ticksUntilExpired = 30;
    public static final WorldAttached<Cache<Integer, ChainConveyorPackagePhysicsData>> physicsDataCache = new WorldAttached<>($ -> new TickBasedCache<>(ticksUntilExpired,
        true
    ));

    public static ChainConveyorPackagePhysicsData physicsData(ChainConveyorPackage box, WorldAccess level) {
        if (box.physicsData == null) {
            try {
                ChainConveyorPackagePhysicsData physicsData = physicsDataCache.get(level).get(box.netId, ChainConveyorPackagePhysicsData::new);
                box.physicsData = physicsData;
                return physicsData;
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        physicsDataCache.get(level).getIfPresent(box.netId);
        return (ChainConveyorPackagePhysicsData) box.physicsData;
    }

    public ChainConveyorClientBehaviour(ChainConveyorBlockEntity be) {
        super(be);
    }

    @Override
    public void blockEntityTickBoxVisuals() {
        // We can use TickableVisuals if flywheel is enabled
        if (!VisualizationManager.supportsVisualization(blockEntity.getWorld()))
            tickBoxVisuals();
    }

    @Override
    public void tickBoxVisuals() {
        for (ChainConveyorPackage box : blockEntity.getLoopingPackages())
            tickBoxVisuals(box);
        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : blockEntity.getTravellingPackages().entrySet())
            for (ChainConveyorPackage box : entry.getValue())
                tickBoxVisuals(box);
    }

    private void tickBoxVisuals(ChainConveyorPackage box) {
        if (box.worldPosition == null)
            return;

        ChainConveyorPackagePhysicsData physicsData = physicsData(box, blockEntity.getWorld());
        physicsData.setBE(blockEntity);

        if (!physicsData.shouldTick() && !blockEntity.isVirtual())
            return;

        physicsData.prevTargetPos = physicsData.targetPos;
        physicsData.prevPos = physicsData.pos;
        physicsData.prevYaw = physicsData.yaw;
        physicsData.flipped = blockEntity.reversed;

        if (physicsData.pos != null) {
            if (physicsData.pos.squaredDistanceTo(box.worldPosition) > 1.5f * 1.5f)
                physicsData.pos = box.worldPosition.add(physicsData.pos.subtract(box.worldPosition).normalize().multiply(1.5));
            physicsData.motion = physicsData.motion.add(0, -0.25, 0).multiply(0.75).add((box.worldPosition.subtract(physicsData.pos)).multiply(0.25));
            physicsData.pos = physicsData.pos.add(physicsData.motion);
        }

        physicsData.targetPos = box.worldPosition.subtract(0, 9 / 16f, 0);

        if (physicsData.pos == null) {
            physicsData.pos = physicsData.targetPos;
            physicsData.prevPos = physicsData.targetPos;
            physicsData.prevTargetPos = physicsData.targetPos;
        }

        physicsData.yaw = AngleHelper.angleLerp(.25, physicsData.yaw, box.yaw);
    }

    @Override
    public void initialize() {
        updateChainShapes();
    }

    @Override
    public void updateChainShapes() {
        List<ChainConveyorShape> shapes = new ArrayList<>();
        shapes.add(new ChainConveyorShape.ChainConveyorBB(Vec3d.ofBottomCenter(BlockPos.ZERO)));
        BlockPos pos = blockEntity.getPos();
        for (BlockPos target : blockEntity.connections) {
            ChainConveyorBlockEntity.ConnectionStats stats = blockEntity.connectionStats.get(target);
            if (stats == null)
                continue;
            Vec3d localStart = stats.start().subtract(Vec3d.of(pos));
            Vec3d localEnd = stats.end().subtract(Vec3d.of(pos));
            shapes.add(new ChainConveyorShape.ChainConveyorOBB(target, localStart, localEnd));
        }
        ChainConveyorInteractionHandler.loadedChains.get(blockEntity.getWorld()).put(pos, shapes);
    }

    @Override
    public void invalidate() {
        ChainConveyorInteractionHandler.loadedChains.get(blockEntity.getWorld()).invalidate(blockEntity.getPos());
    }
}
