package com.zurrtum.create.client.mixin;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Predicate;

@Mixin(WrapperBlockStateModel.class)
public abstract class WrapperBlockStateModelMixin implements FabricBlockStateModel {
    @Unique
    private static final ThreadLocal<List<BlockStateModelPart>> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ObjectArrayList::new);

    @Shadow(remap = false)
    public abstract void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    );

    @Shadow(remap = false)
    public abstract boolean needUpdateTerrainParticle();

    @Shadow(remap = false)
    public abstract Baked particleMaterial();

    @Shadow(remap = false)
    public abstract Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state);

    @Shadow(remap = false)
    private static BlockStateModel extractBlockDestroyModel(
        WrapperBlockStateModel model,
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        long seed
    ) {
        return null;
    }

    @Unique
    private static BlockStateModel unwrap(BlockStateModel model) {
        while (model instanceof WrapperBlockStateModelAccessor accessor) {
            BlockStateModel child = accessor.getWrapped();
            if (child == model) {
                return model;
            }
            model = child;
        }
        return model;
    }

    @Overwrite(remap = false)
    public static BlockStateModel getBlockDestroyModel(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        long seed
    ) {
        if (unwrap(model) instanceof WrapperBlockStateModel wrapper) {
            return extractBlockDestroyModel(wrapper, level, pos, state, seed);
        }
        return null;
    }

    @Overwrite(remap = false)
    public static BlockStateModel getBlockDestroyModel(
        BlockStateModel model,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state
    ) {
        if (unwrap(model) instanceof WrapperBlockStateModel wrapper) {
            return extractBlockDestroyModel(wrapper, level, pos, state, state.getSeed(pos));
        }
        return null;
    }

    @Override
    public void emitQuads(
        @NonNull QuadEmitter emitter,
        @NonNull BlockAndTintGetter level,
        @NonNull BlockPos pos,
        @NonNull BlockState state,
        @NonNull RandomSource random,
        @NonNull Predicate<@Nullable Direction> cullTest
    ) {
        List<BlockStateModelPart> parts = THREAD_LOCAL_OBJECTS.get();
        addPartsWithInfo(level, pos, state, random, parts);
        int size = parts.size();
        if (size == 0) {
            return;
        }
        BlockStateModelPart part = parts.getFirst();
        TriState ao = part.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
        emitQuads(emitter, part, ao, cullTest);
        for (int i = 1; i < size; i++) {
            emitQuads(emitter, parts.get(i), ao, cullTest);
        }
        parts.clear();
    }

    @Unique
    private static void emitQuads(
        QuadEmitter emitter,
        BlockStateModelPart part,
        TriState ao,
        Predicate<@Nullable Direction> cullTest
    ) {
        for (Direction direction : Iterate.directions) {
            if (cullTest.test(direction)) {
                continue;
            }
            for (BakedQuad quad : part.getQuads(direction)) {
                emitter.cullFace(direction);
                emitter.fromBakedQuad(quad);
                emitter.ambientOcclusion(ao);
                emitter.emit();
            }
        }
        if (cullTest.test(null)) {
            return;
        }
        for (BakedQuad quad : part.getQuads(null)) {
            emitter.fromBakedQuad(quad);
            emitter.ambientOcclusion(ao);
            emitter.emit();
        }
    }

    @Override
    public @NonNull Baked particleMaterial(
        @NonNull BlockAndTintGetter level,
        @NonNull BlockPos pos,
        @NonNull BlockState state
    ) {
        if (needUpdateTerrainParticle()) {
            return particleMaterialWithInfo(level, WrapperBlockStateModel.findPos(level, pos, state), state);
        } else {
            return particleMaterial();
        }
    }
}
