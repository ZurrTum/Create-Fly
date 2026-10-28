package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.SmartRecycler;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBehaviour;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChainConveyorVisual extends SingleAxisRotatingVisual<ChainConveyorBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    private final List<TransformedInstance> guards = new ArrayList<>();

    private final SmartRecycler<Identifier, TransformedInstance> boxes;
    private final SmartRecycler<Identifier, TransformedInstance> rigging;

    public ChainConveyorVisual(VisualizationContext context, ChainConveyorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.CHAIN_CONVEYOR_SHAFT));

        setupGuards();

        boxes = new SmartRecycler<>(key -> instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(AllPartialModels.PACKAGES.get(key))
        ).createInstance());
        rigging = new SmartRecycler<>(key -> instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(AllPartialModels.PACKAGE_RIGGING.get(key))
        ).createInstance());
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        int offsetX = pos.getX();
        int offsetZ = pos.getZ();
        int minSectionX = SectionPos.posToSectionCoord(offsetX - 1);
        int minSectionZ = SectionPos.posToSectionCoord(offsetZ - 1);
        int maxSectionX = SectionPos.posToSectionCoord(offsetX + 1);
        int maxSectionZ = SectionPos.posToSectionCoord(offsetZ + 1);
        int y = SectionPos.posToSectionCoord(pos.getY());
        LongSet longSet = new LongArraySet();
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int z = minSectionZ; z <= maxSectionZ; z++) {
                longSet.add(SectionPos.asLong(x, y, z));
            }
        }
        sectionCollector.sections(longSet);
    }

    @Override
    public void update(float pt) {
        super.update(pt);

        setupGuards();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        ChainConveyorBehaviour behaviour = blockEntity.getBehaviour(ChainConveyorClientBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.tickBoxVisuals();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        var partialTicks = ctx.partialTick();

        boxes.resetCount();
        rigging.resetCount();

        for (ChainConveyorPackage box : blockEntity.getLoopingPackages()) {
            setupBoxVisual(blockEntity, box, partialTicks);
        }

        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : blockEntity.getTravellingPackages().entrySet()) {
            for (ChainConveyorPackage box : entry.getValue()) {
                setupBoxVisual(blockEntity, box, partialTicks);
            }
        }

        boxes.discardExtra();
        rigging.discardExtra();
    }


    private void setupBoxVisual(ChainConveyorBlockEntity be, ChainConveyorPackage box, float partialTicks) {
        if (box.worldPosition == null) {
            return;
        }
        if (box.item == null || box.item.isEmpty()) {
            return;
        }

        ChainConveyorPackagePhysicsData physicsData = ChainConveyorClientBehaviour.physicsData(box, be.getLevel());
        if (physicsData.prevPos == null) {
            return;
        }

        Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
        Vec3 targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
        float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
        Vec3 offset = new Vec3(
            targetPosition.x - pos.getX(),
            targetPosition.y - pos.getY(),
            targetPosition.z - pos.getZ()
        );

        BlockPos containingPos = BlockPos.containing(position);
        Level level = be.getLevel();
        int light = LightCoordsUtil.pack(
            level.getBrightness(LightLayer.BLOCK, containingPos),
            level.getBrightness(LightLayer.SKY, containingPos)
        );

        if (physicsData.modelKey == null) {
            Identifier key = BuiltInRegistries.ITEM.getKey(box.item.getItem());
            if (key == BuiltInRegistries.ITEM.getDefaultKey()) {
                return;
            }
            physicsData.modelKey = key;
        }

        TransformedInstance rigBuffer = rigging.get(physicsData.modelKey);
        TransformedInstance boxBuffer = boxes.get(physicsData.modelKey);

        Vec3 dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Direction.Axis.Y);
        float zRot = Mth.wrapDegrees((float) Mth.atan2(-dangleDiff.x, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        float xRot = Mth.wrapDegrees((float) Mth.atan2(dangleDiff.z, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        zRot = Mth.clamp(zRot, -25, 25);
        xRot = Mth.clamp(xRot, -25, 25);

        for (TransformedInstance buf : new TransformedInstance[]{rigBuffer, boxBuffer}) {
            buf.setIdentityTransform();
            buf.translate(getVisualPosition());
            buf.translate(offset);
            buf.translate(0, 10 / 16.0f, 0);
            buf.rotateYDegrees(yaw);

            buf.rotateZDegrees(zRot);
            buf.rotateXDegrees(xRot);

            if (physicsData.flipped && buf == rigBuffer) {
                buf.rotateY(Radians.DEG_180);
            }

            buf.uncenter();
            buf.translate(0, -PackageItem.getHookDistance(box.item) + 7 / 16.0f, 0);

            buf.light(light);

            buf.setChanged();
        }
    }

    private void deleteGuards() {
        for (TransformedInstance guard : guards) {
            guard.delete();
        }
        guards.clear();
    }

    private void setupGuards() {
        deleteGuards();

        var guardInstancer = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CHAIN_CONVEYOR_GUARD)
        );

        for (BlockPos blockPos : blockEntity.connections) {
            ChainConveyorBlockEntity.ConnectionStats stats = blockEntity.connectionStats.get(blockPos);
            if (stats == null) {
                continue;
            }

            Vec3 diff = stats.end().subtract(stats.start());
            float yawRadians = (float) Mth.atan2(diff.x, diff.z);

            TransformedInstance guard = guardInstancer.createInstance();
            guard.translate(getVisualPosition()).rotateYCentered(yawRadians).light(rotatingModel.light).setChanged();

            guards.add(guard);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(guards);
    }

    @Override
    protected void _delete() {
        super._delete();
        deleteGuards();
        boxes.delete();
        rigging.delete();
    }
}
