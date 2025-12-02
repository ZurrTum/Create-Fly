package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

import static com.zurrtum.create.Create.MOD_ID;

public class ExtendoGripModel implements ItemModel, SpecialModelRenderer<ExtendoGripModel.RenderData> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/extendo_grip");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/extendo_grip/item");
    public static final Identifier COG_ID = Identifier.of(MOD_ID, "item/extendo_grip/cog");
    public static final Identifier THIN_SHORT_ID = Identifier.of(MOD_ID, "item/extendo_grip/thin_short");
    public static final Identifier WIDE_SHORT_ID = Identifier.of(MOD_ID, "item/extendo_grip/wide_short");
    public static final Identifier THIN_LONG_ID = Identifier.of(MOD_ID, "item/extendo_grip/thin_long");
    public static final Identifier WIDE_LONG_ID = Identifier.of(MOD_ID, "item/extendo_grip/wide_long");
    public static final Identifier DEPLOYER_HAND_POINTING = Identifier.of(MOD_ID, "block/deployer/hand_pointing");
    public static final Identifier DEPLOYER_HAND_PUNCHING = Identifier.of(MOD_ID, "block/deployer/hand_punching");
    public static final Identifier DEPLOYER_HAND_HOLDING = Identifier.of(MOD_ID, "block/deployer/hand_holding");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final int[] tints = new int[0];
    private final ModelSettings settings;
    private final Supplier<Vector3f[]> vector;
    private final List<BakedQuad> item;
    private final List<BakedQuad> cog;
    private final List<BakedQuad> thinShort;
    private final List<BakedQuad> wideShort;
    private final List<BakedQuad> thinLong;
    private final List<BakedQuad> wideLong;
    private final List<BakedQuad> pointing;
    private final List<BakedQuad> punching;
    private final List<BakedQuad> holding;

    public ExtendoGripModel(
        ModelSettings settings,
        List<BakedQuad> item,
        List<BakedQuad> cog,
        List<BakedQuad> thinShort,
        List<BakedQuad> wideShort,
        List<BakedQuad> thinLong,
        List<BakedQuad> wideLong,
        List<BakedQuad> pointing,
        List<BakedQuad> punching,
        List<BakedQuad> holding
    ) {
        this.settings = settings;
        this.item = item;
        this.vector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(item));
        this.cog = cog;
        this.thinShort = thinShort;
        this.wideShort = wideShort;
        this.thinLong = thinLong;
        this.wideLong = wideLong;
        this.pointing = pointing;
        this.punching = punching;
        this.holding = holding;
    }

    @Override
    public void update(
        ItemRenderState state,
        ItemStack stack,
        ItemModelManager resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientWorld world,
        @Nullable HeldItemContext ctx,
        int seed
    ) {
        state.addModelKey(this);
        state.markAnimated();

        RenderData data = new RenderData();
        data.animation = 0.25f;
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean rightHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        if (leftHand || rightHand)
            data.animation = MathHelper.lerp(
                AnimationTickHolder.getPartialTicks(),
                ExtendoGripRenderHandler.lastMainHandAnimation,
                ExtendoGripRenderHandler.mainHandAnimation
            );
        data.animation = data.animation * data.animation * data.animation;
        float extensionAngle = MathHelper.lerp(data.animation, 24f, 156f);
        data.state = state.newLayer();
        data.state.setRenderLayer(layer);
        data.state.setVertices(vector);
        settings.addSettings(data.state, displayContext);
        data.state.getQuads().addAll(item);
        data.halfAngle = extensionAngle / 2;
        data.oppositeAngle = 180 - extensionAngle;
        data.hand = (leftHand || rightHand) ? ExtendoGripRenderHandler.holding ? holding : punching : pointing;
        data.angle = AnimationTickHolder.getRenderTime() * -2;
        if (leftHand || rightHand)
            data.angle += 360 * data.animation;
        data.angle %= 360;
        if (stack == null) {
            data.self = true;
        } else if (!stack.isOf(AllItems.EXTENDO_GRIP)) {
            data.item = new ItemRenderState();
            data.item.displayContext = displayContext;
            resolver.update(data.item, stack, displayContext, world, ctx, seed);
            Arm mainArm = Arm.RIGHT;
            if (ctx instanceof PlayerLikeEntity entity) {
                mainArm = entity.getMainArm();
            } else {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    mainArm = player.getMainArm();
                }
            }
            data.flip = rightHand ^ mainArm == Arm.LEFT ? 1 : -1;
        }
        data.state.setSpecialModel(this, data);
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
        if (data.self) {
            data.self = false;
            matrices.push();
            matrices.translate(0.45f, 0.65f, -0.7f - (data.animation * 2.25f));
            settings.transforms().getTransformation(displayContext).apply(displayContext.isLeftHand(), matrices.peek());
            render(data, displayContext, matrices, queue, light, overlay, glint, i);
            matrices.pop();
        } else if (data.item != null) {
            matrices.push();
            matrices.translate(0.45f, 0.65f, -0.7f - (data.animation * 2.25f));
            if (data.item.isSideLit()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(data.flip * 45));
                matrices.translate(data.flip * 0.15f, -0.15f, -.05f);
                matrices.scale(1.25f, 1.25f, 1.25f);
            }
            data.item.render(matrices, queue, light, overlay, i);
            matrices.pop();
        }

        // grip
        LayerRenderState grip = data.state;
        queue.submitItem(matrices, displayContext, light, overlay, 0, grip.tints, grip.getQuads(), grip.renderLayer, grip.glint);

        // bits
        matrices.push();
        matrices.translate(0, 0.5625f, 0.0625f);
        matrices.scale(1, 1, 1 + data.animation);

        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(data.halfAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, thinShort);
        matrices.translate(0, 0.34375f, 0);
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(data.oppositeAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, wideLong);
        matrices.translate(0, 0.6875f, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(data.oppositeAngle));
        matrices.translate(0, 0.03125f, 0);
        renderQuads(displayContext, matrices, queue, light, overlay, thinShort);
        matrices.pop();

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180 + data.halfAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, wideShort);
        matrices.translate(0, 0.34375f, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(data.oppositeAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, thinLong);
        matrices.translate(0, 0.6875f, 0);
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(data.oppositeAngle));
        matrices.translate(0, 0.03125f, 0);
        renderQuads(displayContext, matrices, queue, light, overlay, wideShort);

        // hand
        matrices.translate(0, 0.34375f, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180 - data.halfAngle));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(0, 0, -0.25f);
        matrices.scale(1, 1, 1 / (1 + data.animation));
        matrices.translate(-1f, -0.5f, -0.5f);
        renderQuads(displayContext, matrices, queue, light, overlay, data.hand);
        matrices.pop();

        matrices.pop();

        // cog
        matrices.push();
        matrices.translate(0.5f, 0.5625f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(data.angle));
        matrices.translate(-0.5f, -0.5625f, -0.5f);
        renderQuads(displayContext, matrices, queue, light, overlay, cog);
        matrices.pop();
    }

    private void renderQuads(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        int overlay,
        List<BakedQuad> quads
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, tints, quads, layer, ItemRenderState.Glint.NONE);
    }

    public static class RenderData {
        ItemRenderState item;
        LayerRenderState state;
        List<BakedQuad> hand;
        float halfAngle;
        float oppositeAngle;
        float animation;
        float angle;
        boolean self;
        int flip;
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
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(COG_ID);
            resolver.markDependency(THIN_SHORT_ID);
            resolver.markDependency(WIDE_SHORT_ID);
            resolver.markDependency(THIN_LONG_ID);
            resolver.markDependency(WIDE_LONG_ID);
            resolver.markDependency(DEPLOYER_HAND_POINTING);
            resolver.markDependency(DEPLOYER_HAND_PUNCHING);
            resolver.markDependency(DEPLOYER_HAND_HOLDING);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel model = baker.getModel(ITEM_ID);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new ExtendoGripModel(
                settings,
                quads,
                bakeQuads(baker, COG_ID),
                bakeQuads(baker, THIN_SHORT_ID),
                bakeQuads(baker, WIDE_SHORT_ID),
                bakeQuads(baker, THIN_LONG_ID),
                bakeQuads(baker, WIDE_LONG_ID),
                bakeQuads(baker, DEPLOYER_HAND_POINTING),
                bakeQuads(baker, DEPLOYER_HAND_PUNCHING),
                bakeQuads(baker, DEPLOYER_HAND_HOLDING)
            );
        }

        private static List<BakedQuad> bakeQuads(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            return model.bakeGeometry(model.getTextures(), baker, ModelRotation.X0_Y0).getAllQuads();
        }
    }
}
