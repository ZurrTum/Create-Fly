package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BogeyStyle {
    public final Identifier id;
    public final Identifier cycleGroup;
    public final Text displayName;
    public final Supplier<SoundEvent> soundEvent;
    public final ParticleEffect contactParticle;
    public final ParticleEffect smokeParticle;
    public final NbtCompound defaultData;
    private final Map<BogeySize, AbstractBogeyBlock<?>> sizes;

    public BogeyStyle(
        Identifier id,
        Identifier cycleGroup,
        Text displayName,
        Supplier<SoundEvent> soundEvent,
        ParticleEffect contactParticle,
        ParticleEffect smokeParticle,
        NbtCompound defaultData,
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

        protected Text displayName = Text.translatable("create.bogey.style.invalid");
        protected Supplier<SoundEvent> soundEvent = AllSoundEvents.TRAIN2::getMainEvent;
        protected ParticleEffect contactParticle = ParticleTypes.CRIT;
        protected ParticleEffect smokeParticle = ParticleTypes.POOF;
        protected NbtCompound defaultData = new NbtCompound();

        public Builder(Identifier id, Identifier cycleGroup) {
            this.id = id;
            this.cycleGroup = cycleGroup;
        }

        public Builder displayName(Text displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder soundEvent(Supplier<SoundEvent> soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }

        public Builder contactParticle(ParticleEffect contactParticle) {
            this.contactParticle = contactParticle;
            return this;
        }

        public Builder smokeParticle(ParticleEffect smokeParticle) {
            this.smokeParticle = smokeParticle;
            return this;
        }

        public Builder defaultData(NbtCompound defaultData) {
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
