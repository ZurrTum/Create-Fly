package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class BacktankRenderer extends KineticBlockEntityRenderer<BacktankBlockEntity> {
    public BacktankRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BacktankBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState blockState = be.getCachedState();
        SuperByteBuffer cogs = CachedBuffers.partial(getCogsModel(blockState), blockState);
        cogs.center().rotateYDegrees(180 + AngleHelper.horizontalAngle(blockState.get(BacktankBlock.HORIZONTAL_FACING))).uncenter()
            .translate(0, 6.5f / 16, 11f / 16)
            .rotate(AngleHelper.rad(be.getSpeed() / 4f * AnimationTickHolder.getRenderTime(be.getWorld()) % 360), Direction.EAST)
            .translate(0, -6.5f / 16, -11f / 16);
        cogs.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BacktankBlockEntity be, BlockState state) {
        return CachedBuffers.partial(getShaftModel(state), state);
    }

    public static PartialModel getCogsModel(BlockState state) {
        if (state.isOf(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_COGS;
        }
        return AllPartialModels.COPPER_BACKTANK_COGS;
    }

    public static PartialModel getShaftModel(BlockState state) {
        if (state.isOf(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_SHAFT;
        }
        return AllPartialModels.COPPER_BACKTANK_SHAFT;
    }
}
