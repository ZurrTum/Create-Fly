package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.material.Material;
import net.minecraft.client.render.BlockRenderLayer;
import org.jetbrains.annotations.Nullable;

public interface BlockMaterialFunction {
    @Nullable Material apply(BlockRenderLayer chunkRenderType, boolean shaded, boolean ambientOcclusion);
}
