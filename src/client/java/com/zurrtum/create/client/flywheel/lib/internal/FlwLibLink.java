package com.zurrtum.create.client.flywheel.lib.internal;

import com.zurrtum.create.client.flywheel.impl.FlwLibLinkImpl;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public interface FlwLibLink {
    FlwLibLink INSTANCE = new FlwLibLinkImpl();

    Logger getLogger();

    PoseTransformStack getPoseTransformStackOf(MatrixStack stack);

    Map<String, ModelPart> getModelPartChildren(ModelPart part);

    void compileModelPart(ModelPart part, MatrixStack.Entry pose, VertexConsumer consumer, int light, int overlay, int color);

    List<MatrixStack.Entry> getPoseStack(MatrixStack stack);

    boolean isIrisLoaded();

    boolean isOptifineInstalled();

    boolean isShaderPackInUse();

    boolean isRenderingShadowPass();
}
