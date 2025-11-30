package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class ExtendoGripModel implements ItemModel, SpecialModelRenderer<ExtendoGripModel.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/extendo_grip");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/item");
    public static final Identifier POLE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/pole");
    public static final Identifier COG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/cog");
    public static final Identifier THIN_SHORT_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/thin_short");
    public static final Identifier WIDE_SHORT_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/wide_short");
    public static final Identifier THIN_LONG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/thin_long");
    public static final Identifier WIDE_LONG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/wide_long");
    public static final Identifier DEPLOYER_HAND_POINTING = Identifier.fromNamespaceAndPath(MOD_ID, "block/deployer/hand_pointing");
    public static final Identifier DEPLOYER_HAND_PUNCHING = Identifier.fromNamespaceAndPath(MOD_ID, "block/deployer/hand_punching");
    public static final Identifier DEPLOYER_HAND_HOLDING = Identifier.fromNamespaceAndPath(MOD_ID, "block/deployer/hand_holding");

    private final RenderType itemLayer = Sheets.translucentItemSheet();
    private final RenderType blockLayer = Sheets.translucentBlockItemSheet();
    private final int[] tints = new int[0];
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> vector;
    private final List<BakedQuad> item;
    private final List<BakedQuad> pole;
    private final List<BakedQuad> cog;
    private final List<BakedQuad> thinShort;
    private final List<BakedQuad> wideShort;
    private final List<BakedQuad> thinLong;
    private final List<BakedQuad> wideLong;
    private final List<BakedQuad> pointing;
    private final List<BakedQuad> punching;
    private final List<BakedQuad> holding;

    public ExtendoGripModel(
        ModelRenderProperties settings,
        List<BakedQuad> item,
        List<BakedQuad> pole,
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
        this.pole = pole;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(item));
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
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner ctx,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();

        RenderData data = new RenderData();
        data.animation = 0.25f;
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean rightHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        if (leftHand || rightHand)
            data.animation = Mth.lerp(
                AnimationTickHolder.getPartialTicks(),
                ExtendoGripRenderHandler.lastMainHandAnimation,
                ExtendoGripRenderHandler.mainHandAnimation
            );
        data.animation = data.animation * data.animation * data.animation;
        float extensionAngle = Mth.lerp(data.animation, 24f, 156f);
        data.state = state.newLayer();
        data.state.setRenderType(itemLayer);
        data.state.setExtents(vector);
        settings.applyToLayer(data.state, displayContext);
        data.state.prepareQuadList().addAll(item);
        data.halfAngle = extensionAngle / 2;
        data.oppositeAngle = 180 - extensionAngle;
        data.hand = (leftHand || rightHand) ? ExtendoGripRenderHandler.holding ? holding : punching : pointing;
        data.angle = AnimationTickHolder.getRenderTime() * -2;
        if (leftHand || rightHand)
            data.angle += 360 * data.animation;
        data.angle %= 360;
        if (stack == null) {
            data.self = true;
        } else if (!stack.is(AllItems.EXTENDO_GRIP)) {
            data.item = new ItemStackRenderState();
            data.item.displayContext = displayContext;
            resolver.appendItemLayers(data.item, stack, displayContext, world, ctx, seed);
            HumanoidArm mainArm = HumanoidArm.RIGHT;
            if (ctx instanceof Avatar entity) {
                mainArm = entity.getMainArm();
            } else {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    mainArm = player.getMainArm();
                }
            }
            data.flip = rightHand ^ mainArm == HumanoidArm.LEFT ? 1 : -1;
        }
        data.state.setupSpecialModel(this, data);
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
        if (data.self) {
            data.self = false;
            matrices.pushPose();
            matrices.translate(0.45f, 0.65f, -0.7f - (data.animation * 2.25f));
            settings.transforms().getTransform(displayContext).apply(displayContext.leftHand(), matrices.last());
            submit(data, displayContext, matrices, queue, light, overlay, glint, i);
            matrices.popPose();
        } else if (data.item != null) {
            matrices.pushPose();
            matrices.translate(0.45f, 0.65f, -0.7f - (data.animation * 2.25f));
            if (data.item.usesBlockLight()) {
                matrices.mulPose(Axis.YP.rotationDegrees(data.flip * 45));
                matrices.translate(data.flip * 0.15f, -0.15f, -.05f);
                matrices.scale(1.25f, 1.25f, 1.25f);
            }
            data.item.submit(matrices, queue, light, overlay, i);
            matrices.popPose();
        }

        // grip
        LayerRenderState grip = data.state;
        queue.submitItem(matrices, displayContext, light, overlay, 0, grip.tintLayers, grip.prepareQuadList(), grip.renderType, grip.foilType);
        renderQuads(displayContext, matrices, queue, light, overlay, pole, blockLayer);

        // bits
        matrices.pushPose();
        matrices.translate(0, 0.5625f, 0.0625f);
        matrices.scale(1, 1, 1 + data.animation);

        matrices.pushPose();
        matrices.mulPose(Axis.XN.rotationDegrees(data.halfAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, thinShort, itemLayer);
        matrices.translate(0, 0.34375f, 0);
        matrices.mulPose(Axis.XN.rotationDegrees(data.oppositeAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, wideLong, itemLayer);
        matrices.translate(0, 0.6875f, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(data.oppositeAngle));
        matrices.translate(0, 0.03125f, 0);
        renderQuads(displayContext, matrices, queue, light, overlay, thinShort, itemLayer);
        matrices.popPose();

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(-180 + data.halfAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, wideShort, itemLayer);
        matrices.translate(0, 0.34375f, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(data.oppositeAngle));
        renderQuads(displayContext, matrices, queue, light, overlay, thinLong, itemLayer);
        matrices.translate(0, 0.6875f, 0);
        matrices.mulPose(Axis.XN.rotationDegrees(data.oppositeAngle));
        matrices.translate(0, 0.03125f, 0);
        renderQuads(displayContext, matrices, queue, light, overlay, wideShort, itemLayer);

        // hand
        matrices.translate(0, 0.34375f, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(180 - data.halfAngle));
        matrices.mulPose(Axis.YP.rotationDegrees(180));
        matrices.translate(0, 0, -0.25f);
        matrices.scale(1, 1, 1 / (1 + data.animation));
        matrices.translate(-1f, -0.5f, -0.5f);
        renderQuads(displayContext, matrices, queue, light, overlay, data.hand, blockLayer);
        matrices.popPose();

        matrices.popPose();

        // cog
        matrices.pushPose();
        matrices.translate(0.5f, 0.5625f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(data.angle));
        matrices.translate(-0.5f, -0.5625f, -0.5f);
        renderQuads(displayContext, matrices, queue, light, overlay, cog, blockLayer);
        matrices.popPose();
    }

    private void renderQuads(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        List<BakedQuad> quads,
        RenderType layer
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, tints, quads, layer, ItemStackRenderState.FoilType.NONE);
    }

    public static class RenderData {
        ItemStackRenderState item;
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
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.ExtendoGripModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.ExtendoGripModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.ExtendoGripModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(POLE_ID);
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
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new ExtendoGripModel(
                settings,
                quads,
                bakeQuads(baker, POLE_ID),
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

        private static List<BakedQuad> bakeQuads(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            return model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
        }
    }
}
