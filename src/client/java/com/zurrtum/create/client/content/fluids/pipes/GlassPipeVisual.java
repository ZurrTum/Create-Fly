package com.zurrtum.create.client.content.fluids.pipes;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.content.fluids.FluidInstance;
import com.zurrtum.create.client.content.fluids.FluidMesh;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.SmartRecycler;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class GlassPipeVisual extends AbstractBlockEntityVisual<StraightPipeBlockEntity> implements SimpleDynamicVisual {

    private int light;

    private final SmartRecycler<TextureAtlasSprite, FluidInstance> stream;
    private final SmartRecycler<TextureAtlasSprite, TransformedInstance> surface;

    public GlassPipeVisual(VisualizationContext ctx, StraightPipeBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        stream = new SmartRecycler<>(sprite -> ctx.instancerProvider().instancer(AllInstanceTypes.FLUID, FluidMesh.stream(sprite)).createInstance());
        surface = new SmartRecycler<>(sprite -> ctx.instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, FluidMesh.surface(sprite, FluidMesh.PIPE_RADIUS)).createInstance());
    }

    @Override
    public void beginFrame(Context ctx) {
        stream.resetCount();
        surface.resetCount();

        FluidTransportBehaviour pipe = blockEntity.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe == null) {
            stream.discardExtra();
            surface.discardExtra();
            return;
        }

        for (Direction side : Iterate.directions) {

            Flow flow = pipe.getFlow(side);
            if (flow == null)
                continue;
            FluidStack fluidStack = flow.fluid;
            if (fluidStack.isEmpty())
                continue;
            LerpedFloat progressLerp = flow.progress;
            if (progressLerp == null)
                continue;

            float progress = progressLerp.getValue(ctx.partialTick());
            boolean inbound = flow.inbound;
            if (progress == 1) {
                if (inbound) {
                    Flow opposite = pipe.getFlow(side.getOpposite());
                    if (opposite == null)
                        progress -= 1e-6f;
                } else {
                    FluidTransportBehaviour adjacent = BlockEntityBehaviour.get(level, pos.relative(side), FluidTransportBehaviour.TYPE);
                    if (adjacent == null)
                        progress -= 1e-6f;
                    else {
                        Flow other = adjacent.getFlow(side.getOpposite());
                        if (other == null || !other.inbound && !other.complete)
                            progress -= 1e-6f;
                    }
                }
            }

            Fluid fluid = fluidStack.getFluid();
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config == null) {
                continue;
            }
            TextureAtlasSprite flowTexture = config.flowing().get();

            int color = config.tint().apply(fluidStack.getComponentChanges()) | 0xff000000;
            int blockLightIn = (light >> 4) & 0xF;
            int luminosity = Math.max(blockLightIn, fluid.defaultFluidState().createLegacyBlock().getLightEmission());
            int light = (this.light & 0xF00000) | luminosity << 4;

            if (inbound)
                side = side.getOpposite();

            var yStart = (inbound ? 0 : .5f);
            var progressOffset = Mth.clamp(progress * .5f, 0, 1);

            var fluidInstance = stream.get(flowTexture);

            fluidInstance.setIdentityTransform().translate(getVisualPosition()).center().rotateTo(Direction.UP, side)
                .translate(0, -Translate.CENTER + yStart, 0);

            fluidInstance.light(light).colorArgb(color);


            fluidInstance.vScale = (flowTexture.getV1() - flowTexture.getV0()) * 0.5f;
            fluidInstance.v0 = flowTexture.getV0() + yStart * fluidInstance.vScale;
            fluidInstance.progress = progressOffset;

            fluidInstance.setChanged();

            if (progress != 1) {
                TextureAtlasSprite stillTexture = config.still().get();
                surface.get(stillTexture).setIdentityTransform().translate(getVisualPosition()).center().rotateTo(Direction.UP, side)
                    .translate(0, -Translate.CENTER + yStart + progressOffset, 0).light(light).colorArgb(color).setChanged();
            }
        }

        stream.discardExtra();
        surface.discardExtra();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {

    }

    @Override
    public void updateLight(float partialTick) {
        light = computePackedLight();
    }

    @Override
    protected void _delete() {
        stream.delete();
        surface.delete();
    }

}
