package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.util.List;

public class FactoryPanelModel extends WrapperBlockStateModel {
    public FactoryPanelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        model.collectParts(random, parts);
        boolean ponder = world instanceof PonderLevel;
        for (PanelSlot slot : PanelSlot.values()) {
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(
                world,
                new FactoryPanelPosition(pos, slot)
            );
            if (behaviour == null) {
                continue;
            }
            addPanel(random, parts, state, slot, behaviour, ponder);
        }
    }

    private static Vector3fc calcXYZ(Vector3fc position, double xOffset, double yOffset, float xRot, float yRot) {
        Vec3 vertex = new Vec3(position);
        vertex = vertex.add(xOffset, 0, yOffset);
        vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
        vertex = VecHelper.rotateCentered(vertex, xRot, Axis.X);
        vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);
        return vertex.toVector3f();
    }

    public void addPanel(
        RandomSource random,
        List<BlockStateModelPart> parts,
        BlockState state,
        PanelSlot slot,
        ServerFactoryPanelBehaviour behaviour,
        boolean ponder
    ) {
        PartialModel factoryPanel;
        if (behaviour.panelBE().restocker) {
            factoryPanel = behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL_RESTOCKER :
                AllPartialModels.FACTORY_PANEL_RESTOCKER_WITH_BULB;
        } else {
            factoryPanel =
                behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL : AllPartialModels.FACTORY_PANEL_WITH_BULB;
        }

        float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state) + 90;
        float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);
        double xOffset = slot.xOffset * 0.5;
        double yOffset = slot.yOffset * 0.5;

        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        factoryPanel.get().collectParts(random, modelParts);
        for (BlockStateModelPart part : modelParts) {
            parts.add(replacePart(part, xRot, yRot, xOffset, yOffset, ponder));
        }
    }

    private static BlockStateModelPart replacePart(
        BlockStateModelPart part,
        float xRot,
        float yRot,
        double xOffset,
        double yOffset,
        boolean ponder
    ) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (Direction direction : Iterate.directions) {
            for (BakedQuad bakedQuad : part.getQuads(direction)) {
                builder.addCulledFace(direction, replaceQuad(bakedQuad, xRot, yRot, xOffset, yOffset, ponder));
            }
        }
        for (BakedQuad bakedQuad : part.getQuads(null)) {
            builder.addUnculledFace(replaceQuad(bakedQuad, xRot, yRot, xOffset, yOffset, ponder));
        }
        return new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial());
    }

    private static BakedQuad replaceQuad(
        BakedQuad bakedQuad,
        float xRot,
        float yRot,
        double xOffset,
        double yOffset,
        boolean ponder
    ) {
        Vec3 quadNormal = Vec3.atLowerCornerOf(bakedQuad.direction().getUnitVec3i());
        quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
        quadNormal = VecHelper.rotate(quadNormal, xRot, Axis.X);
        quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);
        Direction newNormal = Direction.getNearest(
            (int) Math.round(quadNormal.x),
            (int) Math.round(quadNormal.y),
            (int) Math.round(quadNormal.z),
            null
        );
        MaterialInfo info = bakedQuad.materialInfo();
        if (ponder && info.shade()) {
            info = new MaterialInfo(
                info.sprite(),
                info.layer(),
                info.itemRenderType(),
                info.tintIndex(),
                false,
                info.lightEmission()
            );
        }
        return new BakedQuad(
            calcXYZ(bakedQuad.position0(), xOffset, yOffset, xRot, yRot),
            calcXYZ(bakedQuad.position1(), xOffset, yOffset, xRot, yRot),
            calcXYZ(bakedQuad.position2(), xOffset, yOffset, xRot, yRot),
            calcXYZ(bakedQuad.position3(), xOffset, yOffset, xRot, yRot),
            bakedQuad.packedUV0(),
            bakedQuad.packedUV1(),
            bakedQuad.packedUV2(),
            bakedQuad.packedUV3(),
            newNormal,
            info
        );
    }
}
