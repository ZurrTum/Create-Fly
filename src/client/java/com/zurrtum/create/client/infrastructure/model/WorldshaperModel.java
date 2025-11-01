package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class WorldshaperModel implements ItemModel, SpecialModelRenderer<WorldshaperModel.RenderData> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/handheld_worldshaper");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/handheld_worldshaper/item");
    public static final Identifier CORE_ID = Identifier.of(MOD_ID, "item/handheld_worldshaper/core");
    public static final Identifier CORE_GLOW_ID = Identifier.of(MOD_ID, "item/handheld_worldshaper/core_glow");
    public static final Identifier ACCELERATOR_ID = Identifier.of(MOD_ID, "item/handheld_worldshaper/accelerator");
    private static final int[] TINTS = new int[0];
    private static final Random random = Random.create();
    private static final MatrixStack matrices = new MatrixStack();

    private final ModelSettings settings;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> accelerator;
    private final Supplier<Vector3f[]> vector;

    public WorldshaperModel(
        ModelSettings settings,
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
            Set<Vector3f> set = new HashSet<>();
            calculatePosition(item, set::add);
            calculatePosition(core, set::add);
            calculatePosition(coreGlow, set::add);
            calculatePosition(accelerator, set::add);
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
        ItemRenderState.LayerRenderState renderState = state.newLayer();
        renderState.setVertices(vector);
        renderState.setUseLight(settings.usesBlockLight());
        renderState.setParticle(settings.particleIcon());
        RenderData data = new RenderData();
        data.transform = settings.transforms().getTransformation(displayContext);
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        boolean mainHand = player.getMainHandStack() == stack;
        data.rightHand = mainHand ^ (player.getMainArm() == Arm.LEFT);
        data.inHand = mainHand || player.getOffHandStack() == stack;
        if (displayContext == ItemDisplayContext.GUI) {
            data.state = stack.get(AllDataComponents.SHAPER_BLOCK_USED);
            data.used = UsedRenderState.create(mc, data.state, displayContext, world, user, seed);
        }
        state.addModelKey(data);
        renderState.setSpecialModel(this, data);
    }

    @Override
    public void render(
        RenderData data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.push();
        data.transform.apply(displayContext.isLeftHand(), matrices.peek());
        RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
        renderItem(displayContext, matrices, queue, light, overlay, item, layer);

        float pt = AnimationTickHolder.getPartialTicks();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float animation = MathHelper.clamp(Create.ZAPPER_RENDER_HANDLER.getAnimation(data.rightHand, pt) * 5, 0, 1);

        // Core glows
        float multiplier;
        if (data.inHand)
            multiplier = animation;
        else
            multiplier = MathHelper.sin(worldTime * 5);
        int lightItensity = (int) (15 * MathHelper.clamp(multiplier, 0, 1));
        int glowLight = LightmapTextureManager.pack(lightItensity, Math.max(lightItensity, 4));
        renderItem(displayContext, matrices, queue, glowLight, overlay, core, RenderTypes.itemGlowingSolid());
        renderItem(displayContext, matrices, queue, glowLight, overlay, coreGlow, RenderTypes.itemGlowingTranslucent());

        // Accelerator spins
        float angle = worldTime * -25;
        if (data.inHand)
            angle += 360 * animation;

        angle %= 360;
        matrices.translate(0.5f, 0.345f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.345f, -0.5f);
        renderItem(displayContext, matrices, queue, light, overlay, accelerator, layer);
        matrices.pop();

        if (data.used != null) {
            data.used.render(matrices, queue, light, overlay);
        }
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
        queue.submitItem(matrices, displayContext, light, overlay, 0, TINTS, item, layer, ItemRenderState.Glint.NONE);
    }

    public static class RenderData {
        public Transformation transform;
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
    public void collectVertices(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData getData(ItemStack stack) {
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
            resolver.markDependency(ACCELERATOR_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel model = baker.getModel(ITEM_ID);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new WorldshaperModel(settings, quads, bake(baker, CORE_ID), bake(baker, CORE_GLOW_ID), bake(baker, ACCELERATOR_ID));
        }

        private static List<BakedQuad> bake(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            return model.bakeGeometry(model.getTextures(), baker, ModelRotation.X0_Y0).getAllQuads();
        }
    }

    public interface UsedRenderState {
        static UsedRenderState create(
            MinecraftClient mc,
            BlockState state,
            ItemDisplayContext displayContext,
            @Nullable ClientWorld world,
            @Nullable HeldItemContext user,
            int seed
        ) {
            if (state == null) {
                return null;
            }
            if (state.getBlock() instanceof HorizontalConnectingBlock block) {
                return UsedItemRenderState.create(mc, block, displayContext, world, user, seed);
            }
            return UsedBlockRenderState.create(mc, state, random, matrices);
        }

        void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay);
    }

    public record UsedItemRenderState(
        DiffuseLighting diffuseLighting, VertexConsumerProvider.Immediate entityVertexConsumers, RenderDispatcher entityRenderDispatcher,
        ItemRenderState state
    ) implements UsedRenderState {
        public static UsedItemRenderState create(
            MinecraftClient mc,
            HorizontalConnectingBlock block,
            ItemDisplayContext displayContext,
            @Nullable ClientWorld world,
            @Nullable HeldItemContext user,
            int seed
        ) {
            ItemRenderState item = new ItemRenderState();
            item.displayContext = displayContext;
            mc.getItemModelManager().update(item, block.asItem().getDefaultStack(), displayContext, world, user, seed);
            if (item.isSideLit()) {
                return new UsedItemRenderState(null, null, null, item);
            }
            GameRenderer gameRenderer = mc.gameRenderer;
            return new UsedItemRenderState(
                gameRenderer.getDiffuseLighting(),
                mc.getBufferBuilders().getEntityVertexConsumers(),
                gameRenderer.getEntityRenderDispatcher(),
                item
            );
        }

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
            if (diffuseLighting != null) {
                entityRenderDispatcher.render();
                entityVertexConsumers.draw();
                diffuseLighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
            }
            matrices.translate(-0.242f, -0.278f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            state.render(matrices, queue, light, overlay, 0);
        }
    }

    public record UsedBlockRenderState(
        RenderLayer layer, BlockRenderManager blockRenderManager, MatrixStack matrices, SinglePosVirtualBlockGetter world, BlockState state,
        List<BlockModelPart> parts
    ) implements UsedRenderState, OrderedRenderCommandQueue.Custom {
        public static UsedBlockRenderState create(MinecraftClient mc, BlockState state, Random random, MatrixStack matrices) {
            RenderLayer layer = RenderLayers.getBlockLayer(state) == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
            BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
            world.blockState(state);
            random.setSeed(42L);
            List<BlockModelPart> parts = blockRenderManager.getModel(state).getParts(random);
            return new UsedBlockRenderState(layer, blockRenderManager, matrices, world, state, parts);
        }

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
            matrices.translate(-0.42f, -0.385f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            matrices.peek().copy(matricesEntry);
            blockRenderManager.renderBlock(state, BlockPos.ORIGIN, world, matrices, vertexConsumer, false, parts);
        }
    }
}
