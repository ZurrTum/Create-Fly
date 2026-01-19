package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FanProcessingType {
    @Nullable
    static FanProcessingType parse(String str) {
        return CreateRegistries.FAN_PROCESSING_TYPE.getValue(Identifier.tryParse(str));
    }

    @Nullable
    static FanProcessingType getAt(Level level, BlockPos pos) {
        for (FanProcessingType type : FanProcessingTypeRegistry.SORTED_TYPES_VIEW) {
            if (type.isValidAt(level, pos)) {
                return type;
            }
        }
        return null;
    }

    boolean isValidAt(Level level, BlockPos pos);

    int getPriority();

    boolean canProcess(ItemStack stack, Level level);

    @Nullable List<ItemStack> process(ItemStack stack, Level level);

    void spawnProcessingParticles(Level level, Vec3 pos);

    void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random);

    void affectEntity(Entity entity, Level level);

    interface AirFlowParticleAccess {
        void setColor(int color);

        void setAlpha(float alpha);

        void spawnExtraParticle(ParticleOptions options, float speedMultiplier);
    }
}
