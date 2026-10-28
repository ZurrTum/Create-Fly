package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler.Mode;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel.RenderData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class LinkedControllerModel implements ItemModel, SpecialModelRenderer<RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/linked_controller");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/item");
    public static final Identifier POWERED_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/linked_controller/powered"
    );
    public static final Identifier TORCH_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/torch");
    public static final Identifier TORCH_OFF_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/linked_controller/torch_off"
    );
    public static final Identifier BUTTON_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/linked_controller/button");

    private static final LerpedFloat equipProgress = LerpedFloat.linear().startWithValue(0);
    private static final List<LerpedFloat> buttons = Util.make(
        new ArrayList<>(6), list -> {
            for (int i = 0; i < 6; i++) {
                list.add(LerpedFloat.linear().startWithValue(0));
            }
        }
    );

    public static void tick(Minecraft mc) {
        if (mc.isPaused()) {
            return;
        }

        boolean active = LinkedControllerClientHandler.MODE != Mode.IDLE;
        equipProgress.chase(active ? 1 : 0, 0.2f, Chaser.EXP);
        equipProgress.tickChaser();

        if (!active) {
            return;
        }

        for (int i = 0; i < buttons.size(); i++) {
            LerpedFloat lerpedFloat = buttons.get(i);
            lerpedFloat.chase(LinkedControllerClientHandler.currentlyPressed.contains(i) ? 1 : 0, 0.4f, Chaser.EXP);
            lerpedFloat.tickChaser();
        }
    }

    public static void resetButtons() {
        for (LerpedFloat button : buttons) {
            button.startWithValue(0);
        }
    }

    private final int[] tints = new int[0];
    private final ModelRenderProperties settings;
    private final Matrix4fc transformation;
    private final Supplier<Vector3fc[]> extents;
    private final List<BakedQuad> item;
    private final List<BakedQuad> powered;
    private final List<BakedQuad> torch;
    private final List<BakedQuad> torchOff;
    private final List<BakedQuad> button;

    public LinkedControllerModel(
        ModelRenderProperties settings,
        Matrix4fc transformation,
        List<BakedQuad> item,
        List<BakedQuad> powered,
        List<BakedQuad> torch,
        List<BakedQuad> torchOff,
        List<BakedQuad> button
    ) {
        this.settings = settings;
        this.transformation = transformation;
        this.item = item;
        extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(item));
        this.powered = powered;
        this.torch = torch;
        this.torchOff = torchOff;
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
        layerRenderState.setExtents(extents);
        layerRenderState.setLocalTransform(transformation);
        settings.applyToLayer(layerRenderState, displayContext);

        RenderData data = new RenderData(displayContext);
        Mode mode = LinkedControllerClientHandler.MODE;
        switch (displayContext) {
            case GUI -> updateGuiData(stack, data, mode);
            case FIRST_PERSON_RIGHT_HAND -> updateFirstPersonData(HumanoidArm.RIGHT, data, mode);
            case THIRD_PERSON_RIGHT_HAND -> updateThirdPersonData(HumanoidArm.RIGHT, data, mode);
            case FIRST_PERSON_LEFT_HAND -> updateFirstPersonData(HumanoidArm.LEFT, data, mode);
            case THIRD_PERSON_LEFT_HAND -> updateThirdPersonData(HumanoidArm.LEFT, data, mode);
        }
        layerRenderState.setupSpecialModel(this, data);
        state.appendModelIdentityElement(data.active);
    }

    private static void updateGuiData(ItemStack current, RenderData data, Mode mode) {
        boolean bind = mode == Mode.BIND;
        if (bind || mode == Mode.ACTIVE) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack mainStack = player.getMainHandItem();
                if (current == mainStack || mainStack.getItem() != AllItems.LINKED_CONTROLLER && current == player.getOffhandItem()) {
                    data.active = true;
                    data.bind = bind;
                }
            }
        }
    }

    private static void updateFirstPersonData(HumanoidArm current, RenderData data, Mode mode) {
        Minecraft mc = Minecraft.getInstance();
        if (current == mc.options.mainHand().get()) {
            data.equip = true;
            boolean bind = mode == Mode.BIND;
            if (bind || mode == Mode.ACTIVE) {
                data.active = true;
                data.bind = bind;
            }
        } else {
            LocalPlayer player = mc.player;
            if (player != null && player.getMainHandItem().getItem() != AllItems.LINKED_CONTROLLER) {
                data.equip = true;
                boolean bind = mode == Mode.BIND;
                if (bind || mode == Mode.ACTIVE) {
                    data.active = true;
                    data.bind = bind;
                }
            }
        }
    }

    private static void updateThirdPersonData(HumanoidArm current, RenderData data, Mode mode) {
        boolean bind = mode == Mode.BIND;
        if (bind || mode == Mode.ACTIVE) {
            Minecraft mc = Minecraft.getInstance();
            if (current == mc.options.mainHand().get()) {
                data.active = true;
                data.bind = bind;
            } else {
                LocalPlayer player = mc.player;
                if (player != null && player.getMainHandItem().getItem() != AllItems.LINKED_CONTROLLER) {
                    data.active = true;
                    data.bind = bind;
                }
            }
        }
    }

    @Override
    public void submit(
        @Nullable RenderData data,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        submit(data.displayContext, matrices, queue, light, overlay, data.bind, data.equip, data.active, true);
    }

    public void submitInLectern(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean active,
        boolean renderDepression
    ) {
        submit(displayContext, matrices, queue, light, overlay, false, false, active, renderDepression);
    }

    private void submit(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean bind,
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
            Pose pose = matrices.last();
            pose.translate(0, progress / 4, progress / 4 * handModifier);
            pose.translate(0.5f, 0.5f, 0.5f);
            float radians = progress * -Radians.DEG_30;
            pose.rotate(Axis.YP, radians * handModifier);
            pose.rotate(Axis.ZP, radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }

        submitQuads(displayContext, matrices, queue, light, overlay, active ? powered : item);

        if (!active) {
            submitQuads(displayContext, matrices, queue, light, overlay, torchOff);
            matrices.popPose();
            return;
        }
        submitQuads(displayContext, matrices, queue, light, overlay, torch);
        if (bind) {
            light = (int) Mth.lerp((Mth.sin(AnimationTickHolder.getRenderTime() / 4.0f) + 1) / 2, 5, 15) << 20;
        }
        float s = 1 / 16.0f;
        if (renderDepression) {
            if (pt == -1) {
                pt = AnimationTickHolder.getPartialTicks();
            }
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(2 * s, 0, 8 * s);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 0);
            pose.translate(4 * s, 0, 0);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 1);
            pose.translate(-2 * s, 0, 2 * s);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 2);
            pose.translate(0, 0, -4 * s);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 3);
            matrices.popPose();

            pose = matrices.last();
            pose.translate(3 * s, 0, 3 * s);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 4);
            pose.translate(2 * s, 0, 0);
            submitPressButton(displayContext, matrices, queue, light, overlay, button, pt, 5);
        } else {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(2 * s, 0, 8 * s);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
            pose.translate(4 * s, 0, 0);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
            pose.translate(-2 * s, 0, 2 * s);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
            pose.translate(0, 0, -4 * s);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
            matrices.popPose();

            pose = matrices.last();
            pose.translate(3 * s, 0, 3 * s);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
            pose.translate(2 * s, 0, 0);
            submitQuads(displayContext, matrices, queue, light, overlay, button);
        }

        matrices.popPose();
    }

    private void submitPressButton(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        List<BakedQuad> button,
        float pt,
        int index
    ) {
        matrices.pushPose();
        matrices.translate(0, -0.046875f * buttons.get(index).getValue(pt), 0);
        submitQuads(displayContext, matrices, queue, light, overlay, button);
        matrices.popPose();
    }

    private void submitQuads(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        List<BakedQuad> quads
    ) {
        queue.submitItem(matrices, displayContext, light, overlay, 0, tints, quads, FoilType.NONE);
    }

    public static class RenderData {
        ItemDisplayContext displayContext;
        boolean active;
        boolean equip;
        boolean bind;

        public RenderData(ItemDisplayContext displayContext) {
            this.displayContext = displayContext;
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
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(POWERED_ID);
            resolver.markDependency(TORCH_ID);
            resolver.markDependency(TORCH_OFF_ID);
            resolver.markDependency(BUTTON_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new LinkedControllerModel(
                settings,
                transformation,
                quads,
                BakedModelHelper.bakeQuads(baker, POWERED_ID),
                bakeBlockQuads(baker, TORCH_ID),
                BakedModelHelper.bakeQuads(baker, TORCH_OFF_ID),
                BakedModelHelper.bakeQuads(baker, BUTTON_ID)
            );
        }

        private static List<BakedQuad> bakeBlockQuads(ModelBaker baker, Identifier id) {
            return BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(baker, id),
                ChunkSectionLayer.CUTOUT,
                RenderTypes.solidMovingBlock()
            );
        }
    }
}
