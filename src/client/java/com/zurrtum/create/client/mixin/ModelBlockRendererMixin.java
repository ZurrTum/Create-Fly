package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.VanillinMeshEmitterManager;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.ao.ModelLighter;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Shadow
    @Final
    private static Direction[] DIRECTIONS;

    @Shadow
    @Final
    private BlockModelLighter lighter;

    @Shadow
    @Final
    private boolean ambientOcclusion;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    @Final
    private List<BlockStateModelPart> parts;

    @Shadow
    @Final
    private BlockPos.MutableBlockPos scratchPos;

    @Shadow
    @Final
    private QuadInstance quadInstance;

    @Shadow
    protected abstract void resetTintCache();

    @Shadow
    protected abstract void tesselateAmbientOcclusion(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        List<BlockStateModelPart> parts,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos
    );

    @Shadow
    protected abstract void putQuadWithTint(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        BakedQuad quad
    );

    @Shadow
    protected abstract boolean shouldRenderFace(
        BlockAndTintGetter level,
        BlockState state,
        Direction direction,
        BlockPos neighborPos
    );

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "()Lnet/minecraft/client/renderer/block/BlockModelLighter;"))
    private BlockModelLighter createModelLighter(Operation<BlockModelLighter> original) {
        return new ModelLighter();
    }

    @Overwrite
    public void tesselateBlock(
        final BlockQuadOutput output,
        final float x,
        final float y,
        final float z,
        final BlockAndTintGetter level,
        final BlockPos pos,
        final BlockState blockState,
        final BlockStateModel model,
        final long seed
    ) {
        random.setSeed(seed);
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(level, pos, blockState, random, parts);
        } else {
            model.collectParts(random, parts);
        }
        if (!parts.isEmpty()) {
            try {
                Vec3 offset = blockState.getOffset(pos);
                if (ambientOcclusion && (blockState.getBlock() instanceof CopycatBlock block ?
                    block.getLuminance(level, pos) : blockState.getLightEmission()) == 0 && parts.getFirst()
                    .useAmbientOcclusion()) {
                    if (output instanceof VanillinMeshEmitterManager meshEmitter) {
                        meshEmitter.prepareForModelLayer(true);
                    }
                    ((ModelLighter) lighter).prepare();
                    tesselateAmbientOcclusion(
                        output,
                        x + (float) offset.x,
                        y + (float) offset.y,
                        z + (float) offset.z,
                        parts,
                        level,
                        blockState,
                        pos
                    );
                } else {
                    if (output instanceof VanillinMeshEmitterManager meshEmitter) {
                        meshEmitter.prepareForModelLayer(false);
                    }
                    tesselateFlat(
                        output,
                        x + (float) offset.x,
                        y + (float) offset.y,
                        z + (float) offset.z,
                        parts,
                        level,
                        blockState,
                        pos
                    );
                }
            } finally {
                parts.clear();
                resetTintCache();
            }
        }
    }

    @Overwrite
    private void tesselateFlat(
        final BlockQuadOutput output,
        final float x,
        final float y,
        final float z,
        final List<BlockStateModelPart> parts,
        final BlockAndTintGetter level,
        final BlockState state,
        final BlockPos pos
    ) {
        BlockState lightState;
        if (state.getBlock() instanceof CopycatBlock) {
            lightState = CopycatBlock.getMaterial(level, pos);
        } else {
            lightState = state;
        }
        int cacheValid = 0;
        int shouldRenderFaceCache = 0;

        for (BlockStateModelPart part : parts) {
            for (Direction direction : DIRECTIONS) {
                int cacheMask = 1 << direction.ordinal();
                boolean validCacheForDirection = (cacheValid & cacheMask) != 0;
                boolean shouldRenderFace = (shouldRenderFaceCache & cacheMask) != 0;
                if (!validCacheForDirection || shouldRenderFace) {
                    List<BakedQuad> culledQuads = part.getQuads(direction);
                    if (!culledQuads.isEmpty()) {
                        BlockPos relativePos = scratchPos.setWithOffset(pos, direction);
                        if (!validCacheForDirection) {
                            shouldRenderFace = shouldRenderFace(level, state, direction, relativePos);
                            cacheValid |= cacheMask;
                            if (shouldRenderFace) {
                                shouldRenderFaceCache |= cacheMask;
                            }
                        }

                        if (shouldRenderFace) {
                            int lightCoords = lighter.getLightCoords(lightState, level, relativePos);

                            for (BakedQuad quad : culledQuads) {
                                lighter.prepareQuadFlat(level, state, pos, lightCoords, quad, quadInstance);
                                putQuadWithTint(output, x, y, z, level, state, pos, quad);
                            }
                        }
                    }
                }
            }

            for (BakedQuad quad : part.getQuads(null)) {
                lighter.prepareQuadFlat(level, state, pos, -1, quad, quadInstance);
                putQuadWithTint(output, x, y, z, level, state, pos, quad);
            }
        }
    }
}
