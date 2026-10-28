package com.zurrtum.create.client.content.kinetics.saw;

import com.google.common.base.Suppliers;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class SawVisual extends KineticBlockEntityVisual<SawBlockEntity> implements SimpleDynamicVisual {
    private static final Supplier<TextureAtlasSprite> SAW_SPRITE = Suppliers.memoize(() -> Minecraft.getInstance()
        .getAtlasManager().get(Sheets.BLOCKS_MAPPER.defaultNamespaceApply("stonecutter_saw")));
    protected final RotatingInstance rotatingModel;
    private @Nullable TextureAtlasSprite sprite;
    private OrientedInstance saw;

    public SawVisual(VisualizationContext context, SawBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        rotatingModel = shaft(instancerProvider, blockState).setup(blockEntity).setPosition(getVisualPosition());
        rotatingModel.setChanged();
        updateSaw(blockState, blockEntity.getSpeed());
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(FACING)) {
            case UP -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 0);
            case DOWN -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
            case WEST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 0, 0);
            case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 0, 0);
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 0);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 0, 1);
        }
    }

    public static RotatingInstance shaft(InstancerProvider instancerProvider, BlockState state) {
        Direction facing = state.getValue(FACING);
        Axis axis = facing.getAxis();
        // We could change this to return either an Oriented- or SingleAxisRotatingVisual
        if (axis.isHorizontal()) {
            Direction align = facing.getOpposite();
            return instancerProvider.instancer(
                AllInstanceTypes.ROTATING,
                Models.chunkPartial(AllPartialModels.SHAFT_HALF)
            ).createInstance().rotateTo(0, 0, 1, align.getStepX(), align.getStepY(), align.getStepZ());
        }
        return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.chunkPartial(AllPartialModels.SHAFT))
            .createInstance().rotateToFace(state.getValue(AXIS_ALONG_FIRST_COORDINATE) ? Axis.X : Axis.Z);
    }

    @Override
    public void update(float pt) {
        BlockState blockState = blockEntity.getBlockState();
        float speed = blockEntity.getSpeed();
        rotatingModel.setup(blockEntity, rotationAxis(blockState), speed).setChanged();
        saw.delete();
        updateSaw(blockState, speed);
    }

    @Override
    public void beginFrame(Context ctx) {
        if (sprite != null) {
            QuadRenderHelper.markSpriteActive(sprite);
        }
    }

    private void updateSaw(BlockState blockState, float speed) {
        Direction facing = blockState.getValue(FACING);
        Axis axis = facing.getAxis();
        boolean rotate = false;
        PartialModel partial;
        if (axis == Axis.Y) {
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE;
                sprite = SAW_SPRITE.get();
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_REVERSED;
                sprite = SAW_SPRITE.get();
            } else {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE;
                sprite = null;
            }
            rotate = blockState.getValue(AXIS_ALONG_FIRST_COORDINATE);
        } else {
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE;
                sprite = SAW_SPRITE.get();
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_REVERSED;
                sprite = SAW_SPRITE.get();
            } else {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE;
                sprite = null;
            }
        }
        saw = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.chunkPartial(partial)).createInstance()
            .position(getVisualPosition());
        if (rotate) {
            saw.rotateY(Radians.DEG_90);
        }
        saw.rotateY(Radians.horizontal(facing)).rotateX(Radians.vertical(facing)).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel, saw);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
        saw.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(rotatingModel);
        consumer.accept(saw);
    }
}
