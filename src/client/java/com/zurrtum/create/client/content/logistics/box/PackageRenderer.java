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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
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
        if (!VisualizationManager.supportsVisualization(entity.getEntityWorld())) {
            state.box = entity.box;
        }
        state.id = entity.getId();
        state.yaw = entity.getLerpedYaw(tickProgress);
    }

    @Override
    public void render(PackageState state, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        if (state.box != null) {
            ItemStack box = state.box;
            if (box.isEmpty() || !PackageItem.isPackage(box))
                box = AllItems.CARDBOARD_BLOCK.getDefaultStack();
            PartialModel model = AllPartialModels.PACKAGES.get(Registries.ITEM.getId(box.getItem()));
            renderBox(state.id, state.yaw, ms, buffer, light, model);
        }
        super.render(state, ms, buffer, light);
    }

    public static void renderBox(int id, float yaw, MatrixStack ms, VertexConsumerProvider buffer, int light, PartialModel model) {
        if (model == null)
            return;
        SuperByteBuffer sbb = CachedBuffers.partial(model, Blocks.AIR.getDefaultState());
        sbb.translate(-.5, 0, -.5).rotateCentered(-AngleHelper.rad(yaw + 90), Direction.UP).light(light).nudge(id);
        sbb.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    public static class PackageState extends EntityRenderState {
        public ItemStack box;
        public float yaw;
        public int id;
    }
}
