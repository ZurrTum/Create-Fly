package com.zurrtum.create.content.logistics.packagerLink;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;

public class LogisticsNetwork {
    public static final Codec<LogisticsNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("Id").forGetter(i -> i.id),
        RequestPromiseQueue.CODEC.fieldOf("Promises").forGetter(i -> i.panelPromises),
        Codec.list(GlobalPos.CODEC).xmap(Sets::newHashSet, Lists::newArrayList).fieldOf("Links").forGetter(i -> i.totalLinks),
        UUIDUtil.CODEC.optionalFieldOf("Owner").forGetter(i -> Optional.ofNullable(i.owner)),
        Codec.BOOL.fieldOf("Locked").forGetter(i -> i.locked)
    ).apply(instance, LogisticsNetwork::new));

    public UUID id;
    public RequestPromiseQueue panelPromises;

    public HashSet<GlobalPos> totalLinks;
    public HashSet<GlobalPos> loadedLinks;

    public UUID owner;
    public boolean locked;

    public LogisticsNetwork(UUID networkId) {
        id = networkId;
        panelPromises = new RequestPromiseQueue(Create.LOGISTICS::markDirty);
        totalLinks = new HashSet<>();
        loadedLinks = new HashSet<>();
        owner = null;
        locked = false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private LogisticsNetwork(UUID networkId, RequestPromiseQueue panelPromises, HashSet<GlobalPos> totalLinks, Optional<UUID> owner, boolean locked) {
        id = networkId;
        this.panelPromises = panelPromises;
        this.panelPromises.setOnChanged(Create.LOGISTICS::markDirty);
        this.totalLinks = totalLinks;
        loadedLinks = new HashSet<>();
        this.owner = owner.orElse(null);
        this.locked = locked;
    }
}
