package com.zurrtum.create.content.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPotatoProjectileRenderModes {
    public static void register() {
        register("billboard", Billboard.CODEC);
        register("tumble", Tumble.CODEC);
        register("toward_motion", TowardMotion.CODEC);
        register("stuck_to_entity", StuckToEntity.CODEC);
    }

    private static void register(String name, MapCodec<? extends PotatoProjectileRenderMode> codec) {
        Registry.register(CreateRegistries.POTATO_PROJECTILE_RENDER_MODE, Identifier.of(MOD_ID, name), codec);
    }

    public enum Billboard implements PotatoProjectileRenderMode {
        INSTANCE;

        public static final MapCodec<Billboard> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<? extends PotatoProjectileRenderMode> codec() {
            return CODEC;
        }
    }

    public enum Tumble implements PotatoProjectileRenderMode {
        INSTANCE;

        public static final MapCodec<Tumble> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<? extends PotatoProjectileRenderMode> codec() {
            return CODEC;
        }
    }

    public record TowardMotion(int spriteAngleOffset, float spin) implements PotatoProjectileRenderMode {
        public static final MapCodec<TowardMotion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf(
                "sprite_angle_offset").forGetter(i -> i.spriteAngleOffset), Codec.FLOAT.fieldOf("spin").forGetter(i -> i.spin)
        ).apply(instance, TowardMotion::new));

        @Override
        public MapCodec<? extends PotatoProjectileRenderMode> codec() {
            return CODEC;
        }
    }

    public record StuckToEntity(Vec3d offset) implements PotatoProjectileRenderMode {
        public static final MapCodec<StuckToEntity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3d.CODEC.fieldOf("offset")
            .forGetter(i -> i.offset)).apply(instance, StuckToEntity::new));

        @Override
        public MapCodec<? extends PotatoProjectileRenderMode> codec() {
            return CODEC;
        }
    }

    private static int entityRandom(Entity entity, int maxValue) {
        return (System.identityHashCode(entity) * 31) % maxValue;
    }
}
