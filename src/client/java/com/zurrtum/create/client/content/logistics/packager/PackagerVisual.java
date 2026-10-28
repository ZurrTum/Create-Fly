package com.zurrtum.create.client.content.logistics.packager;

import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class PackagerVisual<T extends PackagerBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual, ShaderLightVisual {
    public final TransformedInstance hatch;
    public final TransformedInstance tray;

    public float lastTrayOffset = Float.NaN;
    public PartialModel lastHatchPartial;


    public PackagerVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        lastHatchPartial = PackagerRenderer.getHatchModel(blockEntity);
        hatch = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(lastHatchPartial))
            .createInstance();

        tray = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(PackagerRenderer.getTrayModel(blockState))
        ).createInstance();

        Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();

        var lowerCorner = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        hatch.setIdentityTransform().translate(getVisualPosition()).translate(lowerCorner.scale(0.49999f))
            .rotateYCentered(Radians.horizontal(facing)).rotateXCentered(Radians.vertical(facing)).setChanged();

        // TODO: I think we need proper ItemVisuals to handle rendering the boxes in here

        animate(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.FACING)) {
            case UP -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
            case DOWN -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 0);
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 0, 1);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 0);
            case WEST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 0, 0);
            case EAST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    public void animate(float partialTick) {
        var hatchPartial = PackagerRenderer.getHatchModel(blockEntity);

        if (hatchPartial != lastHatchPartial) {
            instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(hatchPartial))
                .stealInstance(hatch);

            lastHatchPartial = hatchPartial;
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
