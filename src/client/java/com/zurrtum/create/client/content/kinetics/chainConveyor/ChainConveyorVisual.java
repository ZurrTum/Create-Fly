package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
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
import com.zurrtum.create.client.foundation.render.SpecialModels;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChainConveyorVisual extends SingleAxisRotatingVisual<ChainConveyorBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    private final List<TransformedInstance> guards = new ArrayList<>();

    private final SmartRecycler<Identifier, TransformedInstance> boxes;
    private final SmartRecycler<Identifier, TransformedInstance> rigging;

    public ChainConveyorVisual(VisualizationContext context, ChainConveyorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT));

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
    public void update(float pt) {
        super.update(pt);

        setupGuards();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        blockEntity.getBehaviour(ChainConveyorClientBehaviour.TYPE).tickBoxVisuals();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        var partialTicks = ctx.partialTick();

        boxes.resetCount();
        rigging.resetCount();

        for (ChainConveyorPackage box : blockEntity.getLoopingPackages())
            setupBoxVisual(blockEntity, box, partialTicks);

        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : blockEntity.getTravellingPackages().entrySet())
            for (ChainConveyorPackage box : entry.getValue())
                setupBoxVisual(blockEntity, box, partialTicks);

        boxes.discardExtra();
        rigging.discardExtra();
    }


    private void setupBoxVisual(ChainConveyorBlockEntity be, ChainConveyorPackage box, float partialTicks) {
        if (box.worldPosition == null)
            return;
        if (box.item == null || box.item.isEmpty())
            return;

        ChainConveyorPackagePhysicsData physicsData = ChainConveyorClientBehaviour.physicsData(box, be.getWorld());
        if (physicsData.prevPos == null)
            return;

        Vec3d position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
        Vec3d targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
        float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
        Vec3d offset = new Vec3d(targetPosition.x - this.pos.getX(), targetPosition.y - this.pos.getY(), targetPosition.z - this.pos.getZ());

        BlockPos containingPos = BlockPos.ofFloored(position);
        World level = be.getWorld();
        int light = LightmapTextureManager.pack(
            level.getLightLevel(LightType.BLOCK, containingPos),
            level.getLightLevel(LightType.SKY, containingPos)
        );

        if (physicsData.modelKey == null) {
            Identifier key = Registries.ITEM.getId(box.item.getItem());
            if (key == Registries.ITEM.getDefaultId())
                return;
            physicsData.modelKey = key;
        }

        TransformedInstance rigBuffer = rigging.get(physicsData.modelKey);
        TransformedInstance boxBuffer = boxes.get(physicsData.modelKey);

        Vec3d dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Direction.Axis.Y);
        float zRot = MathHelper.wrapDegrees((float) MathHelper.atan2(-dangleDiff.x, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        float xRot = MathHelper.wrapDegrees((float) MathHelper.atan2(dangleDiff.z, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        zRot = MathHelper.clamp(zRot, -25, 25);
        xRot = MathHelper.clamp(xRot, -25, 25);

        for (TransformedInstance buf : new TransformedInstance[]{rigBuffer, boxBuffer}) {
            buf.setIdentityTransform();
            buf.translate(getVisualPosition());
            buf.translate(offset);
            buf.translate(0, 10 / 16f, 0);
            buf.rotateYDegrees(yaw);

            buf.rotateZDegrees(zRot);
            buf.rotateXDegrees(xRot);

            if (physicsData.flipped && buf == rigBuffer)
                buf.rotateYDegrees(180);

            buf.uncenter();
            buf.translate(0, -PackageItem.getHookDistance(box.item) + 7 / 16f, 0);

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

        var wheelInstancer = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            SpecialModels.chunkDiffuse(AllPartialModels.CHAIN_CONVEYOR_WHEEL)
        );
        var guardInstancer = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            SpecialModels.chunkDiffuse(AllPartialModels.CHAIN_CONVEYOR_GUARD)
        );

        TransformedInstance wheel = wheelInstancer.createInstance();

        wheel.translate(getVisualPosition()).light(rotatingModel.light).setChanged();

        guards.add(wheel);

        for (BlockPos blockPos : blockEntity.connections) {
            ChainConveyorBlockEntity.ConnectionStats stats = blockEntity.connectionStats.get(blockPos);
            if (stats == null) {
                continue;
            }

            Vec3d diff = stats.end().subtract(stats.start());
            double yaw = MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(diff.x, diff.z);

            TransformedInstance guard = guardInstancer.createInstance();
            guard.translate(getVisualPosition()).center().rotateYDegrees((float) yaw).uncenter().light(rotatingModel.light).setChanged();

            guards.add(guard);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        for (TransformedInstance guard : guards) {
            relight(guard);
        }
    }

    @Override
    protected void _delete() {
        super._delete();
        deleteGuards();
        boxes.delete();
        rigging.delete();
    }
}
