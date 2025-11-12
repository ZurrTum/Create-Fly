package com.zurrtum.create.content.trains.signal;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.function.Consumer;

public class SignalEdgeGroup {
    private static final Codec<Map<UUID, UUID>> INTERSECTING_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC);
    public static final Codec<SignalEdgeGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.STRING_CODEC.fieldOf("Id").forGetter(i -> i.id),
        EdgeGroupColor.CODEC.fieldOf("Color").forGetter(group -> group.color),
        INTERSECTING_CODEC.fieldOf("Connected").forGetter(group -> group.intersecting),
        Codec.BOOL.fieldOf("Fallback").forGetter(group -> group.fallbackGroup)
    ).apply(instance, SignalEdgeGroup::new));

    public UUID id;
    public EdgeGroupColor color;

    public Set<Train> trains;
    public SignalBoundary reserved;

    public Map<UUID, UUID> intersecting;
    public Set<SignalEdgeGroup> intersectingResolved;
    public Set<UUID> adjacent;

    public boolean fallbackGroup;

    public SignalEdgeGroup(UUID id) {
        this.id = id;
        trains = new HashSet<>();
        adjacent = new HashSet<>();
        intersecting = new HashMap<>();
        intersectingResolved = new HashSet<>();
        color = EdgeGroupColor.getDefault();
    }

    private SignalEdgeGroup(UUID id, EdgeGroupColor color, Map<UUID, UUID> intersecting, boolean fallbackGroup) {
        this.id = id;
        this.color = color;
        this.intersecting = new HashMap<>(intersecting);
        this.fallbackGroup = fallbackGroup;
        trains = new HashSet<>();
        adjacent = new HashSet<>();
        intersectingResolved = new HashSet<>();
    }

    public SignalEdgeGroup asFallback() {
        fallbackGroup = true;
        return this;
    }

    public boolean isOccupiedUnless(Train train) {
        if (intersectingResolved.isEmpty())
            walkIntersecting(intersectingResolved::add);
        for (SignalEdgeGroup group : intersectingResolved)
            if (group.isThisOccupiedUnless(train))
                return true;
        return false;
    }

    private boolean isThisOccupiedUnless(Train train) {
        return reserved != null || trains.size() > 1 || !trains.contains(train) && !trains.isEmpty();
    }

    public boolean isOccupiedUnless(SignalBoundary boundary) {
        if (intersectingResolved.isEmpty())
            walkIntersecting(intersectingResolved::add);
        for (SignalEdgeGroup group : intersectingResolved)
            if (group.isThisOccupiedUnless(boundary))
                return true;
        return false;
    }

    private boolean isThisOccupiedUnless(SignalBoundary boundary) {
        return !trains.isEmpty() || reserved != null && reserved != boundary;
    }

    public void putIntersection(MinecraftServer server, UUID intersectionId, UUID targetGroup) {
        intersecting.put(intersectionId, targetGroup);
        walkIntersecting(g -> g.intersectingResolved.clear());
        resolveColor(server);
    }

    public void removeIntersection(MinecraftServer server, UUID intersectionId) {
        walkIntersecting(g -> g.intersectingResolved.clear());

        UUID removed = intersecting.remove(intersectionId);
        SignalEdgeGroup other = Create.RAILWAYS.signalEdgeGroups.get(removed);
        if (other != null)
            other.intersecting.remove(intersectionId);

        resolveColor(server);
    }

    public void putAdjacent(UUID adjacent) {
        this.adjacent.add(adjacent);
    }

    public void removeAdjacent(UUID adjacent) {
        this.adjacent.remove(adjacent);
    }

    public void resolveColor(MinecraftServer server) {
        if (intersectingResolved.isEmpty())
            walkIntersecting(intersectingResolved::add);

        MutableInt mask = new MutableInt(0);
        intersectingResolved.forEach(group -> group.adjacent.stream().map(Create.RAILWAYS.signalEdgeGroups::get).filter(Objects::nonNull)
            .filter(Predicates.not(intersectingResolved::contains)).forEach(adjacent -> mask.setValue(adjacent.color.strikeFrom(mask.getValue()))));

        EdgeGroupColor newColour = EdgeGroupColor.findNextAvailable(mask.getValue());
        if (newColour == color)
            return;

        walkIntersecting(group -> Create.RAILWAYS.sync.edgeGroupCreated(server, group.id, group.color = newColour));
        Create.RAILWAYS.markTracksDirty();
    }

    private void walkIntersecting(Consumer<SignalEdgeGroup> callback) {
        walkIntersectingRec(new HashSet<>(), callback);
    }

    private void walkIntersectingRec(Set<SignalEdgeGroup> visited, Consumer<SignalEdgeGroup> callback) {
        if (!visited.add(this))
            return;
        callback.accept(this);
        for (UUID uuid : intersecting.values()) {
            SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(uuid);
            if (group != null)
                group.walkIntersectingRec(visited, callback);
        }
    }
}
