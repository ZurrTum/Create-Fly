package com.zurrtum.create.client.content.kinetics.belt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.ShadowRenderHelper;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.belt.*;
import com.zurrtum.create.content.kinetics.belt.transport.BeltInventory;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderer.BeltRenderState> {
    protected final ItemModelResolver itemModelManager;

    public BeltRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public BeltRenderState createRenderState() {
        return new BeltRenderState();
    }

    @Override
    public void extractRenderState(
        BeltBlockEntity be,
        BeltRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Level world = be.getLevel();
        float speed = be.getSpeed();
        boolean stopped = speed == 0;
        state.render = !VisualizationManager.supportsVisualization(world) && state.blockState.is(AllBlocks.BELT);
        if (state.render) {
            BeltSlope beltSlope = state.blockState.getValue(BeltBlock.SLOPE);
            BeltPart part = state.blockState.getValue(BeltBlock.PART);
            Direction facing = state.blockState.getValue(BeltBlock.HORIZONTAL_FACING);
            AxisDirection axisDirection = facing.getAxisDirection();

            boolean downward = beltSlope == BeltSlope.DOWNWARD;
            boolean upward = beltSlope == BeltSlope.UPWARD;
            boolean diagonal = downward || upward;
            boolean start = part == BeltPart.START;
            boolean end = part == BeltPart.END;
            boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
            boolean alongX = facing.getAxis() == Axis.X;

            state.localTransforms = new PoseStack();
            var msr = TransformStack.of(state.localTransforms);
            state.layer = RenderTypes.solidMovingBlock();

            msr.center().rotateYDegrees(AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0))
                .rotateZDegrees(sideways ? 90 : 0).rotateXDegrees(!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0).uncenter();

            if (downward || beltSlope == BeltSlope.VERTICAL && axisDirection == AxisDirection.POSITIVE) {
                boolean b = start;
                start = end;
                end = b;
            }
            DyeColor color = be.color.orElse(null);
            state.top = CachedBuffers.partial(getBeltPartial(diagonal, start, end, false), state.blockState);
            boolean needScroll = !stopped || color != null;
            double scroll = 0;
            if (needScroll) {
                float time = AnimationTickHolder.getRenderTime(world) * axisDirection.getStep();
                if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX || sideways && axisDirection == AxisDirection.NEGATIVE) {
                    speed = -speed;
                }
                scroll = speed * time / (31.5 * 16);
                float scrollMult = diagonal ? 3f / 8f : 0.5f;
                state.topShift = getSpriteShiftEntry(color, diagonal, false);
                TextureAtlasSprite target = state.topShift.getTarget();
                float spriteSize = target.getV1() - target.getV0();
                state.topScroll = (float) ((scroll - Math.floor(scroll)) * spriteSize * scrollMult);
            }
            if (!diagonal) {
                state.bottom = CachedBuffers.partial(getBeltPartial(false, start, end, true), state.blockState);
                if (needScroll) {
                    scroll += 0.5;
                    state.bottomShift = getSpriteShiftEntry(color, false, true);
                    TextureAtlasSprite target = state.bottomShift.getTarget();
                    float spriteSize = target.getV1() - target.getV0();
                    state.bottomScroll = (float) ((scroll - Math.floor(scroll)) * spriteSize * 0.5f);
                }
            }
            if (be.hasPulley()) {
                Direction dir = sideways ? Direction.UP : facing.getClockWise();

                Supplier<PoseStack> matrixStackSupplier = () -> {
                    PoseStack stack = new PoseStack();
                    var stacker = TransformStack.of(stack);
                    stacker.center();
                    if (dir.getAxis() == Axis.X)
                        stacker.rotateYDegrees(90);
                    if (dir.getAxis() == Axis.Y)
                        stacker.rotateXDegrees(90);
                    stacker.rotateXDegrees(90);
                    stacker.uncenter();
                    return stack;
                };

                state.pulley = CachedBuffers.partialDirectional(AllPartialModels.BELT_PULLEY, state.blockState, dir, matrixStackSupplier);
                Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
                state.pulleyAngle = KineticBlockEntityRenderer.getAngleForBe(be, state.blockPos, axis);
                state.pulleyDirection = Direction.get(AxisDirection.POSITIVE, axis);
                state.pulleyColor = KineticBlockEntityRenderer.getColor(be);
            }
        }
        state.beltLength = be.isController() ? be.beltLength : 0;
        if (state.beltLength != 0) {
            BeltInventory inventory = be.getInventory();
            List<TransportedItemStack> transportedItems = inventory.getTransportedItems();
            TransportedItemStack lazyClientItem = inventory.getLazyClientItem();
            int transportedSize = transportedItems.size();
            BeltItemState[] items;
            if (transportedSize == 0 && lazyClientItem == null) {
                state.beltLength = 0;
                return;
            }
            items = new BeltItemState[lazyClientItem == null ? transportedSize : transportedSize + 1];
            state.items = items;
            state.beltFacing = be.getBeltFacing();
            state.directionVec = state.beltFacing.getUnitVec3i();
            state.beltStartOffset = Vec3.atLowerCornerOf(state.directionVec).scale(-.5).add(.5, 15 / 16f, .5);
            state.slope = state.blockState.getValue(BeltBlock.SLOPE);
            state.verticality = state.slope == BeltSlope.DOWNWARD ? -1 : state.slope == BeltSlope.UPWARD ? 1 : 0;
            state.slopeAlongX = state.beltFacing.getAxis() == Axis.X;
            state.partialTicks = tickProgress;
            state.camera = cameraPos;
            state.onContraption = world instanceof WrappedLevel;
            state.onPonder = world instanceof PonderLevel;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < transportedSize; i++) {
                items[i] = BeltItemState.create(itemModelManager, transportedItems.get(i), state, stopped, world, mutablePos);
            }
            if (lazyClientItem != null) {
                items[transportedSize] = BeltItemState.create(itemModelManager, lazyClientItem, state, stopped, world, mutablePos);
            }
        }
    }

    @Override
    public void submit(BeltRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.render) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
        if (state.beltLength != 0) {
            Vec3 beltStartOffset = state.beltStartOffset;
            matrices.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
            for (BeltItemState item : state.items) {
                renderItem(state, item, matrices, queue);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(/*BeltBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

    public static SpriteShiftEntry getSpriteShiftEntry(DyeColor color, boolean diagonal, boolean bottom) {
        if (color != null) {
            return (diagonal ? AllSpriteShifts.DYED_DIAGONAL_BELTS : bottom ? AllSpriteShifts.DYED_OFFSET_BELTS : AllSpriteShifts.DYED_BELTS).get(
                color);
        } else
            return diagonal ? AllSpriteShifts.BELT_DIAGONAL : bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
    }

    public static PartialModel getBeltPartial(boolean diagonal, boolean start, boolean end, boolean bottom) {
        if (diagonal) {
            if (start)
                return AllPartialModels.BELT_DIAGONAL_START;
            if (end)
                return AllPartialModels.BELT_DIAGONAL_END;
            return AllPartialModels.BELT_DIAGONAL_MIDDLE;
        } else if (bottom) {
            if (start)
                return AllPartialModels.BELT_START_BOTTOM;
            if (end)
                return AllPartialModels.BELT_END_BOTTOM;
            return AllPartialModels.BELT_MIDDLE_BOTTOM;
        } else {
            if (start)
                return AllPartialModels.BELT_START;
            if (end)
                return AllPartialModels.BELT_END;
            return AllPartialModels.BELT_MIDDLE;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void renderItem(BeltRenderState state, BeltItemState item, PoseStack ms, SubmitNodeCollector queue) {
        float offset = item.offset;
        float verticalMovement;
        if (offset < .5)
            verticalMovement = 0;
        else
            verticalMovement = state.verticality * (Math.min(offset, state.beltLength - .5f) - .5f);
        Vec3 offsetVec = Vec3.atLowerCornerOf(state.directionVec).scale(offset);
        if (verticalMovement != 0)
            offsetVec = offsetVec.add(0, verticalMovement, 0);
        boolean onSlope = state.slope != BeltSlope.HORIZONTAL && Mth.clamp(offset, .5f, state.beltLength - .5f) == offset;
        boolean tiltForward = (state.slope == BeltSlope.DOWNWARD ^ state.beltFacing.getAxisDirection() == AxisDirection.POSITIVE) == (state.beltFacing.getAxis() == Axis.Z);
        float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

        BlockPos pos = state.blockPos;
        Vec3 itemPos = state.beltStartOffset.add(pos.getX(), pos.getY(), pos.getZ()).add(offsetVec);

        ms.pushPose();
        TransformStack.of(ms).nudge(item.angle);
        ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

        boolean alongX = state.beltFacing.getClockWise().getAxis() == Axis.X;
        float sideOffset = item.sideOffset;
        if (!alongX)
            sideOffset *= -1;
        ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

        int stackLight;
        if (state.onContraption) {
            stackLight = state.lightCoords;
        } else {
            stackLight = item.light;
        }

        boolean renderUpright = item.upright;
        boolean blockItem = item.state.usesBlockLight();

        int count = 0;
        if (state.onPonder || state.camera.distanceTo(itemPos) < 16)
            count = Mth.log2(item.count) / 2;

        Random r = new Random(item.angle);

        boolean slopeShadowOnly = renderUpright && onSlope;
        float slopeOffset = 1 / 8f;
        if (slopeShadowOnly)
            ms.pushPose();
        if (!renderUpright || slopeShadowOnly)
            ms.mulPose((state.slopeAlongX ? com.mojang.math.Axis.ZP : com.mojang.math.Axis.XP).rotationDegrees(slopeAngle));
        if (onSlope)
            ms.translate(0, slopeOffset, 0);
        ms.pushPose();
        ms.translate(0, -1 / 8f + 0.005f, 0);
        ShadowRenderHelper.renderShadow(ms, queue, .75f, .2f);
        ms.popPose();
        if (slopeShadowOnly) {
            ms.popPose();
            ms.translate(0, slopeOffset, 0);
        }

        if (renderUpright) {
            Vec3 vectorForOffset = BeltHelper.getVectorForOffset(
                state.blockPos,
                state.slope,
                state.verticality,
                state.beltLength,
                state.directionVec,
                offset
            );
            Vec3 diff = vectorForOffset.subtract(state.camera);
            float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
            ms.mulPose(com.mojang.math.Axis.YP.rotation(yRot));
            ms.translate(0, 3 / 32d, 1 / 16f);
        }

        for (int i = 0; i <= count; i++) {
            ms.pushPose();

            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(item.angle));
            if (!blockItem && !renderUpright) {
                ms.translate(0, -.09375, 0);
                ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            }

            if (blockItem && !item.box)
                ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);

            if (item.box) {
                ms.translate(0, 4 / 16f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else {
                ms.scale(.5f, .5f, .5f);
            }

            item.state.submit(ms, queue, stackLight, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();

            if (!renderUpright) {
                if (!blockItem)
                    ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(10));
                ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
            } else
                ms.translate(0, 0, -1 / 16f);

        }

        ms.popPose();
    }

    public static class BeltRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public boolean render;
        public RenderType layer;
        public PoseStack localTransforms;
        public SuperByteBuffer top;
        public SpriteShiftEntry topShift;
        public float topScroll;
        public SuperByteBuffer bottom;
        public SpriteShiftEntry bottomShift;
        public float bottomScroll;
        public SuperByteBuffer pulley;
        public float pulleyAngle;
        public Direction pulleyDirection;
        public Color pulleyColor;
        public int beltLength;
        public BeltItemState[] items;
        public Direction beltFacing;
        public boolean onContraption;
        public Vec3i directionVec;
        public Vec3 beltStartOffset;
        public BeltSlope slope;
        public int verticality;
        public boolean slopeAlongX;
        public float partialTicks;
        public Vec3 camera;
        boolean onPonder;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            top.light(lightCoords);
            if (topShift != null) {
                top.shiftUVScrolling(topShift, topScroll);
            }
            top.transform(localTransforms);
            top.renderInto(matricesEntry, vertexConsumer);
            if (bottom != null) {
                bottom.light(lightCoords);
                if (bottomShift != null) {
                    bottom.shiftUVScrolling(bottomShift, bottomScroll);
                }
                bottom.transform(localTransforms);
                bottom.renderInto(matricesEntry, vertexConsumer);
            }
            if (pulley != null) {
                pulley.light(lightCoords);
                pulley.rotateCentered(pulleyAngle, pulleyDirection);
                pulley.color(pulleyColor);
                pulley.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }

    public record BeltItemState(
        ItemStackRenderState state, float offset, float sideOffset, Integer light, boolean upright, boolean box, int angle, int count
    ) {
        public static BeltItemState create(
            ItemModelResolver itemModelManager,
            TransportedItemStack transported,
            BeltRenderState state,
            boolean stopped,
            Level world,
            BlockPos.MutableBlockPos mutablePos
        ) {
            float offset, sideOffset;
            if (stopped) {
                offset = transported.beltPosition;
                sideOffset = transported.sideOffset;
            } else {
                offset = Mth.lerp(state.partialTicks, transported.prevBeltPosition, transported.beltPosition);
                sideOffset = Mth.lerp(state.partialTicks, transported.prevSideOffset, transported.sideOffset);
            }
            Integer light;
            if (state.onContraption) {
                light = null;
            } else {
                int segment = (int) Math.floor(offset);
                mutablePos.set(state.blockPos)
                    .move(state.directionVec.getX() * segment, state.verticality * segment, state.directionVec.getZ() * segment);
                light = world != null ? LevelRenderer.getLightColor(world, mutablePos) : LightTexture.FULL_BRIGHT;
            }
            ItemStack stack = transported.stack;
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, stack, ItemDisplayContext.FIXED, null, null, 0);
            boolean upright = BeltHelper.isItemUpright(transported.stack);
            boolean box = PackageItem.isPackage(transported.stack);
            return new BeltItemState(renderState, offset, sideOffset, light, upright, box, transported.angle, stack.getCount());
        }
    }
}
