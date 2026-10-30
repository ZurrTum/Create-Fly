package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack.Pose;

public interface BufferPoseEmitter extends BufferEmitter {
    Pose getPose();
}
