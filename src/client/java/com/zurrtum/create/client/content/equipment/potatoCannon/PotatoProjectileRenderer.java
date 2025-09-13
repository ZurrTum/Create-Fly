package com.zurrtum.create.client.content.equipment.potatoCannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.client.AllPotatoProjectileTransforms;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity, PotatoProjectileRenderer.PotatoProjectileState> {

    public PotatoProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public PotatoProjectileState createRenderState() {
        return new PotatoProjectileState();
    }

    @Override
    public void updateRenderState(PotatoProjectileEntity entity, PotatoProjectileState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        ItemStack stack = entity.getItem();
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getItemModelManager().clearAndUpdate(state.item, stack, ItemDisplayContext.GROUND, entity.getWorld(), null, 0);
        if (stack.isEmpty()) {
            return;
        }
        state.box = entity.getBoundingBox();
        state.velocity = entity.getVelocity();
        state.translateY = (float) (state.box.getLengthY() / 2 - 1 / 8f);
        state.mode = entity.getRenderMode();
        state.transformer = AllPotatoProjectileTransforms.get(state.mode);
        state.camera = mc.getCameraEntity();
        state.pt = tickProgress;
        state.hash = System.identityHashCode(entity) * 31;
    }

    @Override
    public void render(PotatoProjectileState state, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        if (state.item.isEmpty())
            return;
        ms.push();
        ms.translate(0, state.translateY, 0);
        if (state.transformer != null) {
            state.transformer.transform(state.mode, ms, state);
        }

        state.item.render(ms, buffer, light, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }

    public static class PotatoProjectileState extends EntityRenderState {
        public ItemRenderState item = new ItemRenderState();
        public float translateY;
        public PotatoProjectileRenderMode mode;
        public PotatoProjectileTransform<PotatoProjectileRenderMode> transformer;
        public float pt;
        public Box box;
        public Entity camera;
        public Vec3d velocity;
        public int hash;
    }
}
