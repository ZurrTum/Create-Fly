package com.zurrtum.create.client.content.kinetics.fan;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.FACING;

public class EncasedFanRenderer extends KineticBlockEntityRenderer<EncasedFanBlockEntity> {

    public EncasedFanRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(EncasedFanBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        World world = be.getWorld();
        if (VisualizationManager.supportsVisualization(world))
            return;

        BlockState state = be.getCachedState();
        Direction direction = state.get(FACING);
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getCutoutMipped());

        BlockPos pos = be.getPos();
        Direction opposite = direction.getOpposite();
        int lightBehind = WorldRenderer.getLightmapCoordinates(world, pos.offset(opposite));
        int lightInFront = WorldRenderer.getLightmapCoordinates(world, pos.offset(direction));

        SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, opposite);
        SuperByteBuffer fanInner = CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, state, opposite);

        float time = AnimationTickHolder.getRenderTime(world);
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = MathHelper.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = MathHelper.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        angle = angle / 180f * (float) Math.PI;

        standardKineticRotationTransform(shaftHalf, be, lightBehind).renderInto(ms, vb);
        kineticRotationTransform(fanInner, be, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
    }

}
