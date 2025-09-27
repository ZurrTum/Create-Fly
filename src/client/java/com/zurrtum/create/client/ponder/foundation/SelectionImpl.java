package com.zurrtum.create.client.ponder.foundation;

import com.zurrtum.create.client.catnip.outliner.Outline.OutlineParams;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SelectionImpl {

    public static Selection of(BlockBox bb) {
        return new Simple(bb);
    }

    private static class Compound implements Selection {
        private final Set<BlockPos> posSet;
        @Nullable
        private Vec3d center;

        public Compound(Simple initial) {
            posSet = new HashSet<>();
            add(initial);
        }

        private Compound(Set<BlockPos> template) {
            posSet = new HashSet<>(template);
        }

        @Override
        public boolean test(BlockPos t) {
            return posSet.contains(t);
        }

        @Override
        public Selection add(Selection other) {
            other.forEach(p -> posSet.add(p.toImmutable()));
            center = null;
            return this;
        }

        @Override
        public Selection substract(Selection other) {
            other.forEach(p -> posSet.remove(p.toImmutable()));
            center = null;
            return this;
        }

        @Override
        public OutlineParams makeOutline(Outliner outliner, Object slot) {
            return outliner.showCluster(slot, posSet);
        }

        @Override
        public Vec3d getCenter() {
            return center == null ? center = evalCenter() : center;
        }

        private Vec3d evalCenter() {
            Vec3d center = Vec3d.ZERO;
            if (posSet.isEmpty())
                return center;
            for (BlockPos blockPos : posSet)
                center = center.add(Vec3d.of(blockPos));
            center = center.multiply(1f / posSet.size());
            return center.add(new Vec3d(.5, .5, .5));
        }

        @Override
        public Selection copy() {
            return new Compound(posSet);
        }

        @Override
        @NotNull
        public Iterator<BlockPos> iterator() {
            return posSet.iterator();
        }
    }

    private static class Simple implements Selection {
        private final BlockBox bb;
        private final Box aabb;
        private final Iterable<BlockPos> iterable;

        public Simple(BlockBox bb) {
            this.bb = bb;
            this.aabb = new Box(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX() + 1, bb.getMaxY() + 1, bb.getMaxZ() + 1);
            iterable = BlockPos.iterate(
                Math.min(bb.getMinX(), bb.getMaxX()),
                Math.min(bb.getMinY(), bb.getMaxY()),
                Math.min(bb.getMinZ(), bb.getMaxZ()),
                Math.max(bb.getMinX(), bb.getMaxX()),
                Math.max(bb.getMinY(), bb.getMaxY()),
                Math.max(bb.getMinZ(), bb.getMaxZ())
            );
        }

        @Override
        public boolean test(BlockPos t) {
            return bb.contains(t);
        }

        @Override
        public Selection add(Selection other) {
            return new Compound(this).add(other);
        }

        @Override
        public Selection substract(Selection other) {
            return new Compound(this).substract(other);
        }

        @Override
        public Vec3d getCenter() {
            return aabb.getCenter();
        }

        @Override
        public OutlineParams makeOutline(Outliner outliner, Object slot) {
            return outliner.showAABB(slot, aabb);
        }

        @Override
        public Selection copy() {
            return new Simple(new BlockBox(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ()));
        }

        @Override
        @NotNull
        public Iterator<BlockPos> iterator() {
            return iterable.iterator();
        }
    }

}