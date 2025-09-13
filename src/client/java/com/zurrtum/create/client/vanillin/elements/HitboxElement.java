package com.zurrtum.create.client.vanillin.elements;

import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.Visual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.LineModelBuilder;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.SmartRecycler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public final class HitboxElement implements Visual, SimpleDynamicVisual {
    //    010------110
    //    /|       /|
    //   / |      / |
    // 011------111 |
    //  |  |     |  |
    //  | 000----|-100
    //  | /      | /
    //  |/       |/
    // 001------101
    public static final Model BOX_MODEL = new LineModelBuilder(12)
        // Starting from 0, 0, 0
        .line(0, 0, 0, 0, 0, 1).line(0, 0, 0, 0, 1, 0).line(0, 0, 0, 1, 0, 0)
        // Starting from 0, 1, 1
        .line(0, 1, 1, 0, 1, 0).line(0, 1, 1, 0, 0, 1).line(0, 1, 1, 1, 1, 1)
        // Starting from 1, 0, 1
        .line(1, 0, 1, 1, 0, 0).line(1, 0, 1, 1, 1, 1).line(1, 0, 1, 0, 0, 1)
        // Starting from 1, 1, 0
        .line(1, 1, 0, 1, 1, 1).line(1, 1, 0, 1, 0, 0).line(1, 1, 0, 0, 1, 0).build();

    public static final Model LINE_MODEL = new LineModelBuilder(1).line(0, 0, 0, 0, 2, 0).build();

    private final VisualizationContext context;
    private final Entity entity;

    private final SmartRecycler<Model, TransformedInstance> recycler;

    private boolean showEyeBox;

    public HitboxElement(VisualizationContext context, Entity entity, float partialTick) {
        this.context = context;
        this.entity = entity;
        this.showEyeBox = entity instanceof LivingEntity;

        this.recycler = new SmartRecycler<>(this::createInstance);

        animate(partialTick);
    }

    public HitboxElement(VisualizationContext context, Entity entity, float partialTick, boolean showEyeBox) {
        this(context, entity, partialTick);
        this.showEyeBox = showEyeBox;
    }

    private TransformedInstance createInstance(Model model) {
        TransformedInstance instance = context.instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        instance.light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
        instance.setChanged();
        return instance;
    }

    public boolean doesShowEyeBox() {
        return showEyeBox;
    }

    public HitboxElement showEyeBox(boolean showEyeBox) {
        this.showEyeBox = showEyeBox;
        return this;
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        animate(context.partialTick());
    }

    @Override
    public void update(float partialTick) {

    }

    @Override
    public void delete() {
        recycler.delete();
    }

    public void animate(float partialTick) {
        recycler.resetCount();

        var shouldRenderHitBoxes = MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes();
        if (shouldRenderHitBoxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            double entityX = MathHelper.lerp(partialTick, entity.lastRenderX, entity.getX());
            double entityY = MathHelper.lerp(partialTick, entity.lastRenderY, entity.getY());
            double entityZ = MathHelper.lerp(partialTick, entity.lastRenderZ, entity.getZ());

            var bb = entity.getBoundingBox();

            var boxX = entityX + bb.minX - entity.getX();
            var boxY = entityY + bb.minY - entity.getY();
            var boxZ = entityZ + bb.minZ - entity.getZ();

            var widthX = (float) (bb.maxX - bb.minX);
            var widthY = (float) (bb.maxY - bb.minY);
            var widthZ = (float) (bb.maxZ - bb.minZ);
            recycler.get(BOX_MODEL).setIdentityTransform().translate(boxX, boxY, boxZ).scale(widthX, widthY, widthZ).setChanged();

            // TODO: multipart entities, but forge seems to have an
            //  injection for them so we'll need platform specific code.

            if (showEyeBox) {
                recycler.get(BOX_MODEL).setIdentityTransform().translate(boxX, entityY + entity.getStandingEyeHeight() - 0.01, boxZ)
                    .scale(widthX, 0.02f, widthZ).color(255, 0, 0).setChanged();
            }

            var viewVector = entity.getRotationVec(partialTick);

            recycler.get(LINE_MODEL).setIdentityTransform().translate(entityX, entityY + entity.getStandingEyeHeight(), entityZ)
                .rotate(new Quaternionf().rotateTo(0, 1, 0, (float) viewVector.x, (float) viewVector.y, (float) viewVector.z)).color(0, 0, 255)
                .setChanged();
        }

        recycler.discardExtra();
    }
}
