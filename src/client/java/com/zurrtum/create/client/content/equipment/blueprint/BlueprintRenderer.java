package com.zurrtum.create.client.content.equipment.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, BlueprintRenderer.BlueprintState> {
    protected final ItemModelResolver itemModelManager;

    public BlueprintRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelManager = context.getItemModelResolver();
    }

    @Override
    public BlueprintState createRenderState() {
        return new BlueprintState();
    }

    @Override
    public void extractRenderState(BlueprintEntity entity, BlueprintState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        float yaw = entity.getYRot(tickProgress);
        float pitch = entity.getXRot();
        int size = entity.size;
        state.layer = Sheets.solidBlockSheet();
        PartialModel partialModel = size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3 : size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;
        state.model = CachedBuffers.partial(partialModel, Blocks.AIR.defaultBlockState());
        state.yRot = Mth.DEG_TO_RAD * -yaw;
        state.xRot = Mth.DEG_TO_RAD * (90.0F + pitch);
        Vec3 offset = new Vec3(-.5, -1 / 32f, -.5);
        if (size == 2) {
            offset = offset.add(.5, 0, -.5);
        }
        state.offset = offset;
        Level level = entity.level();
        int itemSize = size * size * 2;
        ItemStackRenderState[] items = new ItemStackRenderState[itemSize];
        boolean empty = true;
        for (int i = 0; i < itemSize; ) {
            Couple<ItemStack> displayItems = entity.getSection(i >> 1).getDisplayItems();
            ItemStack firstStack = displayItems.getFirst();
            if (!firstStack.isEmpty()) {
                empty = false;
                items[i] = createItemRenderState(itemModelManager, firstStack, level);
            }
            i++;
            ItemStack secondStack = displayItems.getSecond();
            if (!secondStack.isEmpty()) {
                empty = false;
                items[i] = createItemRenderState(itemModelManager, secondStack, level);
            }
            i++;
        }
        if (empty) {
            return;
        }
        state.items = items;
        state.size = size;
        int bl = state.lightCoords >> 4 & 0xf;
        int sl = state.lightCoords >> 20 & 0xf;
        state.normalYRot = pitch != 0 ? 0 : state.yRot;
        if (pitch == -90) {
            state.normalXRot = Mth.DEG_TO_RAD * -45;
        } else if (pitch == 90 || yaw % 180 != 0) {
            state.normalXRot = Mth.DEG_TO_RAD * -15;
            bl = (int) (bl / 1.35);
            sl = (int) (sl / 1.35);
        } else {
            state.normalXRot = Mth.DEG_TO_RAD * -15;
        }
        state.itemXRot = Mth.DEG_TO_RAD * pitch;
        state.itemOffsetZ = 1 / 32f + .001f;
        if (size == 3) {
            state.itemOffsetXY = -1;
        }
        state.itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;
    }

    private static ItemStackRenderState createItemRenderState(ItemModelResolver itemModelManager, ItemStack stack, Level world) {
        ItemStackRenderState state = new ItemStackRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
        return state;
    }

    @Override
    public void submit(BlueprintState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
        ItemStackRenderState[] items = state.items;
        if (items == null) {
            return;
        }
        matrices.pushPose();
        PoseStack.Pose entry = matrices.last();
        Matrix3f normal = entry.normal();
        if (state.normalYRot != 0) {
            normal.rotate(Axis.YP.rotation(state.normalYRot));
        }
        normal.rotate(Axis.XP.rotation(state.normalXRot));
        Matrix4f pose = entry.pose();
        pose.rotate(Axis.YP.rotation(state.yRot));
        pose.rotate(Axis.XP.rotation(state.itemXRot));
        pose.translate(state.itemOffsetXY, state.itemOffsetXY, state.itemOffsetZ);
        Matrix4f copy = new Matrix4f(pose);
        int light = state.itemLight;
        for (int i = 0, size = items.length, n = 0, w = state.size - 1; i < size; ) {
            ItemStackRenderState firstState = items[i++];
            ItemStackRenderState secondState = items[i++];
            if (firstState != null || secondState != null) {
                pose.scale(0.5f, 0.5f, 0.0009765625f);
                if (firstState != null) {
                    firstState.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
                }
                if (secondState != null) {
                    pose.translate(0.325f, -0.325f, 1);
                    pose.scale(.625f, .625f, 1);
                    secondState.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
                }
            }
            if (n < w) {
                copy.translate(1, 0, 0);
                n++;
            } else {
                copy.translate(-w, 1, 0);
                n = 0;
            }
            pose.set(copy);
        }
        matrices.popPose();
    }

    public static class BlueprintState extends EntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float yRot;
        public float xRot;
        public Vec3 offset;
        public ItemStackRenderState[] items;
        public int size;
        public float normalYRot;
        public float normalXRot;
        public float itemXRot;
        public int itemOffsetXY;
        public float itemOffsetZ;
        public int itemLight;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.rotateY(yRot).rotateX(xRot).translate(offset).disableDiffuse().light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
