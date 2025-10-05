package com.zurrtum.create.client.content.equipment.bell;

import com.google.common.collect.Streams;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoulPulseEffect {

    public static final int MAX_DISTANCE = 11;
    private static final List<List<BlockPos>> LAYERS = genLayers();

    private static final int WAITING_TICKS = 100;
    public static final int TICKS_PER_LAYER = 6;
    private int ticks;
    public final BlockPos pos;
    public final int distance;
    public final List<BlockPos> added;

    public SoulPulseEffect(BlockPos pos, int distance, boolean canOverlap) {
        this.ticks = TICKS_PER_LAYER * distance;
        this.pos = pos;
        this.distance = distance;
        this.added = canOverlap ? null : new ArrayList<>();
    }

    public boolean finished() {
        return ticks <= -WAITING_TICKS;
    }

    public boolean canOverlap() {
        return added == null;
    }

    public List<BlockPos> tick(World world) {
        if (finished())
            return null;

        ticks--;
        if (ticks < 0 || ticks % TICKS_PER_LAYER != 0)
            return null;

        List<BlockPos> spawns = getPotentialSoulSpawns(world);
        while (spawns.isEmpty() && ticks > 0) {
            ticks -= TICKS_PER_LAYER;
            spawns.addAll(getPotentialSoulSpawns(world));
        }
        return spawns;
    }

    public int currentLayerIdx() {
        return distance - ticks / TICKS_PER_LAYER - 1;
    }

    public List<BlockPos> getPotentialSoulSpawns(World world) {
        if (world == null)
            return new ArrayList<>();

        return getLayer(currentLayerIdx()).map(p -> p.add(pos)).filter(p -> canSpawnSoulAt(world, p, true)).collect(Collectors.toList());
    }

    public static boolean isDark(World world, BlockPos at) {
        return world.getLightLevel(LightType.BLOCK, at) < 1;
    }

    public static boolean canSpawnSoulAt(World world, BlockPos at, boolean ignoreLight) {
        EntityType<?> dummy = EntityType.ZOMBIE;
        double dummyWidth = 0.2, dummyHeight = 0.75;
        double w2 = dummyWidth / 2;

        return world != null && SpawnLocationTypes.ON_GROUND.isSpawnPositionOk(world, at, dummy) && (ignoreLight || isDark(
            world,
            at
        )) && Streams.stream(world.getBlockCollisions(
            null,
            new Box(at.getX() + 0.5 - w2, at.getY(), at.getZ() + 0.5 - w2, at.getX() + 0.5 + w2, at.getY() + dummyHeight, at.getZ() + 0.5 + w2)
        )).allMatch(VoxelShape::isEmpty);
    }

    public void spawnParticles(World world, BlockPos at) {
        if (world == null || !world.isClient())
            return;

        Vec3d p = Vec3d.of(at);
        if (canOverlap())
            world.addImportantParticleClient(
                ((int) Math.round(VecHelper.getCenterOf(pos)
                    .distanceTo(VecHelper.getCenterOf(at)))) >= distance ? AllParticleTypes.SOUL_PERIMETER : AllParticleTypes.SOUL_EXPANDING_PERIMETER,
                p.x + 0.5,
                p.y + 0.5,
                p.z + 0.5,
                0,
                0,
                0
            );
        if (SoulPulseEffect.isDark(world, at)) {
            world.addImportantParticleClient(AllParticleTypes.SOUL, p.x + 0.5, p.y + 0.5, p.z + 0.5, 0, 0, 0);
            world.addParticleClient(AllParticleTypes.SOUL_BASE, p.x + 0.5, p.y + 0.01, p.z + 0.5, 0, 0, 0);
        }
    }

    private static List<List<BlockPos>> genLayers() {
        List<List<BlockPos>> layers = new ArrayList<>();
        for (int i = 0; i < MAX_DISTANCE; i++)
            layers.add(new ArrayList<>());

        for (int x = 0; x < MAX_DISTANCE; x++) {
            for (int y = 0; y < MAX_DISTANCE; y++) {
                for (int z = 0; z < MAX_DISTANCE; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);

                    int dist = (int) Math.round(Math.sqrt(candidate.getSquaredDistance(BlockPos.ZERO)));
                    if (dist > MAX_DISTANCE)
                        continue;
                    if (dist <= 0)
                        dist = 1;

                    List<BlockPos> layer = layers.get(dist - 1);
                    int start = layer.size(), end = start + 1;
                    layer.add(candidate);

                    if (candidate.getX() != 0) {
                        layer.add(new BlockPos(-candidate.getX(), candidate.getY(), candidate.getZ()));
                        end += 1;
                    }
                    if (candidate.getY() != 0) {
                        for (int i = start; i < end; i++) {
                            BlockPos prev = layer.get(i);
                            layer.add(new BlockPos(prev.getX(), -prev.getY(), prev.getZ()));
                        }
                        end += end - start;
                    }
                    if (candidate.getZ() != 0) {
                        for (int i = start; i < end; i++) {
                            BlockPos prev = layer.get(i);
                            layer.add(new BlockPos(prev.getX(), prev.getY(), -prev.getZ()));
                        }
                    }
                }
            }
        }

        return layers;
    }

    public static Stream<BlockPos> getLayer(int idx) {
        if (idx < 0 || idx >= MAX_DISTANCE)
            return Stream.empty();
        return LAYERS.get(idx).stream();
    }

}
