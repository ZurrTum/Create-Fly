package com.zurrtum.create.api.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.Type;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Billboard;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.TowardMotion;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Tumble;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: 1.21.7 - Move into api package
public record PotatoCannonProjectileType(
    RegistryEntryList<Item> items, int reloadTicks, int damage, int split, float knockback, float drag, float velocityMultiplier,
    float gravityMultiplier, float soundPitch, boolean sticky, ItemStack dropStack, PotatoProjectileRenderMode renderMode,
    Optional<PotatoProjectileEntityHitAction> preEntityHit, Optional<PotatoProjectileEntityHitAction> onEntityHit,
    Optional<PotatoProjectileBlockHitAction> onBlockHit
) {
    public static final Codec<PotatoCannonProjectileType> CODEC = RecordCodecBuilder.create(i -> i.group(
        RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("items").forGetter(PotatoCannonProjectileType::items),
        Codec.INT.optionalFieldOf("reload_ticks", 10).forGetter(PotatoCannonProjectileType::reloadTicks),
        Codec.INT.optionalFieldOf("damage", 1).forGetter(PotatoCannonProjectileType::damage),
        Codec.INT.optionalFieldOf("split", 1).forGetter(PotatoCannonProjectileType::split),
        Codec.FLOAT.optionalFieldOf("knockback", 1f).forGetter(PotatoCannonProjectileType::knockback),
        Codec.FLOAT.optionalFieldOf("drag", .99f).forGetter(PotatoCannonProjectileType::drag),
        Codec.FLOAT.optionalFieldOf("velocity_multiplier", 1f).forGetter(PotatoCannonProjectileType::velocityMultiplier),
        Codec.FLOAT.optionalFieldOf("gravity_multiplier", 1f).forGetter(PotatoCannonProjectileType::gravityMultiplier),
        Codec.FLOAT.optionalFieldOf("sound_pitch", 1f).forGetter(PotatoCannonProjectileType::soundPitch),
        Codec.BOOL.optionalFieldOf("sticky", false).forGetter(PotatoCannonProjectileType::sticky),
        ItemStack.CODEC.optionalFieldOf("drop_stack", ItemStack.EMPTY).forGetter(PotatoCannonProjectileType::dropStack),
        PotatoProjectileRenderMode.CODEC.optionalFieldOf("render_mode", Billboard.INSTANCE).forGetter(PotatoCannonProjectileType::renderMode),
        PotatoProjectileEntityHitAction.CODEC.optionalFieldOf("pre_entity_hit").forGetter(p -> p.preEntityHit),
        PotatoProjectileEntityHitAction.CODEC.optionalFieldOf("on_entity_hit").forGetter(p -> p.onEntityHit),
        PotatoProjectileBlockHitAction.CODEC.optionalFieldOf("on_block_hit").forGetter(p -> p.onBlockHit)
    ).apply(i, PotatoCannonProjectileType::new));

    @SuppressWarnings("deprecation")
    public static Optional<Reference<PotatoCannonProjectileType>> getTypeForItem(DynamicRegistryManager registryAccess, Item item) {
        // Cache this if it causes performance issues, but it probably won't
        return registryAccess.getOrThrow(CreateRegistryKeys.POTATO_PROJECTILE_TYPE).streamEntries()
            .filter(ref -> ref.value().items.contains(item.getRegistryEntry())).findFirst();
    }

    public boolean preEntityHit(ItemStack stack, EntityHitResult ray) {
        return preEntityHit.map(i -> i.execute(stack, ray, Type.PRE_HIT)).orElse(false);
    }

    public boolean onEntityHit(ItemStack stack, EntityHitResult ray) {
        return onEntityHit.map(i -> i.execute(stack, ray, Type.ON_HIT)).orElse(false);
    }

    public boolean onBlockHit(WorldAccess level, ItemStack stack, BlockHitResult ray) {
        return onBlockHit.map(i -> i.execute(level, stack, ray)).orElse(false);
    }

    // Copy the stack so it's not mutated and lost
    @Override
    public ItemStack dropStack() {
        return dropStack.copy();
    }

    public static class Builder {
        private final List<RegistryEntry<Item>> items = new ArrayList<>();
        private int reloadTicks = 10;
        private int damage = 1;
        private int split = 1;
        private float knockback = 1f;
        private float drag = 0.99f;
        private float velocityMultiplier = 1f;
        private float gravityMultiplier = 1f;
        private float soundPitch = 1f;
        private boolean sticky = false;
        private ItemStack dropStack = ItemStack.EMPTY;
        private PotatoProjectileRenderMode renderMode = Billboard.INSTANCE;
        private PotatoProjectileEntityHitAction preEntityHit = null;
        private PotatoProjectileEntityHitAction onEntityHit = null;
        private PotatoProjectileBlockHitAction onBlockHit = null;

        public Builder reloadTicks(int reload) {
            this.reloadTicks = reload;
            return this;
        }

        public Builder damage(int damage) {
            this.damage = damage;
            return this;
        }

        public Builder splitInto(int split) {
            this.split = split;
            return this;
        }

        public Builder knockback(float knockback) {
            this.knockback = knockback;
            return this;
        }

        public Builder drag(float drag) {
            this.drag = drag;
            return this;
        }

        public Builder velocity(float velocity) {
            this.velocityMultiplier = velocity;
            return this;
        }

        public Builder gravity(float modifier) {
            this.gravityMultiplier = modifier;
            return this;
        }

        public Builder soundPitch(float pitch) {
            this.soundPitch = pitch;
            return this;
        }

        public Builder sticky() {
            this.sticky = true;
            return this;
        }

        public Builder dropStack(ItemStack stack) {
            this.dropStack = stack;
            return this;
        }

        public Builder renderMode(PotatoProjectileRenderMode renderMode) {
            this.renderMode = renderMode;
            return this;
        }

        public Builder renderBillboard() {
            renderMode(Billboard.INSTANCE);
            return this;
        }

        public Builder renderTumbling() {
            renderMode(Tumble.INSTANCE);
            return this;
        }

        public Builder renderTowardMotion(int spriteAngle, float spin) {
            renderMode(new TowardMotion(spriteAngle, spin));
            return this;
        }

        public Builder preEntityHit(PotatoProjectileEntityHitAction entityHitAction) {
            this.preEntityHit = entityHitAction;
            return this;
        }

        public Builder onEntityHit(PotatoProjectileEntityHitAction entityHitAction) {
            this.onEntityHit = entityHitAction;
            return this;
        }

        public Builder onBlockHit(PotatoProjectileBlockHitAction blockHitAction) {
            this.onBlockHit = blockHitAction;
            return this;
        }

        @SuppressWarnings("deprecation")
        public Builder addItems(ItemConvertible... items) {
            for (ItemConvertible provider : items)
                this.items.add(provider.asItem().getRegistryEntry());
            return this;
        }

        public PotatoCannonProjectileType build() {
            return new PotatoCannonProjectileType(
                RegistryEntryList.of(items),
                reloadTicks,
                damage,
                split,
                knockback,
                drag,
                velocityMultiplier,
                gravityMultiplier,
                soundPitch,
                sticky,
                dropStack,
                renderMode,
                Optional.ofNullable(preEntityHit),
                Optional.ofNullable(onEntityHit),
                Optional.ofNullable(onBlockHit)
            );
        }
    }
}
