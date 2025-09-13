package com.zurrtum.create.client.catnip.ghostblock;

import com.zurrtum.create.client.catnip.client.render.model.BakedModelBufferer;
import com.zurrtum.create.client.catnip.impl.client.render.ColoringVertexConsumer;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class GhostBlockRenderer {

    private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();
    private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

    public static GhostBlockRenderer standard() {
        return STANDARD;
    }

    public static GhostBlockRenderer transparent() {
        return TRANSPARENT;
    }

    public abstract void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, GhostBlockParams params);

    private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {
        @Override
        public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, GhostBlockParams params) {
            BlockState state = params.state;
            BlockStateModel model = mc.getBlockRenderManager().getModel(state);
            BlockPos pos = params.pos;

            ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

            ms.push();
            BakedModelBufferer.bufferModel(
                model,
                pos,
                EmptyVirtualBlockGetter.FULL_BRIGHT,
                state,
                ms,
                (layer, shade) -> buffer.getEarlyBuffer(layer)
            );
            ms.pop();
        }
    }

    private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {
        @Override
        public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, GhostBlockParams params) {
            BlockState state = params.state;
            BlockStateModel model = mc.getBlockRenderManager().getModel(state);
            BlockPos pos = params.pos;
            float alpha = params.alphaSupplier.get() * .75f * PlacementClient.getCurrentAlpha();
            VertexConsumer vb = new ColoringVertexConsumer(buffer.getEarlyBuffer(BlockRenderLayer.TRANSLUCENT), 1, 1, 1, alpha);

            ms.push();
            ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

            ms.translate(.5, .5, .5);
            ms.scale(.85f, .85f, .85f);
            ms.translate(-.5, -.5, -.5);
            BakedModelBufferer.bufferModel(model, pos, EmptyVirtualBlockGetter.FULL_BRIGHT, state, ms, (layer, shade) -> vb);
            ms.pop();
        }
    }
}
