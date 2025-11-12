package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.blockEntity.EntityControlStructureProcessor;
import com.zurrtum.create.foundation.blockEntity.StructureEntityInfoIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Unique
    private static final ThreadLocal<List<EntityControlStructureProcessor>> list = new ThreadLocal<>();

    @Inject(method = "placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;ZLnet/minecraft/util/ProblemReporter;)V"))
    private void setProcessors(
        ServerLevelAccessor world,
        BlockPos pos,
        BlockPos pivot,
        StructurePlaceSettings placementData,
        RandomSource random,
        int flags,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (world instanceof Level) {
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

    @WrapOperation(method = "placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;ZLnet/minecraft/util/ProblemReporter;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<StructureTemplate.StructureEntityInfo> getIterator(
        List<StructureTemplate.StructureEntityInfo> instance,
        Operation<Iterator<StructureTemplate.StructureEntityInfo>> original,
        @Local(argsOnly = true) ServerLevelAccessor access
    ) {
        Iterator<StructureTemplate.StructureEntityInfo> iterator = original.call(instance);
        List<EntityControlStructureProcessor> controls = list.get();
        if (controls == null) {
            return iterator;
        }
        return new StructureEntityInfoIterator((Level) access, controls, iterator);
    }

    @Inject(method = "placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;ZLnet/minecraft/util/ProblemReporter;)V", at = @At("TAIL"))
    private void clearProcessors(CallbackInfo ci) {
        list.remove();
    }
}
