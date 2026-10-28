package com.zurrtum.create.client.content.equipment.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintRenderer.BlueprintState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, BlueprintState> {
    protected final ItemModelResolver itemModelManager;

    public BlueprintRenderer(Context context) {
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
        Level level = entity.level();
        CardinalLighting cardinalLighting =
            level instanceof BlockAndTintGetter getter ? getter.cardinalLighting() : null;
        PartialModel partialModel = size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3 :
            size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;
        state.model = CachedBuffers.partial(partialModel, Blocks.AIR.defaultBlockState())
            .cardinalLighting(cardinalLighting).light(state.lightCoords).disableDiffuse().extractRenderState();
        state.yRadians = Mth.DEG_TO_RAD * -yaw;
        state.xRot = 90.0F + pitch;
        Vec3 offset = new Vec3(-0.5, -0.03125f, -0.5);
        if (size == 2) {
            offset = offset.add(0.5, 0, -0.5);
        }
        state.offset = offset;
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
        state.horizontal = pitch == 0 && yaw != 0;
        if (pitch == -90) {
            state.normalXRadians = Radians.DEG_315;
        } else if (pitch == 90 || yaw % 180 != 0) {
            state.normalXRadians = Radians.DEG_345;
            bl = (int) (bl / 1.35);
            sl = (int) (sl / 1.35);
        } else {
            state.normalXRadians = Radians.DEG_345;
        }
        state.itemXRot = pitch;
        state.itemOffsetZ = 1 / 32.0f + 0.001f;
        if (size == 3) {
            state.itemOffsetXY = -1;
        }
        state.itemLight = Mth.floor(sl + 0.5) << 20 | (Mth.floor(bl + 0.5) & 0xf) << 4;
    }

    private static ItemStackRenderState createItemRenderState(
        ItemModelResolver itemModelManager,
        ItemStack stack,
        Level world
    ) {
        ItemStackRenderState state = new ItemStackRenderState();
        state.displayContext = ItemDisplayContext.GUI;
        itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
        return state;
    }

    @Override
    public void submit(
        BlueprintState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.pushPose();
        Pose entry = matrices.last();
        if (state.yRadians != 0) {
            entry.rotate(Axis.YP, state.yRadians);
        }
        if (state.xRot != 0) {
            entry.rotate(Axis.XP, Mth.DEG_TO_RAD * state.xRot);
        }
        Vec3 offset = state.offset;
        entry.translate((float) offset.x, (float) offset.y, (float) offset.z);
        state.model.submit(matrices, queue);
        matrices.popPose();
        @Nullable ItemStackRenderState[] items = state.items;
        if (items == null) {
            return;
        }
        matrices.pushPose();
        Matrix3f normal = entry.normal();
        if (state.horizontal) {
            normal.rotateY(state.yRadians);
        }
        normal.rotateX(state.normalXRadians);
        Matrix4f pose = entry.pose();
        if (state.yRadians != 0) {
            pose.rotateY(state.yRadians);
        }
        if (state.itemXRot != 0) {
            pose.rotateX(Mth.DEG_TO_RAD * state.itemXRot);
        }
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
                    pose.scale(0.625f, 0.625f, 1);
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

    public static class BlueprintState extends EntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public float yRadians;
        public float xRot;
        public @UnknownNullability Vec3 offset;
        public @Nullable ItemStackRenderState @Nullable [] items;
        public int size;
        public boolean horizontal;
        public float normalXRadians;
        public float itemXRot;
        public int itemOffsetXY;
        public float itemOffsetZ;
        public int itemLight;
    }
}
