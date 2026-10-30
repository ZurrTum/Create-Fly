package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.platform.Lighting;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3fc;

import java.util.EnumMap;
import java.util.Map;

public final class LevelUniforms extends UniformWriter {
    private static final int SIZE = 16 * 4 + 4 * 12;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);
    static final Map<Lighting.Entry, float[]> CACHE = new EnumMap<>(Lighting.Entry.class);

    public static float[] LIGHT_DIRECTION;

    private LevelUniforms() {
    }

    public static void update(Lighting.Entry type, Vector3fc light0Diffusion, Vector3fc light1Diffusion) {
        float[] diffusions = CACHE.computeIfAbsent(type, t -> new float[6]);
        diffusions[0] = light0Diffusion.x();
        diffusions[1] = light0Diffusion.y();
        diffusions[2] = light0Diffusion.z();
        diffusions[3] = light1Diffusion.x();
        diffusions[4] = light1Diffusion.y();
        diffusions[5] = light1Diffusion.z();
    }

    public static void set(Lighting.Entry type) {
        LIGHT_DIRECTION = CACHE.computeIfAbsent(type, t -> new float[6]);
    }

    public static void update(RenderContext context) {
        long ptr = BUFFER.ptr();

        LevelRenderState levelRenderState = context.levelRenderState();
        LevelInfoHolder levelInfoHolder = (LevelInfoHolder) levelRenderState;
        SkyRenderState skyRenderState = levelRenderState.skyRenderState;

        Vector3fc skyColor = skyRenderState.skyColor;
        if (skyColor != null) {
            ptr = writeVec4(ptr, skyColor.x(), skyColor.y(), skyColor.z(), 1.0f);
        } else {
            ptr = writeVec4(ptr, 0, 0, 0, 1.0f);
        }
        int cloudColor = levelRenderState.cloudColor;
        ptr = writeVec4(ptr, ARGB.redFloat(cloudColor), ARGB.greenFloat(cloudColor), ARGB.blueFloat(cloudColor), 1.0f);

        ptr = writeVec3(ptr, LIGHT_DIRECTION[0], LIGHT_DIRECTION[1], LIGHT_DIRECTION[2]);
        ptr = writeVec3(ptr, LIGHT_DIRECTION[3], LIGHT_DIRECTION[4], LIGHT_DIRECTION[5]);

        ptr = writeInt(ptr, levelInfoHolder.flywheel$levelDay());
        ptr = writeFloat(ptr, levelInfoHolder.flywheel$timeOfDay());

        ptr = writeInt(ptr, levelInfoHolder.flywheel$skyLight());

        ptr = writeFloat(ptr, skyRenderState.sunAngle);

        int moonPhase = skyRenderState.moonPhase.index();
        ptr = writeFloat(ptr, DimensionType.MOON_BRIGHTNESS_PER_PHASE[moonPhase]);
        ptr = writeInt(ptr, moonPhase);

        ptr = writeInt(ptr, levelInfoHolder.flywheel$raining());
        ptr = writeFloat(ptr, levelRenderState.weatherRenderState.intensity);
        ptr = writeInt(ptr, levelInfoHolder.flywheel$thundering());
        ptr = writeFloat(ptr, levelInfoHolder.flywheel$thunderLevel());

        ptr = writeFloat(ptr, levelInfoHolder.flywheel$skyDarken());

        ptr = writeInt(ptr, levelInfoHolder.flywheel$constantAmbientLight());

        ptr = writeInt(ptr, levelInfoHolder.flywheel$dimensionId());

        BUFFER.markDirty();
    }
}
