package com.zurrtum.create.client.content.redstone.deskBell;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class DeskBellVisual extends AbstractBlockEntityVisual<DeskBellBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private final OrientedInstance plunger;
    private final TransformedInstance bell;

    public DeskBellVisual(VisualizationContext ctx, DeskBellBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        plunger = instancerProvider.instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.DESK_BELL_PLUNGER)
        ).createInstance();
        Direction facing = blockState.getValue(DeskBellBlock.FACING);
        plunger.rotateY(Radians.horizontal(facing)).rotateX(Radians.verticalPlus90(facing));
        bell = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.DESK_BELL_BELL)
        ).createInstance();
        transformModels(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        if (!blockEntity.animation.settled()) {
            transformModels(ctx.partialTick());
        }
    }

    private void transformModels(float partialTick) {
        float p = blockEntity.animation.getValue(partialTick);
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = blockState.getValue(DeskBellBlock.FACING);
        float f = (float) (1 - 4 * Math.pow(Math.max(p - 0.5, 0) - 0.5, 2));
        float f2 = (float) Math.pow(p, 1.25f);
        float plungerOffset = f * -0.046875f;
        BlockPos blockPos = getVisualPosition();
        Vec3i facingVec = facing.getUnitVec3i();
        float x = blockPos.getX() + plungerOffset * facingVec.getX();
        float y = blockPos.getY() + plungerOffset * facingVec.getY();
        float z = blockPos.getZ() + plungerOffset * facingVec.getZ();
        plunger.position(x, y, z).setChanged();
        bell.setIdentityTransform().translate(getVisualPosition()).center().rotateY(Radians.horizontal(facing))
            .rotateX(Radians.verticalPlus90(facing)).translate(0, -0.0625, 0)
            .rotateXDegrees(f2 * 8 * Mth.sin(p * Mth.PI * 4 + blockEntity.animationOffset))
            .rotateZDegrees(f2 * 8 * Mth.cos(p * Mth.PI * 4 + blockEntity.animationOffset)).translate(0, 0.0625, 0)
            .uncenter().setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(plunger);
        consumer.accept(bell);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(plunger, bell);
    }

    @Override
    protected void _delete() {
        plunger.delete();
        bell.delete();
    }
}
