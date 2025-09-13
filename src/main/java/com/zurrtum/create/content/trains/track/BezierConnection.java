package com.zurrtum.create.content.trains.track;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BezierConnection implements Iterable<BezierConnection.Segment> {
    public static final Codec<BezierConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CreateCodecs.COUPLE_BLOCK_POS_CODEC.fieldOf("Positions").forGetter(i -> i.bePositions),
        CreateCodecs.COUPLE_VEC3D_CODEC.fieldOf("Starts").forGetter(i -> i.starts),
        CreateCodecs.COUPLE_VEC3D_CODEC.fieldOf("Axes").forGetter(i -> i.axes),
        CreateCodecs.COUPLE_VEC3D_CODEC.fieldOf("Normals").forGetter(i -> i.normals),
        Codec.BOOL.fieldOf("Primary").forGetter(i -> i.primary),
        Codec.BOOL.fieldOf("Girder").forGetter(i -> i.hasGirder),
        TrackMaterial.CODEC.fieldOf("Material").forGetter(i -> i.trackMaterial),
        CreateCodecs.COUPLE_INT_CODEC.optionalFieldOf("Smoothing").forGetter(i -> Optional.ofNullable(i.smoothing))
    ).apply(instance, BezierConnection::new));

    public final Couple<BlockPos> bePositions;
    public final Couple<Vec3d> starts;
    public final Couple<Vec3d> axes;
    public final Couple<Vec3d> normals;
    @Nullable
    public Couple<Integer> smoothing;
    public final boolean primary;
    public final boolean hasGirder;
    protected TrackMaterial trackMaterial;

    // runtime
    private final AtomicReference<@Nullable Runtime> lazyRuntime = new AtomicReference<>(null);

    public BezierConnection(
        Couple<BlockPos> positions,
        Couple<Vec3d> starts,
        Couple<Vec3d> axes,
        Couple<Vec3d> normals,
        boolean primary,
        boolean girder,
        TrackMaterial material
    ) {
        bePositions = positions;
        this.starts = starts;
        this.axes = axes;
        this.normals = normals;
        this.primary = primary;
        this.hasGirder = girder;
        this.trackMaterial = material;
    }

    public BezierConnection secondary() {
        BezierConnection bezierConnection = new BezierConnection(
            bePositions.swap(),
            starts.swap(),
            axes.swap(),
            normals.swap(),
            !primary,
            hasGirder,
            trackMaterial
        );
        if (smoothing != null)
            bezierConnection.smoothing = smoothing.swap();
        return bezierConnection;
    }

    public BezierConnection clone() {
        var out = new BezierConnection(bePositions.copy(), starts.copy(), axes.copy(), normals.copy(), primary, hasGirder, trackMaterial);
        if (smoothing != null) {
            out.smoothing = smoothing.copy();
        }
        return out;
    }

    private static boolean coupleEquals(Couple<?> a, Couple<?> b) {
        return (a.getFirst().equals(b.getFirst()) && a.getSecond()
            .equals(b.getSecond())) || (a.getFirst() instanceof Vec3d aFirst && a.getSecond() instanceof Vec3d aSecond && b.getFirst() instanceof Vec3d bFirst && b.getSecond() instanceof Vec3d bSecond && aFirst.isInRange(bFirst,
            1e-6
        ) && aSecond.isInRange(bSecond, 1e-6));
    }

    public boolean equalsSansMaterial(BezierConnection other) {
        return equalsSansMaterialInner(other) || equalsSansMaterialInner(other.secondary());
    }

    private boolean equalsSansMaterialInner(BezierConnection other) {
        return this == other || (other != null && coupleEquals(this.bePositions, other.bePositions) && coupleEquals(
            this.starts,
            other.starts
        ) && coupleEquals(this.axes, other.axes) && coupleEquals(this.normals, other.normals) && this.hasGirder == other.hasGirder);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BezierConnection(
        Couple<BlockPos> bePositions,
        Couple<Vec3d> starts,
        Couple<Vec3d> axes,
        Couple<Vec3d> normals,
        boolean primary,
        boolean hasGirder,
        TrackMaterial trackMaterial,
        Optional<Couple<Integer>> smoothing
    ) {
        this(bePositions, starts, axes, normals, primary, hasGirder, trackMaterial);
        this.smoothing = smoothing.orElse(null);
    }

    public BezierConnection(ReadView view, BlockPos localTo) {
        this(
            view.read("Positions", CreateCodecs.COUPLE_BLOCK_POS_CODEC).orElseThrow().map(b -> b.add(localTo)),
            view.read("Starts", CreateCodecs.COUPLE_VEC3D_CODEC).orElseThrow().map(v -> v.add(Vec3d.of(localTo))),
            view.read("Axes", CreateCodecs.COUPLE_VEC3D_CODEC).orElseThrow(),
            view.read("Normals", CreateCodecs.COUPLE_VEC3D_CODEC).orElseThrow(),
            view.getBoolean("Primary", false),
            view.getBoolean("Girder", false),
            view.read("Material", TrackMaterial.CODEC).orElseThrow()
        );

        view.read("Smoothing", CreateCodecs.COUPLE_INT_CODEC).ifPresent(couple -> smoothing = couple);
    }

    public void write(WriteView view, BlockPos localTo) {
        Couple<BlockPos> tePositions = this.bePositions.map(b -> b.subtract(localTo));
        Couple<Vec3d> starts = this.starts.map(v -> v.subtract(Vec3d.of(localTo)));

        view.putBoolean("Girder", hasGirder);
        view.putBoolean("Primary", primary);
        view.put("Positions", CreateCodecs.COUPLE_BLOCK_POS_CODEC, tePositions);
        view.put("Starts", CreateCodecs.COUPLE_VEC3D_CODEC, starts);
        view.put("Axes", CreateCodecs.COUPLE_VEC3D_CODEC, axes);
        view.put("Normals", CreateCodecs.COUPLE_VEC3D_CODEC, normals);
        view.put("Material", TrackMaterial.CODEC, getMaterial());

        if (smoothing != null)
            view.put("Smoothing", CreateCodecs.COUPLE_INT_CODEC, smoothing);
    }

    public BezierConnection(PacketByteBuf buffer) {
        this(
            Couple.create(buffer::readBlockPos),
            Couple.create(() -> VecHelper.read(buffer)),
            Couple.create(() -> VecHelper.read(buffer)),
            Couple.create(() -> VecHelper.read(buffer)),
            buffer.readBoolean(),
            buffer.readBoolean(),
            TrackMaterial.fromId(buffer.readIdentifier())
        );
        if (buffer.readBoolean())
            smoothing = Couple.create(buffer::readVarInt);
    }

    public void write(PacketByteBuf buffer) {
        bePositions.forEach(buffer::writeBlockPos);
        starts.forEach(v -> VecHelper.write(v, buffer));
        axes.forEach(v -> VecHelper.write(v, buffer));
        normals.forEach(v -> VecHelper.write(v, buffer));
        buffer.writeBoolean(primary);
        buffer.writeBoolean(hasGirder);
        buffer.writeIdentifier(getMaterial().getId());
        buffer.writeBoolean(smoothing != null);
        if (smoothing != null)
            smoothing.forEach(buffer::writeVarInt);
    }

    public BlockPos getKey() {
        return bePositions.getSecond();
    }

    public boolean isPrimary() {
        return primary;
    }

    public int yOffsetAt(Vec3d end) {
        if (smoothing == null)
            return 0;
        if (TrackBlockEntityTilt.compareHandles(starts.getFirst(), end))
            return smoothing.getFirst();
        if (TrackBlockEntityTilt.compareHandles(starts.getSecond(), end))
            return smoothing.getSecond();
        return 0;
    }

    // Runtime information

    public double getLength() {
        return resolve().length;
    }

    public float[] getStepLUT() {
        return resolve().stepLUT;
    }

    public int getSegmentCount() {
        return resolve().segments;
    }

    public Vec3d getPosition(double t) {
        var runtime = resolve();
        return VecHelper.bezier(starts.getFirst(), starts.getSecond(), runtime.finish1, runtime.finish2, (float) t);
    }

    public double getRadius() {
        return resolve().radius;
    }

    public double getHandleLength() {
        return resolve().handleLength;
    }

    public float getSegmentT(int index) {
        return resolve().getSegmentT(index);
    }

    public double incrementT(double currentT, double distance) {
        var runtime = resolve();
        double dx = VecHelper.bezierDerivative(starts.getFirst(), starts.getSecond(), runtime.finish1, runtime.finish2, (float) currentT)
            .length() / getLength();
        return currentT + distance / dx;
    }

    public Box getBounds() {
        return resolve().bounds;
    }

    public Vec3d getNormal(double t) {
        var runtime = resolve();
        Vec3d end1 = starts.getFirst();
        Vec3d end2 = starts.getSecond();
        Vec3d fn1 = normals.getFirst();
        Vec3d fn2 = normals.getSecond();

        Vec3d derivative = VecHelper.bezierDerivative(end1, end2, runtime.finish1, runtime.finish2, (float) t).normalize();
        Vec3d faceNormal = fn1.equals(fn2) ? fn1 : VecHelper.slerp((float) t, fn1, fn2);
        Vec3d normal = faceNormal.crossProduct(derivative).normalize();
        return derivative.crossProduct(normal);
    }

    @NotNull
    private Runtime resolve() {
        var out = lazyRuntime.get();

        if (out == null) {
            // Since this can be accessed from multiple threads, we consolidate the intermediary
            // computation into a class and only publish complete results.
            out = new Runtime(starts, axes);
            // Doesn't matter if this one becomes the canonical value because all results are the same.
            lazyRuntime.set(out);
        }

        return out;
    }

    @Override
    public Iterator<Segment> iterator() {
        var offset = Vec3d.of(bePositions.getFirst()).multiply(-1).add(0, 3 / 16f, 0);
        return new Bezierator(this, offset);
    }

    public void addItemsToPlayer(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        int tracks = getTrackItemCost();
        while (tracks > 0) {
            inv.offerOrDrop(new ItemStack(getMaterial().getBlock(), Math.min(64, tracks)));
            tracks -= 64;
        }
        int girders = getGirderItemCost();
        while (girders > 0) {
            inv.offerOrDrop(new ItemStack(AllItems.METAL_GIRDER, Math.min(64, girders)));
            girders -= 64;
        }
    }

    public int getGirderItemCost() {
        return hasGirder ? getTrackItemCost() * 2 : 0;
    }

    public int getTrackItemCost() {
        return (getSegmentCount() + 1) / 2;
    }

    public void spawnItems(World level) {
        if (!(level instanceof ServerWorld serverWorld) || !serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
            return;
        Vec3d origin = Vec3d.of(bePositions.getFirst());
        for (Segment segment : this) {
            if (segment.index % 2 != 0 || segment.index == getSegmentCount())
                continue;
            Vec3d v = VecHelper.offsetRandomly(segment.position, level.random, .125f).add(origin);
            ItemEntity entity = new ItemEntity(level, v.x, v.y, v.z, new ItemStack(getMaterial()));
            entity.setToDefaultPickupDelay();
            level.spawnEntity(entity);
            if (!hasGirder)
                continue;
            for (int i = 0; i < 2; i++) {
                entity = new ItemEntity(level, v.x, v.y, v.z, AllItems.METAL_GIRDER.getDefaultStack());
                entity.setToDefaultPickupDelay();
                level.spawnEntity(entity);
            }
        }
    }

    public void spawnDestroyParticles(World level) {
        if (!(level instanceof ServerWorld slevel))
            return;
        BlockStateParticleEffect data = new BlockStateParticleEffect(ParticleTypes.BLOCK, getMaterial().getBlock().getDefaultState());
        BlockStateParticleEffect girderData = new BlockStateParticleEffect(ParticleTypes.BLOCK, AllBlocks.METAL_GIRDER.getDefaultState());
        Vec3d origin = Vec3d.of(bePositions.getFirst());
        for (Segment segment : this) {
            for (int offset : Iterate.positiveAndNegative) {
                Vec3d v = segment.position.add(segment.normal.multiply(14 / 16f * offset)).add(origin);
                slevel.spawnParticles(data, v.x, v.y, v.z, 1, 0, 0, 0, 0);
                if (!hasGirder)
                    continue;
                slevel.spawnParticles(girderData, v.x, v.y - .5f, v.z, 1, 0, 0, 0, 0);
            }
        }
    }

    public TrackMaterial getMaterial() {
        return trackMaterial;
    }

    public void setMaterial(TrackMaterial material) {
        trackMaterial = material;
    }

    private static class Runtime {
        private final Vec3d finish1;
        private final Vec3d finish2;
        private final double length;
        private final float[] stepLUT;
        private final int segments;

        private double radius;
        private double handleLength;

        private final Box bounds;

        private Runtime(Couple<Vec3d> starts, Couple<Vec3d> axes) {
            Vec3d end1 = starts.getFirst();
            Vec3d end2 = starts.getSecond();
            Vec3d axis1 = axes.getFirst().normalize();
            Vec3d axis2 = axes.getSecond().normalize();

            determineHandles(end1, end2, axis1, axis2);

            finish1 = axis1.multiply(handleLength).add(end1);
            finish2 = axis2.multiply(handleLength).add(end2);

            int scanCount = 16;

            this.length = computeLength(finish1, finish2, end1, end2, scanCount);

            segments = (int) (length * 2);
            stepLUT = new float[segments + 1];
            stepLUT[0] = 1;
            float combinedDistance = 0;

            Box bounds = new Box(end1, end2);

            // determine step lut
            {
                Vec3d previous = end1;
                for (int i = 0; i <= segments; i++) {
                    float t = i / (float) segments;
                    Vec3d result = VecHelper.bezier(end1, end2, finish1, finish2, t);
                    bounds = bounds.union(new Box(result, result));
                    if (i > 0) {
                        combinedDistance += result.distanceTo(previous) / length;
                        stepLUT[i] = (float) (t / combinedDistance);
                    }
                    previous = result;
                }
            }

            this.bounds = bounds.expand(1.375f);
        }

        private static double computeLength(Vec3d finish1, Vec3d finish2, Vec3d end1, Vec3d end2, int scanCount) {
            double length = 0;

            Vec3d previous = end1;
            for (int i = 0; i <= scanCount; i++) {
                float t = i / (float) scanCount;
                Vec3d result = VecHelper.bezier(end1, end2, finish1, finish2, t);
                if (previous != null)
                    length += result.distanceTo(previous);
                previous = result;
            }
            return length;
        }

        public float getSegmentT(int index) {
            return index == segments ? 1 : index * stepLUT[index] / segments;
        }

        private void determineHandles(Vec3d end1, Vec3d end2, Vec3d axis1, Vec3d axis2) {
            Vec3d cross1 = axis1.crossProduct(new Vec3d(0, 1, 0));
            Vec3d cross2 = axis2.crossProduct(new Vec3d(0, 1, 0));

            radius = 0;
            double a1 = MathHelper.atan2(-axis2.z, -axis2.x);
            double a2 = MathHelper.atan2(axis1.z, axis1.x);
            double angle = a1 - a2;

            float circle = 2 * MathHelper.PI;
            angle = (angle + circle) % circle;
            if (Math.abs(circle - angle) < Math.abs(angle))
                angle = circle - angle;

            if (MathHelper.approximatelyEquals(angle, 0)) {
                double[] intersect = VecHelper.intersect(end1, end2, axis1, cross2, Axis.Y);
                if (intersect != null) {
                    double t = Math.abs(intersect[0]);
                    double u = Math.abs(intersect[1]);
                    double min = Math.min(t, u);
                    double max = Math.max(t, u);

                    if (min > 1.2 && max / min > 1 && max / min < 3) {
                        handleLength = (max - min);
                        return;
                    }
                }

                handleLength = end2.distanceTo(end1) / 3;
                return;
            }

            double n = circle / angle;
            double factor = 4 / 3d * Math.tan(Math.PI / (2 * n));
            double[] intersect = VecHelper.intersect(end1, end2, cross1, cross2, Axis.Y);

            if (intersect == null) {
                handleLength = end2.distanceTo(end1) / 3;
                return;
            }

            radius = Math.abs(intersect[1]);
            handleLength = radius * factor;
            if (MathHelper.approximatelyEquals(handleLength, 0))
                handleLength = 1;
        }
    }

    public static class Segment {

        public int index;
        public Vec3d position;
        public Vec3d derivative;
        public Vec3d faceNormal;
        public Vec3d normal;

    }

    private static class Bezierator implements Iterator<Segment> {
        private final Segment segment;
        private final Vec3d end1;
        private final Vec3d end2;
        private final Vec3d finish1;
        private final Vec3d finish2;
        private final Vec3d faceNormal1;
        private final Vec3d faceNormal2;
        private final Runtime runtime;

        private Bezierator(BezierConnection bc, Vec3d offset) {
            runtime = bc.resolve();

            end1 = bc.starts.getFirst().add(offset);
            end2 = bc.starts.getSecond().add(offset);

            finish1 = bc.axes.getFirst().multiply(runtime.handleLength).add(end1);
            finish2 = bc.axes.getSecond().multiply(runtime.handleLength).add(end2);

            faceNormal1 = bc.normals.getFirst();
            faceNormal2 = bc.normals.getSecond();
            segment = new Segment();
            segment.index = -1; // will get incremented to 0 in #next()
        }

        @Override
        public boolean hasNext() {
            return segment.index + 1 <= runtime.segments;
        }

        @Override
        public Segment next() {
            segment.index++;
            float t = runtime.getSegmentT(segment.index);
            segment.position = VecHelper.bezier(end1, end2, finish1, finish2, t);
            segment.derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
            segment.faceNormal = faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
            segment.normal = segment.faceNormal.crossProduct(segment.derivative).normalize();
            return segment;
        }
    }

    public Object bakedSegments;
    public Object bakedGirders;

    @SuppressWarnings("unchecked")
    public <T> T getBakedSegments(Function<BezierConnection, T> factory) {
        if (bakedSegments != null) {
            return (T) bakedSegments;
        }
        T segments = factory.apply(this);
        bakedSegments = segments;
        return segments;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBakedGirders(Function<BezierConnection, T> factory) {
        if (bakedGirders != null) {
            return (T) bakedGirders;
        }
        T girders = factory.apply(this);
        bakedGirders = girders;
        return girders;
    }

    public Map<Pair<Integer, Integer>, Double> rasterise() {
        Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
        BlockPos tePosition = bePositions.getFirst();
        Vec3d end1 = starts.getFirst().subtract(Vec3d.of(tePosition)).add(0, 3 / 16f, 0);
        Vec3d end2 = starts.getSecond().subtract(Vec3d.of(tePosition)).add(0, 3 / 16f, 0);
        Vec3d axis1 = axes.getFirst();
        Vec3d axis2 = axes.getSecond();

        double handleLength = getHandleLength();
        Vec3d finish1 = axis1.multiply(handleLength).add(end1);
        Vec3d finish2 = axis2.multiply(handleLength).add(end2);

        Vec3d faceNormal1 = normals.getFirst();
        Vec3d faceNormal2 = normals.getSecond();

        int segCount = getSegmentCount();
        float[] lut = getStepLUT();
        Vec3d[] samples = new Vec3d[segCount];

        for (int i = 0; i < segCount; i++) {
            float t = MathHelper.clamp((i + 0.5f) * lut[i] / segCount, 0, 1);
            Vec3d result = VecHelper.bezier(end1, end2, finish1, finish2, t);
            Vec3d derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
            Vec3d faceNormal = faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
            Vec3d normal = faceNormal.crossProduct(derivative).normalize();
            Vec3d below = result.add(faceNormal.multiply(-.25f));
            Vec3d rail1 = below.add(normal.multiply(.05f));
            Vec3d rail2 = below.subtract(normal.multiply(.05f));
            Vec3d railMiddle = rail1.add(rail2).multiply(.5);
            samples[i] = railMiddle;
        }

        Vec3d center = end1.add(end2).multiply(0.5);

        Pair<Integer, Integer> prev = null;
        Pair<Integer, Integer> prev2 = null;
        Pair<Integer, Integer> prev3 = null;

        for (int i = 0; i < segCount; i++) {
            Vec3d railMiddle = samples[i];
            BlockPos pos = BlockPos.ofFloored(railMiddle);
            Pair<Integer, Integer> key = Pair.of(pos.getX(), pos.getZ());
            boolean alreadyPresent = yLevels.containsKey(key);
            if (alreadyPresent && yLevels.get(key) <= railMiddle.y)
                continue;
            yLevels.put(key, railMiddle.y);
            if (alreadyPresent)
                continue;

            if (prev3 != null) { // Remove obsolete pixels
                boolean doubledViaPrev = isLineDoubled(prev2, prev, key);
                boolean doubledViaPrev2 = isLineDoubled(prev3, prev2, prev);
                boolean prevCloser = diff(prev, center) > diff(prev2, center);

                if (doubledViaPrev2 && (!doubledViaPrev || !prevCloser)) {
                    yLevels.remove(prev2);
                    prev2 = prev;
                    prev = key;
                    continue;

                } else if (doubledViaPrev && doubledViaPrev2 && prevCloser) {
                    yLevels.remove(prev);
                    prev = key;
                    continue;
                }
            }

            prev3 = prev2;
            prev2 = prev;
            prev = key;
        }

        return yLevels;
    }

    private double diff(Pair<Integer, Integer> pFrom, Vec3d to) {
        return to.squaredDistanceTo(pFrom.getFirst() + 0.5, to.y, pFrom.getSecond() + 0.5);
    }

    private boolean isLineDoubled(Pair<Integer, Integer> pFrom, Pair<Integer, Integer> pVia, Pair<Integer, Integer> pTo) {
        int diff1x = pVia.getFirst() - pFrom.getFirst();
        int diff1z = pVia.getSecond() - pFrom.getSecond();
        int diff2x = pTo.getFirst() - pVia.getFirst();
        int diff2z = pTo.getSecond() - pVia.getSecond();
        return Math.abs(diff1x) + Math.abs(diff1z) == 1 && Math.abs(diff2x) + Math.abs(diff2z) == 1 && diff1x != diff2x && diff1z != diff2z;
    }

}
