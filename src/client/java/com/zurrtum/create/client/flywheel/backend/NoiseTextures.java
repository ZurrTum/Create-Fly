package com.zurrtum.create.client.flywheel.backend;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.opengl.GL32;

import java.io.IOException;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class NoiseTextures {
    public static final ResourceLocation NOISE_TEXTURE = ResourceUtil.rl("textures/flywheel/noise/blue.png");

    @UnknownNullability
    public static DynamicTexture BLUE_NOISE;

    public static void reload(ResourceManager manager) {
        if (BLUE_NOISE != null) {
            BLUE_NOISE.close();
            BLUE_NOISE = null;
        }
        var optional = manager.getResource(NOISE_TEXTURE);

        if (optional.isEmpty()) {
            return;
        }

        try (var is = optional.get().open()) {
            var image = NativeImage.read(NativeImage.Format.LUMINANCE, is);

            BLUE_NOISE = new DynamicTexture(() -> "Flywheel Blue Noise", image);

            GlTextureUnit.T0.makeActive();
            GlStateManager._bindTexture(((GlTexture) BLUE_NOISE.getTexture()).glId());

            NoiseTextures.BLUE_NOISE.setFilter(true, false);
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);

            GlStateManager._bindTexture(0);
        } catch (IOException e) {

        }
    }
}
