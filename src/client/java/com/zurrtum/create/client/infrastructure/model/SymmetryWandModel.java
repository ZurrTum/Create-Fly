package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitCustomLayerWithLight;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class SymmetryWandModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/wand_of_symmetry");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/item");
    public static final Identifier CORE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/core");
    public static final Identifier CORE_GLOW_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/wand_of_symmetry/core_glow"
    );
    public static final Identifier BITS_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/bits");
    private final ModelRenderProperties settings;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> bits;
    private final Supplier<Vector3fc[]> vector;

    public SymmetryWandModel(
        ModelRenderProperties settings,
        List<BakedQuad> item,
        List<BakedQuad> core,
        List<BakedQuad> coreGlow,
        List<BakedQuad> bits
    ) {
        this.settings = settings;
        this.item = item;
        this.core = core;
        this.coreGlow = coreGlow;
        this.bits = bits;
        vector = Suppliers.memoize(() -> {
            Set<Vector3fc> set = new HashSet<>();
            addPosition(set, item);
            addPosition(set, core);
            addPosition(set, coreGlow);
            addPosition(set, bits);
            return set.toArray(Vector3fc[]::new);
        });
    }

    private static void addPosition(Set<Vector3fc> set, List<BakedQuad> quads) {
        for (BakedQuad bakedQuad : quads) {
            set.add(bakedQuad.position0());
            set.add(bakedQuad.position1());
            set.add(bakedQuad.position2());
            set.add(bakedQuad.position3());
        }
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner user,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        submitQuads(state, settings, displayContext, item).setExtents(vector);
        int maxLight = displayContext == ItemDisplayContext.GUI ? 0 : LightCoordsUtil.FULL_BRIGHT;
        submitCustomLayerWithLight(state, settings, displayContext, maxLight, core);
        submitCustomLayerWithLight(state, settings, displayContext, maxLight, coreGlow);
        LayerRenderState bitsLayer = submitCustomLayerWithLight(state, settings, displayContext, maxLight, bits);
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float floating = Mth.sin(worldTime) * 0.05f;
        float angle = worldTime * -10 % 360;
        Matrix4f pose = bitsLayer.localTransform;
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotateY(Mth.DEG_TO_RAD * angle);
        pose.translate(-0.5f, floating - 0.5f, -0.5f);
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(CORE_ID);
            resolver.markDependency(CORE_GLOW_ID);
            resolver.markDependency(BITS_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            List<BakedQuad> core = BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(baker, CORE_ID),
                ChunkSectionLayer.SOLID,
                CreateRenderTypes.itemGlowingSolid()
            );
            List<BakedQuad> coreGlow = BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(
                    baker,
                    CORE_GLOW_ID
                ),
                ChunkSectionLayer.TRANSLUCENT,
                CreateRenderTypes.itemGlowingTranslucent()
            );
            List<BakedQuad> bits = BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(baker, BITS_ID),
                ChunkSectionLayer.TRANSLUCENT,
                CreateRenderTypes.itemGlowingTranslucent()
            );
            return new SymmetryWandModel(settings, quads, core, coreGlow, bits);
        }
    }
}
