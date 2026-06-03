package com.zurrtum.create.client.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.*;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class WorldSectionElementImpl extends AnimatedSceneElementBase implements WorldSectionElement {

    public static final Compartment<Integer> PONDER_WORLD_SECTION = new Compartment<>();

    private static final ThreadLocal<EntityBlockLevelSbbBuilder> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        EntityBlockLevelSbbBuilder::new);

    @Nullable List<BlockEntity> renderedBlockEntities;
    @Nullable List<Pair<BlockEntity, Consumer<Level>>> tickableBlockEntities;
    @Nullable Selection section;
    boolean redraw;

    Vec3 prevAnimatedOffset = Vec3.ZERO;
    Vec3 animatedOffset = Vec3.ZERO;
    Vec3 prevAnimatedRotation = Vec3.ZERO;
    Vec3 animatedRotation = Vec3.ZERO;
    Vec3 centerOfRotation = Vec3.ZERO;
    @Nullable Vec3 stabilizationAnchor;

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
        if (other.isEmpty()) {
            other.set(section);
        } else {
            other.add(section);
        }
    }

    @Override
    public void set(Selection selection) {
        applyNewSelection(selection.copy());
    }

    @Override
    public void add(Selection toAdd) {
        applyNewSelection(section.add(toAdd));
    }

    @Override
    public void erase(Selection toErase) {
        applyNewSelection(section.substract(toErase));
    }

    private void applyNewSelection(Selection selection) {
        section = selection;
        queueRedraw();
    }

    @Override
    public void setCenterOfRotation(Vec3 center) {
        centerOfRotation = center;
    }

    @Override
    public void stabilizeRotation(Vec3 anchor) {
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
        prevAnimatedOffset = Vec3.ZERO;
        animatedOffset = Vec3.ZERO;
        prevAnimatedRotation = Vec3.ZERO;
        animatedRotation = Vec3.ZERO;
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
    public void setAnimatedRotation(Vec3 eulerAngles, boolean force) {
        animatedRotation = eulerAngles;
        if (force) {
            prevAnimatedRotation = animatedRotation;
        }
    }

    @Override
    public Vec3 getAnimatedRotation() {
        return animatedRotation;
    }

    @Override
    public void setAnimatedOffset(Vec3 offset, boolean force) {
        animatedOffset = offset;
        if (force) {
            prevAnimatedOffset = animatedOffset;
        }
    }

    @Override
    public Vec3 getAnimatedOffset() {
        return animatedOffset;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && !isEmpty();
    }

    @Override
    public Pair<Vec3, BlockHitResult> rayTrace(PonderLevel world, Vec3 source, Vec3 target) {
        world.setMask(section);
        Vec3 transformedTarget = reverseTransformVec(target);
        BlockHitResult rayTraceBlocks = world.clip(new ClipContext(
            reverseTransformVec(source),
            transformedTarget,
            ClipContext.Block.OUTLINE,
            Fluid.NONE,
            CollisionContext.empty()
        ));
        world.clearMask();

        double t = rayTraceBlocks.getLocation().subtract(transformedTarget).lengthSqr() / source.subtract(target)
            .lengthSqr();
        Vec3 actualHit = VecHelper.lerp((float) t, target, source);
        return Pair.of(actualHit, rayTraceBlocks);
    }

    private Vec3 reverseTransformVec(Vec3 in) {
        float pt = AnimationTickHolder.getPartialTicks();
        in = in.subtract(VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset));
        if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
            double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
            double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
            double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);
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

    public void transformMS(PoseStack ms, float pt) {

        Vec3 vec = VecHelper.lerp(pt, prevAnimatedOffset, animatedOffset);
        ms.translate(vec.x, vec.y, vec.z);
        if (!animatedRotation.equals(Vec3.ZERO) || !prevAnimatedRotation.equals(Vec3.ZERO)) {
            double rotX = Mth.lerp(pt, prevAnimatedRotation.x, animatedRotation.x);
            double rotZ = Mth.lerp(pt, prevAnimatedRotation.z, animatedRotation.z);
            double rotY = Mth.lerp(pt, prevAnimatedRotation.y, animatedRotation.y);

            TransformStack.of(ms).translate(centerOfRotation).rotateXDegrees((float) rotX).rotateYDegrees((float) rotY)
                .rotateZDegrees((float) rotZ).translateBack(centerOfRotation);

            if (stabilizationAnchor != null) {
                TransformStack.of(ms).translate(stabilizationAnchor).rotateXDegrees((float) -rotX)
                    .rotateYDegrees((float) -rotY).rotateZDegrees((float) -rotZ).translateBack(stabilizationAnchor);
            }
        }
    }

    @Override
    public void tick(PonderScene scene) {
        prevAnimatedOffset = animatedOffset;
        prevAnimatedRotation = animatedRotation;
        if (!isVisible()) {
            return;
        }
        loadBEsIfMissing(scene.getLevel(), false);
        renderedBlockEntities.removeIf(be -> scene.getLevel().getBlockEntity(be.getBlockPos()) != be);
        tickableBlockEntities.removeIf(be -> scene.getLevel()
            .getBlockEntity(be.getFirst().getBlockPos()) != be.getFirst());
        tickableBlockEntities.forEach(be -> be.getSecond().accept(scene.getLevel()));
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
    protected void loadBEsIfMissing(PonderLevel world, boolean init) {
        if (renderedBlockEntities != null) {
            return;
        }
        tickableBlockEntities = new ArrayList<>();
        renderedBlockEntities = new ArrayList<>();
        section.forEach(pos -> {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (blockEntity == null) {
                return;
            }
            if (!(block instanceof EntityBlock provider)) {
                return;
            }
            blockEntity.setBlockState(world.getBlockState(pos));
            BlockEntityTicker<?> ticker = provider.getTicker(world, blockState, blockEntity.getType());
            if (ticker != null) {
                addTicker(blockEntity, ticker);
            }
            renderedBlockEntities.add(blockEntity);
        });
        if (init) {
            tickableBlockEntities.forEach(be -> be.getSecond().accept(world));
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> void addTicker(T blockEntity, BlockEntityTicker<?> ticker) {
        tickableBlockEntities.add(Pair.of(
            blockEntity,
            w -> ((BlockEntityTicker<T>) ticker).tick(
                w,
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity
            )
        ));
    }

    @Override
    public void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        ModelManager modelManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack poseStack,
        float fade,
        float pt
    ) {
        int light = -1;
        if (fade != 1) {
            light = (int) Mth.lerp(fade, 5, 15);
        }
        if (redraw) {
            renderedBlockEntities = null;
            tickableBlockEntities = null;
        }

        poseStack.pushPose();
        transformMS(poseStack, pt);
        world.pushFakeLight(light);
        renderBlockEntities(blockEntityRenderDispatcher, world, poseStack, queue, camera, cameraRenderState, pt);
        world.popLight();

        Map<BlockPos, Integer> blockBreakingProgressions = world.getBlockBreakingProgressions();
        PoseStack overlayMS = null;

        BlockStateModelSet blockStateModelSet = modelManager.getBlockStateModelSet();
        for (Entry<BlockPos, Integer> entry : blockBreakingProgressions.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!section.test(pos)) {
                continue;
            }

            if (overlayMS == null) {
                overlayMS = new PoseStack();
                Pose matrixEntry = poseStack.last();
                overlayMS.last().pose().set(matrixEntry.pose());
                overlayMS.last().normal().set(matrixEntry.normal());
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            BlockState state = world.getBlockState(pos);
            BlockStateModel model = blockStateModelSet.get(state);
            long seed = state.getSeed(pos);
            BlockStateModel renderModel = WrapperBlockStateModel.getBlockDestroyModel(model, world, pos, state, seed);
            queue.submitBreakingBlockModel(
                poseStack,
                renderModel != null ? renderModel : model,
                seed,
                entry.getValue()
            );
            poseStack.popPose();
        }

        SuperByteBufferCache bufferCache = SuperByteBufferCache.getInstance();
        Integer key = hashCode() ^ world.hashCode();
        if (redraw) {
            bufferCache.invalidate(PONDER_WORLD_SECTION, key);
        }
        QuadRenderHelper.markFluidSpriteActive(world, section);
        bufferCache.get(PONDER_WORLD_SECTION, key, () -> buildStructureBuffer(world, modelManager))
            .light(lightCoordsFromFade(fade)).submit(poseStack, queue);
        poseStack.popPose();
    }

    @Override
    protected void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack poseStack,
        float fade,
        float pt
    ) {
        redraw = false;
        if (selectedBlock == null) {
            return;
        }
        BlockState blockState = world.getBlockState(selectedBlock);
        if (blockState.isAir()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        VoxelShape shape = blockState.getShape(world, selectedBlock, CollisionContext.of(mc.player));
        if (shape.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        transformMS(poseStack, pt);
        poseStack.translate(selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());

        AABBOutline aabbOutline = new AABBOutline(shape.bounds().inflate(1 / 128.0f));
        aabbOutline.getParams().lineWidth(1 / 64.0f).colored(0xefefef).disableLineNormals();
        aabbOutline.render(mc, poseStack, (SuperRenderTypeBuffer) buffer, Vec3.ZERO, pt);

        poseStack.popPose();
    }

    private void renderBlockEntities(
        BlockEntityRenderDispatcher dispatcher,
        PonderLevel world,
        PoseStack ms,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        float pt
    ) {
        loadBEsIfMissing(world, true);

        Iterator<BlockEntity> iterator = renderedBlockEntities.iterator();
        Vec3 cameraPos = camera.position();
        while (iterator.hasNext()) {
            BlockEntity tile = iterator.next();
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = dispatcher.getRenderer(tile);
            if (renderer == null) {
                iterator.remove();
                continue;
            }
            if (!renderer.shouldRender(tile, cameraPos)) {
                continue;
            }

            BlockPos pos = tile.getBlockPos();
            ms.pushPose();
            ms.translate(pos.getX(), pos.getY(), pos.getZ());

            try {
                BlockEntityRenderState state = renderer.createRenderState();
                renderer.extractRenderState(tile, state, pt, cameraPos, null);
                if (state.blockEntityType != BlockEntityType.TEST_BLOCK) {
                    renderer.submit(state, ms, queue, cameraRenderState);
                }
            } catch (Exception e) {
                iterator.remove();
                String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(tile.getType()) + " could not be rendered virtually.";
                Ponder.LOGGER.error(message, e);
            }

            ms.popPose();
        }
    }

    private SuperByteBuffer buildStructureBuffer(PonderLevel world, ModelManager modelManager) {
        EntityBlockLevelSbbBuilder sbbBuilder = THREAD_LOCAL_OBJECTS.get();
        BlockStateModelSet blockStateModelSet = modelManager.getBlockStateModelSet();
        FluidRenderer fluidRenderer = new FluidRenderer(modelManager.getFluidStateModelSet());
        ModelConsumer blockRenderer = ModelRenderHelper.getCullHelper(sbbBuilder);

        world.setMask(section);
        world.pushFakeLight(0);

        BlockModelLighter.enableCaching();
        for (BlockPos pos : section) {
            BlockState state = world.getBlockState(pos);
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                sbbBuilder.prepareForFluid(pos);
                fluidRenderer.tesselate(world, pos, sbbBuilder, state, fluidState);
            }
            if (state.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    world,
                    pos,
                    state,
                    blockStateModelSet.get(state),
                    state.getSeed(pos)
                );
            }
        }
        BlockModelLighter.clearCache();

        world.popLight();
        world.clearMask();

        return sbbBuilder.build();
    }
}
