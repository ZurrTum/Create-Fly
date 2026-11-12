package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.material.Material;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;

public class GpuTextureManager {
    private final Object2IntOpenHashMap<Material> materials = new Object2IntOpenHashMap<>();

    public void add(Material material) {
        materials.addTo(material, 1);
    }

    public void prepare() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Material material : materials.keySet()) {
            material.prepareTexture(textureManager);
        }
    }

    public void remove(Material material) {
        materials.computeIntIfPresent(
            material, (m, count) -> {
                int result = count - 1;
                return result > 0 ? result : null;
            }
        );
    }
}
