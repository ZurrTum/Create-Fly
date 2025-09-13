package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.material.CutoutShaders;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.part.InstanceTree;
import com.zurrtum.create.client.flywheel.lib.model.part.ModelTrees;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChestVisual<T extends BlockEntity & LidOpenable> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
    private static final Material MATERIAL = SimpleMaterial.builder().cutout(CutoutShaders.ONE_TENTH)
        .texture(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).mipmap(false).build();

    private static final Map<ChestType, EntityModelLayer> LAYER_LOCATIONS = new EnumMap<>(ChestType.class);

    static {
        LAYER_LOCATIONS.put(ChestType.SINGLE, EntityModelLayers.CHEST);
        LAYER_LOCATIONS.put(ChestType.LEFT, EntityModelLayers.DOUBLE_CHEST_LEFT);
        LAYER_LOCATIONS.put(ChestType.RIGHT, EntityModelLayers.DOUBLE_CHEST_RIGHT);
    }

    @Nullable
    private final InstanceTree instances;
    @Nullable
    private final InstanceTree lid;
    @Nullable
    private final InstanceTree lock;

    @Nullable
    private final Matrix4fc initialPose;
    private final BrightnessCombiner brightnessCombiner = new BrightnessCombiner();
    @Nullable
    private final DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> neighborCombineResult;
    @Nullable
    private final Float2FloatFunction lidProgress;

    private float lastProgress = Float.NaN;

    public ChestVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        Block block = blockState.getBlock();
        if (block instanceof AbstractChestBlock<?> chestBlock) {
            ChestType chestType = blockState.contains(ChestBlock.CHEST_TYPE) ? blockState.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
            SpriteIdentifier texture = TexturedRenderLayers.getChestTextureId(blockEntity, chestType, isChristmas());
            instances = InstanceTree.create(instancerProvider(), ModelTrees.of(LAYER_LOCATIONS.get(chestType), texture, MATERIAL));
            lid = instances.childOrThrow("lid");
            lock = instances.childOrThrow("lock");

            initialPose = createInitialPose();
            neighborCombineResult = chestBlock.getBlockEntitySource(blockState, level, pos, true);
            lidProgress = neighborCombineResult.apply(ChestBlock.getAnimationProgressRetriever(blockEntity));

            lastProgress = lidProgress.get(partialTick);
            applyLidTransform(lastProgress);
        } else {
            instances = null;
            lid = null;
            lock = null;
            initialPose = null;
            neighborCombineResult = null;
            lidProgress = null;
        }
    }

    private static boolean isChristmas() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
    }

    private Matrix4f createInitialPose() {
        BlockPos visualPos = getVisualPosition();
        float horizontalAngle = blockState.get(ChestBlock.FACING).getPositiveHorizontalDegrees();
        return new Matrix4f().translate(visualPos.getX(), visualPos.getY(), visualPos.getZ()).translate(0.5F, 0.5F, 0.5F)
            .rotateY(-horizontalAngle * MathHelper.RADIANS_PER_DEGREE).translate(-0.5F, -0.5F, -0.5F);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        this.lightSections = sectionCollector;

        if (neighborCombineResult != null) {
            lightSections.sections(neighborCombineResult.apply(new SectionPosCombiner()));
        } else {
            lightSections.sections(LongSet.of(ChunkSectionPos.toLong(pos)));
        }
    }

    @Override
    public void beginFrame(Context context) {
        if (instances == null) {
            return;
        }

        if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
            return;
        }

        float progress = lidProgress.get(context.partialTick());
        if (lastProgress == progress) {
            return;
        }
        lastProgress = progress;

        applyLidTransform(progress);
    }

    private void applyLidTransform(float progress) {
        progress = 1.0F - progress;
        progress = 1.0F - progress * progress * progress;

        lid.xRot(-(progress * ((float) Math.PI / 2F)));
        lock.xRot(lid.xRot());
        instances.updateInstancesStatic(initialPose);
    }

    @Override
    public void updateLight(float partialTick) {
        if (instances != null) {
            int packedLight = neighborCombineResult.apply(brightnessCombiner);
            instances.traverse(instance -> {
                instance.light(packedLight).setChanged();
            });
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (instances != null) {
            instances.traverse(consumer);
        }
    }

    @Override
    protected void _delete() {
        if (instances != null) {
            instances.delete();
        }
    }

    private class SectionPosCombiner implements DoubleBlockProperties.PropertyRetriever<BlockEntity, LongSet> {
        @Override
        public LongSet getFromBoth(BlockEntity first, BlockEntity second) {
            long firstSection = ChunkSectionPos.toLong(first.getPos());
            long secondSection = ChunkSectionPos.toLong(second.getPos());

            if (firstSection == secondSection) {
                return LongSet.of(firstSection);
            } else {
                return LongSet.of(firstSection, secondSection);
            }
        }

        @Override
        public LongSet getFrom(BlockEntity single) {
            return LongSet.of(ChunkSectionPos.toLong(single.getPos()));
        }

        @Override
        public LongSet getFallback() {
            return LongSet.of(ChunkSectionPos.toLong(pos));
        }
    }

    private class BrightnessCombiner implements DoubleBlockProperties.PropertyRetriever<BlockEntity, Integer> {
        @Override
        public Integer getFromBoth(BlockEntity first, BlockEntity second) {
            int firstLight = WorldRenderer.getLightmapCoordinates(first.getWorld(), first.getPos());
            int secondLight = WorldRenderer.getLightmapCoordinates(second.getWorld(), second.getPos());
            int firstBlockLight = LightmapTextureManager.getBlockLightCoordinates(firstLight);
            int secondBlockLight = LightmapTextureManager.getBlockLightCoordinates(secondLight);
            int firstSkyLight = LightmapTextureManager.getSkyLightCoordinates(firstLight);
            int secondSkyLight = LightmapTextureManager.getSkyLightCoordinates(secondLight);
            return LightmapTextureManager.pack(Math.max(firstBlockLight, secondBlockLight), Math.max(firstSkyLight, secondSkyLight));
        }

        @Override
        public Integer getFrom(BlockEntity single) {
            return WorldRenderer.getLightmapCoordinates(single.getWorld(), single.getPos());
        }

        @Override
        public Integer getFallback() {
            return WorldRenderer.getLightmapCoordinates(level, pos);
        }
    }
}
