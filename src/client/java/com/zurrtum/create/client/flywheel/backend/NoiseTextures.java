package com.zurrtum.create.client.flywheel.backend;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PngInfo;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class NoiseTextures {
    public static final Identifier NOISE_TEXTURE = ResourceUtil.rl("textures/flywheel/noise/blue.png");

    @UnknownNullability
    public static AbstractTexture BLUE_NOISE;

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
            var image = readLuminance(is);
            BLUE_NOISE = new NoiseTexture(image);
        } catch (IOException _) {
        }
    }

    private static NativeImage readLuminance(final InputStream inputStream) throws IOException {
        ByteBuffer bytes = null;
        try {
            bytes = TextureUtil.readResource(inputStream);
            if (MemoryUtil.memAddress(bytes) == 0L) {
                throw new IllegalArgumentException("Invalid buffer");
            }
            PngInfo.validateHeader(bytes);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                ByteBuffer pixels = STBImage.stbi_load_from_memory(
                    bytes,
                    w,
                    h,
                    comp,
                    NativeImage.Format.LUMINANCE.components()
                );
                if (pixels == null) {
                    throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
                }
                long address = MemoryUtil.memAddress(pixels);
                NativeImage.MEMORY_POOL.malloc(address, pixels.limit());
                return new NativeImage(NativeImage.Format.LUMINANCE, w.get(0), h.get(0), true, address);
            }
        } finally {
            MemoryUtil.memFree(bytes);
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static class NoiseTexture extends AbstractTexture {
        private final NativeImage pixels;

        public NoiseTexture(NativeImage image) {
            pixels = image;
            GpuDevice device = RenderSystem.getDevice();
            texture = device.createTexture(
                () -> "Flywheel Blue Noise",
                5,
                GpuFormat.R8_UNORM,
                pixels.getWidth(),
                pixels.getHeight(),
                1,
                1
            );
            sampler = RenderSystem.getSamplerCache()
                .getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, false);
            textureView = device.createTextureView(texture);
            device.createCommandEncoder().writeToTexture(texture, pixels);
        }

        @Override
        public void close() {
            pixels.close();
            super.close();
        }
    }
}
