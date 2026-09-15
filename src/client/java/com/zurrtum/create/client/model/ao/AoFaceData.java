package com.zurrtum.create.client.model.ao;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter.Cache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AoFaceData {
    public float a0, a1, a2, a3;
    public int b0, b1, b2, b3;
    public int s0, s1, s2, s3;

    protected abstract float shade(CardinalLighting cardinalLighting);

    protected abstract void moveU(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveV(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void movePartialFace(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveFullFace(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveU0(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveU1(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveV0(MutableBlockPos searchPos, int x, int y, int z);

    protected abstract void moveV1(MutableBlockPos searchPos, int x, int y, int z);

    public final void computePartial(
        BlockAndTintGetter level,
        BlockState blockState,
        BlockPos pos,
        Cache lightCache,
        MutableBlockPos searchPos
    ) {
        computePartial(level, blockState, pos, lightCache, searchPos, shade(level.cardinalLighting()));
    }

    public final void computeBlock(
        BlockAndTintGetter level,
        BlockState blockState,
        BlockPos pos,
        Cache lightCache,
        MutableBlockPos searchPos
    ) {
        computeBlock(level, blockState, pos, lightCache, searchPos, shade(level.cardinalLighting()));
    }

    public final void computePartial(
        BlockAndTintGetter level,
        BlockState blockState,
        BlockPos pos,
        Cache lightCache,
        MutableBlockPos searchPos,
        float shadeBrightness
    ) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        moveV(searchPos, x, y, z);
        movePartialFace(searchPos, x, y, z);
        float shadeCenter = lightCache.getShadeBrightness(blockState, level, pos);
        compute(level, blockState, pos, lightCache, searchPos, shadeBrightness, shadeCenter, x, y, z);
    }

    public final void computeBlock(
        BlockAndTintGetter level,
        BlockState blockState,
        BlockPos pos,
        Cache lightCache,
        MutableBlockPos searchPos,
        float shadeBrightness
    ) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        moveU(searchPos, x, y, z);
        moveV(searchPos, x, y, z);
        moveFullFace(searchPos, x, y, z);
        BlockState searchState = level.getBlockState(searchPos);
        boolean solid = searchState.isSolidRender();
        BlockState centerState = solid ? blockState : searchState;
        BlockPos centerPos = solid ? pos : searchPos;
        float shadeCenter = lightCache.getShadeBrightness(searchState, level, searchPos);
        compute(level, centerState, centerPos, lightCache, searchPos, shadeBrightness, shadeCenter, x, y, z);
    }

    private void compute(
        BlockAndTintGetter level,
        BlockState centerState,
        BlockPos centerPos,
        Cache lightCache,
        MutableBlockPos searchPos,
        float shadeBrightness,
        float shadeCenter,
        int x,
        int y,
        int z
    ) {
        float shade = shadeBrightness * 0.25F;
        int light = lightCache.getLightCoords(centerState, level, centerPos);
        int centerBlock = light & 0xFFFF;
        int centerSky = light >>> 16;
        boolean translucentCenter = !centerState.isViewBlocking(
            level,
            centerPos
        ) || centerState.getLightDampening() == 0;
        int centerTranslucentBlock = translucentCenter ? centerBlock : 0x100;
        int centerMinBlock = translucentCenter ? centerBlock : 0;
        int centerTranslucentSky = translucentCenter ? centerSky : 0x100;
        int centerMinSky = translucentCenter ? centerSky : 0;
        moveU0(searchPos, x, y, z);
        BlockState searchState = level.getBlockState(searchPos);
        float shadeU0 = lightCache.getShadeBrightness(searchState, level, searchPos);
        light = lightCache.getLightCoords(searchState, level, searchPos);
        int blockU0 = light & 0xFFFF;
        int skyU0 = light >>> 16;
        boolean translucentU0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        int translucentBlockU0 = translucentU0 ? blockU0 : 0x100;
        int translucentSkyU0 = translucentU0 ? skyU0 : 0x100;
        moveU1(searchPos, x, y, z);
        searchState = level.getBlockState(searchPos);
        float shadeU1 = lightCache.getShadeBrightness(searchState, level, searchPos);
        light = lightCache.getLightCoords(searchState, level, searchPos);
        int blockU1 = light & 0xFFFF;
        int skyU1 = light >>> 16;
        boolean translucentU1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        int translucentBlockU1 = translucentU1 ? blockU1 : 0x100;
        int translucentSkyU1 = translucentU1 ? skyU1 : 0x100;
        moveU(searchPos, x, y, z);
        moveV0(searchPos, x, y, z);
        searchState = level.getBlockState(searchPos);
        float shadeTemp = lightCache.getShadeBrightness(searchState, level, searchPos) + shadeCenter;
        float lightLevelU0V0 = (shadeU0 + shadeTemp) * shade;
        float lightLevelU1V0 = (shadeU1 + shadeTemp) * shade;
        light = lightCache.getLightCoords(searchState, level, searchPos);
        int blockV0 = light & 0xFFFF;
        int skyV0 = light >>> 16;
        boolean translucentV0 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        int translucentBlockV0 = translucentV0 ? blockV0 : 0x100;
        int translucentSkyV0 = translucentV0 ? skyV0 : 0x100;
        boolean hasU0V0 = translucentV0 | translucentU0;
        boolean hasU1V0 = translucentV0 | translucentU1;
        moveV1(searchPos, x, y, z);
        searchState = level.getBlockState(searchPos);
        shadeTemp = lightCache.getShadeBrightness(searchState, level, searchPos) + shadeCenter;
        float lightLevelU0V1 = (shadeU0 + shadeTemp) * shade;
        float lightLevelU1V1 = (shadeU1 + shadeTemp) * shade;
        light = lightCache.getLightCoords(searchState, level, searchPos);
        int blockV1 = light & 0xFFFF;
        int skyV1 = light >>> 16;
        boolean translucentV1 = !searchState.isViewBlocking(level, searchPos) || searchState.getLightDampening() == 0;
        int translucentBlockV1 = translucentV1 ? blockV1 : 0x100;
        int translucentSkyV1 = translucentV1 ? skyV1 : 0x100;
        boolean hasU0V1 = translucentV1 | translucentU0;
        boolean hasU1V1 = translucentV1 | translucentU1;
        if (hasU0V1) {
            moveU0(searchPos, x, y, z);
            searchState = level.getBlockState(searchPos);
            a0 = Math.fma(lightCache.getShadeBrightness(searchState, level, searchPos), shade, lightLevelU0V1);
            light = lightCache.getLightCoords(searchState, level, searchPos);
            int blockU0V1 = light & 0xFFFF;
            int skyU0V1 = light >>> 16;
            boolean translucentU0V1 = !searchState.isViewBlocking(
                level,
                searchPos
            ) || searchState.getLightDampening() == 0;
            int translucentBlockU0V1 = translucentU0V1 ? blockU0V1 : 0x100;
            int translucentSkyU0V1 = translucentU0V1 ? skyU0V1 : 0x100;
            int minBlock = Math.min(
                Math.min(translucentBlockU0, translucentBlockV1),
                Math.min(centerTranslucentBlock, translucentBlockU0V1)
            ) & 0xff;
            b0 = Math.max(blockU0, minBlock) + Math.max(blockV1, minBlock) + Math.max(centerBlock, minBlock) + Math.max(blockU0V1,
                minBlock
            ) >>> 2;
            int minSky = Math.min(
                Math.min(translucentSkyU0, translucentSkyV1),
                Math.min(centerTranslucentSky, translucentSkyU0V1)
            ) & 0xff;
            s0 = Math.max(skyU0, minSky) + Math.max(skyV1, minSky) + Math.max(centerSky, minSky) + Math.max(
                skyU0V1,
                minSky
            ) >>> 2;
        } else {
            a0 = Math.fma(shadeU0, shade, lightLevelU0V1);
            b0 = (Math.max(blockU0, centerMinBlock) << 1) + centerBlock + Math.max(blockV1, centerMinBlock) >>> 2;
            s0 = (Math.max(skyU0, centerMinSky) << 1) + centerSky + Math.max(skyV1, centerMinSky) >>> 2;
        }
        if (hasU1V1) {
            moveU1(searchPos, x, y, z);
            searchState = level.getBlockState(searchPos);
            a3 = Math.fma(lightCache.getShadeBrightness(searchState, level, searchPos), shade, lightLevelU1V1);
            light = lightCache.getLightCoords(searchState, level, searchPos);
            int blockU1V1 = light & 0xFFFF;
            int skyU1V1 = light >>> 16;
            boolean translucentU1V1 = !searchState.isViewBlocking(
                level,
                searchPos
            ) || searchState.getLightDampening() == 0;
            int translucentBlockU1V1 = translucentU1V1 ? blockU1V1 : 0x100;
            int translucentSkyU1V1 = translucentU1V1 ? skyU1V1 : 0x100;
            int minBlock = Math.min(
                Math.min(translucentBlockU1, translucentBlockV1),
                Math.min(centerTranslucentBlock, translucentBlockU1V1)
            ) & 0xff;
            b3 = Math.max(blockU1, minBlock) + Math.max(blockV1, minBlock) + Math.max(centerBlock, minBlock) + Math.max(blockU1V1,
                minBlock
            ) >>> 2;
            int minSky = Math.min(
                Math.min(translucentSkyU1, translucentSkyV1),
                Math.min(centerTranslucentSky, translucentSkyU1V1)
            ) & 0xff;
            s3 = Math.max(skyU1, minSky) + Math.max(skyV1, minSky) + Math.max(centerSky, minSky) + Math.max(
                skyU1V1,
                minSky
            ) >>> 2;
        } else {
            a3 = Math.fma(shadeU1, shade, lightLevelU1V1);
            b3 = (Math.max(blockU1, centerMinBlock) << 1) + centerBlock + Math.max(blockV1, centerMinBlock) >>> 2;
            s3 = (Math.max(skyU1, centerMinSky) << 1) + centerSky + Math.max(skyV1, centerMinSky) >>> 2;
        }
        moveV0(searchPos, x, y, z);
        if (hasU0V0) {
            moveU0(searchPos, x, y, z);
            searchState = level.getBlockState(searchPos);
            a1 = Math.fma(lightCache.getShadeBrightness(searchState, level, searchPos), shade, lightLevelU0V0);
            light = lightCache.getLightCoords(searchState, level, searchPos);
            int blockU0V0 = light & 0xFFFF;
            int skyU0V0 = light >>> 16;
            boolean translucentU0V0 = !searchState.isViewBlocking(
                level,
                searchPos
            ) || searchState.getLightDampening() == 0;
            int translucentBlockU0V0 = translucentU0V0 ? blockU0V0 : 0x100;
            int translucentSkyU0V0 = translucentU0V0 ? skyU0V0 : 0x100;
            int minBlock = Math.min(
                Math.min(translucentBlockU0, translucentBlockV0),
                Math.min(centerTranslucentBlock, translucentBlockU0V0)
            ) & 0xff;
            b1 = Math.max(blockU0, minBlock) + Math.max(blockV0, minBlock) + Math.max(centerBlock, minBlock) + Math.max(blockU0V0,
                minBlock
            ) >>> 2;
            int minSky = Math.min(
                Math.min(translucentSkyU0, translucentSkyV0),
                Math.min(centerTranslucentSky, translucentSkyU0V0)
            ) & 0xff;
            s1 = Math.max(skyU0, minSky) + Math.max(skyV0, minSky) + Math.max(centerSky, minSky) + Math.max(
                skyU0V0,
                minSky
            ) >>> 2;
        } else {
            a1 = Math.fma(shadeU0, shade, lightLevelU0V0);
            b1 = (Math.max(blockU0, centerMinBlock) << 1) + centerBlock + Math.max(blockV0, centerMinBlock) >>> 2;
            s1 = (Math.max(skyU0, centerMinSky) << 1) + centerSky + Math.max(skyV0, centerMinSky) >>> 2;
        }
        if (hasU1V0) {
            moveU1(searchPos, x, y, z);
            searchState = level.getBlockState(searchPos);
            a2 = Math.fma(lightCache.getShadeBrightness(searchState, level, searchPos), shade, lightLevelU1V0);
            light = lightCache.getLightCoords(searchState, level, searchPos);
            int blockU1V0 = light & 0xFFFF;
            int skyU1V0 = light >>> 16;
            boolean translucentU1V0 = !searchState.isViewBlocking(
                level,
                searchPos
            ) || searchState.getLightDampening() == 0;
            int translucentBlockU1V0 = translucentU1V0 ? blockU1V0 : 0x100;
            int translucentSkyU1V0 = translucentU1V0 ? skyU1V0 : 0x100;
            int minBlock = Math.min(
                Math.min(translucentBlockU1, translucentBlockV0),
                Math.min(centerTranslucentBlock, translucentBlockU1V0)
            ) & 0xff;
            b2 = Math.max(blockU1, minBlock) + Math.max(blockV0, minBlock) + Math.max(centerBlock, minBlock) + Math.max(blockU1V0,
                minBlock
            ) >>> 2;
            int minSky = Math.min(
                Math.min(translucentSkyU1, translucentSkyV0),
                Math.min(centerTranslucentSky, translucentSkyU1V0)
            ) & 0xff;
            s2 = Math.max(skyU1, minSky) + Math.max(skyV0, minSky) + Math.max(centerSky, minSky) + Math.max(
                skyU1V0,
                minSky
            ) >>> 2;
        } else {
            a2 = Math.fma(shadeU1, shade, lightLevelU1V0);
            b2 = (Math.max(blockU1, centerMinBlock) << 1) + centerBlock + Math.max(blockV0, centerMinBlock) >>> 2;
            s2 = (Math.max(skyU1, centerMinSky) << 1) + centerSky + Math.max(skyV0, centerMinSky) >>> 2;
        }
    }
}
