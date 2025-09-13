package com.zurrtum.create.content.redstone.link;

import com.mojang.serialization.Codec;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.levelWrappers.WorldHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldAccess;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RedstoneLinkNetworkHandler {

    static final Map<WorldAccess, Map<Couple<Frequency>, Set<IRedstoneLinkable>>> connections = new IdentityHashMap<>();

    public final AtomicInteger globalPowerVersion = new AtomicInteger();

    public static class Frequency {
        public static final Codec<Frequency> CODEC = ItemStack.OPTIONAL_CODEC.xmap(Frequency::of, Frequency::getStack);
        public static final Frequency EMPTY = new Frequency(ItemStack.EMPTY);
        private static final Map<Item, Frequency> simpleFrequencies = new IdentityHashMap<>();
        private ItemStack stack;
        private Item item;
        private int color;

        public static Frequency of(ItemStack stack) {
            if (stack.isEmpty())
                return EMPTY;
            if (stack.getComponents().isEmpty())
                return simpleFrequencies.computeIfAbsent(stack.getItem(), $ -> new Frequency(stack));
            return new Frequency(stack);
        }

        private Frequency(ItemStack stack) {
            this.stack = stack;
            item = stack.getItem();
            color = stack.contains(DataComponentTypes.DYED_COLOR) ? stack.get(DataComponentTypes.DYED_COLOR).rgb() : -1;
        }

        public ItemStack getStack() {
            return stack;
        }

        @Override
        public int hashCode() {
            return (item.hashCode() * 31) ^ color;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            return obj instanceof Frequency ? ((Frequency) obj).item == item && ((Frequency) obj).color == color : false;
        }

    }

    public void onLoadWorld(WorldAccess world) {
        connections.put(world, new HashMap<>());
        Create.LOGGER.debug("Prepared Redstone Network Space for " + WorldHelper.getDimensionID(world));
    }

    public void onUnloadWorld(WorldAccess world) {
        connections.remove(world);
        Create.LOGGER.debug("Removed Redstone Network Space for " + WorldHelper.getDimensionID(world));
    }

    public Set<IRedstoneLinkable> getNetworkOf(WorldAccess world, IRedstoneLinkable actor) {
        Map<Couple<Frequency>, Set<IRedstoneLinkable>> networksInWorld = networksIn(world);
        Couple<Frequency> key = actor.getNetworkKey();
        if (!networksInWorld.containsKey(key))
            networksInWorld.put(key, new LinkedHashSet<>());
        return networksInWorld.get(key);
    }

    public void addToNetwork(WorldAccess world, IRedstoneLinkable actor) {
        getNetworkOf(world, actor).add(actor);
        updateNetworkOf(world, actor);
    }

    public void removeFromNetwork(WorldAccess world, IRedstoneLinkable actor) {
        Set<IRedstoneLinkable> network = getNetworkOf(world, actor);
        network.remove(actor);
        if (network.isEmpty()) {
            networksIn(world).remove(actor.getNetworkKey());
            return;
        }
        updateNetworkOf(world, actor);
    }

    public void updateNetworkOf(WorldAccess world, IRedstoneLinkable actor) {
        Set<IRedstoneLinkable> network = getNetworkOf(world, actor);
        globalPowerVersion.incrementAndGet();
        int power = 0;

        for (Iterator<IRedstoneLinkable> iterator = network.iterator(); iterator.hasNext(); ) {
            IRedstoneLinkable other = iterator.next();
            if (!other.isAlive()) {
                iterator.remove();
                continue;
            }

            if (!withinRange(actor, other))
                continue;

            if (power < 15)
                power = Math.max(other.getTransmittedStrength(), power);
        }

        if (actor instanceof ServerLinkBehaviour linkBehaviour) {
            // fix one-to-one loading order problem
            if (linkBehaviour.isListening()) {
                linkBehaviour.newPosition = true;
                linkBehaviour.setReceivedStrength(power);
            }
        }

        for (IRedstoneLinkable other : network) {
            if (other != actor && other.isListening() && withinRange(actor, other))
                other.setReceivedStrength(power);
        }
    }

    public static boolean withinRange(IRedstoneLinkable from, IRedstoneLinkable to) {
        if (from == to)
            return true;
        return from.getLocation().isWithinDistance(to.getLocation(), AllConfigs.server().logistics.linkRange.get());
    }

    public Map<Couple<Frequency>, Set<IRedstoneLinkable>> networksIn(WorldAccess world) {
        if (!connections.containsKey(world)) {
            Create.LOGGER.warn("Tried to Access unprepared network space of " + WorldHelper.getDimensionID(world));
            return new HashMap<>();
        }
        return connections.get(world);
    }

    public boolean hasAnyLoadedPower(Couple<Frequency> frequency) {
        for (Map<Couple<Frequency>, Set<IRedstoneLinkable>> map : connections.values()) {
            Set<IRedstoneLinkable> set = map.get(frequency);
            if (set == null || set.isEmpty())
                continue;
            for (IRedstoneLinkable link : set)
                if (link.getTransmittedStrength() > 0)
                    return true;
        }
        return false;
    }

}
