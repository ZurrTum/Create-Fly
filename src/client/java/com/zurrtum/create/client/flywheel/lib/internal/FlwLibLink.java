package com.zurrtum.create.client.flywheel.lib.internal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.impl.FlwLibLinkImpl;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;

public interface FlwLibLink {
    FlwLibLink INSTANCE = new FlwLibLinkImpl();

    Logger getLogger();

    PoseTransformStack getPoseTransformStackOf(PoseStack stack);

    Map<String, ModelPart> getModelPartChildren(ModelPart part);

    void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color);

    List<PoseStack.Pose> getPoseStack(PoseStack stack);

    boolean isIrisLoaded();

    boolean isOptifineInstalled();

    boolean isShaderPackInUse();

    boolean isRenderingShadowPass();
}
