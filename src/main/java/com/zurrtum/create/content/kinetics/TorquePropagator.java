package com.zurrtum.create.content.kinetics;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.levelWrappers.WorldHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TorquePropagator {

    static Map<LevelAccessor, Map<Long, KineticNetwork>> networks = new HashMap<>();

    public void onLoadWorld(LevelAccessor world) {
        networks.put(world, new HashMap<>());
        Create.LOGGER.debug("Prepared Kinetic Network Space for " + WorldHelper.getDimensionID(world));
    }

    public void onUnloadWorld(LevelAccessor world) {
        networks.remove(world);
        Create.LOGGER.debug("Removed Kinetic Network Space for " + WorldHelper.getDimensionID(world));
    }

    @Nullable
    public KineticNetwork getOrCreateNetworkFor(KineticBlockEntity be) {
        Long id = be.network;
        KineticNetwork network;
        Map<Long, KineticNetwork> map = networks.computeIfAbsent(be.getLevel(), $ -> new HashMap<>());
        if (id == null)
            return null;

        if (!map.containsKey(id)) {
            network = new KineticNetwork(id);
            map.put(id, network);
        }
        network = map.get(id);
        return network;
    }

}