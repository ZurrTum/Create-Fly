package com.zurrtum.create.client.vanillin.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.vertex.MutableVertexList;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.QuadMesh;
import com.zurrtum.create.client.flywheel.lib.model.SingleMeshModel;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.SmartRecycler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * A component that uses instances to render the fire animation on an entity.
 */
public final class FireElement extends AbstractVisual implements SimpleDynamicVisual {
    private static final Material FIRE_MATERIAL = SimpleMaterial.builderOf(Materials.CUTOUT_UNSHADED_BLOCK)
        .backfaceCulling(false) // Disable backface because we want to be able to flip the model.
        .build();

    // Parameterize by the material instead of the sprite
    // because Material#sprite is a surprisingly heavy operation
    // and because sprites are invalidated after a resource reload.
    private static final RendererReloadCache<net.minecraft.client.resources.model.Material, Model> FIRE_MODELS = new RendererReloadCache<>(texture -> {
        return new SingleMeshModel(new FireMesh(Minecraft.getInstance().getAtlasManager().get(texture)), FIRE_MATERIAL);
    });

    private final Entity entity;
    private final PoseStack stack = new PoseStack();

    private final SmartRecycler<Model, TransformedInstance> recycler;

    public FireElement(VisualizationContext ctx, Entity entity, float partialTick) {
        super(ctx, entity.level(), partialTick);

        this.entity = entity;

        recycler = new SmartRecycler<>(this::createInstance);
    }

    private TransformedInstance createInstance(Model model) {
        TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        instance.light(LightTexture.FULL_BLOCK);
        instance.setChanged();
        return instance;
    }

    /**
     * Update the fire instances. You'd typically call this in your visual's
     * {@link SimpleDynamicVisual#beginFrame(DynamicVisual.Context) beginFrame} method.
     *
     * @param context The frame context.
     */
    @Override
    public void beginFrame(DynamicVisual.Context context) {
        recycler.resetCount();

        if (entity.displayFireAnimation()) {
            setupInstances(context);
        }

        recycler.discardExtra();
    }

    private void setupInstances(DynamicVisual.Context context) {
        double entityX = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
        double entityY = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
        double entityZ = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());
        var renderOrigin = visualizationContext.renderOrigin();

        final float scale = entity.getBbWidth() * 1.4F;
        final float maxHeight = entity.getBbHeight() / scale;
        float width = 1;
        float y = 0;
        float z = 0;

        stack.setIdentity();
        stack.translate(entityX - renderOrigin.getX(), entityY - renderOrigin.getY(), entityZ - renderOrigin.getZ());
        stack.scale(scale, scale, scale);
        stack.mulPose(Axis.YP.rotationDegrees(-context.camera().yRot()));
        stack.translate(0.0F, 0.0F, -0.3F + (float) ((int) maxHeight) * 0.02F);

        for (int i = 0; y < maxHeight; ++i) {
            var instance = recycler.get(FIRE_MODELS.get(i % 2 == 0 ? ModelBakery.FIRE_0 : ModelBakery.FIRE_1)).setTransform(stack).scaleX(width)
                .translate(0, y, z);

            if (i / 2 % 2 == 0) {
                // Vanilla flips the uv directly, but it's easier for us to flip the whole model.
                instance.scaleX(-1);
            }

            instance.setChanged();

            y += 0.45F;
            // Get narrower as we go up.
            width *= 0.9F;
            // Offset each one so they don't z-fight.
            z += 0.03F;
        }
    }

    @Override
    public void _delete() {
        recycler.delete();
    }

    private record FireMesh(TextureAtlasSprite sprite) implements QuadMesh {
        private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0, 0.5f, 0, Mth.SQRT_OF_TWO * 0.5f);

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public void write(MutableVertexList vertexList) {
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();
            writeVertex(vertexList, 0, 0.5f, 0, u1, v1);
            writeVertex(vertexList, 1, -0.5f, 0, u0, v1);
            writeVertex(vertexList, 2, -0.5f, 1.4f, u0, v0);
            writeVertex(vertexList, 3, 0.5f, 1.4f, u1, v0);
        }

        // Magic numbers taken from:
        // net.minecraft.client.renderer.entity.EntityRenderDispatcher#fireVertex
        private static void writeVertex(MutableVertexList vertexList, int i, float x, float y, float u, float v) {
            vertexList.x(i, x);
            vertexList.y(i, y);
            vertexList.z(i, 0);
            vertexList.r(i, 1);
            vertexList.g(i, 1);
            vertexList.b(i, 1);
            vertexList.u(i, u);
            vertexList.v(i, v);
            vertexList.light(i, LightTexture.FULL_BLOCK);
            vertexList.normalX(i, 0);
            vertexList.normalY(i, 1);
            vertexList.normalZ(i, 0);
        }

        @Override
        public Vector4fc boundingSphere() {
            return BOUNDING_SPHERE;
        }
    }
}
