package com.zurrtum.create.client.content.logistics.box;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PackageVisual extends AbstractEntityVisual<PackageEntity> implements SimpleDynamicVisual {
    public final TransformedInstance instance;

    public PackageVisual(VisualizationContext ctx, PackageEntity entity, float partialTick) {
        super(ctx, entity, partialTick);

        ItemStack box = entity.box;
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = AllItems.CARDBOARD_BLOCK.getDefaultStack();
        PartialModel model = AllPartialModels.PACKAGES.get(Registries.ITEM.getId(box.getItem()));

        instance = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(model)).createInstance();

        animate(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float partialTick) {
        float yaw = MathHelper.lerp(partialTick, entity.lastYaw, entity.getYaw());

        Vec3d pos = entity.getEntityPos();
        var renderOrigin = renderOrigin();
        var x = (float) (MathHelper.lerp(partialTick, entity.lastX, pos.x) - renderOrigin.getX());
        var y = (float) (MathHelper.lerp(partialTick, entity.lastY, pos.y) - renderOrigin.getY());
        var z = (float) (MathHelper.lerp(partialTick, entity.lastZ, pos.z) - renderOrigin.getZ());

        long randomBits = (long) entity.getId() * 31L * 493286711L;
        randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
        float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

        instance.setIdentityTransform().translate(x - 0.5 + xNudge, y + yNudge, z - 0.5 + zNudge).rotateYCenteredDegrees(-yaw - 90)
            .light(computePackedLight(partialTick)).setChanged();
    }

    @Override
    protected void _delete() {
        instance.delete();
    }
}
