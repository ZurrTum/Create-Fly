package com.zurrtum.create.client.content.contraptions.pulley;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.math.MoreMath;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.SmartRecycler;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.function.Consumer;

public abstract class AbstractPulleyVisual<T extends KineticBlockEntity> extends ShaftVisual<T> implements SimpleDynamicVisual {
    private final ScrollInstance coil;
    private final TransformedInstance magnet;
    private final SmartRecycler<Boolean, TransformedInstance> rope;

    protected final Direction rotatingAbout;
    protected final RotationAxis rotationAxis;

    private final LightCache lightCache = new LightCache();

    private float offset;

    public AbstractPulleyVisual(VisualizationContext dispatcher, T blockEntity, float partialTick) {
        super(dispatcher, blockEntity, partialTick);

        rotatingAbout = Direction.get(Direction.AxisDirection.POSITIVE, rotationAxis());
        rotationAxis = RotationAxis.of(rotatingAbout.getUnitVector());

        float blockStateAngle = AngleHelper.horizontalAngle(rotatingAbout);
        Quaternionfc rotation = new Quaternionf().rotationY(MathHelper.RADIANS_PER_DEGREE * blockStateAngle);

        coil = getCoilModel().createInstance().rotation(rotation).position(getVisualPosition()).setSpriteShift(getCoilAnimation());

        coil.setChanged();

        magnet = magnetInstancer().createInstance();

        rope = new SmartRecycler<>(b -> b ? getHalfRopeModel().createInstance() : getRopeModel().createInstance());

        updateOffset(partialTick);
        updateLight(partialTick);
        animate();
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        super.setSectionCollector(sectionCollector);
        lightCache.updateSections();
    }

    protected abstract Instancer<TransformedInstance> getRopeModel();

    protected abstract Instancer<TransformedInstance> getMagnetModel();

    protected abstract Instancer<TransformedInstance> getHalfMagnetModel();

    protected abstract Instancer<ScrollInstance> getCoilModel();

    protected abstract Instancer<TransformedInstance> getHalfRopeModel();

    protected abstract float getOffset(float pt);

    protected abstract boolean isRunning();

    protected abstract SpriteShiftEntry getCoilAnimation();

    private Instancer<TransformedInstance> magnetInstancer() {
        return offset > .25f ? getMagnetModel() : getHalfMagnetModel();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        updateOffset(ctx.partialTick());
        animate();
    }

    private void animate() {
        coil.offsetV = -offset;
        coil.setChanged();

        magnet.setVisible(isRunning() || offset == 0);

        magnetInstancer().stealInstance(magnet);

        magnet.setIdentityTransform().translate(getVisualPosition()).translate(0, -offset, 0)
            .light(lightCache.getPackedLight(Math.max(0, MathHelper.floor(offset)))).setChanged();

        rope.resetCount();

        if (shouldRenderHalfRope()) {
            float f = offset % 1;
            float halfRopeNudge = f > .75f ? f - 1 : f;

            rope.get(true).setIdentityTransform().translate(getVisualPosition()).translate(0, -halfRopeNudge, 0).light(lightCache.getPackedLight(0))
                .setChanged();
        }

        if (isRunning()) {
            int neededRopeCount = getNeededRopeCount();

            for (int i = 0; i < neededRopeCount; i++) {

                rope.get(false).setIdentityTransform().translate(getVisualPosition()).translate(0, -offset + i + 1, 0)
                    .light(lightCache.getPackedLight(neededRopeCount - 1 - i)).setChanged();
            }
        }

        rope.discardExtra();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(coil);

        lightCache.update();
    }

    private void updateOffset(float pt) {
        offset = getOffset(pt);
        lightCache.setSize(MathHelper.ceil(offset) + 2);
    }

    private int getNeededRopeCount() {
        return Math.max(0, MathHelper.ceil(offset - 1.25f));
    }

    private boolean shouldRenderHalfRope() {
        float f = offset % 1;
        return offset > .75f && (f < .25f || f > .75f);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(coil);
        consumer.accept(magnet);
    }

    @Override
    protected void _delete() {
        super._delete();
        coil.delete();
        magnet.delete();
        rope.delete();
    }

    private class LightCache {
        private final ByteList data = new ByteArrayList();
        private final LongSet sections = new LongOpenHashSet();
        private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        private int sectionCount;

        public void setSize(int size) {
            if (size != data.size()) {
                data.size(size);
                update();

                int sectionCount = MoreMath.ceilingDiv(size + 15 - pos.getY() + pos.getY() / 4 * 4, ChunkSectionPos.field_33097);
                if (sectionCount != this.sectionCount) {
                    this.sectionCount = sectionCount;
                    sections.clear();
                    int sectionX = ChunkSectionPos.getSectionCoord(pos.getX());
                    int sectionY = ChunkSectionPos.getSectionCoord(pos.getY());
                    int sectionZ = ChunkSectionPos.getSectionCoord(pos.getZ());
                    for (int i = 0; i < sectionCount; i++) {
                        sections.add(ChunkSectionPos.asLong(sectionX, sectionY - i, sectionZ));
                    }
                    // Will be null during initialization
                    if (lightSections != null) {
                        updateSections();
                    }
                }
            }
        }

        public void updateSections() {
            lightSections.sections(sections);
        }

        public void update() {
            mutablePos.set(pos);

            for (int i = 0; i < data.size(); i++) {
                int blockLight = level.getLightLevel(LightType.BLOCK, mutablePos);
                int skyLight = level.getLightLevel(LightType.SKY, mutablePos);
                int light = ((skyLight & 0xF) << 4) | (blockLight & 0xF);
                data.set(i, (byte) light);
                mutablePos.move(Direction.DOWN);
            }
        }

        public int getPackedLight(int offset) {
            if (offset < 0 || offset >= data.size()) {
                return 0;
            }

            int light = Byte.toUnsignedInt(data.getByte(offset));
            int blockLight = light & 0xF;
            int skyLight = (light >>> 4) & 0xF;
            return LightmapTextureManager.pack(blockLight, skyLight);
        }
    }
}
