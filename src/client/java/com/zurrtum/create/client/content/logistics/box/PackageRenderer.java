package com.zurrtum.create.client.content.logistics.box;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Direction;

public class PackageRenderer extends EntityRenderer<PackageEntity, PackageRenderer.PackageState> {
    public PackageRenderer(EntityRendererFactory.Context pContext) {
        super(pContext);
        shadowRadius = 0.5f;
    }

    @Override
    public PackageState createRenderState() {
        return new PackageState();
    }

    @Override
    public void updateRenderState(PackageEntity entity, PackageState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        if (VisualizationManager.supportsVisualization(entity.getEntityWorld())) {
            return;
        }
        ItemStack box = entity.box;
        if (box.isEmpty() || !PackageItem.isPackage(box)) {
            box = AllItems.CARDBOARD_BLOCK.getDefaultStack();
        }
        PartialModel model = AllPartialModels.PACKAGES.get(Registries.ITEM.getId(box.getItem()));
        if (model == null) {
            return;
        }
        int id = entity.getId();
        float yaw = entity.getLerpedYaw(tickProgress);
        state.box = getBoxRenderState(id, yaw, state.light, model);
    }

    @Override
    public void render(PackageState state, MatrixStack ms, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.box != null) {
            state.box.render(ms, queue);
        }
        super.render(state, ms, queue, cameraState);
    }

    public static BoxRenderState getBoxRenderState(int id, float yaw, int light, PartialModel model) {
        BoxRenderState state = new BoxRenderState();
        state.layer = RenderLayer.getSolid();
        state.model = CachedBuffers.partial(model, Blocks.AIR.getDefaultState());
        state.angle = -AngleHelper.rad(yaw + 90);
        state.nudge = id;
        state.light = light;
        return state;
    }

    public static class PackageState extends EntityRenderState {
        public BoxRenderState box;
    }

    public static class BoxRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float angle;
        public int nudge;
        public int light;

        public void render(MatrixStack ms, OrderedRenderCommandQueue queue) {
            queue.submitCustom(ms, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(-.5, 0, -.5).rotateCentered(angle, Direction.UP).light(light).nudge(nudge).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
