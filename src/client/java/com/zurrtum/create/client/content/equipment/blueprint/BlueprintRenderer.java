package com.zurrtum.create.client.content.equipment.blueprint;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, BlueprintRenderer.BlueprintState> {
    protected final ItemModelManager itemModelManager;

    public BlueprintRenderer(EntityRendererFactory.Context context) {
        super(context);
        itemModelManager = context.getItemModelManager();
    }

    @Override
    public BlueprintState createRenderState() {
        return new BlueprintState();
    }

    @Override
    public void updateRenderState(BlueprintEntity entity, BlueprintState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        float yaw = entity.getLerpedYaw(tickProgress);
        float pitch = entity.getPitch();
        int size = entity.size;
        state.layer = TexturedRenderLayers.getEntitySolid();
        PartialModel partialModel = size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3 : size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;
        state.model = CachedBuffers.partial(partialModel, Blocks.AIR.getDefaultState());
        state.yRot = MathHelper.RADIANS_PER_DEGREE * -yaw;
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (90.0F + pitch);
        Vec3d offset = new Vec3d(-.5, -1 / 32f, -.5);
        if (size == 2) {
            offset = offset.add(.5, 0, -.5);
        }
        state.offset = offset;
        World world = entity.getEntityWorld();
        int itemSize = size * size * 2;
        ItemRenderState[] items = new ItemRenderState[itemSize];
        boolean empty = true;
        for (int i = 0; i < itemSize; ) {
            Couple<ItemStack> displayItems = entity.getSection(i >> 1).getDisplayItems();
            ItemStack firstStack = displayItems.getFirst();
            if (!firstStack.isEmpty()) {
                empty = false;
                items[i] = createItemRenderState(itemModelManager, firstStack, world);
            }
            i++;
            ItemStack secondStack = displayItems.getSecond();
            if (!secondStack.isEmpty()) {
                empty = false;
                items[i] = createItemRenderState(itemModelManager, secondStack, world);
            }
            i++;
        }
        if (empty) {
            return;
        }
        state.items = items;
        state.size = size;
        int bl = state.light >> 4 & 0xf;
        int sl = state.light >> 20 & 0xf;
        state.normalYRot = pitch != 0 ? 0 : state.yRot;
        if (pitch == -90) {
            state.normalXRot = MathHelper.RADIANS_PER_DEGREE * -45;
        } else if (pitch == 90 || yaw % 180 != 0) {
            state.normalXRot = MathHelper.RADIANS_PER_DEGREE * -15;
            bl = (int) (bl / 1.35);
            sl = (int) (sl / 1.35);
        } else {
            state.normalXRot = MathHelper.RADIANS_PER_DEGREE * -15;
        }
        state.itemXRot = MathHelper.RADIANS_PER_DEGREE * pitch;
        state.itemOffsetZ = 1 / 32f + .001f;
        if (size == 3) {
            state.itemOffsetXY = -1;
        }
        state.itemLight = MathHelper.floor(sl + .5) << 20 | (MathHelper.floor(bl + .5) & 0xf) << 4;
    }

    private static ItemRenderState createItemRenderState(ItemModelManager itemModelManager, ItemStack stack, World world) {
        ItemRenderState state = new ItemRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        itemModelManager.update(state, stack, state.displayContext, world, null, 0);
        return state;
    }

    public void render(BlueprintState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        ItemRenderState[] items = state.items;
        if (items == null) {
            return;
        }
        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Matrix3f normal = entry.getNormalMatrix();
        if (state.normalYRot != 0) {
            normal.rotate(RotationAxis.POSITIVE_Y.rotation(state.normalYRot));
        }
        normal.rotate(RotationAxis.POSITIVE_X.rotation(state.normalXRot));
        Matrix4f pose = entry.getPositionMatrix();
        pose.rotate(RotationAxis.POSITIVE_Y.rotation(state.yRot));
        pose.rotate(RotationAxis.POSITIVE_X.rotation(state.itemXRot));
        pose.translate(state.itemOffsetXY, state.itemOffsetXY, state.itemOffsetZ);
        Matrix4f copy = new Matrix4f(pose);
        int light = state.itemLight;
        for (int i = 0, size = items.length, n = 0, w = state.size - 1; i < size; ) {
            ItemRenderState firstState = items[i++];
            ItemRenderState secondState = items[i++];
            if (firstState != null || secondState != null) {
                pose.scale(0.5f, 0.5f, 0.0009765625f);
                if (firstState != null) {
                    firstState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
                }
                if (secondState != null) {
                    pose.translate(0.325f, -0.325f, 1);
                    pose.scale(.625f, .625f, 1);
                    secondState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
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
        matrices.pop();
    }

    public static class BlueprintState extends EntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float yRot;
        public float xRot;
        public Vec3d offset;
        public ItemRenderState[] items;
        public int size;
        public float normalYRot;
        public float normalXRot;
        public float itemXRot;
        public int itemOffsetXY;
        public float itemOffsetZ;
        public int itemLight;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.rotateY(yRot).rotateX(xRot).translate(offset).disableDiffuse().light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
