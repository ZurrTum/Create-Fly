package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public interface BindableTexture {

    default TextureSetup bind() {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        GpuTextureView gpuTextureView = manager.getTexture(getLocation()).getTextureView();
        return TextureSetup.singleTexture(gpuTextureView);
    }

    ResourceLocation getLocation();

}
