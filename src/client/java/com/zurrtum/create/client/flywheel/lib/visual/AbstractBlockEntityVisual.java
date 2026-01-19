package com.zurrtum.create.client.flywheel.lib.visual;

import com.zurrtum.create.client.flywheel.api.visual.*;
import com.zurrtum.create.client.flywheel.api.visualization.VisualManager;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.math.MoreMath;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.FrustumIntersection;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

/**
 * The layer between a {@link BlockEntity} and the Flywheel backend.
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
 * <br> Implementing one or more of these will give an {@link AbstractBlockEntityVisual} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link BlockEntity}.
 */
public abstract class AbstractBlockEntityVisual<T extends BlockEntity> extends AbstractVisual implements BlockEntityVisual<T>, LightUpdatedVisual {
    protected final T blockEntity;
    protected final BlockPos pos;
    protected final BlockPos visualPos;
    protected final BlockState blockState;
    @UnknownNullability
    protected SectionCollector lightSections;

    public AbstractBlockEntityVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
        super(ctx, blockEntity.getLevel(), partialTick);
        this.blockEntity = blockEntity;
        this.pos = blockEntity.getBlockPos();
        this.blockState = blockEntity.getBlockState();
        this.visualPos = pos.subtract(ctx.renderOrigin());
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        this.lightSections = sectionCollector;
        lightSections.sections(LongSet.of(SectionPos.asLong(pos)));
    }

    /**
     * In order to accommodate for floating point precision errors at high coordinates,
     * {@link VisualManager}s are allowed to arbitrarily adjust the origin, and
     * shift the level matrix provided as a shader uniform accordingly.
     *
     * @return The {@link BlockPos position} of the {@link BlockEntity} this visual
     * represents should be rendered at to appear in the correct location.
     */
    public BlockPos getVisualPosition() {
        return visualPos;
    }

    /**
     * Check if this visual is within the given frustum.
     *
     * @param frustum The current frustum.
     * @return {@code true} if this visual is possibly visible.
     */
    public boolean isVisible(FrustumIntersection frustum) {
        float x = visualPos.getX() + 0.5f;
        float y = visualPos.getY() + 0.5f;
        float z = visualPos.getZ() + 0.5f;
        // Default to checking a sphere exactly encompassing the block.
        return frustum.testSphere(x, y, z, MoreMath.SQRT_3_OVER_2);
    }

    /**
     * Limits which frames this visual is updated on based on its distance from the camera.
     * <p>
     * You may optionally do this check to avoid updating your visual every frame when it is far away.
     *
     * @param context The current frame context.
     * @return {@code true} if this visual shouldn't be updated this frame based on its distance from the camera.
     */
    public boolean doDistanceLimitThisFrame(DynamicVisual.Context context) {
        return !context.limiter().shouldUpdate(pos.distToCenterSqr(context.camera().position()));
    }

    protected int computePackedLight() {
        return LevelRenderer.getLightCoords(level, pos);
    }

    protected void relight(BlockPos pos, @Nullable FlatLit... instances) {
        FlatLit.relight(LevelRenderer.getLightCoords(level, pos), instances);
    }

    protected void relight(@Nullable FlatLit... instances) {
        relight(pos, instances);
    }

    protected void relight(BlockPos pos, Iterator<@Nullable FlatLit> instances) {
        FlatLit.relight(LevelRenderer.getLightCoords(level, pos), instances);
    }

    protected void relight(Iterator<@Nullable FlatLit> instances) {
        relight(pos, instances);
    }

    protected void relight(BlockPos pos, Iterable<@Nullable FlatLit> instances) {
        FlatLit.relight(LevelRenderer.getLightCoords(level, pos), instances);
    }

    protected void relight(Iterable<@Nullable FlatLit> instances) {
        relight(pos, instances);
    }
}
