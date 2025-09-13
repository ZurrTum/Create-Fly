package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;

public interface BindableTexture {

    default TextureSetup bind() {
        TextureManager manager = MinecraftClient.getInstance().getTextureManager();
        GpuTextureView gpuTextureView = manager.getTexture(getLocation()).getGlTextureView();
        return TextureSetup.withoutGlTexture(gpuTextureView);
    }

    Identifier getLocation();

}
