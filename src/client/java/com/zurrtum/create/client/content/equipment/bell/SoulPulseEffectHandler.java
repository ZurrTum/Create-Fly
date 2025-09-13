package com.zurrtum.create.client.content.equipment.bell;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SoulPulseEffectHandler {

    private final List<SoulPulseEffect> pulses;
    private final Set<BlockPos> occupied;

    public SoulPulseEffectHandler() {
        pulses = new ArrayList<>();
        occupied = new HashSet<>();
    }

    public void tick(World world) {
        for (SoulPulseEffect pulse : pulses) {
            List<BlockPos> spawns = pulse.tick(world);
            if (spawns == null)
                continue;

            if (pulse.canOverlap()) {
                for (BlockPos pos : spawns) {
                    pulse.spawnParticles(world, pos);
                }
            } else {
                for (BlockPos pos : spawns) {
                    if (occupied.contains(pos))
                        continue;

                    pulse.spawnParticles(world, pos);
                    pulse.added.add(pos);
                    occupied.add(pos);
                }
            }
        }

        for (SoulPulseEffect pulse : pulses) {
            if (pulse.finished() && !pulse.canOverlap())
                occupied.removeAll(pulse.added);
        }
        pulses.removeIf(SoulPulseEffect::finished);
    }

    public void addPulse(SoulPulseEffect pulse) {
        pulses.add(pulse);
    }

    public void refresh() {
        pulses.clear();
        occupied.clear();
    }

}
