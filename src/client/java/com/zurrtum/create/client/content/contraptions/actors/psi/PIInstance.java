package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Consumer;

public class PIInstance {
    private final InstancerProvider instancerProvider;
    private final BlockState blockState;
    private final BlockPos instancePos;
    private final float angleX;
    private final float angleY;

    private boolean lit;
    TransformedInstance middle;
    TransformedInstance top;

    public PIInstance(InstancerProvider instancerProvider, BlockState blockState, BlockPos instancePos, boolean lit) {
        this.instancerProvider = instancerProvider;
        this.blockState = blockState;
        this.instancePos = instancePos;
        Direction facing = blockState.get(PortableStorageInterfaceBlock.FACING);
        angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
        angleY = AngleHelper.horizontalAngle(facing);
        this.lit = lit;

        middle = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit))
        ).createInstance();
        top = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableStorageInterfaceRenderer.getTopForState(blockState)))
            .createInstance();
    }

    public void beginFrame(float progress) {
        middle.setIdentityTransform().translate(instancePos).center().rotateYDegrees(angleY).rotateXDegrees(angleX).uncenter();

        top.setIdentityTransform().translate(instancePos).center().rotateYDegrees(angleY).rotateXDegrees(angleX).uncenter();

        middle.translate(0, progress * 0.5f + 0.375f, 0);
        top.translate(0, progress, 0);

        middle.setChanged();
        top.setChanged();
    }

    public void tick(boolean lit) {
        if (this.lit != lit) {
            this.lit = lit;
            instancerProvider.instancer(
                InstanceTypes.TRANSFORMED,
                Models.partial(PortableStorageInterfaceRenderer.getMiddleForState(blockState, lit))
            ).stealInstance(middle);
        }
    }

    public void remove() {
        middle.delete();
        top.delete();
    }

    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(middle);
        consumer.accept(top);
    }
}
