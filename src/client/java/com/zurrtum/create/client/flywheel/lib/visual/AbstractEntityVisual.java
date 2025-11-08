package com.zurrtum.create.client.flywheel.lib.visual;

import com.zurrtum.create.client.flywheel.api.visual.*;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Vector3f;

/**
 * The layer between an {@link Entity} and the Flywheel backend.
 * <br>
 * <br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicVisual}</li>
 *     <li>{@link TickableVisual}</li>
 *     <li>{@link LightUpdatedVisual}</li>
 *     <li>{@link ShaderLightVisual}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give an {@link AbstractEntityVisual} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link Entity}.
 */
public abstract class AbstractEntityVisual<T extends Entity> extends AbstractVisual implements EntityVisual<T> {
    protected final T entity;
    protected final EntityVisibilityTester visibilityTester;

    public AbstractEntityVisual(VisualizationContext ctx, T entity, float partialTick) {
        super(ctx, entity.getWorld(), partialTick);
        this.entity = entity;
        visibilityTester = new EntityVisibilityTester(entity, ctx.renderOrigin(), 1.5f);
    }

    /**
     * Calculate the distance squared between this visual and the given <em>level</em> position.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The distance squared between this visual and the given position.
     */
    public double distanceSquared(double x, double y, double z) {
        return entity.squaredDistanceTo(x, y, z);
    }

    /**
     * In order to accommodate for floating point precision errors at high coordinates,
     * {@link VisualizationManager}s are allowed to arbitrarily adjust the origin, and
     * shift the level matrix provided as a shader uniform accordingly.
     *
     * @return The position this visual should be rendered at to appear in the correct location.
     */
    public Vector3f getVisualPosition() {
        Vec3d pos = entity.getPos();
        var renderOrigin = renderOrigin();
        return new Vector3f((float) (pos.x - renderOrigin.getX()), (float) (pos.y - renderOrigin.getY()), (float) (pos.z - renderOrigin.getZ()));
    }

    /**
     * In order to accommodate for floating point precision errors at high coordinates,
     * {@link VisualizationManager}s are allowed to arbitrarily adjust the origin, and
     * shift the level matrix provided as a shader uniform accordingly.
     *
     * @return The position this visual should be rendered at to appear in the correct location.
     */
    public Vector3f getVisualPosition(float partialTick) {
        Vec3d pos = entity.getPos();
        var renderOrigin = renderOrigin();
        return new Vector3f(
            (float) (MathHelper.lerp(partialTick, entity.lastRenderX, pos.x) - renderOrigin.getX()),
            (float) (MathHelper.lerp(partialTick, entity.lastRenderY, pos.y) - renderOrigin.getY()),
            (float) (MathHelper.lerp(partialTick, entity.lastRenderZ, pos.z) - renderOrigin.getZ())
        );
    }

    public boolean isVisible(FrustumIntersection frustum) {
        return !MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).canBeCulled(entity) || visibilityTester.check(frustum);
    }

    protected int computePackedLight(float partialTick) {
        BlockPos pos = BlockPos.ofFloored(entity.getClientCameraPosVec(partialTick));
        int blockLight = entity.isOnFire() ? 15 : level.getLightLevel(LightType.BLOCK, pos);
        int skyLight = level.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(blockLight, skyLight);
    }

    protected void relight(float partialTick, @Nullable FlatLit... instances) {
        FlatLit.relight(computePackedLight(partialTick), instances);
    }
}
