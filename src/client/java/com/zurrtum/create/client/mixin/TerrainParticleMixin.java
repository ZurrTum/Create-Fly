package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin {
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
    private static BlockPos findPos(ClientLevel world, BlockPos pos, BlockState state) {
        BlockState target = world.getBlockState(pos);
        if (target == state) {
            return pos;
        }
        BlockPos.MutableBlockPos position = pos.mutable();
        for (Vec3i move : DIRECTIONS) {
            target = world.getBlockState(position.setWithOffset(pos, move));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private static TextureAtlasSprite onParticle(
        BlockModelShaper models,
        BlockState state,
        Operation<TextureAtlasSprite> original,
        @Local(argsOnly = true) ClientLevel world,
        @Local(argsOnly = true) BlockPos pos,
        @Share("pos") LocalRef<BlockPos> blockPos
    ) {
        BlockStateModel model = models.getBlockModel(state);
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            blockPos.set(findPos(world, pos, state));
            return wrapper.particleSpriteWithInfo(world, blockPos.get(), state);
        } else {
            return model.particleIcon();
        }
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean isGrass(
        BlockState state,
        Block block,
        Operation<Boolean> original,
        @Local(argsOnly = true) ClientLevel world,
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
