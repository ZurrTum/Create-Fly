package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.render.EntityBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import com.zurrtum.create.client.foundation.utility.worldWrappers.WrappedBlockAndTintGetter;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState> extends EntityRenderer<C, S> {
    public static final SuperByteBufferCache.Compartment<Contraption> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
    private static final ThreadLocal<EntityBlockSbbBuilder> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        EntityBlockSbbBuilder::new);
    private final BlockStateModelSet blockStateModelSet;
    private final Pose identity = new Pose();

    public ContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockStateModelSet = context.getBlockModelResolver().modelManager.getBlockStateModelSet();
    }

    @SuppressWarnings("unchecked")
    public ClientContraption getOrCreateClientContraptionLazy(Contraption contraption) {
        AtomicReference<@Nullable ClientContraption> clientContraption = (AtomicReference<ClientContraption>) contraption.clientContraption;
        var out = clientContraption.getAcquire();
        if (out == null) {
            // Another thread may hit this block in the same moment.
            // One thread will win and the ContraptionRenderInfo that
            // it generated will become canonical. It's important that
            // we only maintain one RenderInfo instance, specifically
            // for the VirtualRenderWorld inside.
            clientContraption.compareAndExchangeRelease(null, createClientContraption(contraption));

            // Must get again to ensure we have the canonical instance.
            out = clientContraption.getAcquire();
        }
        return out;
    }

    protected ClientContraption createClientContraption(Contraption contraption) {
        return new ClientContraption(contraption);
    }

    private static SuperByteBuffer buildStructureBuffer(
        Contraption contraption,
        ClientContraption clientContraption,
        VirtualRenderWorld renderWorld,
        BlockStateModelSet blockStateModelSet
    ) {
        EntityBlockSbbBuilder sbbBuilder = THREAD_LOCAL_OBJECTS.get();
        ModelConsumer renderer = ModelRenderHelper.getCullHelper(sbbBuilder);
        RenderedBlocks blocks = clientContraption.getRenderedBlocks();
        Function<BlockPos, BlockState> lookup = blocks.lookup();
        BlockAndTintGetter modelWorld = new WrappedBlockAndTintGetter(renderWorld) {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                return lookup.apply(pos);
            }
        };
        Vec3 offset = contraption.entity.toGlobalVector(Vec3.ZERO, 1);
        int offsetX = (int) Math.round(offset.x);
        int offsetY = (int) Math.round(offset.y);
        int offsetZ = (int) Math.round(offset.z);
        MutableBlockPos globalPos = new MutableBlockPos();
        BlockModelLighter.enableCaching();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = lookup.apply(pos);
            if (state.getRenderShape() == RenderShape.MODEL) {
                renderer.tesselateBlock(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    modelWorld,
                    pos,
                    state,
                    blockStateModelSet.get(state),
                    state.getSeed(globalPos.setWithOffset(pos, offsetX, offsetY, offsetZ))
                );
            }
        }
        BlockModelLighter.clearCache();
        return sbbBuilder.build();
    }

    @Override
    public boolean shouldRender(
        C entity,
        Frustum frustum,
        double cameraX,
        double cameraY,
        double cameraZ,
        float partialTicks
    ) {
        if (entity.getContraption() == null) {
            return false;
        }
        if (!entity.isAliveOrStale()) {
            return false;
        }
        if (!entity.isReadyForRender()) {
            return false;
        }

        return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ, partialTicks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new AbstractContraptionState();
    }

    @Override
    public void extractRenderState(C entity, S state, float tickProgress) {
        state.entityType = entity.getType();
        Contraption contraption = entity.getContraption();
        ClientContraption clientContraption =
            contraption != null ? getOrCreateClientContraptionLazy(contraption) : null;
        if (clientContraption == null) {
            return;
        }
        Camera camera = entityRenderDispatcher.camera;
        if (camera == null) {
            return;
        }
        state.x = Mth.lerp(tickProgress, entity.xOld, entity.getX());
        state.y = Mth.lerp(tickProgress, entity.yOld, entity.getY());
        state.z = Mth.lerp(tickProgress, entity.zOld, entity.getZ());
        Pose transform = createTransform(entity, tickProgress);
        Level world = entity.level();
        VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
        Matrix4f worldMatrix4f = new Matrix4f().setTranslation((float) state.x, (float) state.y, (float) state.z);
        boolean support = VisualizationManager.supportsVisualization(world);
        if (!support) {
            SuperByteBuffer buffer = SuperByteBufferCache.getInstance().get(
                CONTRAPTION,
                contraption,
                () -> buildStructureBuffer(contraption, clientContraption, renderWorld, blockStateModelSet)
            );
            if (!buffer.isEmpty()) {
                state.layers = buffer.transform(transform).useLevelLight(world, worldMatrix4f).extractRenderState();
            }
        }
        BitSet adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
        clientContraption.scratchErroredBlockEntities.clear();
        Vec3 cameraPos = camera.position();
        Matrix4f lightTransform = worldMatrix4f.mul(transform.pose(), new Matrix4f());
        state.blockEntity = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            support,
            clientContraption.renderedBlockEntityView,
            adjustRenderedBlockEntities,
            clientContraption.scratchErroredBlockEntities,
            renderWorld,
            world,
            transform,
            lightTransform,
            contraption.entity.toLocalVector(cameraPos, tickProgress),
            tickProgress
        );
        clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
        state.actors = createActors(cameraPos, getFont(), world, renderWorld, contraption, transform, worldMatrix4f);
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        if (state.layers != null) {
            state.layers.submit(poseStack, queue);
        }
        if (state.blockEntity != null) {
            state.blockEntity.submit(poseStack, queue, cameraRenderState);
        }
        if (state.actors != null) {
            for (MovementRenderState actor : state.actors) {
                actor.submit(poseStack, queue);
            }
        }
    }

    public Pose createTransform(C entity, float tickProgress) {
        return identity;
    }

    public static class AbstractContraptionState extends EntityRenderState {
        public @Nullable SuperByteBufferRenderState layers;
        public @Nullable BlockEntityListRenderState blockEntity;
        public @Nullable List<MovementRenderState> actors;
    }

    @Nullable
    public static List<MovementRenderState> createActors(
        Vec3 camera,
        Font textRenderer,
        Level world,
        VirtualRenderWorld renderWorld,
        Contraption contraption,
        Pose transform,
        Matrix4f worldMatrix4f
    ) {
        List<MovementRenderState> actors = new ArrayList<>();
        for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null) {
                continue;
            }
            if (context.world == null) {
                context.world = world;
            }
            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(context.state);
            if (movementBehaviour != null) {
                MovementRenderBehaviour render = movementBehaviour.getAttachRender();
                if (render == null || contraption.isHiddenInPortal(context.localPos)) {
                    continue;
                }
                MovementRenderState renderState = render.getRenderState(
                    camera,
                    textRenderer,
                    context,
                    renderWorld,
                    transform,
                    worldMatrix4f
                );
                if (renderState != null) {
                    actors.add(renderState);
                }
            }
        }
        if (actors.isEmpty()) {
            return null;
        }
        return actors;
    }
}
