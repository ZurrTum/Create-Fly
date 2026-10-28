package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTransformRenderKey {
    public BlockState state;
    public BlockStateModel model;
    public boolean dirty;
    public int padding;
    public float size, xRadians, yRadians, zRadians;

    public BlockTransformRenderKey(BlockState state) {
        this.state = state;
        model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    public void update(float scale, int padding, float xRadians, float yRadians, float zRadians) {
        float size = scale * 16 + padding;
        if (size != this.size || xRadians != this.xRadians || yRadians != this.yRadians || zRadians != this.zRadians) {
            dirty = true;
            this.size = size;
            this.padding = padding;
            this.xRadians = xRadians;
            this.yRadians = yRadians;
            this.zRadians = zRadians;
        }
    }
}
