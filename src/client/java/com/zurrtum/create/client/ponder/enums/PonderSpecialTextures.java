package com.zurrtum.create.client.ponder.enums;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

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
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        GpuTextureView gpuTextureView = manager.getTexture(location).getTextureView();
        return TextureSetup.singleTexture(gpuTextureView);
    }

    @Override
    public Identifier getLocation() {
        return location;
    }

}
