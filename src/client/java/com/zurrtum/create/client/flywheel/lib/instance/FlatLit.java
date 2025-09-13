package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import net.minecraft.client.render.LightmapTextureManager;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * An interface that implementors of {@link Instance} should also implement if they wish to make use of
 * {@link #relight} and the relighting utilities in {@link AbstractBlockEntityVisual} and {@link AbstractEntityVisual}.
 */
public interface FlatLit extends Instance {
    /**
     * Set the packed light value for this instance.
     *
     * @param packedLight the packed light per {@link LightTexture#pack(int, int)}
     * @return {@code this} for chaining
     */
    FlatLit light(int packedLight);

    /**
     * Set the block and sky light values for this instance.
     *
     * @param blockLight the block light value
     * @param skyLight   the sky light value
     * @return {@code this} for chaining
     */
    default FlatLit light(int blockLight, int skyLight) {
        return light(LightmapTextureManager.pack(blockLight, skyLight));
    }

    static void relight(int packedLight, @Nullable FlatLit... instances) {
        for (FlatLit instance : instances) {
            if (instance != null) {
                instance.light(packedLight).handle().setChanged();
            }
        }
    }

    static void relight(int packedLight, Iterator<@Nullable FlatLit> instances) {
        while (instances.hasNext()) {
            FlatLit instance = instances.next();

            if (instance != null) {
                instance.light(packedLight).handle().setChanged();
            }
        }
    }

    static void relight(int packedLight, Iterable<@Nullable FlatLit> instances) {
        relight(packedLight, instances.iterator());
    }

    static void relight(int packedLight, Stream<@Nullable FlatLit> instances) {
        relight(packedLight, instances.iterator());
    }
}
