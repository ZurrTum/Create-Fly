package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.Arrays;
import java.util.List;

public class FactoryPanelModel extends WrapperBlockStateModel {
    public FactoryPanelModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        model.addParts(random, parts);
        boolean ponder = false;//TODO world instanceof PonderLevel;
        for (PanelSlot slot : PanelSlot.values()) {
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(world, new FactoryPanelPosition(pos, slot));
            if (behaviour == null)
                continue;
            addPanel(parts, state, slot, behaviour, ponder);
        }
    }

    public void addPanel(List<BlockModelPart> parts, BlockState state, PanelSlot slot, ServerFactoryPanelBehaviour behaviour, boolean ponder) {
        PartialModel factoryPanel;
        if (behaviour.panelBE().restocker) {
            factoryPanel = behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL_RESTOCKER : AllPartialModels.FACTORY_PANEL_RESTOCKER_WITH_BULB;
        } else {
            factoryPanel = behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL : AllPartialModels.FACTORY_PANEL_WITH_BULB;
        }

        float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(state);
        float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(state);

        GeometryBakedModel model = factoryPanel.get();
        BakedGeometry.Builder builder = new BakedGeometry.Builder();
        for (BakedQuad bakedQuad : model.quads().getAllQuads()) {
            int[] vertices = bakedQuad.vertexData();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            Vec3d quadNormal = Vec3d.of(bakedQuad.face().getVector());
            quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
            quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Axis.X);
            quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3d vertex = BakedQuadHelper.getXYZ(vertices, i);

                vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
                vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
                vertex = VecHelper.rotateCentered(vertex, xRot + 90, Axis.X);
                vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);

                BakedQuadHelper.setXYZ(transformedVertices, i, vertex);
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3d(0, 1, 0));
            }

            Direction newNormal = Direction.fromVector(
                (int) Math.round(quadNormal.x),
                (int) Math.round(quadNormal.y),
                (int) Math.round(quadNormal.z),
                null
            );
            BakedQuad quad = new BakedQuad(
                transformedVertices,
                bakedQuad.tintIndex(),
                newNormal,
                bakedQuad.sprite(),
                !ponder && bakedQuad.shade(),
                bakedQuad.lightEmission()
            );
            NormalsBakedQuad.markNormals(quad);
            builder.add(quad);
        }
        parts.add(new GeometryBakedModel(builder.build(), model.useAmbientOcclusion(), model.particleSprite()));
    }
}
