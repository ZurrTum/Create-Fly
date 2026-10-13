package com.zurrtum.create.client.content.kinetics.belt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.belt.BeltRenderer.BeltRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.kinetics.belt.*;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.transport.BeltInventory;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private static final List<ShadowPiece> SHADOW = Collections.singletonList(new ShadowPiece(
        0,
        0,
        0,
        Shapes.create(-0.2, 0, -0.2, 0.2, 0.2, 0.2),
        0.375f
    ));
    protected static final Function<Direction, Pose> PULLEY_POSE = Util.memoize(facing -> {
        Pose pose = new Pose();
        pose.translate(0.5f, 0.5f, 0.5f);
        switch (facing.getAxis()) {
            case Y:
                pose.rotate(Axis.XP.rotation(RAD_180));
                break;
            case X:
                pose.rotate(Axis.YP.rotation(RAD_90));
            default:
                pose.rotate(Axis.XP.rotation(RAD_90));
        }
        pose.translate(-0.5f, -0.5f, -0.5f);
        return pose;
    });
    protected static final PartialModel[] BELTS = Util.make(() -> {
        PartialModel[] map = new PartialModel[40];
        map[0] = AllPartialModels.BELT_START;
        map[1] = AllPartialModels.BELT_MIDDLE;
        map[2] = AllPartialModels.BELT_END;
        map[3] = map[1];
        map[4] = AllPartialModels.CASING_BELT_START;
        map[5] = AllPartialModels.CASING_BELT_MIDDLE;
        map[6] = AllPartialModels.CASING_BELT_END;
        map[7] = map[5];
        map[8] = AllPartialModels.BELT_DIAGONAL_START;
        map[9] = AllPartialModels.BELT_DIAGONAL_MIDDLE;
        map[10] = AllPartialModels.BELT_DIAGONAL_END;
        map[11] = map[9];
        map[12] = AllPartialModels.CASING_BELT_DIAGONAL_START;
        map[13] = AllPartialModels.CASING_BELT_DIAGONAL_MIDDLE;
        map[14] = AllPartialModels.CASING_BELT_DIAGONAL_END;
        map[15] = map[13];
        map[16] = map[10];
        map[17] = map[9];
        map[18] = map[8];
        map[19] = map[11];
        map[20] = map[14];
        map[21] = map[13];
        map[22] = map[12];
        map[23] = map[15];
        map[24] = map[0];
        map[25] = map[1];
        map[26] = map[2];
        map[27] = map[1];
        map[28] = AllPartialModels.CASING_BELT_START_VERTICAL;
        map[29] = AllPartialModels.CASING_BELT_MIDDLE_VERTICAL;
        map[30] = AllPartialModels.CASING_BELT_END_VERTICAL;
        map[31] = AllPartialModels.CASING_BELT_MIDDLE_VERTICAL_PULLEY;
        map[32] = map[24];
        map[33] = map[25];
        map[34] = map[26];
        map[35] = map[27];
        map[36] = map[28];
        map[37] = map[29];
        map[38] = map[30];
        map[39] = map[31];
        return map;
    });
    protected final ItemModelResolver itemModelManager;

    public BeltRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public boolean shouldRender(BeltBlockEntity blockEntity, Vec3 cameraPosition) {
        if (BlockEntityRenderer.super.shouldRender(blockEntity, cameraPosition)) {
            if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
                return blockEntity.isController() && blockEntity.beltLength > 0;
            }
            return true;
        }
        return false;
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        boolean support = VisualizationManager.supportsVisualization(level);
        boolean stopped = true;
        if (!support) {
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            BeltSlope beltSlope = state.blockState.getValue(BeltBlock.SLOPE);
            BeltPart part = state.blockState.getValue(BeltBlock.PART);
            Direction facing = state.blockState.getValue(BeltBlock.HORIZONTAL_FACING);
            boolean upward = beltSlope == BeltSlope.UPWARD;
            boolean diagonal = upward || beltSlope == BeltSlope.DOWNWARD;
            boolean casing = be.casing != CasingType.NONE;
            boolean vertical = beltSlope == BeltSlope.VERTICAL;
            boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
            boolean alongX = facing.getAxis() == Direction.Axis.X;
            boolean alongZ = facing.getAxis() == Direction.Axis.Z;
            float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (sideways ? 90 : 0) + (
                vertical && alongZ ? 180 : 0);
            float rotY = facing.toYRot() + (alongX ^ upward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (
                vertical && alongX ? 90 : 0);
            float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);
            if (rotX != 0 || rotY != 0) {
                state.rotate = new Quaternionf().rotationXYZ(
                    rotX * Mth.DEG_TO_RAD,
                    rotY * Mth.DEG_TO_RAD,
                    rotZ * Mth.DEG_TO_RAD
                );
            }
            DyeColor color = be.color.orElse(null);
            CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
            PartialModel model = getBeltPartial(beltSlope, part, casing);
            SuperByteBuffer belt = CachedBuffers.partial(model, state.blockState);
            float speed = be.getSpeed();
            stopped = speed == 0;
            if (!stopped || color != null) {
                if (facing.getAxisDirection() == AxisDirection.NEGATIVE ^ upward ^ (alongX && !diagonal || alongZ && diagonal)) {
                    speed = -speed;
                }
                if (sideways && (facing == Direction.SOUTH || facing == Direction.WEST) || vertical && facing == Direction.EAST) {
                    speed = -speed;
                }
                double scroll = speed * AnimationTickHolder.getRenderTime(level) / (31.5 * 16);
                float scrollMult = diagonal ? 0.375f : 0.5f;
                SpriteShiftEntry shift = getSpriteShiftEntry(color, diagonal);
                TextureAtlasSprite target = shift.getTarget();
                float spriteSize = target.getV1() - target.getV0();
                float scrollV = (float) ((scroll - Math.floor(scroll)) * spriteSize * scrollMult);
                belt.shiftUVScrolling(shift, scrollV);
            }
            state.belt = belt.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            if (part != BeltPart.MIDDLE) {
                Direction dir = sideways ? Direction.UP : facing.getClockWise();
                Direction.Axis axis = getRotationAxisOf(state.blockState);
                float pulleyAngle = getAngleForBe(be, state.blockPos, axis);
                Direction pulleyDirection = Direction.get(AxisDirection.POSITIVE, axis);
                int pulleyColor = getTintColor(be);
                state.pulley = CachedBuffers.partialDirectional(
                        casing ? AllPartialModels.ENCASED_SHAFT : AllPartialModels.BELT_PULLEY,
                        state.blockState,
                        dir,
                        PULLEY_POSE
                    ).cardinalLighting(cardinalLighting).light(state.lightCoords)
                    .rotateCentered(pulleyAngle, pulleyDirection).color(pulleyColor).extractRenderState();
            }
            if (!be.isController() || be.beltLength == 0) {
                return;
            }
        }
        BeltInventory inventory = be.getInventory();
        List<TransportedItemStack> transportedItems = inventory.getTransportedItems();
        TransportedItemStack lazyClientItem = inventory.getLazyClientItem();
        int transportedSize = transportedItems.size();
        BeltItemState[] items;
        if (transportedSize == 0) {
            if (lazyClientItem == null) {
                return;
            }
            items = new BeltItemState[1];
        } else {
            items = new BeltItemState[lazyClientItem == null ? transportedSize : transportedSize + 1];
        }
        if (support) {
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            stopped = be.getSpeed() == 0;
        }
        state.items = items;
        state.beltLength = be.beltLength;
        state.beltFacing = be.getBeltFacing();
        state.directionVec = state.beltFacing.getUnitVec3i();
        state.beltStartOffset = Vec3.atLowerCornerOf(state.directionVec).scale(-0.5).add(0.5, 15 / 16.0f, 0.5);
        state.slope = state.blockState.getValue(BeltBlock.SLOPE);
        state.verticality = state.slope == BeltSlope.DOWNWARD ? -1 : state.slope == BeltSlope.UPWARD ? 1 : 0;
        state.slopeAlongX = state.beltFacing.getAxis() == Direction.Axis.X;
        state.partialTicks = tickProgress;
        state.camera = cameraPos;
        state.onContraption = level instanceof WrappedLevel;
        state.onPonder = level instanceof PonderLevel;
        MutableBlockPos mutablePos = new MutableBlockPos();
        for (int i = 0; i < transportedSize; i++) {
            items[i] = BeltItemState.create(
                itemModelManager,
                transportedItems.get(i),
                state,
                stopped,
                level,
                mutablePos
            );
        }
        if (lazyClientItem != null) {
            items[transportedSize] = BeltItemState.create(
                itemModelManager,
                lazyClientItem,
                state,
                stopped,
                level,
                mutablePos
            );
        }
    }

    @Override
    public void submit(
        BeltRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.pulley != null) {
            state.pulley.submit(matrices, queue);
        }
        if (state.belt != null) {
            if (state.rotate != null) {
                matrices.pushPose();
                matrices.rotateAround(state.rotate, 0.5f, 0.5f, 0.5f);
                state.belt.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.belt.submit(matrices, queue);
            }
        }
        if (state.items != null) {
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

    public static SpriteShiftEntry getSpriteShiftEntry(@Nullable DyeColor color, boolean diagonal) {
        if (color != null) {
            return (diagonal ? AllSpriteShifts.DYED_DIAGONAL_BELTS : AllSpriteShifts.DYED_BELTS).get(color);
        }
        return diagonal ? AllSpriteShifts.BELT_DIAGONAL : AllSpriteShifts.BELT;
    }

    public static PartialModel getBeltPartial(BeltSlope slope, BeltPart part, boolean casing) {
        return BELTS[slope.ordinal() << 3 | (casing ? 4 : 0) | part.ordinal()];
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void renderItem(BeltRenderState state, BeltItemState item, PoseStack ms, SubmitNodeCollector queue) {
        float offset = item.offset;
        float verticalMovement;
        if (offset < 0.5) {
            verticalMovement = 0;
        } else {
            verticalMovement = state.verticality * (Math.min(offset, state.beltLength - 0.5f) - 0.5f);
        }
        Vec3 offsetVec = Vec3.atLowerCornerOf(state.directionVec).scale(offset);
        if (verticalMovement != 0) {
            offsetVec = offsetVec.add(0, verticalMovement, 0);
        }
        boolean onSlope = state.slope != BeltSlope.HORIZONTAL && Mth.clamp(
            offset,
            0.5f,
            state.beltLength - 0.5f
        ) == offset;
        boolean tiltForward = (state.slope == BeltSlope.DOWNWARD ^ state.beltFacing.getAxisDirection() == AxisDirection.POSITIVE) == (state.beltFacing.getAxis() == Direction.Axis.Z);
        float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

        BlockPos pos = state.blockPos;
        Vec3 itemPos = state.beltStartOffset.add(pos.getX(), pos.getY(), pos.getZ()).add(offsetVec);

        ms.pushPose();
        TransformStack.of(ms).nudge(item.angle);
        ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

        boolean alongX = state.beltFacing.getClockWise().getAxis() == Direction.Axis.X;
        float sideOffset = item.sideOffset;
        if (!alongX) {
            sideOffset *= -1;
        }
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
        if (state.onPonder || state.camera.distanceTo(itemPos) < 16) {
            count = Mth.log2(item.count) / 2;
        }

        Random r = new Random(item.angle);

        boolean slopeShadowOnly = renderUpright && onSlope;
        float slopeOffset = 0.125f;
        if (slopeShadowOnly) {
            ms.pushPose();
        }
        if (!renderUpright || slopeShadowOnly) {
            ms.mulPose((state.slopeAlongX ? Axis.ZP : Axis.XP).rotationDegrees(slopeAngle));
        }
        if (onSlope) {
            ms.translate(0, slopeOffset, 0);
        }
        ms.pushPose();
        ms.translate(0, -0.12f, 0);
        queue.submitShadow(ms, 0.2f, SHADOW);
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
            ms.mulPose(Axis.YP.rotation(yRot));
            ms.translate(0, 0.09375f, 0.0625f);
        }

        for (int i = 0; i <= count; i++) {
            ms.pushPose();

            ms.mulPose(Axis.YP.rotationDegrees(item.angle));
            if (!blockItem && !renderUpright) {
                ms.translate(0, -0.09375, 0);
                ms.mulPose(Axis.XP.rotationDegrees(90));
            }

            if (blockItem && !item.box) {
                ms.translate(r.nextFloat() * 0.0625f * i, 0, r.nextFloat() * 0.0625f * i);
            }

            if (item.box) {
                ms.translate(0, 0.25f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else {
                ms.scale(0.5f, 0.5f, 0.5f);
            }

            item.state.submit(ms, queue, stackLight, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();

            if (!renderUpright) {
                if (!blockItem) {
                    ms.mulPose(Axis.YP.rotationDegrees(10));
                }
                ms.translate(0, blockItem ? 0.015625f : 0.0625f, 0);
            } else {
                ms.translate(0, 0, -0.0625f);
            }

        }

        ms.popPose();
    }

    public static class BeltRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState belt;
        public @Nullable SuperByteBufferRenderState pulley;
        public @Nullable Quaternionf rotate;
        public int beltLength;
        public BeltItemState @Nullable [] items;
        public @UnknownNullability Direction beltFacing;
        public boolean onContraption;
        public @UnknownNullability Vec3i directionVec;
        public @UnknownNullability Vec3 beltStartOffset;
        public @UnknownNullability BeltSlope slope;
        public int verticality;
        public boolean slopeAlongX;
        public float partialTicks;
        public @UnknownNullability Vec3 camera;
        boolean onPonder;
    }

    public record BeltItemState(ItemStackRenderState state, float offset, float sideOffset, int light, boolean upright,
                                boolean box, int angle, int count) {
        public static BeltItemState create(
            ItemModelResolver itemModelManager,
            TransportedItemStack transported,
            BeltRenderState state,
            boolean stopped,
            @Nullable Level world,
            MutableBlockPos mutablePos
        ) {
            float offset, sideOffset;
            if (stopped) {
                offset = transported.beltPosition;
                sideOffset = transported.sideOffset;
            } else {
                offset = Mth.lerp(state.partialTicks, transported.prevBeltPosition, transported.beltPosition);
                sideOffset = Mth.lerp(state.partialTicks, transported.prevSideOffset, transported.sideOffset);
            }
            int light;
            if (state.onContraption) {
                light = 0;
            } else {
                int segment = (int) Math.floor(offset);
                mutablePos.set(state.blockPos).move(
                    state.directionVec.getX() * segment,
                    state.verticality * segment,
                    state.directionVec.getZ() * segment
                );
                light = world != null ? LightCoordsUtil.getLightCoords(world, mutablePos) : LightCoordsUtil.FULL_BRIGHT;
            }
            ItemStack stack = transported.stack;
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, stack, ItemDisplayContext.FIXED, null, null, 0);
            boolean upright = BeltHelper.isItemUpright(transported.stack);
            boolean box = PackageItem.isPackage(transported.stack);
            return new BeltItemState(
                renderState,
                offset,
                sideOffset,
                light,
                upright,
                box,
                transported.angle,
                stack.getCount()
            );
        }
    }
}
