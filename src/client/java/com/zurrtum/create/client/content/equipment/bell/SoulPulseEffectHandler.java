package com.zurrtum.create.client.content.equipment.bell;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SoulPulseEffectHandler {
    private final List<SoulPulseEffect> pulses;
    private final Queue<SoulPulseEffect> queue;
    private final Set<BlockPos> occupied;

    public SoulPulseEffectHandler() {
        pulses = new LinkedList<>();
        queue = new ConcurrentLinkedQueue<>();
        occupied = new HashSet<>();
    }

    public void tick(World world) {
        Iterator<SoulPulseEffect> iterator = pulses.iterator();
        SoulPulseEffect pulse;
        while (iterator.hasNext()) {
            pulse = iterator.next();
            List<BlockPos> spawns = pulse.tick(world);
            if (spawns != null) {
                if (pulse.canOverlap()) {
                    for (BlockPos pos : spawns) {
                        pulse.spawnParticles(world, pos);
                    }
                    if (pulse.finished()) {
                        iterator.remove();
                    }
                } else if (pulse.finished()) {
                    for (BlockPos pos : spawns) {
                        if (occupied.contains(pos)) {
                            continue;
                        }
                        pulse.spawnParticles(world, pos);
                    }
                    pulse.added.forEach(occupied::remove);
                    iterator.remove();
                } else {
                    for (BlockPos pos : spawns) {
                        if (occupied.add(pos)) {
                            pulse.spawnParticles(world, pos);
                            pulse.added.add(pos);
                        }
                    }
                }
            } else if (pulse.finished()) {
                if (!pulse.canOverlap()) {
                    pulse.added.forEach(occupied::remove);
                }
                iterator.remove();
            }
        }
        while ((pulse = queue.poll()) != null) {
            List<BlockPos> spawns = pulse.tick(world);
            if (spawns != null) {
                if (pulse.canOverlap()) {
                    for (BlockPos pos : spawns) {
                        pulse.spawnParticles(world, pos);
                    }
                } else {
                    for (BlockPos pos : spawns) {
                        if (occupied.add(pos)) {
                            pulse.spawnParticles(world, pos);
                            pulse.added.add(pos);
                        }
                    }
                }
            }
            pulses.add(pulse);
        }
    }

    public void addPulse(SoulPulseEffect pulse) {
        queue.offer(pulse);
    }

    public void refresh() {
        pulses.clear();
        occupied.clear();
    }
}
