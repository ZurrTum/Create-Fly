package com.zurrtum.create.client.vanillin.elements;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
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
    private static final RendererReloadCache<SpriteIdentifier, Model> FIRE_MODELS = new RendererReloadCache<>(texture -> {
        return new SingleMeshModel(new FireMesh(MinecraftClient.getInstance().getAtlasManager().getSprite(texture)), FIRE_MATERIAL);
    });

    private final Entity entity;
    private final MatrixStack stack = new MatrixStack();

    private final SmartRecycler<Model, TransformedInstance> recycler;

    public FireElement(VisualizationContext ctx, Entity entity, float partialTick) {
        super(ctx, entity.getEntityWorld(), partialTick);

        this.entity = entity;

        recycler = new SmartRecycler<>(this::createInstance);
    }

    private TransformedInstance createInstance(Model model) {
        TransformedInstance instance = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        instance.light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
        instance.setChanged();
        return instance;
    }

    /**
     * Update the fire instances. You'd typically call this in your visual's
     * {@link com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual#beginFrame(DynamicVisual.Context) beginFrame} method.
     *
     * @param context The frame context.
     */
    @Override
    public void beginFrame(DynamicVisual.Context context) {
        recycler.resetCount();

        if (entity.doesRenderOnFire()) {
            setupInstances(context);
        }

        recycler.discardExtra();
    }

    private void setupInstances(DynamicVisual.Context context) {
        double entityX = MathHelper.lerp(context.partialTick(), entity.lastRenderX, entity.getX());
        double entityY = MathHelper.lerp(context.partialTick(), entity.lastRenderY, entity.getY());
        double entityZ = MathHelper.lerp(context.partialTick(), entity.lastRenderZ, entity.getZ());
        var renderOrigin = visualizationContext.renderOrigin();

        final float scale = entity.getWidth() * 1.4F;
        final float maxHeight = entity.getHeight() / scale;
        float width = 1;
        float y = 0;
        float z = 0;

        stack.loadIdentity();
        stack.translate(entityX - renderOrigin.getX(), entityY - renderOrigin.getY(), entityZ - renderOrigin.getZ());
        stack.scale(scale, scale, scale);
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-context.camera().getYaw()));
        stack.translate(0.0F, 0.0F, -0.3F + (float) ((int) maxHeight) * 0.02F);

        for (int i = 0; y < maxHeight; ++i) {
            var instance = recycler.get(FIRE_MODELS.get(i % 2 == 0 ? ModelBaker.FIRE_0 : ModelBaker.FIRE_1)).setTransform(stack).scaleX(width)
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

    private record FireMesh(Sprite sprite) implements QuadMesh {
        private static final Vector4fc BOUNDING_SPHERE = new Vector4f(0, 0.5f, 0, MathHelper.SQUARE_ROOT_OF_TWO * 0.5f);

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public void write(MutableVertexList vertexList) {
            float u0 = sprite.getMinU();
            float v0 = sprite.getMinV();
            float u1 = sprite.getMaxU();
            float v1 = sprite.getMaxV();
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
            vertexList.light(i, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
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
