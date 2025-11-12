package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler.Mode;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zurrtum.create.Create.MOD_ID;

public class LinkedControllerModel implements ItemModel, SpecialModelRenderer<LinkedControllerModel.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/linked_controller");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/item");
    public static final Identifier POWERED_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/powered");
    public static final Identifier TORCH_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/torch");
    public static final Identifier BUTTON_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/button");

    private static final LerpedFloat equipProgress = LerpedFloat.linear().startWithValue(0);
    private static final List<LerpedFloat> buttons = Util.make(
        new ArrayList<>(6), list -> {
            for (int i = 0; i < 6; i++)
                list.add(LerpedFloat.linear().startWithValue(0));
        }
    );

    public static void tick(Minecraft mc) {
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

    private final net.minecraft.client.renderer.RenderType itemLayer = Sheets.translucentItemSheet();
    private final net.minecraft.client.renderer.RenderType cutoutLayer = net.minecraft.client.renderer.RenderType.cutout();
    private final int[] tints = new int[0];
    private final ModelRenderProperties settings;
    private final Supplier<Vector3f[]> vector;
    private final List<BakedQuad> item;
    private final List<BakedQuad> powered;
    private final List<BakedQuad> torch;
    private final List<BakedQuad> button;

    public LinkedControllerModel(
        ModelRenderProperties settings,
        List<BakedQuad> item,
        List<BakedQuad> powered,
        List<BakedQuad> torch,
        List<BakedQuad> button
    ) {
        this.settings = settings;
        this.item = item;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(item));
        this.powered = powered;
        this.torch = torch;
        this.button = button;
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
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);

        RenderData data = new RenderData();
        Minecraft mc = Minecraft.getInstance();
        boolean rightHanded = mc.options.mainHand().get() == HumanoidArm.RIGHT;
        ItemDisplayContext mainHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemDisplayContext offHand = rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean noControllerInMain = !mc.player.getMainHandItem().is(AllItems.LINKED_CONTROLLER);
        if (displayContext == mainHand || (displayContext == offHand && noControllerInMain)) {
            data.equip = true;
            data.active = true;
        }
        if (displayContext == ItemDisplayContext.GUI) {
            if (stack == mc.player.getMainHandItem())
                data.active = true;
            if (stack == mc.player.getOffhandItem() && noControllerInMain)
                data.active = true;
        }
        data.active &= LinkedControllerClientHandler.MODE != Mode.IDLE;
        layerRenderState.setupSpecialModel(this, data);
    }

    @Override
    public void submit(
        LinkedControllerModel.RenderData data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        render(displayContext, matrices, queue, light, overlay, RenderType.NORMAL, data.equip, data.active, true);
    }

    public void renderInLectern(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean active,
        boolean renderDepression
    ) {
        render(displayContext, matrices, queue, light, overlay, RenderType.LECTERN, false, active, renderDepression);
    }

    private void render(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        RenderType renderType,
        boolean equip,
        boolean active,
        boolean renderDepression
    ) {
        float pt = -1;
        matrices.pushPose();

        if (equip) {
            pt = AnimationTickHolder.getPartialTicks();
            float progress = equipProgress.getValue(pt);
            int handModifier = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
            matrices.translate(0, progress / 4, progress / 4 * handModifier);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotationDegrees(progress * -30 * handModifier));
            matrices.mulPose(Axis.ZP.rotationDegrees(progress * -30));
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }

        renderQuads(displayContext, matrices, queue, light, overlay, itemLayer, active ? powered : item);

        if (!active) {
            matrices.popPose();
            return;
        }
        renderQuads(displayContext, matrices, queue, light, overlay, cutoutLayer, torch);
        if (renderType == RenderType.NORMAL) {
            if (LinkedControllerClientHandler.MODE == Mode.BIND) {
                int i = Mth.lerpInt((Mth.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
                light = i << 20;
            }
        }
        float s = 1 / 16f;
        float b = s * -.75f;
        int index = 0;
        if (pt == -1) {
            pt = AnimationTickHolder.getPartialTicks();
        }
        matrices.pushPose();
        matrices.translate(2 * s, 0, 8 * s);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(4 * s, 0, 0);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(-2 * s, 0, 2 * s);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(0, 0, -4 * s);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index++, renderDepression);
        matrices.popPose();

        matrices.translate(3 * s, 0, 3 * s);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index++, renderDepression);
        matrices.translate(2 * s, 0, 0);
        renderButton(displayContext, matrices, queue, light, overlay, button, pt, b, index, renderDepression);

        matrices.popPose();
    }

    private void renderButton(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        List<BakedQuad> button,
        float pt,
        float b,
        int index,
        boolean renderDepression
    ) {
        matrices.pushPose();
        if (renderDepression) {
            float depression = b * buttons.get(index).getValue(pt);
            matrices.translate(0, depression, 0);
        }
        renderQuads(displayContext, matrices, queue, light, overlay, itemLayer, button);
        matrices.popPose();
    }

    private void renderQuads(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        net.minecraft.client.renderer.RenderType layer,
        List<BakedQuad> quads
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, tints, quads, layer, ItemStackRenderState.FoilType.NONE);
    }

    public static class RenderData {
        boolean equip;
        boolean active;
    }

    @Override
    public void getExtents(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.LinkedControllerModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.LinkedControllerModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.LinkedControllerModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(POWERED_ID);
            resolver.markDependency(TORCH_ID);
            resolver.markDependency(BUTTON_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new LinkedControllerModel(settings, quads, bakeQuads(baker, POWERED_ID), bakeQuads(baker, TORCH_ID), bakeQuads(baker, BUTTON_ID));
        }

        private static List<BakedQuad> bakeQuads(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            return model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.X0_Y0).getAll();
        }
    }

    protected enum RenderType {
        NORMAL,
        LECTERN;
    }
}
