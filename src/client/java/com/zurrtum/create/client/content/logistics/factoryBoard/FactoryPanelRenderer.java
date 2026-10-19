package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelRenderer.FactoryPanelRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.content.logistics.factoryBoard.*;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getEastRadiansRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRadiansRotateAngle;

public class FactoryPanelRenderer implements BlockEntityRenderer<FactoryPanelBlockEntity, FactoryPanelRenderState> {
    public static final Quaternionfc UP_ANGLE = new Quaternionf().setAngleAxis(Mth.PI, 0, 1, 0);
    protected final ItemModelResolver itemModelManager;
    protected final TextureAtlasSprite sprite;

    public FactoryPanelRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
        sprite = context.sprites().get(Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(
            MOD_ID,
            "factory_panel_connections_animated"
        )));
    }

    @Override
    public FactoryPanelRenderState createRenderState() {
        return new FactoryPanelRenderState();
    }

    @Override
    public void extractRenderState(
        FactoryPanelBlockEntity be,
        FactoryPanelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        BlockPos blockPos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        int lightCoords = SmartBlockEntityRenderer.getLightCoords(level, blockPos);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
        );
        List<SingleFactoryPanelRenderState> panels = new ArrayList<>();
        boolean activeSprite = true;
        for (ServerFactoryPanelBehaviour behaviour : be.panels.values()) {
            if (behaviour.isActive()) {
                boolean bulb = behaviour.getAmount() > 0;
                boolean target = !behaviour.targetedBy.isEmpty() || !behaviour.targetedByLinks.isEmpty();
                if (!target && !bulb) {
                    continue;
                }
                SingleFactoryPanelRenderState panel = new SingleFactoryPanelRenderState();
                boolean missingAddress = behaviour.isMissingAddress();
                panel.offsetX = behaviour.slot.xOffset * 0.5f;
                panel.offsetY = behaviour.slot.yOffset * 0.5f;
                float glow = behaviour.bulb.getValue(tickProgress);
                CardinalLighting cardinalLighting =
                    level instanceof BlockAndTintGetter getter ? getter.cardinalLighting() : null;
                if (target) {
                    List<List<LineRenderData>> paths = panel.paths = new ArrayList<>();
                    FactoryPanelPosition to = behaviour.getPanelPosition();
                    FactoryPanelBehaviour fromBehaviour = be.getBehaviour(FactoryPanelBehaviour.getTypeForSlot(behaviour.slot));
                    Vec3 start = fromBehaviour != null ? fromBehaviour.getSlotPositioning().getLocalOffset(blockState)
                        .add(Vec3.atLowerCornerOf(blockPos)) : Vec3.ZERO;
                    for (FactoryPanelConnection connection : behaviour.targetedBy.values()) {
                        List<Direction> path = connection.getPath(level, blockState, to, start);
                        if (path.isEmpty()) {
                            continue;
                        }
                        paths.add(getPathRenderState(
                            behaviour,
                            connection,
                            path,
                            level,
                            cardinalLighting,
                            lightCoords,
                            blockState,
                            missingAddress,
                            glow
                        ));
                    }
                    for (FactoryPanelConnection connection : behaviour.targetedByLinks.values()) {
                        List<Direction> path = connection.getPath(level, blockState, to, start);
                        if (path.isEmpty()) {
                            continue;
                        }
                        paths.add(getPathRenderState(
                            behaviour,
                            connection,
                            path,
                            level,
                            cardinalLighting,
                            lightCoords,
                            blockState,
                            missingAddress,
                            glow
                        ));
                    }
                    if (activeSprite && !paths.isEmpty()) {
                        QuadRenderHelper.markSpriteActive(sprite);
                        activeSprite = false;
                    }
                }
                if (bulb) {
                    panel.bulb = getBulbRenderState(
                        behaviour,
                        cardinalLighting,
                        lightCoords,
                        blockState,
                        missingAddress,
                        glow
                    );
                }
                panels.add(panel);
            }
        }
        if (!panels.isEmpty()) {
            state.xRot = getEastRadiansRotateAngle(FactoryPanelBlock.getXRot(blockState) + Mth.PI / 2);
            state.yRot = getUpRadiansRotateAngle(FactoryPanelBlock.getYRot(blockState));
            state.panels = panels;
        }
        if (state.filter != null || state.panels != null) {
            state.blockPos = blockPos;
            state.blockState = blockState;
            state.blockEntityType = be.getType();
            state.lightCoords = lightCoords;
        }
    }

    @Override
    public void submit(
        FactoryPanelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.panels != null) {
            if (state.yRot != null) {
                matrices.rotateAround(state.yRot, 0.5f, 0.5f, 0.5f);
            }
            if (state.xRot != null) {
                matrices.rotateAround(state.xRot, 0.5f, 0.5f, 0.5f);
            }
            matrices.rotateAround(UP_ANGLE, 0.5f, 0.5f, 0.5f);
            for (SingleFactoryPanelRenderState panel : state.panels) {
                matrices.pushPose();
                matrices.translate(panel.offsetX, 0, panel.offsetY);
                if (panel.bulb != null) {
                    panel.bulb.submit(matrices, queue);
                }
                if (panel.paths != null) {
                    matrices.translate(0.25f, 0, 0.25f);
                    for (List<LineRenderData> path : panel.paths) {
                        for (LineRenderData line : path) {
                            matrices.pushPose();
                            matrices.translate(line.x, line.y, line.z);
                            line.model.submit(matrices, queue);
                            matrices.popPose();
                        }
                    }
                }
                matrices.popPose();
            }
        }
    }

    public static BulbRenderState getBulbRenderState(
        ServerFactoryPanelBehaviour behaviour,
        @Nullable CardinalLighting cardinalLighting,
        int lightCoords,
        BlockState blockState,
        boolean missingAddress,
        float glow
    ) {
        BulbRenderState state = new BulbRenderState();
        PartialModel partial = behaviour.redstonePowered || missingAddress ? AllPartialModels.FACTORY_PANEL_RED_LIGHT :
            AllPartialModels.FACTORY_PANEL_LIGHT;
        SuperByteBuffer model = CachedBuffers.partial(partial, blockState);
        if (glow < 0.125f) {
            state.model = model.cardinalLighting(cardinalLighting).light(lightCoords).extractRenderState();
            return state;
        }
        state.model = model.cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
        glow = (float) (1 - 2 * Math.pow(glow - 0.75f, 2));
        glow = Mth.clamp(glow, -1, 1);
        int color = (int) (200 * glow);
        state.glow = model.cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT)
            .color(color, color, color, 255).extractRenderState();
        return state;
    }

    public static List<LineRenderData> getPathRenderState(
        ServerFactoryPanelBehaviour behaviour,
        FactoryPanelConnection connection,
        List<Direction> path,
        Level world,
        @Nullable CardinalLighting cardinalLighting,
        int lightCoords,
        BlockState blockState,
        boolean missingAddress,
        float glow
    ) {
        FactoryPanelSupportBehaviour sbe = ServerFactoryPanelBehaviour.linkAt(world, connection);
        boolean displayLinkMode, redstoneLinkMode, pathReversed;
        if (sbe != null) {
            displayLinkMode = sbe.blockEntity instanceof DisplayLinkBlockEntity;
            redstoneLinkMode = sbe.blockEntity instanceof RedstoneLinkBlockEntity;
            pathReversed = !sbe.isOutput();
        } else {
            displayLinkMode = false;
            redstoneLinkMode = false;
            pathReversed = false;
        }
        int color;
        float yOffset;
        boolean dots;
        if (displayLinkMode) {
            // Display status
            color = 0xFF3C9852;
            dots = true;
            yOffset = 0;
        } else if (redstoneLinkMode) {
            // Link status
            color = pathReversed ? behaviour.count == 0 ? 0xFF888898 : behaviour.satisfied ? 0xFFEF0000 : 0xFF580101 :
                behaviour.redstonePowered ? 0xFFEF0000 : 0xFF580101;
            dots = false;
            yOffset = 0.5f;
        } else {
            // Regular ingredient status
            color = behaviour.getIngredientStatusColor();
            dots = false;
            yOffset = 1 + (behaviour.promisedSatisfied ? 1 : behaviour.satisfied ? 0 : 2);
            if (!behaviour.redstonePowered && !behaviour.waitingForNetwork && glow > 0 && !behaviour.satisfied) {
                float p = 1 - (1 - glow) * (1 - glow);
                boolean success = connection.success;
                color = Color.mixColors(color, success ? 0xFFEAF2EC : 0xFFE5654B, p);
                if (!behaviour.promisedSatisfied) {
                    yOffset += (success ? 1 : 2) * p;
                }
            }
        }
        boolean shiftUV = !displayLinkMode && !redstoneLinkMode && !missingAddress && !behaviour.waitingForNetwork && !behaviour.satisfied && !behaviour.redstonePowered;
        float currentX = 0;
        float currentZ = 0;
        List<LineRenderData> lines = new ArrayList<>();
        for (int i = 0, size = path.size(), end = size - 1; i < size; i++) {
            Direction direction = path.get(i);
            if (!pathReversed) {
                currentX += direction.getStepX() * 0.5f;
                currentZ += direction.getStepZ() * 0.5f;
            }
            Map<Direction, PartialModel> group = dots ? AllPartialModels.FACTORY_PANEL_DOTTED :
                (pathReversed ? i == end : i == 0) ? AllPartialModels.FACTORY_PANEL_ARROWS :
                    AllPartialModels.FACTORY_PANEL_LINES;
            PartialModel partial = group.get(pathReversed ? direction : direction.getOpposite());
            SuperByteBuffer model = CachedBuffers.partial(partial, blockState).cardinalLighting(cardinalLighting)
                .light(lightCoords).color(color);
            if (shiftUV) {
                model.shiftUV(AllSpriteShifts.FACTORY_PANEL_CONNECTIONS);
            }
            float currentY = (yOffset + (direction.get2DDataValue() % 2) * 0.125f) / 512.0f;
            lines.add(new LineRenderData(model.extractRenderState(), currentX, currentY, currentZ));
            if (pathReversed) {
                currentX += direction.getStepX() * 0.5f;
                currentZ += direction.getStepZ() * 0.5f;
            }
        }
        return lines;
    }

    public static class FactoryPanelRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable Quaternionf xRot;
        public @Nullable Quaternionf yRot;
        public @Nullable List<SingleFactoryPanelRenderState> panels;
    }

    public static class SingleFactoryPanelRenderState {
        public float offsetX;
        public float offsetY;
        public @Nullable List<List<LineRenderData>> paths;
        public @Nullable BulbRenderState bulb;
    }

    public static class BulbRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable SuperByteBufferRenderState glow;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            model.submit(matrices, queue);
            if (glow != null) {
                glow.submit(CreateRenderTypes.additive(), matrices, queue.order(1));
            }
        }
    }

    public record LineRenderData(SuperByteBufferRenderState model, float x, float y, float z) {
    }
}
