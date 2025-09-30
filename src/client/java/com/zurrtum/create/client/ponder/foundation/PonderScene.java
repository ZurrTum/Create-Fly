package com.zurrtum.create.client.ponder.foundation;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.ponder.api.element.*;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry.SceneOrderingEntry;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.foundation.element.WorldSectionElementImpl;
import com.zurrtum.create.client.ponder.foundation.instruction.HideAllInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import com.zurrtum.create.client.ponder.foundation.registration.PonderLocalization;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PonderScene {

    public static final String TITLE_KEY = "header";

    final PonderLocalization localization;

    private boolean finished;
    //	private int sceneIndex;
    private int textIndex;
    Identifier sceneId;

    private final IntList keyframeTimes;

    List<PonderInstruction> schedule;
    private final List<PonderInstruction> activeSchedule;
    private final Map<UUID, PonderElement> linkedElements;
    private final Set<PonderElement> elements;
    private final List<PonderTag> tags;
    private final List<SceneOrderingEntry> orderingEntries;

    private final PonderLevel world;
    private final String namespace;
    private final Identifier location;
    private final SceneCamera camera;
    private final Outliner outliner;
    private SceneTransform transform;
    //	private String defaultTitle;

    private final WorldSectionElement baseWorldSection;
    private final Entity renderViewEntity;
    private Vec3d pointOfInterest;
    @Nullable
    private Vec3d chasingPointOfInterest;

    int basePlateOffsetX;
    int basePlateOffsetZ;
    int basePlateSize;
    float scaleFactor;
    float yOffset;
    boolean hidePlatformShadow;

    private boolean stoppedCounting;
    private int totalTime;
    private int currentTime;
    private boolean nextUpEnabled = true;

    public PonderScene(
        @Nullable PonderLevel world,
        PonderLocalization localization,
        String namespace,
        Identifier location,
        Collection<Identifier> tags,
        Collection<SceneOrderingEntry> orderingEntries
    ) {
        if (world != null) {
            world.scene = this;
        }
        this.world = world;

        this.localization = localization;

        pointOfInterest = Vec3d.ZERO;
        textIndex = 1;
        hidePlatformShadow = false;

        this.namespace = namespace;
        this.location = location;
        this.sceneId = Identifier.of(namespace, "missing_title");

        outliner = new Outliner();
        elements = new HashSet<>();
        linkedElements = new HashMap<>();
        this.tags = tags.stream().map(PonderIndex.getTagAccess()::getRegisteredTag).toList();
        this.orderingEntries = new ArrayList<>(orderingEntries);
        schedule = new ArrayList<>();
        activeSchedule = new ArrayList<>();
        transform = new SceneTransform();
        basePlateSize = getBounds().getBlockCountX();
        camera = new SceneCamera();
        baseWorldSection = new WorldSectionElementImpl();
        keyframeTimes = new IntArrayList(4);
        scaleFactor = 1;
        yOffset = 0;

        if (world != null) {
            renderViewEntity = new ArmorStandEntity(world, 0, 0, 0);
        } else {
            renderViewEntity = null;
        }

        setPointOfInterest(new Vec3d(0, 4, 0));
    }

    public void deselect() {
        forEach(WorldSectionElement.class, WorldSectionElement::resetSelectedBlock);
    }

    public Pair<ItemStack, BlockPos> rayTraceScene(Vec3d from, Vec3d to) {
        MutableObject<Pair<WorldSectionElement, Pair<Vec3d, BlockHitResult>>> nearestHit = new MutableObject<>();
        MutableDouble bestDistance = new MutableDouble(0);

        forEach(
            WorldSectionElement.class, wse -> {
                wse.resetSelectedBlock();
                if (!wse.isVisible())
                    return;
                Pair<Vec3d, BlockHitResult> rayTrace = wse.rayTrace(world, from, to);
                if (rayTrace == null)
                    return;
                double distanceTo = rayTrace.getFirst().distanceTo(from);
                if (nearestHit.getValue() != null && distanceTo >= bestDistance.getValue())
                    return;

                nearestHit.setValue(Pair.of(wse, rayTrace));
                bestDistance.setValue(distanceTo);
            }
        );

        if (nearestHit.getValue() == null)
            return Pair.of(ItemStack.EMPTY, BlockPos.ORIGIN);

        Pair<Vec3d, BlockHitResult> selectedHit = nearestHit.getValue().getSecond();
        BlockPos selectedPos = selectedHit.getSecond().getBlockPos();

        BlockPos origin = new BlockPos(basePlateOffsetX, 0, basePlateOffsetZ);
        if (!world.getBounds().contains(selectedPos))
            return Pair.of(ItemStack.EMPTY, null);
        if (BlockBox.create(origin, origin.add(new Vec3i(basePlateSize - 1, 0, basePlateSize - 1))).contains(selectedPos)) {
            if (PonderIndex.editingModeActive())
                nearestHit.getValue().getFirst().selectBlock(selectedPos);
            return Pair.of(ItemStack.EMPTY, selectedPos);
        }

        nearestHit.getValue().getFirst().selectBlock(selectedPos);
        BlockState blockState = world.getBlockState(selectedPos);

        ItemStack pickBlock = blockState.getPickStack(world, selectedPos, true);

        return Pair.of(pickBlock, selectedPos);
    }

    public void reset() {
        currentTime = 0;
        activeSchedule.clear();
        schedule.forEach(mdi -> mdi.reset(this));
    }

    public void begin() {
        reset();
        forEach(pe -> pe.reset(this));

        world.restore();
        elements.clear();
        linkedElements.clear();
        keyframeTimes.clear();

        transform = new SceneTransform();
        finished = false;
        setPointOfInterest(new Vec3d(0, 4, 0));

        baseWorldSection.setEmpty();
        baseWorldSection.forceApplyFade(1);
        elements.add(baseWorldSection);

        totalTime = 0;
        stoppedCounting = false;
        activeSchedule.addAll(schedule);
        activeSchedule.forEach(i -> i.onScheduled(this));
    }

    public WorldSectionElement getBaseWorldSection() {
        return baseWorldSection;
    }

    public float getSceneProgress() {
        return totalTime == 0 ? 0 : currentTime / (float) totalTime;
    }

    public void fadeOut() {
        reset();
        activeSchedule.add(new HideAllInstruction(10, null));
    }

    public void renderScene(SuperRenderTypeBuffer buffer, MatrixStack ms, float pt) {
        ms.push();
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity prevRVE = mc.cameraEntity;

        mc.cameraEntity = renderViewEntity;
        forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, ms, pt));
        mc.cameraEntity = prevRVE;

        for (BlockRenderLayer type : BlockRenderLayer.values())
            forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, type, ms, pt));

        forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, ms, pt));
        camera.set(transform.xRotation.getValue(pt) + 90, transform.yRotation.getValue(pt) + 180);
        DiffuseLighting lighting = mc.gameRenderer.getDiffuseLighting();
        lighting.setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        world.renderEntities(ms, buffer, camera, pt);
        lighting.setShaderLights(DiffuseLighting.Type.LEVEL);
        world.renderParticles(ms, buffer, camera, pt);
        outliner.renderOutlines(mc, ms, buffer, Vec3d.ZERO, pt);

        ms.pop();
    }

    public void renderOverlay(PonderUI screen, DrawContext graphics, float partialTicks) {
        Matrix3x2fStack matrices = graphics.getMatrices();
        matrices.pushMatrix();
        forEachVisible(PonderOverlayElement.class, e -> e.render(this, screen, graphics, partialTicks));
        matrices.popMatrix();
    }

    public void setPointOfInterest(Vec3d poi) {
        if (chasingPointOfInterest == null)
            pointOfInterest = poi;
        chasingPointOfInterest = poi;
    }

    public Vec3d getPointOfInterest() {
        return pointOfInterest;
    }

    public void tick() {
        if (chasingPointOfInterest != null)
            pointOfInterest = VecHelper.lerp(.25f, pointOfInterest, chasingPointOfInterest);

        outliner.tickOutlines();
        world.tick();
        transform.tick();
        forEach(e -> e.tick(this));

        if (currentTime < totalTime)
            currentTime++;

        for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext(); ) {
            PonderInstruction instruction = iterator.next();
            instruction.tick(this);
            if (instruction.isComplete()) {
                iterator.remove();
                if (instruction.isBlocking())
                    break;
                continue;
            }
            if (instruction.isBlocking())
                break;
        }

        if (activeSchedule.isEmpty())
            finished = true;
    }

    public void seekToTime(int time) {
        if (time < currentTime)
            throw new IllegalStateException("Cannot seek backwards. Rewind first.");

        while (currentTime < time && !finished) {
            forEach(e -> e.whileSkipping(this));
            tick();
        }

        forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
    }

    public void addToSceneTime(int time) {
        if (!stoppedCounting)
            totalTime += time;
    }

    public void stopCounting() {
        stoppedCounting = true;
    }

    public void markKeyframe(int offset) {
        if (!stoppedCounting)
            keyframeTimes.add(totalTime + offset);
    }

    public void addElement(PonderElement e) {
        elements.add(e);
    }

    public <E extends PonderElement> void linkElement(E e, ElementLink<E> link) {
        linkedElements.put(link.getId(), e);
    }

    @Nullable
    public <E extends PonderElement> E resolve(ElementLink<E> link) {
        return link.cast(linkedElements.get(link.getId()));
    }

    public <E extends PonderElement> Optional<E> resolveOptional(ElementLink<E> link) {
        return Optional.ofNullable(resolve(link));
    }

    public <E extends PonderElement> void runWith(ElementLink<E> link, Consumer<E> callback) {
        callback.accept(resolve(link));
    }

    public <E extends PonderElement, F> F applyTo(ElementLink<E> link, Function<E, F> function) {
        return function.apply(resolve(link));
    }

    public void forEach(Consumer<? super PonderElement> function) {
        for (PonderElement elemtent : elements)
            function.accept(elemtent);
    }

    public <T extends PonderElement> void forEach(Class<T> type, Consumer<T> function) {
        for (PonderElement element : elements)
            if (type.isInstance(element))
                function.accept(type.cast(element));
    }

    public <T extends PonderElement> void forEachVisible(Class<T> type, Consumer<T> function) {
        for (PonderElement element : elements)
            if (type.isInstance(element) && element.isVisible())
                function.accept(type.cast(element));
    }

    public <T extends Entity> void forEachWorldEntity(Class<T> type, Consumer<T> function) {
        for (Entity entity : world.getEntityList()) {
            if (type.isInstance(entity)) {
                function.accept(type.cast(entity));
            }
        }
    }

    public Supplier<String> registerText(String defaultText) {
        final String key = "text_" + textIndex;
        localization.registerSpecific(sceneId, key, defaultText);
        Supplier<String> supplier = () -> localization.getSpecific(sceneId, key);
        textIndex++;
        return supplier;
    }

    public SceneBuilder builder() {
        return new PonderSceneBuilder(this);
    }

    public SceneBuildingUtil getSceneBuildingUtil() {
        return new PonderSceneBuildingUtil(getBounds());
    }

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public String getString(String key) {
        return localization.getSpecific(sceneId, key);
    }

    public PonderLevel getWorld() {
        return world;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getKeyframeCount() {
        return keyframeTimes.size();
    }

    public int getKeyframeTime(int index) {
        return keyframeTimes.getInt(index);
    }

    public List<PonderTag> getTags() {
        return tags;
    }

    public List<SceneOrderingEntry> getOrderingEntries() {
        return orderingEntries;
    }

    public Identifier getLocation() {
        return location;
    }

    public Set<PonderElement> getElements() {
        return elements;
    }

    public BlockBox getBounds() {
        return world == null ? new BlockBox(BlockPos.ORIGIN) : world.getBounds();
    }

    public Identifier getId() {
        return sceneId;
    }

    public SceneTransform getTransform() {
        return transform;
    }

    public Outliner getOutliner() {
        return outliner;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getBasePlateOffsetX() {
        return basePlateOffsetX;
    }

    public int getBasePlateOffsetZ() {
        return basePlateOffsetZ;
    }

    public boolean shouldHidePlatformShadow() {
        return hidePlatformShadow;
    }

    public int getBasePlateSize() {
        return basePlateSize;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public float getYOffset() {
        return yOffset;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setNextUpEnabled(boolean nextUpEnabled) {
        this.nextUpEnabled = nextUpEnabled;
    }

    public boolean isNextUpEnabled() {
        return nextUpEnabled;
    }

    public class SceneTransform {

        public LerpedFloat xRotation, yRotation;

        // Screen params
        private int width, height;
        private double offset;
        private Matrix4f cachedMat;

        public SceneTransform() {
            xRotation = LerpedFloat.angular().disableSmartAngleChasing().startWithValue(-35);
            yRotation = LerpedFloat.angular().disableSmartAngleChasing().startWithValue(55 + 90);
        }

        public void tick() {
            xRotation.tickChaser();
            yRotation.tickChaser();
        }

        public void updateScreenParams(int width, int height, double offset) {
            this.width = width;
            this.height = height;
            this.offset = offset;
            cachedMat = null;
        }

        public MatrixStack apply(MatrixStack ms) {
            return apply(ms, AnimationTickHolder.getPartialTicks(world));
        }

        public MatrixStack apply(MatrixStack ms, float pt) {
            ms.translate(width / 2, height / 2, 200 + offset);

            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35));
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(55));
            ms.translate(offset, 0, 0);
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-55));
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation.getValue(pt)));
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation.getValue(pt)));

            UIRenderHelper.flipForGuiRender(ms);
            float f = 30 * scaleFactor;
            ms.scale(f, f, f);
            ms.translate(basePlateSize / -2f - basePlateOffsetX, -1f + yOffset, basePlateSize / -2f - basePlateOffsetZ);

            return ms;
        }

        public void updateSceneRVE(float pt) {
            Vec3d v = screenToScene(width / 2, height / 2, 500, pt);
            if (renderViewEntity != null)
                renderViewEntity.setPos(v.x, v.y, v.z);
        }

        public Vec3d screenToScene(double x, double y, int depth, float pt) {
            refreshMatrix(pt);
            Vec3d vec = new Vec3d(x, y, depth);

            vec = vec.subtract(width / 2, height / 2, 200 + offset);
            vec = VecHelper.rotate(vec, 35, Axis.X);
            vec = VecHelper.rotate(vec, -55, Axis.Y);
            vec = vec.subtract(offset, 0, 0);
            vec = VecHelper.rotate(vec, 55, Axis.Y);
            vec = VecHelper.rotate(vec, -35, Axis.X);
            vec = VecHelper.rotate(vec, -xRotation.getValue(pt), Axis.X);
            vec = VecHelper.rotate(vec, -yRotation.getValue(pt), Axis.Y);

            float f = 1f / (30 * scaleFactor);

            vec = vec.multiply(f, -f, f);
            vec = vec.subtract(basePlateSize / -2f - basePlateOffsetX, -1f + yOffset, basePlateSize / -2f - basePlateOffsetZ);

            return vec;
        }

        public Vec2f sceneToScreen(Vec3d vec, float pt) {
            refreshMatrix(pt);
            Vector4f vec4 = new Vector4f((float) vec.x, (float) vec.y, (float) vec.z, 1);
            vec4.mul(cachedMat);
            return new Vec2f(vec4.x(), vec4.y());
        }

        protected void refreshMatrix(float pt) {
            if (cachedMat != null)
                return;
            cachedMat = apply(new MatrixStack(), pt).peek().getPositionMatrix();
        }

    }

    public static class SceneCamera extends Camera {

        public void set(float xRotation, float yRotation) {
            setRotation(yRotation, xRotation);
        }

    }

}