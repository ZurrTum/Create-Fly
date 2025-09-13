package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public final class LevelUniforms extends UniformWriter {
    private static final int SIZE = 16 * 4 + 4 * 12;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);
    static final Map<DiffuseLighting.Type, float[]> CACHE = new EnumMap<>(DiffuseLighting.Type.class);

    public static float[] LIGHT_DIRECTION;

    private LevelUniforms() {
    }

    public static void update(DiffuseLighting.Type type, Vector3f light0Diffusion, Vector3f light1Diffusion) {
        float[] diffusions = CACHE.computeIfAbsent(type, t -> new float[6]);
        diffusions[0] = light0Diffusion.x;
        diffusions[1] = light0Diffusion.y;
        diffusions[2] = light0Diffusion.z;
        diffusions[3] = light1Diffusion.x;
        diffusions[4] = light1Diffusion.y;
        diffusions[5] = light1Diffusion.z;
    }

    public static void set(DiffuseLighting.Type type) {
        LIGHT_DIRECTION = CACHE.computeIfAbsent(type, t -> new float[6]);
    }

    public static void update(RenderContext context) {
        long ptr = BUFFER.ptr();

        ClientWorld level = context.level();
        float partialTick = context.partialTick();

        int skyColor = level.getSkyColor(context.camera().getPos(), partialTick);
        int cloudColor = level.getCloudsColor(partialTick);
        ptr = writeVec4(ptr, ColorHelper.getRedFloat(skyColor), ColorHelper.getGreenFloat(skyColor), ColorHelper.getBlueFloat(skyColor), 1f);
        ptr = writeVec4(ptr, ColorHelper.getRedFloat(cloudColor), ColorHelper.getGreenFloat(cloudColor), ColorHelper.getBlueFloat(cloudColor), 1f);

        ptr = writeVec3(ptr, LIGHT_DIRECTION[0], LIGHT_DIRECTION[1], LIGHT_DIRECTION[2]);
        ptr = writeVec3(ptr, LIGHT_DIRECTION[3], LIGHT_DIRECTION[4], LIGHT_DIRECTION[5]);

        long dayTime = level.getTimeOfDay();
        long levelDay = dayTime / 24000L;
        float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
        ptr = writeInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
        ptr = writeFloat(ptr, timeOfDay);

        ptr = writeInt(ptr, level.getDimension().hasSkyLight() ? 1 : 0);

        ptr = writeFloat(ptr, level.getSkyAngleRadians(partialTick));

        ptr = writeFloat(ptr, level.getMoonSize());
        ptr = writeInt(ptr, level.getMoonPhase());

        ptr = writeInt(ptr, level.isRaining() ? 1 : 0);
        ptr = writeFloat(ptr, level.getRainGradient(partialTick));
        ptr = writeInt(ptr, level.isThundering() ? 1 : 0);
        ptr = writeFloat(ptr, level.getThunderGradient(partialTick));

        ptr = writeFloat(ptr, level.getSkyBrightness(partialTick));

        ptr = writeInt(ptr, level.getDimensionEffects().isDarkened() ? 1 : 0);

        // TODO: use defines for custom dimension ids
        int dimensionId;
        RegistryKey<World> dimension = level.getRegistryKey();
        if (World.OVERWORLD.equals(dimension)) {
            dimensionId = 0;
        } else if (World.NETHER.equals(dimension)) {
            dimensionId = 1;
        } else if (World.END.equals(dimension)) {
            dimensionId = 2;
        } else {
            dimensionId = -1;
        }
        ptr = writeInt(ptr, dimensionId);

        BUFFER.markDirty();
    }
}
