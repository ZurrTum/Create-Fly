package com.zurrtum.create.content.contraptions.actors.roller;

import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PaveTask {

    private final Couple<Double> horizontalInterval;
    private final Map<Couple<Integer>, Float> heightValues = new HashMap<>();

    public PaveTask(double h1, double h2) {
        horizontalInterval = Couple.create(h1, h2);
    }

    public Couple<Double> getHorizontalInterval() {
        return horizontalInterval;
    }

    public void put(int x, int z, float y) {
        heightValues.put(Couple.create(x, z), y);
    }

    public float get(Couple<Integer> coords) {
        return heightValues.get(coords);
    }

    public Set<Couple<Integer>> keys() {
        return heightValues.keySet();
    }

    public void put(BlockPos p) {
        put(p.getX(), p.getZ(), p.getY());
    }

}
