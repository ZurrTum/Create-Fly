package com.zurrtum.create.client.catnip.outliner;

import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class ItemOutline extends Outline {

    protected Vec3d pos;
    protected ItemStack stack;
    protected ItemRenderState itemRenderState;

    public ItemOutline(Vec3d pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
        this.itemRenderState = new ItemRenderState();
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        ms.push();

        ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        ms.scale(params.alpha, params.alpha, params.alpha);

        mc.getItemModelManager().clearAndUpdate(this.itemRenderState, stack, ItemDisplayContext.FIXED, null, null, 0);
        this.itemRenderState.render(ms, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);

        ms.pop();
    }
}
