package com.zurrtum.create.client.content.equipment.blueprint;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Matrix3f;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, BlueprintRenderer.BlueprintState> {
    public BlueprintRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateRenderState(BlueprintEntity entity, BlueprintState state, float tickProgress) {
        state.yaw = entity.getLerpedYaw(tickProgress);
        state.world = entity.getEntityWorld();
        state.pitch = entity.getPitch();
        int size = entity.size;
        Couple<ItemStack>[] sections = new Couple[size * size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int index = x * size + y;
                sections[index] = entity.getSection(index).getDisplayItems();
            }
        }
        state.size = size;
        state.sections = sections;
    }

    @Override
    public void render(BlueprintState state, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        PartialModel partialModel = state.size == 3 ? AllPartialModels.CRAFTING_BLUEPRINT_3x3 : state.size == 2 ? AllPartialModels.CRAFTING_BLUEPRINT_2x2 : AllPartialModels.CRAFTING_BLUEPRINT_1x1;
        SuperByteBuffer sbb = CachedBuffers.partial(partialModel, Blocks.AIR.getDefaultState());
        sbb.rotateYDegrees(-state.yaw).rotateXDegrees(90.0F + state.pitch).translate(-.5, -1 / 32f, -.5);
        if (state.size == 2)
            sbb.translate(.5, 0, -.5);

        sbb.disableDiffuse().light(light).renderInto(ms, buffer.getBuffer(TexturedRenderLayers.getEntitySolid()));

        ms.push();

        float fakeNormalXRotation = -15;
        int bl = light >> 4 & 0xf;
        int sl = light >> 20 & 0xf;
        boolean vertical = state.pitch != 0;
        if (state.pitch == -90)
            fakeNormalXRotation = -45;
        else if (state.pitch == 90 || state.yaw % 180 != 0) {
            bl /= 1.35;
            sl /= 1.35;
        }
        int itemLight = MathHelper.floor(sl + .5) << 20 | (MathHelper.floor(bl + .5) & 0xf) << 4;

        TransformStack.of(ms).rotateYDegrees(vertical ? 0 : -state.yaw).rotateXDegrees(fakeNormalXRotation);
        Matrix3f copy = new Matrix3f(ms.peek().getNormalMatrix());

        ms.pop();
        ms.push();

        TransformStack.of(ms).rotateYDegrees(-state.yaw).rotateXDegrees(state.pitch).translate(0, 0, 1 / 32f + .001);

        if (state.size == 3)
            ms.translate(-1, -1, 0);

        MatrixStack squashedMS = new MatrixStack();
        squashedMS.peek().getPositionMatrix().mul(ms.peek().getPositionMatrix());

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        for (int x = 0; x < state.size; x++) {
            squashedMS.push();
            for (int y = 0; y < state.size; y++) {
                squashedMS.push();
                squashedMS.scale(.5f, .5f, 1 / 1024f);
                state.sections[x * state.size + y].forEachWithContext((stack, primary) -> {
                    if (stack.isEmpty())
                        return;

                    squashedMS.push();
                    if (!primary) {
                        squashedMS.translate(0.325f, -0.325f, 1);
                        squashedMS.scale(.625f, .625f, 1);
                    }

                    squashedMS.peek().getNormalMatrix().set(copy);

                    itemRenderer.renderItem(stack, ItemDisplayContext.GUI, itemLight, OverlayTexture.DEFAULT_UV, squashedMS, buffer, state.world, 0);
                    squashedMS.pop();
                });
                squashedMS.pop();
                squashedMS.translate(1, 0, 0);
            }
            squashedMS.pop();
            squashedMS.translate(0, 1, 0);
        }

        ms.pop();
    }

    @Override
    public BlueprintState createRenderState() {
        return new BlueprintState();
    }

    public static class BlueprintState extends EntityRenderState {
        public int size;
        public float yaw;
        public World world;
        public float pitch;
        public Couple<ItemStack>[] sections;
    }
}
