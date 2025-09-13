package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler.Mode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zurrtum.create.Create.MOD_ID;

public class LinkedControllerModel implements ItemModel, SpecialModelRenderer<LinkedControllerModel.RenderData> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/linked_controller");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/linked_controller/item");
    public static final Identifier POWERED_ID = Identifier.of(MOD_ID, "item/linked_controller/powered");
    public static final Identifier TORCH_ID = Identifier.of(MOD_ID, "item/linked_controller/torch");
    public static final Identifier BUTTON_ID = Identifier.of(MOD_ID, "item/linked_controller/button");

    private static final LerpedFloat equipProgress = LerpedFloat.linear().startWithValue(0);
    private static final List<LerpedFloat> buttons = Util.make(
        new ArrayList<>(6), list -> {
            for (int i = 0; i < 6; i++)
                list.add(LerpedFloat.linear().startWithValue(0));
        }
    );

    public static void tick(MinecraftClient mc) {
        if (mc.isPaused())
            return;

        boolean active = LinkedControllerClientHandler.MODE != Mode.IDLE;
        equipProgress.chase(active ? 1 : 0, .2f, Chaser.EXP);
        equipProgress.tickChaser();

        if (!active)
            return;

        for (int i = 0; i < buttons.size(); i++) {
            LerpedFloat lerpedFloat = buttons.get(i);
            lerpedFloat.chase(LinkedControllerClientHandler.currentlyPressed.contains(i) ? 1 : 0, .4f, Chaser.EXP);
            lerpedFloat.tickChaser();
        }
    }

    public static void resetButtons() {
        for (LerpedFloat button : buttons) {
            button.startWithValue(0);
        }
    }

    private final RenderLayer itemLayer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final RenderLayer cutoutLayer = RenderLayer.getCutout();
    private final int[] tints = new int[0];
    private final ModelSettings settings;
    private final Supplier<Vector3f[]> vector;
    private final List<BakedQuad> item;
    private final List<BakedQuad> powered;
    private final List<BakedQuad> torch;
    private final List<BakedQuad> button;

    public LinkedControllerModel(
        ModelSettings settings,
        List<BakedQuad> item,
        List<BakedQuad> powered,
        List<BakedQuad> torch,
        List<BakedQuad> button
    ) {
        this.settings = settings;
        this.item = item;
        this.vector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(item));
        this.powered = powered;
        this.torch = torch;
        this.button = button;
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
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setVertices(vector);
        settings.addSettings(layerRenderState, displayContext);

        RenderData data = new RenderData();
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean rightHanded = mc.options.getMainArm().getValue() == Arm.RIGHT;
        ItemDisplayContext mainHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemDisplayContext offHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean noControllerInMain = !mc.player.getMainHandStack().isOf(AllItems.LINKED_CONTROLLER);
        if (displayContext == mainHand || (displayContext == offHand && noControllerInMain)) {
            data.equip = true;
            data.active = true;
        }
        if (displayContext == ItemDisplayContext.GUI) {
            if (stack == mc.player.getMainHandStack())
                data.active = true;
            if (stack == mc.player.getOffHandStack() && noControllerInMain)
                data.active = true;
        }
        data.active &= LinkedControllerClientHandler.MODE != Mode.IDLE;
        layerRenderState.setSpecialModel(this, data);
    }

    @Override
    public void render(
        LinkedControllerModel.RenderData data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        boolean glint
    ) {
        assert data != null;
        render(displayContext, matrices, vertexConsumers, light, overlay, RenderType.NORMAL, data.equip, data.active, true);
    }

    public void renderInLectern(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        boolean active,
        boolean renderDepression
    ) {
        render(displayContext, matrices, vertexConsumers, light, overlay, RenderType.LECTERN, false, active, renderDepression);
    }

    private void render(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        RenderType renderType,
        boolean equip,
        boolean active,
        boolean renderDepression
    ) {
        float pt = -1;
        matrices.push();

        if (equip) {
            pt = AnimationTickHolder.getPartialTicks();
            float progress = equipProgress.getValue(pt);
            int handModifier = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
            matrices.translate(0, progress / 4, progress / 4 * handModifier);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(progress * -30 * handModifier));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(progress * -30));
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }

        renderQuads(displayContext, matrices, vertexConsumers, light, overlay, itemLayer, active ? powered : item);

        if (!active) {
            matrices.pop();
            return;
        }
        renderQuads(displayContext, matrices, vertexConsumers, light, overlay, cutoutLayer, torch);
        if (renderType == RenderType.NORMAL) {
            if (LinkedControllerClientHandler.MODE == Mode.BIND) {
                int i = MathHelper.lerp((MathHelper.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
                light = i << 20;
            }
        }
        float s = 1 / 16f;
        float b = s * -.75f;
        int index = 0;
        if (pt == -1) {
            pt = AnimationTickHolder.getPartialTicks();
        }
        matrices.push();
        matrices.translate(2 * s, 0, 8 * s);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(4 * s, 0, 0);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(-2 * s, 0, 2 * s);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(0, 0, -4 * s);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index++, renderDepression);
        matrices.pop();

        matrices.translate(3 * s, 0, 3 * s);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(2 * s, 0, 0);
        renderButton(displayContext, matrices, vertexConsumers, light, overlay, button, pt, b, index, renderDepression);

        matrices.pop();
    }

    private void renderButton(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        List<BakedQuad> button,
        float pt,
        float b,
        int index,
        boolean renderDepression
    ) {
        matrices.push();
        if (renderDepression) {
            float depression = b * buttons.get(index).getValue(pt);
            matrices.translate(0, depression, 0);
        }
        renderQuads(displayContext, matrices, vertexConsumers, light, overlay, itemLayer, button);
        matrices.pop();
    }

    private void renderQuads(
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        RenderLayer layer,
        List<BakedQuad> quads
    ) {
        ItemRenderer.renderItem(displayContext, matrices, vertexConsumers, light, overlay, tints, quads, layer, ItemRenderState.Glint.NONE);
    }

    public static class RenderData {
        boolean equip;
        boolean active;
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
            resolver.markDependency(POWERED_ID);
            resolver.markDependency(TORCH_ID);
            resolver.markDependency(BUTTON_ID);
        }

        @Override
        public ItemModel bake(BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel model = baker.getModel(ITEM_ID);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new LinkedControllerModel(settings, quads, bakeQuads(baker, POWERED_ID), bakeQuads(baker, TORCH_ID), bakeQuads(baker, BUTTON_ID));
        }

        private static List<BakedQuad> bakeQuads(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            return model.bakeGeometry(model.getTextures(), baker, ModelRotation.X0_Y0).getAllQuads();
        }
    }

    protected enum RenderType {
        NORMAL,
        LECTERN;
    }
}
