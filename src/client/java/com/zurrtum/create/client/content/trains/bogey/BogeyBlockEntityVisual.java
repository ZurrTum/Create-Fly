package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BogeyBlockEntityVisual extends AbstractBlockEntityVisual<AbstractBogeyBlockEntity> implements SimpleDynamicVisual {
    private final MatrixStack poseStack = new MatrixStack();

    @Nullable
    private final BogeySize bogeySize;
    private BogeyStyle lastStyle;
    @Nullable
    private BogeyVisual bogey;

    public BogeyBlockEntityVisual(VisualizationContext ctx, AbstractBogeyBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        lastStyle = blockEntity.getStyle();

        if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> block)) {
            bogeySize = null;
            return;
        }

        bogeySize = block.getSize();

        BlockPos visualPos = getVisualPosition();
        poseStack.translate(visualPos.getX(), visualPos.getY(), visualPos.getZ());
        poseStack.translate(.5f, .5f, .5f);
        if (blockState.get(AbstractBogeyBlock.AXIS) == Direction.Axis.X)
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        poseStack.translate(0, -1.5 - 1 / 128f, 0);

        bogey = AllBogeyStyleRenders.createVisual(lastStyle, bogeySize, visualizationContext, partialTick, false);

        updateBogey(partialTick);
    }

    @Override
    public void beginFrame(Context context) {
        if (bogeySize == null) {
            return;
        }

        BogeyStyle style = blockEntity.getStyle();
        if (style != lastStyle) {
            if (bogey != null) {
                bogey.delete();
                bogey = null;
            }
            lastStyle = style;
            bogey = AllBogeyStyleRenders.createVisual(lastStyle, bogeySize, visualizationContext, context.partialTick(), false);
            updateLight(context.partialTick());
        }

        updateBogey(context.partialTick());
    }

    private void updateBogey(float partialTick) {
        if (bogey == null) {
            return;
        }

        NbtCompound bogeyData = blockEntity.getBogeyData();
        float angle = blockEntity.getVirtualAngle(partialTick);
        bogey.update(bogeyData, angle, poseStack);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        if (bogey != null) {
            bogey.collectCrumblingInstances(consumer);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        if (bogey != null) {
            bogey.updateLight(computePackedLight());
        }
    }

    @Override
    protected void _delete() {
        if (bogey != null) {
            bogey.delete();
        }
    }
}
