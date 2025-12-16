package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class SymmetryWandModel implements ItemModel, SpecialModelRenderer<Object> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/wand_of_symmetry");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/item");
    public static final Identifier CORE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/core");
    public static final Identifier CORE_GLOW_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/core_glow");
    public static final Identifier BITS_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wand_of_symmetry/bits");
    private static final int[] TINTS = new int[0];

    private final RenderType blockLayer = Sheets.translucentBlockItemSheet();
    private final RenderType itemLayer = CreateRenderTypes.itemGlowingSolid();
    private final RenderType translucent = CreateRenderTypes.itemGlowingTranslucent();
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
        this.vector = Suppliers.memoize(() -> {
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
        LayerRenderState renderState = state.newLayer();
        renderState.setExtents(vector);
        renderState.setupSpecialModel(this, null);
        settings.applyToLayer(renderState, displayContext);
    }

    @Override
    public void submit(
        @Nullable Object data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        int maxLight = LightCoordsUtil.FULL_BRIGHT;

        renderItem(displayContext, matrices, queue, light, overlay, item, blockLayer);
        renderItem(displayContext, matrices, queue, maxLight, overlay, core, itemLayer);
        renderItem(displayContext, matrices, queue, maxLight, overlay, coreGlow, translucent);

        matrices.pushPose();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float floating = Mth.sin(worldTime) * .05f;
        float angle = worldTime * -10 % 360;
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.YP.rotationDegrees(angle));
        matrices.translate(-0.5f, floating - 0.5f, -0.5f);
        renderItem(displayContext, matrices, queue, maxLight, overlay, bits, translucent);
        matrices.popPose();
    }

    private static void renderItem(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        List<BakedQuad> item,
        RenderType layer
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, TINTS, item, layer, FoilType.NONE);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.SymmetryWandModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.SymmetryWandModel.Unbaked::new);

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
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new SymmetryWandModel(settings, quads, bake(baker, CORE_ID), bake(baker, CORE_GLOW_ID), bake(baker, BITS_ID));
        }

        private static List<BakedQuad> bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            return model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
        }
    }
}
