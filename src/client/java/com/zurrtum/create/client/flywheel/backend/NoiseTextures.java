package com.zurrtum.create.client.flywheel.backend;

import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;

import java.io.IOException;

public class NoiseTextures {
    public static final Identifier NOISE_TEXTURE = ResourceUtil.rl("textures/flywheel/noise/blue.png");

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

            GlSampler sampler = (GlSampler) RenderSystem.getSamplerCache()
                .getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, false);
            GL33C.glBindSampler(GlTextureUnit.T0.number, sampler.getId());
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);
            GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);

            GlStateManager._bindTexture(0);
        } catch (IOException e) {

        }
    }
}
