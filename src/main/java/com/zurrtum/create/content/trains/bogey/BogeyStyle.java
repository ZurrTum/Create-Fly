package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllSoundEvents;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class BogeyStyle {
    public final Identifier id;
    public final Identifier cycleGroup;
    public final Component displayName;
    public final Supplier<SoundEvent> soundEvent;
    public final ParticleOptions contactParticle;
    public final ParticleOptions smokeParticle;
    public final CompoundTag defaultData;
    private final Map<BogeySize, AbstractBogeyBlock<?>> sizes;

    public BogeyStyle(
        Identifier id,
        Identifier cycleGroup,
        Component displayName,
        Supplier<SoundEvent> soundEvent,
        ParticleOptions contactParticle,
        ParticleOptions smokeParticle,
        CompoundTag defaultData,
        Map<BogeySize, AbstractBogeyBlock<?>> sizes
    ) {
        this.id = id;
        this.cycleGroup = cycleGroup;
        this.displayName = displayName;
        this.soundEvent = soundEvent;
        this.contactParticle = contactParticle;
        this.smokeParticle = smokeParticle;
        this.defaultData = defaultData;
        this.sizes = sizes;
    }

    public Map<Identifier, BogeyStyle> getCycleGroup() {
        return AllBogeyStyles.getCycleGroup(cycleGroup);
    }

    public Set<BogeySize> validSizes() {
        return sizes.keySet();
    }

    public AbstractBogeyBlock<?> getBlockForSize(BogeySize size) {
        return sizes.get(size);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AbstractBogeyBlock<?> getNextBlock(BogeySize currentSize) {
        return Stream.iterate(currentSize.nextBySize(), BogeySize::nextBySize).filter(sizes::containsKey).findFirst().map(this::getBlockForSize)
            .orElse((AbstractBogeyBlock) getBlockForSize(currentSize));
    }

    public static class Builder {
        protected final Identifier id;
        protected final Identifier cycleGroup;
        protected final Map<BogeySize, AbstractBogeyBlock<?>> sizes = new LinkedHashMap<>();

        protected Component displayName = Component.translatable("create.bogey.style.invalid");
        protected Supplier<SoundEvent> soundEvent = AllSoundEvents.TRAIN2::getMainEvent;
        protected ParticleOptions contactParticle = ParticleTypes.CRIT;
        protected ParticleOptions smokeParticle = ParticleTypes.POOF;
        protected CompoundTag defaultData = new CompoundTag();

        public Builder(Identifier id, Identifier cycleGroup) {
            this.id = id;
            this.cycleGroup = cycleGroup;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder soundEvent(Supplier<SoundEvent> soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }

        public Builder contactParticle(ParticleOptions contactParticle) {
            this.contactParticle = contactParticle;
            return this;
        }

        public Builder smokeParticle(ParticleOptions smokeParticle) {
            this.smokeParticle = smokeParticle;
            return this;
        }

        public Builder defaultData(CompoundTag defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        public Builder size(BogeySize size, AbstractBogeyBlock<?> block) {
            this.sizes.put(size, block);
            return this;
        }

        public BogeyStyle build() {
            BogeyStyle entry = new BogeyStyle(id, cycleGroup, displayName, soundEvent, contactParticle, smokeParticle, defaultData, sizes);
            AllBogeyStyles.BOGEY_STYLES.put(id, entry);
            AllBogeyStyles.CYCLE_GROUPS.computeIfAbsent(cycleGroup, l -> new HashMap<>()).put(id, entry);
            return entry;
        }
    }
}
