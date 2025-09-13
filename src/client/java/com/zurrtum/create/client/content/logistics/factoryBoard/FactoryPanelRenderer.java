package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.content.logistics.factoryBoard.*;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class FactoryPanelRenderer extends SmartBlockEntityRenderer<FactoryPanelBlockEntity> {

    public FactoryPanelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FactoryPanelBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        for (ServerFactoryPanelBehaviour behaviour : be.panels.values()) {
            if (!behaviour.isActive())
                continue;
            if (behaviour.getAmount() > 0)
                renderBulb(behaviour, partialTicks, ms, buffer, light, overlay);
            for (FactoryPanelConnection connection : behaviour.targetedBy.values())
                renderPath(behaviour, connection, partialTicks, ms, buffer, light, overlay);
            for (FactoryPanelConnection connection : behaviour.targetedByLinks.values())
                renderPath(behaviour, connection, partialTicks, ms, buffer, light, overlay);
        }
    }

    public static void renderBulb(
        ServerFactoryPanelBehaviour behaviour,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = behaviour.blockEntity.getCachedState();

        float xRot = FactoryPanelBlock.getXRot(blockState) + MathHelper.PI / 2;
        float yRot = FactoryPanelBlock.getYRot(blockState);
        float glow = behaviour.bulb.getValue(partialTicks);

        boolean missingAddress = behaviour.isMissingAddress();
        PartialModel partial = behaviour.redstonePowered || missingAddress ? AllPartialModels.FACTORY_PANEL_RED_LIGHT : AllPartialModels.FACTORY_PANEL_LIGHT;

        CachedBuffers.partial(partial, blockState).rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST)
            .rotateCentered(MathHelper.PI, Direction.UP).translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
            .light(glow > 0.125f ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light).overlay(overlay)
            .renderInto(ms, buffer.getBuffer(PonderRenderTypes.translucent()));

        if (glow < .125f)
            return;

        glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
        glow = MathHelper.clamp(glow, -1, 1);
        int color = (int) (200 * glow);

        CachedBuffers.partial(partial, blockState).rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST)
            .rotateCentered(MathHelper.PI, Direction.UP).translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).color(color, color, color, 255).overlay(overlay)
            .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
    }

    public static void renderPath(
        ServerFactoryPanelBehaviour behaviour,
        FactoryPanelConnection connection,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = behaviour.blockEntity.getCachedState();
        World world = behaviour.getWorld();
        FactoryPanelPosition to = behaviour.getPanelPosition();
        FactoryPanelBehaviour fromBehaviour = FactoryPanelBehaviour.at(world, to);
        List<Direction> path = connection.getPath(
            world,
            blockState,
            to,
            fromBehaviour != null ? fromBehaviour.getSlotPositioning().getLocalOffset(world, to.pos(), blockState)
                .add(Vec3d.of(to.pos())) : Vec3d.ZERO
        );

        float xRot = FactoryPanelBlock.getXRot(blockState) + MathHelper.PI / 2;
        float yRot = FactoryPanelBlock.getYRot(blockState);
        float glow = behaviour.bulb.getValue(partialTicks);

        FactoryPanelSupportBehaviour sbe = ServerFactoryPanelBehaviour.linkAt(world, connection);
        boolean displayLinkMode = sbe != null && sbe.blockEntity instanceof DisplayLinkBlockEntity;
        boolean redstoneLinkMode = sbe != null && sbe.blockEntity instanceof RedstoneLinkBlockEntity;
        boolean pathReversed = sbe != null && !sbe.isOutput();

        int color = 0;
        float yOffset = 0;
        boolean success = connection.success;
        boolean dots = false;

        if (displayLinkMode) {
            // Display status
            color = 0x3C9852;
            dots = true;

        } else if (redstoneLinkMode) {
            // Link status
            color = pathReversed ? (behaviour.count == 0 ? 0x888898 : behaviour.satisfied ? 0xEF0000 : 0x580101) : (behaviour.redstonePowered ? 0xEF0000 : 0x580101);
            yOffset = 0.5f;

        } else {
            // Regular ingredient status
            color = behaviour.getIngredientStatusColor();

            yOffset = 1;
            yOffset += behaviour.promisedSatisfied ? 1 : behaviour.satisfied ? 0 : 2;

            if (!behaviour.redstonePowered && !behaviour.waitingForNetwork && glow > 0 && !behaviour.satisfied) {
                float p = (1 - (1 - glow) * (1 - glow));
                color = Color.mixColors(color, success ? 0xEAF2EC : 0xE5654B, p);
                if (!behaviour.satisfied && !behaviour.promisedSatisfied)
                    yOffset += (success ? 1 : 2) * p;
            }
        }

        float currentX = 0;
        float currentZ = 0;

        for (int i = 0; i < path.size(); i++) {
            Direction direction = path.get(i);

            if (!pathReversed) {
                currentX += direction.getOffsetX() * .5;
                currentZ += direction.getOffsetZ() * .5;
            }

            boolean isArrowSegment = pathReversed ? i == path.size() - 1 : i == 0;
            PartialModel partial = (dots ? AllPartialModels.FACTORY_PANEL_DOTTED : isArrowSegment ? AllPartialModels.FACTORY_PANEL_ARROWS : AllPartialModels.FACTORY_PANEL_LINES).get(
                pathReversed ? direction : direction.getOpposite());
            SuperByteBuffer connectionSprite = CachedBuffers.partial(partial, blockState).rotateCentered(yRot, Direction.UP)
                .rotateCentered(xRot, Direction.EAST).rotateCentered(MathHelper.PI, Direction.UP)
                .translate(behaviour.slot.xOffset * .5 + .25, 0, behaviour.slot.yOffset * .5 + .25)
                .translate(currentX, (yOffset + (direction.getHorizontalQuarterTurns() % 2) * 0.125f) / 512f, currentZ);

            if (!displayLinkMode && !redstoneLinkMode && !behaviour.isMissingAddress() && !behaviour.waitingForNetwork && !behaviour.satisfied && !behaviour.redstonePowered)
                connectionSprite.shiftUV(AllSpriteShifts.FACTORY_PANEL_CONNECTIONS);

            connectionSprite.color(color).light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));

            if (pathReversed) {
                currentX += direction.getOffsetX() * .5;
                currentZ += direction.getOffsetZ() * .5;
            }
        }
    }

}
