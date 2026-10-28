package com.zurrtum.create.client.content.logistics.crate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CrateSlot extends ValueBoxTransform {
    @Override
    public void rotate(BlockState state, PoseStack ms) {
        ms.rotate(Axis.XP, Radians.DEG_90);
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        return new Vec3(0.5, 13.5 / 16.0d, 0.5);
    }
}
