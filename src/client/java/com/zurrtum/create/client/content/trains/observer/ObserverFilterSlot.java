package com.zurrtum.create.client.content.trains.observer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ObserverFilterSlot extends ValueBoxTransform {
    @Override
    public void rotate(BlockState state, PoseStack ms) {
        TransformStack.of(ms).rotateXDegrees(90);
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        return new Vec3(0.5, 15.5 / 16d, 0.5);
    }
}
