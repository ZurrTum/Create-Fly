package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.client.render.model.BakedModelBufferer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.*;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.compat.sodium.SodiumCompat;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WorldSectionElementImpl extends AnimatedSceneElementBase implements WorldSectionElement {

    public static final Compartment<Pair<Integer, Integer>> PONDER_WORLD_SECTION = new Compartment<>();

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    @Nullable List<BlockEntity> renderedBlockEntities;
    @Nullable List<Pair<BlockEntity, Consumer<World>>> tickableBlockEntities;
    @Nullable Selection section;
    boolean redraw;

    Vec3d prevAnimatedOffset = Vec3d.ZERO;
    Vec3d animatedOffset = Vec3d.ZERO;
    Vec3d prevAnimatedRotation = Vec3d.ZERO;
    Vec3d animatedRotation = Vec3d.ZERO;
    Vec3d centerOfRotation = Vec3d.ZERO;
    @Nullable Vec3d stabilizationAnchor = null;

    @Nullable BlockPos selectedBlock;

    public WorldSectionElementImpl() {
    }

    public WorldSectionElementImpl(Selection section) {
        this.section = section.copy();
        centerOfRotation = section.getCenter();
    }

    @Override
    public void mergeOnto(WorldSectionElement other) {
        setVisible(false);
        if (other.isEmpty())
            other.set(section);
        else
            other.add(section);
    }

    @Override
    public void set(Selection selection) {
        applyNewSelection(selection.copy());
    }

    @Override
    public void add(Selection toAdd) {
        applyNewSelection(this.section.add(toAdd));
    }

    @Override
    public void erase(Selection toErase) {
        applyNewSelection(this.section.substract(toErase));
    }

    private void applyNewSelection(Selection selection) {
        this.section = selection;
        queueRedraw();
    }

    @Override
    public void setCenterOfRotation(Vec3d center) {
        centerOfRotation = center;
    }

    @Override
    public void stabilizeRotation(Vec3d anchor) {
        stabilizationAnchor = anchor;
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        resetAnimatedTransform();
        resetSelectedBlock();
    }

    @Override
    public void selectBlock(BlockPos pos) {
        selectedBlock = pos;
    }

    @Override
    public void resetSelectedBlock() {
        selectedBlock = null;
    }

    public void resetAnimatedTransform() {
        prevAnimatedOffset = Vec3d.ZERO;
        animatedOffset = Vec3d.ZERO;
        prevAnimatedRotation = Vec3d.ZERO;
        animatedRotation = Vec3d.ZERO;
    }

    @Override
    public void queueRedraw() {
        redraw = true;
    }

    @Override
    public boolean isEmpty() {
        return section == null;
    }

    @Override
    public void setEmpty() {
        section = null;
    }

    @Override
    public void setAnimatedRotation(Vec3d eulerAngles, boolean force) {
        this.animatedRotation = eulerAngles;
        if (force)
            prevAnimatedRotation = animatedRotation;
    }

    @Override
    public Vec3d getAnimatedRotation() {
        return animatedRotation;
    }

    @Override
    public void setAnimatedOffset(Vec3d offset, boolean force) {
        this.animatedOffset = offset;
        if (force)
            prevAnimatedOffset = animatedOffset;
    }

    @Override
    public Vec3d getAnimatedOffset() {
        return animatedOffset;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && !isEmpty();
    }

    @Override
    public Pair<Vec3d, BlockHitResult> rayTrace(PonderLevel world, Vec3d source, Vec3d target) {
        world.setMask(this.section);
        Vec3d transformedTarget = reverseTransformVec(target);
        BlockHitResult rayTraceBlocks = world.raycast(new RaycastContext(
            reverseTransformVec(source),
            transformedTarget,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            ShapeContext.absent()
        ));
        world.clearMask();

        double t = rayTraceBlocks.getPos().subtract(transformedTarget).lengthSquared() / source.subtract(target).lengthSquared();
        Vec3d actualHit = VecHelper.lerp((float) t, target, source);
        return Pair.of(actualHit, rayTraceBlocks);
    }

    private Vec3d reverseTransformVec(Vec3d in) {
        float pt = AnimationTickHolder.getPartialTicks();
        in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
        if (!animatedRotation.equals(Vec3d.ZERO) || !prevAnimatedRotation.equals(Vec3d.ZERO)) {
            double rotX = MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
            double rotZ = MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
            double rotY = MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
            in = in.subtract(centerOfRotation);
            in = VecHelper.rotate(in, -rotX, Axis.X);
            in = VecHelper.rotate(in, -rotZ, Axis.Z);
            in = VecHelper.rotate(in, -rotY, Axis.Y);
            in = in.add(centerOfRotation);
            if (stabilizationAnchor != null) {
                in = in.subtract(stabilizationAnchor);
                in = VecHelper.rotate(in, rotX, Axis.X);
                in = VecHelper.rotate(in, rotZ, Axis.Z);
                in = VecHelper.rotate(in, rotY, Axis.Y);
                in = in.add(stabilizationAnchor);
            }
        }
        return in;
    }

    public void transformMS(MatrixStack ms, float pt) {

        Vec3d vec = VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset);
        ms.translate(vec.x, vec.y, vec.z);
        if (!animatedRotation.equals(Vec3d.ZERO) || !prevAnimatedRotation.equals(Vec3d.ZERO)) {
            double rotX = MathHelper.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
            double rotZ = MathHelper.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
            double rotY = MathHelper.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);

            TransformStack.of(ms).translate(centerOfRotation).rotateXDegrees((float) rotX).rotateYDegrees((float) rotY).rotateZDegrees((float) rotZ)
                .translateBack(centerOfRotation);

            if (stabilizationAnchor != null) {
                TransformStack.of(ms).translate(stabilizationAnchor).rotateXDegrees((float) -rotX).rotateYDegrees((float) -rotY)
                    .rotateZDegrees((float) -rotZ).translateBack(stabilizationAnchor);
            }
        }
    }

    @Override
    public void tick(PonderScene scene) {
        prevAnimatedOffset = animatedOffset;
        prevAnimatedRotation = animatedRotation;
        if (!isVisible())
            return;
        loadBEsIfMissing(scene.getWorld());
        renderedBlockEntities.removeIf(be -> scene.getWorld().getBlockEntity(be.getPos()) != be);
        tickableBlockEntities.removeIf(be -> scene.getWorld().getBlockEntity(be.getFirst().getPos()) != be.getFirst());
        tickableBlockEntities.forEach(be -> be.getSecond().accept(scene.getWorld()));
    }

    @Override
    public void whileSkipping(PonderScene scene) {
        if (redraw) {
            renderedBlockEntities = null;
            tickableBlockEntities = null;
        }
        redraw = false;
    }

    @SuppressWarnings("deprecation")
    protected void loadBEsIfMissing(PonderLevel world) {
        if (renderedBlockEntities != null)
            return;
        tickableBlockEntities = new ArrayList<>();
        renderedBlockEntities = new ArrayList<>();
        section.forEach(pos -> {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (blockEntity == null)
                return;
            if (!(block instanceof BlockEntityProvider provider))
                return;
            blockEntity.setCachedState(world.getBlockState(pos));
            BlockEntityTicker<?> ticker = provider.getTicker(world, blockState, blockEntity.getType());
            if (ticker != null)
                addTicker(blockEntity, ticker);
            renderedBlockEntities.add(blockEntity);
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> void addTicker(T blockEntity, BlockEntityTicker<?> ticker) {
        tickableBlockEntities.add(Pair.of(
            blockEntity,
            w -> ((BlockEntityTicker<T>) ticker).tick(w, blockEntity.getPos(), blockEntity.getCachedState(), blockEntity)
        ));
    }

    @Override
    public void renderFirst(
        BlockEntityRenderManager blockEntityRenderDispatcher,
        BlockRenderManager blockRenderManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack poseStack,
        float fade,
        float pt
    ) {
        int light = -1;
        if (fade != 1)
            light = MathHelper.lerp(fade, 5, 15);
        if (redraw) {
            renderedBlockEntities = null;
            tickableBlockEntities = null;
        }

        poseStack.push();
        transformMS(poseStack, pt);
        world.pushFakeLight(light);
        renderBlockEntities(blockEntityRenderDispatcher, world, poseStack, queue, camera, cameraRenderState, pt);
        world.popLight();

        Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
        MatrixStack overlayMS = null;

        for (Map.Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!section.test(pos))
                continue;

            if (overlayMS == null) {
                overlayMS = new MatrixStack();
                MatrixStack.Entry matrixEntry = poseStack.peek();
                overlayMS.peek().getPositionMatrix().set(matrixEntry.getPositionMatrix());
                overlayMS.peek().getNormalMatrix().set(matrixEntry.getNormalMatrix());
            }

            VertexConsumer builder = new OverlayVertexConsumer(
                buffer.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(entry.getValue())),
                overlayMS.peek(),
                1
            );

            poseStack.push();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderManager.renderDamage(world.getBlockState(pos), pos, world, poseStack, builder);
            poseStack.pop();
        }

        poseStack.pop();
    }

    @Override
    protected void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack poseStack, float fade, float pt) {
        SuperByteBufferCache bufferCache = SuperByteBufferCache.getInstance();

        int code = hashCode() ^ world.hashCode();
        Pair<Integer, Integer> key = Pair.of(code, type.ordinal());

        if (redraw)
            bufferCache.invalidate(PONDER_WORLD_SECTION, key);

        SodiumCompat.markPonderSpriteActive(world, section);
        SuperByteBuffer structureBuffer = bufferCache.get(PONDER_WORLD_SECTION, key, () -> buildStructureBuffer(world, type));
        if (structureBuffer.isEmpty())
            return;

        transformMS(structureBuffer.getTransforms(), pt);

        int light = lightCoordsFromFade(fade);
        RenderLayer layer = switch (type) {
            case CUTOUT -> RenderLayer.getCutout();
            case SOLID -> RenderLayer.getSolid();
            case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
            case TRANSLUCENT -> PonderRenderTypes.translucent();
            case TRIPWIRE -> RenderLayer.getTripwire();
        };
        structureBuffer.light(light).renderInto(poseStack.peek(), buffer.getBuffer(layer));
    }

    @Override
    protected void renderLast(
        EntityRenderManager entityRenderManager,
        ItemModelManager itemModelManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack poseStack,
        float fade,
        float pt
    ) {
        redraw = false;
        if (selectedBlock == null)
            return;
        BlockState blockState = world.getBlockState(selectedBlock);
        if (blockState.isAir())
            return;
        MinecraftClient mc = MinecraftClient.getInstance();
        VoxelShape shape = blockState.getOutlineShape(world, selectedBlock, ShapeContext.of(mc.player));
        if (shape.isEmpty())
            return;

        poseStack.push();
        transformMS(poseStack, pt);
        poseStack.translate(selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());

        AABBOutline aabbOutline = new AABBOutline(shape.getBoundingBox().expand(1 / 128f));
        aabbOutline.getParams().lineWidth(1 / 64f).colored(0xefefef).disableLineNormals();
        aabbOutline.render(mc, poseStack, (SuperRenderTypeBuffer) buffer, Vec3d.ZERO, pt);

        poseStack.pop();
    }

    private void renderBlockEntities(
        BlockEntityRenderManager dispatcher,
        PonderLevel world,
        MatrixStack ms,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        float pt
    ) {
        loadBEsIfMissing(world);

        Iterator<BlockEntity> iterator = renderedBlockEntities.iterator();
        Vec3d cameraPos = camera.getCameraPos();
        while (iterator.hasNext()) {
            BlockEntity tile = iterator.next();
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.get(tile);
            if (renderer == null) {
                iterator.remove();
                continue;
            }

            BlockPos pos = tile.getPos();
            ms.push();
            ms.translate(pos.getX(), pos.getY(), pos.getZ());

            try {
                BlockEntityRenderState state = renderer.createRenderState();
                renderer.updateRenderState(tile, state, pt, cameraPos, null);
                if (state.type != BlockEntityType.TEST_BLOCK) {
                    renderer.render(state, ms, queue, cameraRenderState);
                }
            } catch (Exception e) {
                iterator.remove();
                String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(tile.getType()) + " could not be rendered virtually.";
                Ponder.LOGGER.error(message, e);
            }

            ms.pop();
        }
    }

    private SuperByteBuffer buildStructureBuffer(PonderLevel world, BlockRenderLayer layer) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        SbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.prepare(layer);

        world.setMask(section);
        world.pushFakeLight(0);

        BakedModelBufferer.bufferBlocks(section.iterator(), world, null, true, sbbBuilder);

        world.popLight();
        world.clearMask();

        return sbbBuilder.build();
    }

    private static class SbbBuilder extends SuperByteBufferBuilder implements ShadeSeparatedResultConsumer {
        private BlockRenderLayer renderType;

        public void prepare(BlockRenderLayer renderType) {
            prepare();
            this.renderType = renderType;
        }

        @Override
        public void accept(BlockRenderLayer renderType, boolean shaded, BuiltBuffer data) {
            if (renderType != this.renderType) {
                return;
            }

            add(data, shaded);
        }
    }

    private static class ThreadLocalObjects {
        public final SbbBuilder sbbBuilder = new SbbBuilder();
    }

}