package com.zurrtum.create.client;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.content.equipment.potatoCannon.PotatoProjectileTransform;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Billboard;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.StuckToEntity;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.TowardMotion;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Tumble;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllPotatoProjectileTransforms {
    public static final Map<Class<? extends PotatoProjectileRenderMode>, PotatoProjectileTransform<?>> ALL = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends PotatoProjectileRenderMode> PotatoProjectileTransform<T> get(T renderMode) {
        return (PotatoProjectileTransform<T>) ALL.get(renderMode.getClass());
    }

    private static <T extends PotatoProjectileRenderMode> void register(Class<T> renderMode, PotatoProjectileTransform<T> transform) {
        ALL.put(renderMode, transform);
    }

    public static void register() {
        register(
            Billboard.class, (mode, ms, state) -> {
                Vec3 p1 = state.camera.getEyePosition(state.pt);
                Vec3 diff = state.box.getCenter().subtract(p1);

                TransformStack.of(ms).rotateYDegrees(AngleHelper.deg(Mth.atan2(diff.x, diff.z)) + 180)
                    .rotateXDegrees(AngleHelper.deg(Mth.atan2(diff.y, Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
            }
        );
        register(
            Tumble.class, (mode, ms, state) -> {
                get(Billboard.INSTANCE).transform(Billboard.INSTANCE, ms, state);
                TransformStack.of(ms).rotateZDegrees(state.ageInTicks * 2 * (state.hash % 16)).rotateXDegrees(state.ageInTicks * (state.hash % 32));
            }
        );
        register(
            TowardMotion.class, (mode, ms, state) -> {
                Vec3 diff = state.velocity;
                TransformStack.of(ms).rotateYDegrees(AngleHelper.deg(Mth.atan2(diff.x, diff.z)))
                    .rotateXDegrees(270 + AngleHelper.deg(Mth.atan2(diff.y, -Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
                TransformStack.of(ms).rotateYDegrees(state.ageInTicks * 20 * mode.spin() + (state.hash % 360))
                    .rotateZDegrees(-mode.spriteAngleOffset());
            }
        );
        register(
            StuckToEntity.class, (mode, ms, state) -> {
                Vec3 offset = mode.offset();
                TransformStack.of(ms).rotateYDegrees(AngleHelper.deg(Mth.atan2(offset.x, offset.z)));
            }
        );
    }
}
