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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FactoryPanelRenderer extends SmartBlockEntityRenderer<FactoryPanelBlockEntity, FactoryPanelRenderer.FactoryPanelRenderState> {
    public FactoryPanelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public FactoryPanelRenderState createRenderState() {
        return new FactoryPanelRenderState();
    }

    @Override
    public void updateRenderState(
        FactoryPanelBlockEntity be,
        FactoryPanelRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        List<SingleFactoryPanelRenderState> panels = new ArrayList<>();
        boolean[] layers = new boolean[3];
        for (ServerFactoryPanelBehaviour behaviour : be.panels.values()) {
            if (behaviour.isActive()) {
                boolean bulb = behaviour.getAmount() > 0;
                boolean target = !behaviour.targetedBy.isEmpty() || !behaviour.targetedByLinks.isEmpty();
                if (!target && !bulb) {
                    continue;
                }
                SingleFactoryPanelRenderState panel = new SingleFactoryPanelRenderState();
                boolean missingAddress = behaviour.isMissingAddress();
                float offsetX = behaviour.slot.xOffset * .5f;
                float offsetY = behaviour.slot.yOffset * .5f;
                float glow = behaviour.bulb.getValue(tickProgress);
                if (target) {
                    layers[0] = true;
                    panel.offsetX = offsetX + 0.25f;
                    panel.offsetY = offsetY + 0.25f;
                    List<PathRenderState> paths = panel.paths = new ArrayList<>();
                    World world = behaviour.getWorld();
                    FactoryPanelPosition to = behaviour.getPanelPosition();
                    FactoryPanelBehaviour fromBehaviour = be.getBehaviour(FactoryPanelBehaviour.getTypeForSlot(behaviour.slot));
                    Vec3d start = fromBehaviour.getSlotPositioning().getLocalOffset(state.blockState).add(Vec3d.of(state.pos));
                    for (FactoryPanelConnection connection : behaviour.targetedBy.values()) {
                        List<Direction> path = connection.getPath(world, state.blockState, to, start);
                        if (path.isEmpty()) {
                            continue;
                        }
                        paths.add(getPathRenderState(behaviour, connection, path, world, state.blockState, missingAddress, glow));
                    }
                    for (FactoryPanelConnection connection : behaviour.targetedByLinks.values()) {
                        List<Direction> path = connection.getPath(world, state.blockState, to, start);
                        if (path.isEmpty()) {
                            continue;
                        }
                        paths.add(getPathRenderState(behaviour, connection, path, world, state.blockState, missingAddress, glow));
                    }
                }
                if (bulb) {
                    panel.bulb = getBulbRenderState(behaviour, state.blockState, missingAddress, offsetX, offsetY, glow, layers);
                }
                panels.add(panel);
            }
        }
        if (panels.isEmpty()) {
            return;
        }
        state.xRot = FactoryPanelBlock.getXRot(state.blockState) + MathHelper.PI / 2;
        state.yRot = FactoryPanelBlock.getYRot(state.blockState);
        state.panels = panels;
        if (layers[0]) {
            state.cutout = RenderLayer.getCutoutMipped();
        }
        if (layers[1]) {
            state.translucent1 = PonderRenderTypes.translucent();
        }
        if (layers[2]) {
            state.translucent2 = RenderTypes.translucent();
            state.additive = RenderTypes.additive();
        }
    }

    @Override
    public void render(FactoryPanelRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.panels != null) {
            if (state.cutout != null) {
                queue.submitCustom(matrices, state.cutout, state::renderCutout);
            }
            if (state.translucent1 != null) {
                queue.submitCustom(matrices, state.translucent1, state::renderTranslucent1);
            }
            if (state.additive != null) {
                queue.submitCustom(matrices, state.translucent2, state::renderTranslucent2);
                queue.submitCustom(matrices, state.additive, state::renderAdditive);
            }
        }
    }

    public static BulbRenderState getBulbRenderState(
        ServerFactoryPanelBehaviour behaviour,
        BlockState blockState,
        boolean missingAddress,
        float offsetX,
        float offsetY,
        float glow,
        boolean[] layers
    ) {
        BulbRenderState state = new BulbRenderState();
        PartialModel partial = behaviour.redstonePowered || missingAddress ? AllPartialModels.FACTORY_PANEL_RED_LIGHT : AllPartialModels.FACTORY_PANEL_LIGHT;
        state.model = CachedBuffers.partial(partial, blockState);
        state.offsetX = offsetX;
        state.offsetY = offsetY;
        if (glow < 0.125f) {
            layers[1] = true;
            return state;
        }
        layers[2] = true;
        state.glow = true;
        glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
        glow = MathHelper.clamp(glow, -1, 1);
        state.color = (int) (200 * glow);
        return state;
    }

    public static PathRenderState getPathRenderState(
        ServerFactoryPanelBehaviour behaviour,
        FactoryPanelConnection connection,
        List<Direction> path,
        World world,
        BlockState blockState,
        boolean missingAddress,
        float glow
    ) {
        PathRenderState state = new PathRenderState();
        FactoryPanelSupportBehaviour sbe = ServerFactoryPanelBehaviour.linkAt(world, connection);
        boolean displayLinkMode = sbe != null && sbe.blockEntity instanceof DisplayLinkBlockEntity;
        boolean redstoneLinkMode = sbe != null && sbe.blockEntity instanceof RedstoneLinkBlockEntity;
        boolean pathReversed = sbe != null && !sbe.isOutput();
        int color;
        float yOffset;
        boolean dots;
        if (displayLinkMode) {
            // Display status
            color = 0x3C9852;
            dots = true;
            yOffset = 0;
        } else if (redstoneLinkMode) {
            // Link status
            color = pathReversed ? (behaviour.count == 0 ? 0x888898 : behaviour.satisfied ? 0xEF0000 : 0x580101) : (behaviour.redstonePowered ? 0xEF0000 : 0x580101);
            dots = false;
            yOffset = 0.5f;
        } else {
            // Regular ingredient status
            color = behaviour.getIngredientStatusColor();
            dots = false;
            yOffset = 1 + (behaviour.promisedSatisfied ? 1 : behaviour.satisfied ? 0 : 2);
            if (!behaviour.redstonePowered && !behaviour.waitingForNetwork && glow > 0 && !behaviour.satisfied) {
                float p = (1 - (1 - glow) * (1 - glow));
                boolean success = connection.success;
                color = Color.mixColors(color, success ? 0xEAF2EC : 0xE5654B, p);
                if (!behaviour.promisedSatisfied) {
                    yOffset += (success ? 1 : 2) * p;
                }
            }
        }
        state.shiftUV = !displayLinkMode && !redstoneLinkMode && !missingAddress && !behaviour.waitingForNetwork && !behaviour.satisfied && !behaviour.redstonePowered;
        state.color = color;
        float currentX = 0;
        float currentZ = 0;
        List<LineRenderData> lines = state.lines = new ArrayList<>();
        for (int i = 0, size = path.size(), end = size - 1; i < size; i++) {
            Direction direction = path.get(i);
            if (!pathReversed) {
                currentX += direction.getOffsetX() * .5f;
                currentZ += direction.getOffsetZ() * .5f;
            }
            Map<Direction, PartialModel> group = dots ? AllPartialModels.FACTORY_PANEL_DOTTED : (pathReversed ? i == end : i == 0) ? AllPartialModels.FACTORY_PANEL_ARROWS : AllPartialModels.FACTORY_PANEL_LINES;
            PartialModel partial = group.get(pathReversed ? direction : direction.getOpposite());
            SuperByteBuffer model = CachedBuffers.partial(partial, blockState);
            float currentY = (yOffset + (direction.getHorizontalQuarterTurns() % 2) * 0.125f) / 512f;
            lines.add(new LineRenderData(model, currentX, currentY, currentZ));
            if (pathReversed) {
                currentX += direction.getOffsetX() * .5f;
                currentZ += direction.getOffsetZ() * .5f;
            }
        }
        return state;
    }

    public static class FactoryPanelRenderState extends SmartRenderState {
        public RenderLayer cutout;
        public RenderLayer translucent1;
        public RenderLayer translucent2;
        public RenderLayer additive;
        public float xRot;
        public float yRot;
        public List<SingleFactoryPanelRenderState> panels;

        public void renderCutout(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (SingleFactoryPanelRenderState panel : panels) {
                panel.renderPaths(matricesEntry, vertexConsumer, xRot, yRot, lightmapCoordinates);
            }
        }

        public void renderTranslucent1(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (SingleFactoryPanelRenderState panel : panels) {
                panel.renderBulb(false, matricesEntry, vertexConsumer, xRot, yRot, lightmapCoordinates);
            }
        }

        public void renderTranslucent2(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (SingleFactoryPanelRenderState panel : panels) {
                panel.renderBulb(true, matricesEntry, vertexConsumer, xRot, yRot, lightmapCoordinates);
            }
        }

        public void renderAdditive(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (SingleFactoryPanelRenderState panel : panels) {
                panel.renderGlow(matricesEntry, vertexConsumer, xRot, yRot);
            }
        }
    }

    public static class SingleFactoryPanelRenderState {
        public float offsetX;
        public float offsetY;
        public List<PathRenderState> paths;
        public BulbRenderState bulb;

        public void renderPaths(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, float xRot, float yRot, int light) {
            if (paths != null) {
                for (PathRenderState path : paths) {
                    path.render(matricesEntry, vertexConsumer, xRot, yRot, offsetX, offsetY, light);
                }
            }
        }

        public void renderBulb(boolean glow, MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, float xRot, float yRot, int light) {
            if (bulb != null) {
                bulb.renderBulb(glow, matricesEntry, vertexConsumer, xRot, yRot, light);
            }
        }

        public void renderGlow(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, float xRot, float yRot) {
            if (bulb != null) {
                bulb.renderGlow(matricesEntry, vertexConsumer, xRot, yRot);
            }
        }
    }

    public static class BulbRenderState {
        public SuperByteBuffer model;
        public float offsetX;
        public float offsetY;
        public boolean glow;
        public int color;

        public void renderBulb(boolean glow, MatrixStack.Entry entry, VertexConsumer vertexConsumer, float xRot, float yRot, int light) {
            if (glow == this.glow) {
                model.rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST).rotateCentered(MathHelper.PI, Direction.UP)
                    .translate(offsetX, 0, offsetY).light(glow ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light)
                    .overlay(OverlayTexture.DEFAULT_UV).renderInto(entry, vertexConsumer);
            }
        }

        private void renderGlow(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float xRot, float yRot) {
            if (glow) {
                model.rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST).rotateCentered(MathHelper.PI, Direction.UP)
                    .translate(offsetX, 0, offsetY).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).color(color, color, color, 255)
                    .overlay(OverlayTexture.DEFAULT_UV).renderInto(entry, vertexConsumer);
            }
        }
    }

    public static class PathRenderState {
        public boolean shiftUV;
        public int color;
        public List<LineRenderData> lines;

        public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float xRot, float yRot, float offsetX, float offsetY, int light) {
            for (LineRenderData line : lines) {
                line.model.rotateCentered(yRot, Direction.UP).rotateCentered(xRot, Direction.EAST).rotateCentered(MathHelper.PI, Direction.UP)
                    .translate(offsetX, 0, offsetY).translate(line.x, line.y, line.z);
                if (shiftUV) {
                    line.model.shiftUV(AllSpriteShifts.FACTORY_PANEL_CONNECTIONS);
                }
                line.model.color(color).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(entry, vertexConsumer);
            }
        }
    }

    public record LineRenderData(SuperByteBuffer model, float x, float y, float z) {
    }
}
