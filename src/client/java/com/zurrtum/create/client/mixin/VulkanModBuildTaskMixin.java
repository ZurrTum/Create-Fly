package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.vulkanmod.render.chunk.build.task.CompileResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands block entities to Flywheel on the VulkanMod chunk build path.
 *
 * <p>VulkanMod does not use vanilla's {@code SectionCompiler}. It replaces the whole
 * chunk-build pipeline with its own ({@code net.vulkanmod.render.chunk.build.*}) and drives it from
 * {@code net.vulkanmod.render.chunk.build.task.BuildTask}, whose {@code compile} loop walks the
 * same block positions and dispatches every block entity it finds to its own private
 * {@code handleBlockEntity}. As a result {@link SectionCompilerMixin} - the only place vanilla
 * ever offers a block entity to Flywheel - is simply never called under VulkanMod, and no visual is
 * ever created for any block entity in the world.
 *
 * <p>The loop body is structurally identical to the vanilla one it replaces:
 *
 * <pre>
 *   if (state.hasBlockEntity()) {
 *       BlockEntity be = region.getBlockEntity(pos);
 *       if (be != null) handleBlockEntity(results, be);
 *   }
 * </pre>
 *
 * so injecting at the head of that method and cancelling when Flywheel takes the entity reproduces
 * the vanilla behaviour exactly: queue the visual, and skip the native render so it is not drawn
 * twice.
 *
 * <p>The target is named as a string and marked {@link Pseudo} on purpose. VulkanMod ships as a
 * loose jar with no Maven coordinates, so it cannot be a hard compile dependency, and the mixin must
 * be skipped rather than crash the game when VulkanMod is absent.
 */
@Pseudo
@Mixin(targets = "net.vulkanmod.render.chunk.build.task.BuildTask")
public class VulkanModBuildTaskMixin {
    @Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void flywheel$tryAddBlockEntity(
        CompileResult results,
        E blockEntity,
        CallbackInfo ci
    ) {
        if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }
}