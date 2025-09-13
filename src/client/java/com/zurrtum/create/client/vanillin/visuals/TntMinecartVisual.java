package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class TntMinecartVisual<T extends TntMinecartEntity> extends MinecartVisual<T> {
    private static final int WHITE_OVERLAY = OverlayTexture.packUv(OverlayTexture.getU(1.0F), 10);

    public TntMinecartVisual(VisualizationContext ctx, T entity, float partialTick) {
        super(ctx, entity, partialTick, EntityModelLayers.TNT_MINECART);
    }

    @Override
    protected void updateContents(TransformedInstance contents, Matrix4f pose, float partialTick) {
        int fuseTime = entity.getFuseTicks();
        if (fuseTime > -1 && (float) fuseTime - partialTick + 1.0F < 10.0F) {
            float f = 1.0F - ((float) fuseTime - partialTick + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float scale = 1.0F + f * 0.3F;
            pose.scale(scale);
        }

        int overlay;
        if (fuseTime > -1 && fuseTime / 5 % 2 == 0) {
            overlay = WHITE_OVERLAY;
        } else {
            overlay = OverlayTexture.DEFAULT_UV;
        }

        contents.setTransform(pose).overlay(overlay).setChanged();
    }
}
