package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FanProcessingType {
    @Nullable
    static FanProcessingType parse(String str) {
        return CreateRegistries.FAN_PROCESSING_TYPE.get(Identifier.tryParse(str));
    }

    @Nullable
    static FanProcessingType getAt(World level, BlockPos pos) {
        for (FanProcessingType type : FanProcessingTypeRegistry.SORTED_TYPES_VIEW) {
            if (type.isValidAt(level, pos)) {
                return type;
            }
        }
        return null;
    }

    boolean isValidAt(World level, BlockPos pos);

    int getPriority();

    boolean canProcess(ItemStack stack, World level);

    @Nullable List<ItemStack> process(ItemStack stack, World level);

    void spawnProcessingParticles(World level, Vec3d pos);

    void morphAirFlow(AirFlowParticleAccess particleAccess, Random random);

    void affectEntity(Entity entity, World level);

    interface AirFlowParticleAccess {
        void setColor(int color);

        void setAlpha(float alpha);

        void spawnExtraParticle(ParticleEffect options, float speedMultiplier);
    }
}
