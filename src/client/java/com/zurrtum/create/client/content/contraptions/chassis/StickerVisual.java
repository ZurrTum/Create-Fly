package com.zurrtum.create.client.content.contraptions.chassis;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class StickerVisual extends AbstractBlockEntityVisual<StickerBlockEntity> implements SimpleDynamicVisual {

    float lastOffset = Float.NaN;
    final Direction facing;
    final boolean fakeWorld;
    final int offset;

    private final TransformedInstance head;

    public StickerVisual(VisualizationContext context, StickerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        head = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.STICKER_HEAD)).createInstance();

        fakeWorld = blockEntity.getWorld() != MinecraftClient.getInstance().world;
        facing = blockState.get(StickerBlock.FACING);
        offset = blockState.get(StickerBlock.EXTENDED) ? 1 : 0;

        animateHead(offset);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float offset = blockEntity.piston.getValue(ctx.partialTick());

        if (fakeWorld)
            offset = this.offset;

        if (MathHelper.approximatelyEquals(offset, lastOffset))
            return;

        animateHead(offset);

        lastOffset = offset;
    }

    private void animateHead(float offset) {
        head.setIdentityTransform().translate(getVisualPosition()).nudge(blockEntity.hashCode()).center()
            .rotateYDegrees(AngleHelper.horizontalAngle(facing)).rotateXDegrees(AngleHelper.verticalAngle(facing) + 90).uncenter()
            .translate(0, (offset * offset) * 4 / 16f, 0).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(head);
    }

    @Override
    protected void _delete() {
        head.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(head);
    }
}
