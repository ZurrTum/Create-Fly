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
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
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
        @Nullable LivingEntity user,
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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        boolean mainHand = player.getMainHandStack() == stack;
        data.rightHand = mainHand ^ (player.getMainArm() == Arm.LEFT);
        data.inHand = mainHand || player.getOffHandStack() == stack;
        if (displayContext == ItemDisplayContext.GUI) {
            data.state = stack.get(AllDataComponents.SHAPER_BLOCK_USED);
        }
        state.addModelKey(data);
        renderState.setSpecialModel(this, data);
    }

    @Override
    public void render(
        RenderData data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        boolean glint
    ) {
        assert data != null;
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.push();
        data.transform.apply(displayContext.isLeftHand(), matrices.peek());
        RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
        renderItem(matrices, vertexConsumers, light, overlay, item, layer);

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
        renderItem(matrices, vertexConsumers, glowLight, overlay, core, RenderTypes.itemGlowingSolid());
        renderItem(matrices, vertexConsumers, glowLight, overlay, coreGlow, RenderTypes.itemGlowingTranslucent());

        // Accelerator spins
        float angle = worldTime * -25;
        if (data.inHand)
            angle += 360 * animation;

        angle %= 360;
        matrices.translate(0.5f, 0.345f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.345f, -0.5f);
        renderItem(matrices, vertexConsumers, light, overlay, accelerator, layer);
        matrices.pop();

        if (data.state != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (data.state.getBlock() instanceof HorizontalConnectingBlock) {
                ItemRenderer itemRenderer = mc.getItemRenderer();
                ItemStack stack = new ItemStack(data.state.getBlock());
                itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, stack, displayContext, mc.world, mc.player, 0);
                boolean flat = !itemRenderer.itemRenderState.isSideLit();
                if (flat && vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
                    immediate.draw();
                }
                matrices.translate(-0.242f, -0.278f, 0);
                matrices.scale(0.25f, 0.25f, 0.25f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                if (flat) {
                    mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
                }
                itemRenderer.itemRenderState.render(matrices, vertexConsumers, light, overlay);
            } else {
                matrices.translate(-0.42f, -0.385f, 0);
                matrices.scale(0.25f, 0.25f, 0.25f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                RenderLayer blockLayer = RenderLayers.getBlockLayer(data.state) == BlockRenderLayer.TRANSLUCENT ? TexturedRenderLayers.getItemEntityTranslucentCull() : TexturedRenderLayers.getEntityCutout();
                SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
                world.blockState(data.state);
                random.setSeed(42L);
                List<BlockModelPart> parts = mc.getBlockRenderManager().getModel(data.state).getParts(random);
                mc.getBlockRenderManager()
                    .renderBlock(data.state, BlockPos.ORIGIN, world, matrices, vertexConsumers.getBuffer(blockLayer), false, parts);
            }
        }
        matrices.pop();
    }

    private static void renderItem(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        List<BakedQuad> item,
        RenderLayer layer
    ) {
        ItemRenderer.renderItem(null, matrices, vertexConsumers, light, overlay, TINTS, item, layer, ItemRenderState.Glint.NONE);
    }

    public static class RenderData {
        Transformation transform;
        BlockState state;
        boolean rightHand;
        boolean inHand;

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
        public ItemModel bake(BakeContext context) {
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
}
