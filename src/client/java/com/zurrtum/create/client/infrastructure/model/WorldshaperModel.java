package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitCustomLayerWithLightTint;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class WorldshaperModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/handheld_worldshaper");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/item");
    public static final Identifier CORE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/core");
    public static final Identifier CORE_GLOW_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/handheld_worldshaper/core_glow"
    );
    public static final Identifier ACCELERATOR_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/handheld_worldshaper/accelerator"
    );
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final int[] TINTS = new int[]{-1};
    private static final int[][] LIGHT_TINTS = new int[][]{
        {0xff313138},
        {0xff3d3d42},
        {0xff4b494b},
        {0xff585451},
        {0xff665f57},
        {0xff7a7063},
        {0xff8e8070},
        {0xffa1917c},
        {0xffb3a18a},
        {0xffc5b299},
        {0xffd7c3ab},
        {0xffebd7c1},
        {0xfffff3e1},
        {0xffffffff},
        {0xffffffff},
        {0xffffffff}
    };

    private final ModelRenderProperties settings;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> accelerator;
    private final Supplier<Vector3fc[]> extents;

    public WorldshaperModel(
        ModelRenderProperties settings,
        List<BakedQuad> item,
        List<BakedQuad> core,
        List<BakedQuad> coreGlow,
        List<BakedQuad> accelerator
    ) {
        this.settings = settings;
        this.item = item;
        this.core = core;
        this.coreGlow = coreGlow;
        this.accelerator = accelerator;
        extents = Suppliers.memoize(() -> {
            Set<Vector3fc> set = new HashSet<>();
            addPosition(set, item);
            addPosition(set, core);
            addPosition(set, coreGlow);
            addPosition(set, accelerator);
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
        submitQuads(state, settings, displayContext, item).setExtents(extents);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean mainHand = player.getMainHandItem() == stack;
        boolean rightHand = mainHand ^ player.getMainArm() == HumanoidArm.LEFT;
        float pt = AnimationTickHolder.getPartialTicks();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float animation = Mth.clamp(Create.ZAPPER_RENDER_HANDLER.getAnimation(rightHand, pt) * 5, 0, 1);
        float angle = worldTime * -25;
        float multiplier;
        if (mainHand || player.getOffhandItem() == stack) {
            angle += 360 * animation;
            multiplier = animation;
        } else {
            multiplier = Mth.sin(worldTime * 5);
        }
        int lightItensity = (int) (15 * Mth.clamp(multiplier, 0, 1));
        if (displayContext == ItemDisplayContext.GUI) {
            int[] glowTint = LIGHT_TINTS[lightItensity];
            submitCustomLayerWithLightTint(state, settings, ItemDisplayContext.GUI, 0, glowTint, core);
            submitCustomLayerWithLightTint(state, settings, ItemDisplayContext.GUI, 0, glowTint, coreGlow);
        } else {
            int glowLight = LightCoordsUtil.pack(lightItensity, Math.max(lightItensity, 4));
            submitCustomLayerWithLightTint(state, settings, displayContext, glowLight, TINTS, core);
            submitCustomLayerWithLightTint(state, settings, displayContext, glowLight, TINTS, coreGlow);
        }
        LayerRenderState acceleratorLayer = submitQuads(state, settings, displayContext, accelerator);
        Matrix4f pose = acceleratorLayer.localTransform;
        pose.translate(0.5f, 0.345f, 0.5f);
        pose.rotateZ(Mth.DEG_TO_RAD * (angle % 360));
        pose.translate(-0.5f, -0.345f, -0.5f);
        if (displayContext == ItemDisplayContext.GUI) {
            BlockState blockState = stack.get(AllDataComponents.SHAPER_BLOCK_USED);
            if (blockState != null) {
                state.appendModelIdentityElement(blockState);
                LayerRenderState layer = state.newLayer();
                UsedRenderState renderer;
                if (blockState.getBlock() instanceof CrossCollisionBlock block) {
                    renderer = UsedItemRenderState.create(mc, block, ItemDisplayContext.GUI, world, user, seed);
                } else {
                    renderer = UsedBlockRenderState.create(mc, blockState);
                }
                layer.setupSpecialModel(renderer, null);
            }
        }
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
            resolver.markDependency(ACCELERATOR_ID);
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
            return new WorldshaperModel(
                settings,
                quads,
                core,
                coreGlow,
                BakedModelHelper.bakeQuads(baker, ACCELERATOR_ID)
            );
        }
    }

    public interface UsedRenderState extends SpecialModelRenderer<Object> {
        @Override
        default void getExtents(Consumer<Vector3fc> output) {
            throw new UnsupportedOperationException();
        }

        @Override
        default Object extractArgument(ItemStack stack) {
            throw new UnsupportedOperationException();
        }
    }

    private static class UsedItemRenderState implements UsedRenderState {
        private final ItemStackRenderState state;

        public static UsedItemRenderState create(
            Minecraft mc,
            CrossCollisionBlock block,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel world,
            @Nullable ItemOwner user,
            int seed
        ) {
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = displayContext;
            mc.getItemModelResolver()
                .appendItemLayers(state, block.asItem().getDefaultInstance(), displayContext, world, user, seed);
            if (state.usesBlockLight()) {
                return new UsedItemRenderState(state);
            }
            return new Flat(mc, state);
        }

        public UsedItemRenderState(ItemStackRenderState state) {
            this.state = state;
        }

        @Override
        public void submit(
            @Nullable Object argument,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
        ) {
            Pose pose = matrices.last();
            pose.translate(0.258f, 0.222f, 0.5f);
            pose.scale(0.25f, 0.25f, 0.25f);
            pose.rotate(Axis.XP, Radians.DEG_30);
            pose.rotate(Axis.YP, Radians.DEG_45);
            state.submit(matrices, queue, lightCoords, overlayCoords, outlineColor);
        }

        private static class Flat extends UsedItemRenderState {
            private final Lighting diffuseLighting;
            //            private final BufferSource entityVertexConsumers;
            //            private final FeatureRenderDispatcher entityRenderDispatcher;

            public Flat(Minecraft mc, ItemStackRenderState state) {
                super(state);
                GameRenderer gameRenderer = mc.gameRenderer;
                diffuseLighting = gameRenderer.lighting();
                //                entityVertexConsumers = mc.renderBuffers().bufferSource();
                //                entityRenderDispatcher = gameRenderer.getFeatureRenderDispatcher();
            }

            @Override
            public void submit(
                @Nullable Object argument,
                PoseStack matrices,
                SubmitNodeCollector queue,
                int lightCoords,
                int overlayCoords,
                boolean hasFoil,
                int outlineColor
            ) {
                //                entityRenderDispatcher.renderAllFeatures();
                //                entityVertexConsumers.endBatch();
                diffuseLighting.setupFor(Entry.ITEMS_FLAT);
                super.submit(argument, matrices, queue, lightCoords, overlayCoords, hasFoil, outlineColor);
            }
        }
    }

    private record UsedBlockRenderState(BlockModelRenderState model) implements UsedRenderState {
        public static UsedBlockRenderState create(Minecraft mc, BlockState state) {
            BlockModelRenderState model = new BlockModelRenderState();
            mc.getBlockEntityRenderDispatcher().blockModelResolver.update(model, state, BLOCK_DISPLAY_CONTEXT);
            return new UsedBlockRenderState(model);
        }

        @Override
        public void submit(
            @Nullable Object argument,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
        ) {
            Pose pose = matrices.last();
            pose.translate(0.08f, 0.115f, 0.5f);
            pose.scale(0.25f, 0.25f, 0.25f);
            pose.rotate(Axis.XP, Radians.DEG_30);
            pose.rotate(Axis.YP, Radians.DEG_45);
            model.submit(matrices, queue, lightCoords, overlayCoords, outlineColor);
        }
    }
}
