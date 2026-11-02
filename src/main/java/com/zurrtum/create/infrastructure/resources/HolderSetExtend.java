package com.zurrtum.create.infrastructure.resources;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public abstract class HolderSetExtend implements HolderSet<Item> {
    protected final HolderSet<Item> origin;
    protected final HolderSet<Item> tag;

    public HolderSetExtend(HolderSet<Item> origin, HolderSet<Item> tag) {
        this.origin = origin;
        this.tag = tag;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Item> owner) {
        return origin.canSerializeIn(owner);
    }

    @Override
    public Optional<Holder<Item>> getRandomElement(RandomSource random) {
        return origin.getRandomElement(random);
    }

    @Override
    public boolean isBound() {
        return origin.isBound() && tag.isBound();
    }

    @Override
    public Either<TagKey<Item>, List<Holder<Item>>> unwrap() {
        return origin.unwrap();
    }

    @Override
    public Optional<TagKey<Item>> unwrapKey() {
        return origin.unwrapKey();
    }
}
