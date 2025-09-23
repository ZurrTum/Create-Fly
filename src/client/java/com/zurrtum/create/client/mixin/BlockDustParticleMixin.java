package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockDustParticle.class)
public abstract class BlockDustParticleMixin {
    @Unique
    private static final Vec3i[] DIRECTIONS = new Vec3i[]{new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(
        -1,
        0,
        -1
    ), new Vec3i(1, 0, -1), new Vec3i(1, 0, 1), new Vec3i(-1, 0, 1), new Vec3i(0, -1, 0), new Vec3i(0, 1, 0), new Vec3i(
        0,
        -1,
        -1
    ), new Vec3i(0, -1, 1), new Vec3i(-1, -1, 0), new Vec3i(1, -1, 0), new Vec3i(-1, -1, -1), new Vec3i(1, -1, -1), new Vec3i(1, -1, 1), new Vec3i(
        -1,
        -1,
        1
    )};

    @Unique
    private static BlockPos findPos(ClientWorld world, BlockPos pos, BlockState state) {
        BlockState target = world.getBlockState(pos);
        if (target == state) {
            return pos;
        }
        BlockPos.Mutable position = pos.mutableCopy();
        for (Vec3i move : DIRECTIONS) {
            target = world.getBlockState(position.set(pos, move));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite onParticle(
        BlockModels models,
        BlockState state,
        Operation<Sprite> original,
        @Local(argsOnly = true) ClientWorld world,
        @Local(argsOnly = true) BlockPos pos,
        @Share("pos") LocalRef<BlockPos> blockPos
    ) {
        BlockStateModel model = models.getModel(state);
        if (model instanceof WrapperBlockStateModel wrapper) {
            blockPos.set(findPos(world, pos, state));
            return wrapper.particleSpriteWithInfo(world, blockPos.get(), state);
        } else {
            return model.particleSprite();
        }
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean isGrass(
        BlockState state,
        Block block,
        Operation<Boolean> original,
        @Local(argsOnly = true) ClientWorld world,
        @Share("pos") LocalRef<BlockPos> blockPos
    ) {
        if (state.getBlock() instanceof CopycatBlock) {
            BlockPos pos = blockPos.get();
            if (pos != null) {
                state = CopycatBlock.getMaterial(world, pos);
            }
        }
        return original.call(state, block);
    }
}
