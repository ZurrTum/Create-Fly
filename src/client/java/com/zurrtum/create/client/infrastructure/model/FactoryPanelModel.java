package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelModel extends WrapperBlockStateModel {
    public FactoryPanelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        model.collectParts(random, parts);
        boolean ponder = world instanceof PonderLevel;
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

        float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
        float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

        SimpleModelWrapper model = factoryPanel.get();
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad bakedQuad : model.quads().getAll()) {
            int[] vertices = bakedQuad.vertices();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            Vec3 quadNormal = Vec3.atLowerCornerOf(bakedQuad.direction().getUnitVec3i());
            quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
            quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Axis.X);
            quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);

                vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
                vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
                vertex = VecHelper.rotateCentered(vertex, xRot + 90, Axis.X);
                vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);

                BakedQuadHelper.setXYZ(transformedVertices, i, vertex);
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(0, 1, 0));
            }

            Direction newNormal = Direction.getNearest(
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
            builder.addUnculledFace(quad);
        }
        parts.add(new SimpleModelWrapper(builder.build(), model.useAmbientOcclusion(), model.particleIcon()));
    }
}
