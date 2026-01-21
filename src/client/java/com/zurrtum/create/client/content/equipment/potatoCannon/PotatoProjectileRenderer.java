package com.zurrtum.create.client.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.client.AllPotatoProjectileTransforms;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity, PotatoProjectileRenderer.PotatoProjectileState> {
    protected final ItemModelResolver itemModelManager;

    public PotatoProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelManager = context.getItemModelResolver();
    }

    @Override
    public PotatoProjectileState createRenderState() {
        return new PotatoProjectileState();
    }

    @Override
    public void extractRenderState(PotatoProjectileEntity entity, PotatoProjectileState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }
        ItemStackRenderState item = state.item = new ItemStackRenderState();
        item.displayContext = ItemDisplayContext.GROUND;
        itemModelManager.appendItemLayers(item, stack, item.displayContext, entity.level(), null, 0);
        state.box = entity.getBoundingBox();
        state.velocity = entity.getDeltaMovement();
        state.translateY = (float) (state.box.getYsize() / 2 - 1 / 8f);
        state.mode = entity.getRenderMode();
        state.transformer = AllPotatoProjectileTransforms.get(state.mode);
        state.camera = Minecraft.getInstance().getCameraEntity();
        state.pt = tickProgress;
        state.hash = System.identityHashCode(entity) * 31;
    }

    @Override
    public void submit(PotatoProjectileState state, PoseStack ms, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.item.isEmpty()) {
            return;
        }
        ms.pushPose();
        ms.translate(0, state.translateY, 0);
        if (state.transformer != null) {
            state.transformer.transform(state.mode, ms, state);
        }
        state.item.submit(ms, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    public static class PotatoProjectileState extends EntityRenderState {
        public ItemStackRenderState item;
        public float translateY;
        public PotatoProjectileRenderMode mode;
        public @Nullable PotatoProjectileTransform<PotatoProjectileRenderMode> transformer;
        public float pt;
        public AABB box;
        public Entity camera;
        public Vec3 velocity;
        public int hash;
    }
}
