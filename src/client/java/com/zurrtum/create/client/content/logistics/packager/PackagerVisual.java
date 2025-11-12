package com.zurrtum.create.client.content.logistics.packager;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PackagerVisual<T extends PackagerBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
    public final TransformedInstance hatch;
    public final TransformedInstance tray;

    public float lastTrayOffset = Float.NaN;
    public PartialModel lastHatchPartial;


    public PackagerVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        lastHatchPartial = PackagerRenderer.getHatchModel(blockEntity);
        hatch = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(lastHatchPartial)).createInstance();

        tray = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(PackagerRenderer.getTrayModel(blockState))).createInstance();

        Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();

        var lowerCorner = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        hatch.setIdentityTransform().translate(getVisualPosition()).translate(lowerCorner.scale(.49999f))
            .rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing)).rotateXCenteredDegrees(AngleHelper.verticalAngle(facing)).setChanged();

        // TODO: I think we need proper ItemVisuals to handle rendering the boxes in here

        animate(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    public void animate(float partialTick) {
        var hatchPartial = PackagerRenderer.getHatchModel(blockEntity);

        if (hatchPartial != this.lastHatchPartial) {
            instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(hatchPartial)).stealInstance(hatch);

            this.lastHatchPartial = hatchPartial;
        }

        float trayOffset = blockEntity.getTrayOffset(partialTick);

        if (trayOffset != lastTrayOffset) {
            Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();

            var lowerCorner = Vec3.atLowerCornerOf(facing.getUnitVec3i());

            tray.setIdentityTransform().translate(getVisualPosition()).translate(lowerCorner.scale(trayOffset))
                .rotateYCenteredDegrees(facing.toYRot()).setChanged();

            lastTrayOffset = trayOffset;
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(hatch, tray);
    }

    @Override
    protected void _delete() {
        hatch.delete();
        tray.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
    }
}
