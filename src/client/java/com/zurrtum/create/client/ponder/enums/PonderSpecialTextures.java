package com.zurrtum.create.client.ponder.enums;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;

public enum PonderSpecialTextures implements BindableTexture {

    BLANK("blank.png"),

    ;

    public static final String ASSET_PATH = "textures/special/";
    private final Identifier location;

    PonderSpecialTextures(String filename) {
        location = Ponder.asResource(ASSET_PATH + filename);
    }

    @Override
    public TextureSetup bind() {
        TextureManager manager = MinecraftClient.getInstance().getTextureManager();
        GpuTextureView gpuTextureView = manager.getTexture(location).getGlTextureView();
        return TextureSetup.withoutGlTexture(gpuTextureView);
    }

    @Override
    public Identifier getLocation() {
        return location;
    }

}
