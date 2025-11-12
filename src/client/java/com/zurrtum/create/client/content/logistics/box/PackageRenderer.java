package com.zurrtum.create.client.content.logistics.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class PackageRenderer extends EntityRenderer<PackageEntity, PackageRenderer.PackageState> {
    public PackageRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        shadowRadius = 0.5f;
    }

    @Override
    public PackageState createRenderState() {
        return new PackageState();
    }

    @Override
    public void extractRenderState(PackageEntity entity, PackageState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        if (VisualizationManager.supportsVisualization(entity.level())) {
            return;
        }
        ItemStack box = entity.box;
        if (box.isEmpty() || !PackageItem.isPackage(box)) {
            box = AllItems.CARDBOARD_BLOCK.getDefaultInstance();
        }
        PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (model == null) {
            return;
        }
        int id = entity.getId();
        float yaw = entity.getYRot(tickProgress);
        state.box = getBoxRenderState(id, yaw, state.lightCoords, model);
    }

    @Override
    public void submit(PackageState state, PoseStack ms, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.box != null) {
            state.box.render(ms, queue);
        }
        super.submit(state, ms, queue, cameraState);
    }

    public static BoxRenderState getBoxRenderState(int id, float yaw, int light, PartialModel model) {
        BoxRenderState state = new BoxRenderState();
        state.layer = RenderType.solid();
        state.model = CachedBuffers.partial(model, Blocks.AIR.defaultBlockState());
        state.angle = -AngleHelper.rad(yaw + 90);
        state.nudge = id;
        state.light = light;
        return state;
    }

    public static class PackageState extends EntityRenderState {
        public BoxRenderState box;
    }

    public static class BoxRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float angle;
        public int nudge;
        public int light;

        public void render(PoseStack ms, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(ms, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(-.5, 0, -.5).rotateCentered(angle, Direction.UP).light(light).nudge(nudge).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
