package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.layout.FloatRepr;
import com.zurrtum.create.client.flywheel.api.layout.IntegerRepr;
import com.zurrtum.create.client.flywheel.api.layout.LayoutBuilder;
import com.zurrtum.create.client.flywheel.lib.util.ExtraMemoryOps;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import org.lwjgl.system.MemoryUtil;

public final class InstanceTypes {
    public static final InstanceType<TransformedInstance> TRANSFORMED = SimpleInstanceType.builder(TransformedInstance::new)
        .layout(LayoutBuilder.create().vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4).vector("overlay", IntegerRepr.SHORT, 2)
            .vector("light", FloatRepr.UNSIGNED_SHORT, 2).matrix("pose", FloatRepr.FLOAT, 4).build()).writer((ptr, instance) -> {
            MemoryUtil.memPutByte(ptr, instance.red);
            MemoryUtil.memPutByte(ptr + 1, instance.green);
            MemoryUtil.memPutByte(ptr + 2, instance.blue);
            MemoryUtil.memPutByte(ptr + 3, instance.alpha);
            ExtraMemoryOps.put2x16(ptr + 4, instance.overlay);
            ExtraMemoryOps.put2x16(ptr + 8, instance.light);
            ExtraMemoryOps.putMatrix4f(ptr + 12, instance.pose);
        }).vertexShader(ResourceUtil.rl("instance/transformed.vert")).cullShader(ResourceUtil.rl("instance/cull/transformed.glsl")).build();

    public static final InstanceType<PosedInstance> POSED = SimpleInstanceType.builder(PosedInstance::new)
        .layout(LayoutBuilder.create().vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4).vector("overlay", IntegerRepr.SHORT, 2)
            .vector("light", FloatRepr.UNSIGNED_SHORT, 2).matrix("pose", FloatRepr.FLOAT, 4).matrix("normal", FloatRepr.FLOAT, 3).build())
        .writer((ptr, instance) -> {
            MemoryUtil.memPutByte(ptr, instance.red);
            MemoryUtil.memPutByte(ptr + 1, instance.green);
            MemoryUtil.memPutByte(ptr + 2, instance.blue);
            MemoryUtil.memPutByte(ptr + 3, instance.alpha);
            ExtraMemoryOps.put2x16(ptr + 4, instance.overlay);
            ExtraMemoryOps.put2x16(ptr + 8, instance.light);
            ExtraMemoryOps.putMatrix4f(ptr + 12, instance.pose);
            ExtraMemoryOps.putMatrix3f(ptr + 76, instance.normal);
        }).vertexShader(ResourceUtil.rl("instance/posed.vert")).cullShader(ResourceUtil.rl("instance/cull/posed.glsl")).build();

    public static final InstanceType<OrientedInstance> ORIENTED = SimpleInstanceType.builder(OrientedInstance::new)
        .layout(LayoutBuilder.create().vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4).vector("overlay", IntegerRepr.SHORT, 2)
            .vector("light", FloatRepr.UNSIGNED_SHORT, 2).vector("position", FloatRepr.FLOAT, 3).vector("pivot", FloatRepr.FLOAT, 3)
            .vector("rotation", FloatRepr.FLOAT, 4).build()).writer((ptr, instance) -> {
            MemoryUtil.memPutByte(ptr, instance.red);
            MemoryUtil.memPutByte(ptr + 1, instance.green);
            MemoryUtil.memPutByte(ptr + 2, instance.blue);
            MemoryUtil.memPutByte(ptr + 3, instance.alpha);
            ExtraMemoryOps.put2x16(ptr + 4, instance.overlay);
            ExtraMemoryOps.put2x16(ptr + 8, instance.light);
            MemoryUtil.memPutFloat(ptr + 12, instance.posX);
            MemoryUtil.memPutFloat(ptr + 16, instance.posY);
            MemoryUtil.memPutFloat(ptr + 20, instance.posZ);
            MemoryUtil.memPutFloat(ptr + 24, instance.pivotX);
            MemoryUtil.memPutFloat(ptr + 28, instance.pivotY);
            MemoryUtil.memPutFloat(ptr + 32, instance.pivotZ);
            ExtraMemoryOps.putQuaternionf(ptr + 36, instance.rotation);
        }).vertexShader(ResourceUtil.rl("instance/oriented.vert")).cullShader(ResourceUtil.rl("instance/cull/oriented.glsl")).build();

    public static final InstanceType<ShadowInstance> SHADOW = SimpleInstanceType.builder(ShadowInstance::new)
        .layout(LayoutBuilder.create().vector("pos", FloatRepr.FLOAT, 3).vector("entityPosXZ", FloatRepr.FLOAT, 2).vector("size", FloatRepr.FLOAT, 2)
            .scalar("alpha", FloatRepr.FLOAT).scalar("radius", FloatRepr.FLOAT).build()).writer((ptr, instance) -> {
            MemoryUtil.memPutFloat(ptr, instance.x);
            MemoryUtil.memPutFloat(ptr + 4, instance.y);
            MemoryUtil.memPutFloat(ptr + 8, instance.z);
            MemoryUtil.memPutFloat(ptr + 12, instance.entityX);
            MemoryUtil.memPutFloat(ptr + 16, instance.entityZ);
            MemoryUtil.memPutFloat(ptr + 20, instance.sizeX);
            MemoryUtil.memPutFloat(ptr + 24, instance.sizeZ);
            MemoryUtil.memPutFloat(ptr + 28, instance.alpha);
            MemoryUtil.memPutFloat(ptr + 32, instance.radius);
        }).vertexShader(ResourceUtil.rl("instance/shadow.vert")).cullShader(ResourceUtil.rl("instance/cull/shadow.glsl")).build();

    private InstanceTypes() {
    }
}
