package com.zurrtum.create.client.mixin;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(SimpleModelPart.class)
public class SimpleModelPartMixin implements FabricBlockStateModelPart {
    @Shadow
    @Final
    private boolean useAmbientOcclusion;

    @Shadow
    @Final
    private QuadCollection quads;

    @Override
    public void emitQuads(@NonNull QuadEmitter emitter, @NonNull Predicate<@Nullable Direction> cullTest) {
        TriState ao = useAmbientOcclusion ? TriState.DEFAULT : TriState.FALSE;
        for (Direction direction : Iterate.directions) {
            if (cullTest.test(direction)) {
                continue;
            }
            for (BakedQuad quad : quads.getQuads(direction)) {
                emitter.cullFace(direction);
                emitter.fromBakedQuad(quad);
                emitter.ambientOcclusion(ao);
                emitter.emit();
            }
        }
        if (cullTest.test(null)) {
            return;
        }
        for (BakedQuad quad : quads.getQuads(null)) {
            emitter.fromBakedQuad(quad);
            emitter.ambientOcclusion(ao);
            emitter.emit();
        }
    }
}
