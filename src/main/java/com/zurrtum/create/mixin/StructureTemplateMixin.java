package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.blockEntity.EntityControlStructureProcessor;
import com.zurrtum.create.foundation.blockEntity.StructureEntityInfoIterator;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Unique
    private static final ThreadLocal<List<EntityControlStructureProcessor>> list = new ThreadLocal<>();

    @Inject(method = "place(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/structure/StructurePlacementData;Lnet/minecraft/util/math/random/Random;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructureTemplate;spawnEntities(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockMirror;Lnet/minecraft/util/BlockRotation;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockBox;ZLnet/minecraft/util/ErrorReporter;)V"))
    private void setProcessors(
        ServerWorldAccess world,
        BlockPos pos,
        BlockPos pivot,
        StructurePlacementData placementData,
        Random random,
        int flags,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (world instanceof World) {
            List<EntityControlStructureProcessor> controls = new ArrayList<>();
            for (StructureProcessor processor : placementData.getProcessors()) {
                if (processor instanceof EntityControlStructureProcessor control) {
                    controls.add(control);
                }
            }
            if (!controls.isEmpty()) {
                list.set(controls);
            }
        }
    }

    @WrapOperation(method = "spawnEntities(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockMirror;Lnet/minecraft/util/BlockRotation;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockBox;ZLnet/minecraft/util/ErrorReporter;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<StructureTemplate.StructureEntityInfo> getIterator(
        List<StructureTemplate.StructureEntityInfo> instance,
        Operation<Iterator<StructureTemplate.StructureEntityInfo>> original,
        @Local(argsOnly = true) ServerWorldAccess access
    ) {
        Iterator<StructureTemplate.StructureEntityInfo> iterator = original.call(instance);
        List<EntityControlStructureProcessor> controls = list.get();
        if (controls == null) {
            return iterator;
        }
        return new StructureEntityInfoIterator((World) access, controls, iterator);
    }

    @Inject(method = "spawnEntities(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockMirror;Lnet/minecraft/util/BlockRotation;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockBox;ZLnet/minecraft/util/ErrorReporter;)V", at = @At("TAIL"))
    private void clearProcessors(CallbackInfo ci) {
        list.remove();
    }
}
