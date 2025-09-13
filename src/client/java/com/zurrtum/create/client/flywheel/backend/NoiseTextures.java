package com.zurrtum.create.client.flywheel.backend;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.opengl.GL32;

import java.io.IOException;

public class NoiseTextures {
    public static final Identifier NOISE_TEXTURE = ResourceUtil.rl("textures/flywheel/noise/blue.png");

    @UnknownNullability
    public static NativeImageBackedTexture BLUE_NOISE;

    public static void reload(ResourceManager manager) {
        if (BLUE_NOISE != null) {
            BLUE_NOISE.close();
            BLUE_NOISE = null;
        }
        var optional = manager.getResource(NOISE_TEXTURE);

        if (optional.isEmpty()) {
            return;
        }

        try (var is = optional.get().getInputStream()) {
            var image = NativeImage.read(NativeImage.Format.LUMINANCE, is);

            BLUE_NOISE = new NativeImageBackedTexture(() -> "noise", image);

            GlTextureUnit.T0.makeActive();
            GlStateManager._bindTexture(((GlTexture) BLUE_NOISE.getGlTexture()).getGlId());

            NoiseTextures.BLUE_NOISE.setFilter(true, false);
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);

            GlStateManager._bindTexture(0);
        } catch (IOException e) {

        }
    }
}
