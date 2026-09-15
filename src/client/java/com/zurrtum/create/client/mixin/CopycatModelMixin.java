package com.zurrtum.create.client.mixin;

import com.google.common.base.Predicates;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.FabricTextureAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.QuadCollection.Builder;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(CopycatModel.class)
public class CopycatModelMixin {
    // Support Fabric custom model Low-performance implementation
    @Overwrite(remap = false)
    protected void addModelParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        BlockStateModel model,
        List<BlockStateModelPart> parts
    ) {
        SpriteFinder spriteFinder = ((FabricTextureAtlas) Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.BLOCKS)).spriteFinder();
        Builder ao = new Builder();
        Builder flat = new Builder();
        QuadEmitter emitter = Renderer.get().quadEmitter(quad -> {
            Direction direction = quad.cullFace();
            TextureAtlasSprite sprite = spriteFinder.find(quad);
            BakedQuad bakedQuad = quad.toBakedQuad(sprite);
            if (quad.ambientOcclusion() == TriState.FALSE) {
                if (direction != null) {
                    flat.addCulledFace(direction, bakedQuad);
                } else {
                    flat.addUnculledFace(bakedQuad);
                }
            } else {
                if (direction != null) {
                    ao.addCulledFace(direction, bakedQuad);
                } else {
                    ao.addUnculledFace(bakedQuad);
                }
            }
        });
        ((FabricBlockStateModel) model).emitQuads(emitter, world, pos, material, random, Predicates.alwaysFalse());
        Material.Baked particleMaterial = model.particleMaterial();
        QuadCollection quads = ao.build();
        if (quads != QuadCollection.EMPTY) {
            parts.add(new SimpleModelPart(quads, true, particleMaterial));
        }
        quads = flat.build();
        if (quads != QuadCollection.EMPTY) {
            parts.add(new SimpleModelPart(quads, false, particleMaterial));
        }
    }
}
