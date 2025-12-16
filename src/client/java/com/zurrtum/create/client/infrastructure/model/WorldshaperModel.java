package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class WorldshaperModel implements ItemModel, SpecialModelRenderer<WorldshaperModel.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/handheld_worldshaper");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/item");
    public static final Identifier CORE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/core");
    public static final Identifier CORE_GLOW_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/core_glow");
    public static final Identifier ACCELERATOR_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/accelerator");
    private static final int[] TINTS = new int[0];
    private static final RandomSource random = RandomSource.create();
    private static final PoseStack matrices = new PoseStack();

    private final RenderType blockLayer = Sheets.translucentBlockItemSheet();
    private final RenderType itemLayer = CreateRenderTypes.itemGlowingSolid();
    private final RenderType translucent = CreateRenderTypes.itemGlowingTranslucent();
    private final ModelRenderProperties settings;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> accelerator;
    private final Supplier<Vector3fc[]> vector;

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
        this.vector = Suppliers.memoize(() -> {
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
        ItemStackRenderState.LayerRenderState renderState = state.newLayer();
        renderState.setExtents(vector);
        renderState.setUsesBlockLight(settings.usesBlockLight());
        renderState.setParticleIcon(settings.particleIcon());
        RenderData data = new RenderData();
        data.transform = settings.transforms().getTransform(displayContext);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean mainHand = player.getMainHandItem() == stack;
        data.rightHand = mainHand ^ (player.getMainArm() == HumanoidArm.LEFT);
        data.inHand = mainHand || player.getOffhandItem() == stack;
        if (displayContext == ItemDisplayContext.GUI) {
            data.state = stack.get(AllDataComponents.SHAPER_BLOCK_USED);
            data.used = UsedRenderState.create(mc, data.state, displayContext, world, user, seed);
        }
        state.appendModelIdentityElement(data);
        renderState.setupSpecialModel(this, data);
    }

    @Override
    public void submit(
        RenderData data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.pushPose();
        data.transform.apply(displayContext.leftHand(), matrices.last());
        renderItem(displayContext, matrices, queue, light, overlay, item, blockLayer);

        float pt = AnimationTickHolder.getPartialTicks();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float animation = Mth.clamp(Create.ZAPPER_RENDER_HANDLER.getAnimation(data.rightHand, pt) * 5, 0, 1);

        // Core glows
        float multiplier;
        if (data.inHand)
            multiplier = animation;
        else
            multiplier = Mth.sin(worldTime * 5);
        int lightItensity = (int) (15 * Mth.clamp(multiplier, 0, 1));
        int glowLight = LightCoordsUtil.pack(lightItensity, Math.max(lightItensity, 4));
        renderItem(displayContext, matrices, queue, glowLight, overlay, core, itemLayer);
        renderItem(displayContext, matrices, queue, glowLight, overlay, coreGlow, translucent);

        // Accelerator spins
        float angle = worldTime * -25;
        if (data.inHand)
            angle += 360 * animation;

        angle %= 360;
        matrices.translate(0.5f, 0.345f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.345f, -0.5f);
        renderItem(displayContext, matrices, queue, light, overlay, accelerator, blockLayer);
        matrices.popPose();

        if (data.used != null) {
            data.used.render(matrices, queue, light, overlay);
        }
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
        queue.submitItem(matrices, displayContext, light, overlay, 0, TINTS, item, layer, ItemStackRenderState.FoilType.NONE);
    }

    public static class RenderData {
        public ItemTransform transform;
        public BlockState state;
        public boolean rightHand;
        public boolean inHand;
        public UsedRenderState used;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RenderData data))
                return false;
            return transform == data.transform && state == data.state && rightHand == data.rightHand && inHand == data.inHand;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rightHand, inHand);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.WorldshaperModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.WorldshaperModel.Unbaked::new);

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
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new WorldshaperModel(settings, quads, bake(baker, CORE_ID), bake(baker, CORE_GLOW_ID), bake(baker, ACCELERATOR_ID));
        }

        private static List<BakedQuad> bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            return model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
        }
    }

    public interface UsedRenderState {
        static UsedRenderState create(
            Minecraft mc,
            BlockState state,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel world,
            @Nullable ItemOwner user,
            int seed
        ) {
            if (state == null) {
                return null;
            }
            if (state.getBlock() instanceof CrossCollisionBlock block) {
                return UsedItemRenderState.create(mc, block, displayContext, world, user, seed);
            }
            return UsedBlockRenderState.create(mc, state, random, matrices);
        }

        void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay);
    }

    public record UsedItemRenderState(
        Lighting diffuseLighting, MultiBufferSource.BufferSource entityVertexConsumers, FeatureRenderDispatcher entityRenderDispatcher,
        ItemStackRenderState state
    ) implements UsedRenderState {
        public static UsedItemRenderState create(
            Minecraft mc,
            CrossCollisionBlock block,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel world,
            @Nullable ItemOwner user,
            int seed
        ) {
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = displayContext;
            mc.getItemModelResolver().appendItemLayers(item, block.asItem().getDefaultInstance(), displayContext, world, user, seed);
            if (item.usesBlockLight()) {
                return new UsedItemRenderState(null, null, null, item);
            }
            GameRenderer gameRenderer = mc.gameRenderer;
            return new UsedItemRenderState(
                gameRenderer.getLighting(),
                mc.renderBuffers().bufferSource(),
                gameRenderer.getFeatureRenderDispatcher(),
                item
            );
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
            if (diffuseLighting != null) {
                entityRenderDispatcher.renderAllFeatures();
                entityVertexConsumers.endBatch();
                diffuseLighting.setupFor(Lighting.Entry.ITEMS_FLAT);
            }
            matrices.translate(-0.242f, -0.278f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.mulPose(Axis.XP.rotationDegrees(30));
            matrices.mulPose(Axis.YP.rotationDegrees(45));
            state.submit(matrices, queue, light, overlay, 0);
        }
    }

    public record UsedBlockRenderState(
        RenderType layer, BlockRenderDispatcher blockRenderManager, PoseStack matrices, SinglePosVirtualBlockGetter world, BlockState state,
        List<BlockModelPart> parts
    ) implements UsedRenderState, SubmitNodeCollector.CustomGeometryRenderer {
        public static UsedBlockRenderState create(Minecraft mc, BlockState state, RandomSource random, PoseStack matrices) {
            RenderType layer = ItemBlockRenderTypes.getChunkRenderType(state) == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet();
            BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            world.blockState(state);
            random.setSeed(42L);
            List<BlockModelPart> parts = blockRenderManager.getBlockModel(state).collectParts(random);
            return new UsedBlockRenderState(layer, blockRenderManager, matrices, world, state, parts);
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
            matrices.translate(-0.42f, -0.385f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.mulPose(Axis.XP.rotationDegrees(30));
            matrices.mulPose(Axis.YP.rotationDegrees(45));
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            matrices.last().set(matricesEntry);
            blockRenderManager.renderBatched(state, BlockPos.ZERO, world, matrices, vertexConsumer, false, parts);
        }
    }
}
