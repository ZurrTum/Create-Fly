package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.platform.Lighting;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public final class LevelUniforms extends UniformWriter {
    private static final int SIZE = 16 * 4 + 4 * 12;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);
    static final Map<Lighting.Entry, float[]> CACHE = new EnumMap<>(Lighting.Entry.class);

    public static float[] LIGHT_DIRECTION;

    private LevelUniforms() {
    }

    public static void update(Lighting.Entry type, Vector3f light0Diffusion, Vector3f light1Diffusion) {
        float[] diffusions = CACHE.computeIfAbsent(type, t -> new float[6]);
        diffusions[0] = light0Diffusion.x;
        diffusions[1] = light0Diffusion.y;
        diffusions[2] = light0Diffusion.z;
        diffusions[3] = light1Diffusion.x;
        diffusions[4] = light1Diffusion.y;
        diffusions[5] = light1Diffusion.z;
    }

    public static void set(Lighting.Entry type) {
        LIGHT_DIRECTION = CACHE.computeIfAbsent(type, t -> new float[6]);
    }

    public static void update(RenderContext context) {
        long ptr = BUFFER.ptr();

        ClientLevel level = context.level();
        float partialTick = context.partialTick();

        Camera camera = context.camera();
        EnvironmentAttributeProbe attributeProbe = camera.attributeProbe();
        int skyColor = attributeProbe.getValue(EnvironmentAttributes.SKY_COLOR, partialTick);
        int cloudColor = camera.attributeProbe().getValue(EnvironmentAttributes.CLOUD_COLOR, partialTick);
        ptr = writeVec4(ptr, ARGB.redFloat(skyColor), ARGB.greenFloat(skyColor), ARGB.blueFloat(skyColor), 1f);
        ptr = writeVec4(ptr, ARGB.redFloat(cloudColor), ARGB.greenFloat(cloudColor), ARGB.blueFloat(cloudColor), 1f);

        ptr = writeVec3(ptr, LIGHT_DIRECTION[0], LIGHT_DIRECTION[1], LIGHT_DIRECTION[2]);
        ptr = writeVec3(ptr, LIGHT_DIRECTION[3], LIGHT_DIRECTION[4], LIGHT_DIRECTION[5]);

        long dayTime = level.getDayTime();
        long levelDay = dayTime / 24000L;
        float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
        ptr = writeInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
        ptr = writeFloat(ptr, timeOfDay);

        ptr = writeInt(ptr, level.dimensionType().hasSkyLight() ? 1 : 0);

        ptr = writeFloat(ptr, attributeProbe.getValue(EnvironmentAttributes.SUN_ANGLE, partialTick) * (float) (Math.PI / 180.0));

        MoonPhase moonPhase = attributeProbe.getValue(EnvironmentAttributes.MOON_PHASE, partialTick);
        ptr = writeFloat(ptr, DimensionType.MOON_BRIGHTNESS_PER_PHASE[moonPhase.index()]);
        ptr = writeInt(ptr, moonPhase.index());

        ptr = writeInt(ptr, level.isRaining() ? 1 : 0);
        ptr = writeFloat(ptr, level.getRainLevel(partialTick));
        ptr = writeInt(ptr, level.isThundering() ? 1 : 0);
        ptr = writeFloat(ptr, level.getThunderLevel(partialTick));

        ptr = writeFloat(ptr, level.getSkyDarken());

        ptr = writeInt(ptr, level.dimensionType().cardinalLightType() == DimensionType.CardinalLightType.NETHER ? 1 : 0);

        // TODO: use defines for custom dimension ids
        int dimensionId;
        ResourceKey<Level> dimension = level.dimension();
        if (Level.OVERWORLD.equals(dimension)) {
            dimensionId = 0;
        } else if (Level.NETHER.equals(dimension)) {
            dimensionId = 1;
        } else if (Level.END.equals(dimension)) {
            dimensionId = 2;
        } else {
            dimensionId = -1;
        }
        ptr = writeInt(ptr, dimensionId);

        BUFFER.markDirty();
    }
}
