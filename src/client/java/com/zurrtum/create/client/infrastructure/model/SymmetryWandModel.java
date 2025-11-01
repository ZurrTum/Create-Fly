package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class SymmetryWandModel implements ItemModel, SpecialModelRenderer<Object> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/wand_of_symmetry");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/wand_of_symmetry/item");
    public static final Identifier CORE_ID = Identifier.of(MOD_ID, "item/wand_of_symmetry/core");
    public static final Identifier CORE_GLOW_ID = Identifier.of(MOD_ID, "item/wand_of_symmetry/core_glow");
    public static final Identifier BITS_ID = Identifier.of(MOD_ID, "item/wand_of_symmetry/bits");
    private static final int[] TINTS = new int[0];

    private final ModelSettings settings;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> bits;
    private final Supplier<Vector3f[]> vector;

    public SymmetryWandModel(ModelSettings settings, List<BakedQuad> item, List<BakedQuad> core, List<BakedQuad> coreGlow, List<BakedQuad> bits) {
        this.settings = settings;
        this.item = item;
        this.core = core;
        this.coreGlow = coreGlow;
        this.bits = bits;
        this.vector = Suppliers.memoize(() -> {
            Set<Vector3f> set = new HashSet<>();
            calculatePosition(item, set::add);
            calculatePosition(core, set::add);
            calculatePosition(coreGlow, set::add);
            calculatePosition(bits, set::add);
            return set.toArray(Vector3f[]::new);
        });
    }

    private static void calculatePosition(List<BakedQuad> quads, Consumer<Vector3f> consumer) {
        for (BakedQuad bakedQuad : quads) {
            BakedQuadFactory.calculatePosition(bakedQuad.vertexData(), consumer);
        }
    }

    @Override
    public void update(
        ItemRenderState state,
        ItemStack stack,
        ItemModelManager resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientWorld world,
        @Nullable HeldItemContext user,
        int seed
    ) {
        state.addModelKey(this);
        state.markAnimated();
        LayerRenderState renderState = state.newLayer();
        renderState.setVertices(vector);
        renderState.setSpecialModel(this, null);
        settings.addSettings(renderState, displayContext);
    }

    @Override
    public void render(
        @Nullable Object data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        int maxLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        RenderLayer translucent = RenderTypes.itemGlowingTranslucent();

        renderItem(displayContext, matrices, queue, light, overlay, item, TexturedRenderLayers.getItemEntityTranslucentCull());
        renderItem(displayContext, matrices, queue, maxLight, overlay, core, RenderTypes.itemGlowingSolid());
        renderItem(displayContext, matrices, queue, maxLight, overlay, coreGlow, translucent);

        matrices.push();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float floating = MathHelper.sin(worldTime) * .05f;
        float angle = worldTime * -10 % 360;
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        matrices.translate(-0.5f, floating - 0.5f, -0.5f);
        renderItem(displayContext, matrices, queue, maxLight, overlay, bits, translucent);
        matrices.pop();
    }

    private static void renderItem(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        int overlay,
        List<BakedQuad> item,
        RenderLayer layer
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, TINTS, item, layer, Glint.NONE);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getData(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<? extends ItemModel.Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(CORE_ID);
            resolver.markDependency(CORE_GLOW_ID);
            resolver.markDependency(BITS_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel model = baker.getModel(ITEM_ID);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new SymmetryWandModel(settings, quads, bake(baker, CORE_ID), bake(baker, CORE_GLOW_ID), bake(baker, BITS_ID));
        }

        private static List<BakedQuad> bake(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            return model.bakeGeometry(model.getTextures(), baker, ModelRotation.X0_Y0).getAllQuads();
        }
    }
}
