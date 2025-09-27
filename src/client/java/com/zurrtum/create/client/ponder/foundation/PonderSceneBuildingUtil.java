package com.zurrtum.create.client.ponder.foundation;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.ponder.api.scene.*;
import net.minecraft.util.math.*;

/**
 * Helpful shortcuts for marking boundaries, points or sections inside the scene
 */
public class PonderSceneBuildingUtil implements SceneBuildingUtil {

    private final SelectionUtil select;
    private final VectorUtil vector;
    private final PositionUtil grid;

    private final BlockBox sceneBounds;

    PonderSceneBuildingUtil(BlockBox sceneBounds) {
        this.sceneBounds = sceneBounds;
        this.select = new PonderSelectionUtil();
        this.vector = new PonderVectorUtil();
        this.grid = new PonderPositionUtil();
    }

    @Override
    public SelectionUtil select() {
        return select;
    }

    @Override
    public VectorUtil vector() {
        return vector;
    }

    @Override
    public PositionUtil grid() {
        return grid;
    }

    public static class PonderPositionUtil implements PositionUtil {

        @Override
        public BlockPos at(int x, int y, int z) {
            return new BlockPos(x, y, z);
        }

        @Override
        public BlockPos zero() {
            return at(0, 0, 0);
        }

    }

    public class PonderVectorUtil implements VectorUtil {

        @Override
        public Vec3d centerOf(int x, int y, int z) {
            return centerOf(grid().at(x, y, z));
        }

        @Override
        public Vec3d centerOf(BlockPos pos) {
            return VecHelper.getCenterOf(pos);
        }

        @Override
        public Vec3d topOf(int x, int y, int z) {
            return blockSurface(grid().at(x, y, z), Direction.UP);
        }

        @Override
        public Vec3d topOf(BlockPos pos) {
            return blockSurface(pos, Direction.UP);
        }

        @Override
        public Vec3d blockSurface(BlockPos pos, Direction face) {
            return blockSurface(pos, face, 0);
        }

        @Override
        public Vec3d blockSurface(BlockPos pos, Direction face, float margin) {
            return centerOf(pos).add(Vec3d.of(face.getVector()).multiply(.5f + margin));
        }

        @Override
        public Vec3d of(double x, double y, double z) {
            return new Vec3d(x, y, z);
        }

    }

    public class PonderSelectionUtil implements SelectionUtil {

        @Override
        public Selection everywhere() {
            return SelectionImpl.of(sceneBounds);
        }

        @Override
        public Selection position(int x, int y, int z) {
            return position(grid().at(x, y, z));
        }

        @Override
        public Selection position(BlockPos pos) {
            return cuboid(pos, BlockPos.ZERO);
        }

        @Override
        public Selection fromTo(int x, int y, int z, int x2, int y2, int z2) {
            return fromTo(new BlockPos(x, y, z), new BlockPos(x2, y2, z2));
        }

        @Override
        public Selection fromTo(BlockPos pos1, BlockPos pos2) {
            return cuboid(pos1, pos2.subtract(pos1));
        }

        @Override
        public Selection column(int x, int z) {
            return cuboid(new BlockPos(x, 1, z), new Vec3i(0, sceneBounds.getBlockCountY(), 0));
        }

        @Override
        public Selection layer(int y) {
            return layers(y, 1);
        }

        @Override
        public Selection layersFrom(int y) {
            return layers(y, sceneBounds.getBlockCountY() - y);
        }

        @Override
        public Selection layers(int y, int height) {
            return cuboid(
                new BlockPos(0, y, 0),
                new Vec3i(sceneBounds.getBlockCountX() - 1, Math.min(sceneBounds.getBlockCountY() - y, height) - 1, sceneBounds.getBlockCountZ() - 1)
            );
        }

        @Override
        public Selection cuboid(BlockPos origin, Vec3i size) {
            return SelectionImpl.of(BlockBox.create(origin, origin.add(size)));
        }

    }

}