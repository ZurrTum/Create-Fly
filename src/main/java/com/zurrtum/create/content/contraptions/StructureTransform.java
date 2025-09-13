package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.api.contraption.transformable.MovedBlockTransformerRegistries;
import com.zurrtum.create.api.contraption.transformable.MovedBlockTransformerRegistries.BlockEntityTransformer;
import com.zurrtum.create.api.contraption.transformable.MovedBlockTransformerRegistries.BlockTransformer;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.state.property.Properties.*;

public class StructureTransform {
    public static final PacketCodec<PacketByteBuf, StructureTransform> STREAM_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        i -> i.offset,
        PacketCodecs.INTEGER,
        i -> i.angle,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.AXIS),
        i -> i.rotationAxis,
        CatnipStreamCodecBuilders.nullable(BlockRotation.PACKET_CODEC),
        i -> i.rotation,
        CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.MIRROR),
        i -> i.mirror,
        StructureTransform::new
    );

    // Assuming structures cannot be rotated around multiple axes at once
    public Axis rotationAxis;
    public BlockPos offset;
    public int angle;
    public BlockRotation rotation;
    public BlockMirror mirror;

    private StructureTransform(BlockPos offset, int angle, Axis axis, BlockRotation rotation, BlockMirror mirror) {
        this.offset = offset;
        this.angle = angle;
        rotationAxis = axis;
        this.rotation = rotation;
        this.mirror = mirror;
    }

    public StructureTransform(BlockPos offset, Axis axis, BlockRotation rotation, BlockMirror mirror) {
        this(offset, rotation == BlockRotation.NONE ? 0 : (4 - rotation.ordinal()) * 90, axis, rotation, mirror);
    }

    public StructureTransform(BlockPos offset, float xRotation, float yRotation, float zRotation) {
        this.offset = offset;
        if (xRotation != 0) {
            rotationAxis = Axis.X;
            angle = Math.round(xRotation / 90) * 90;
        }
        if (yRotation != 0) {
            rotationAxis = Axis.Y;
            angle = Math.round(yRotation / 90) * 90;
        }
        if (zRotation != 0) {
            rotationAxis = Axis.Z;
            angle = Math.round(zRotation / 90) * 90;
        }

        angle %= 360;
        if (angle < -90)
            angle += 360;

        this.rotation = BlockRotation.NONE;
        if (angle == -90 || angle == 270)
            this.rotation = BlockRotation.CLOCKWISE_90;
        if (angle == 90)
            this.rotation = BlockRotation.COUNTERCLOCKWISE_90;
        if (angle == 180)
            this.rotation = BlockRotation.CLOCKWISE_180;

        mirror = BlockMirror.NONE;
    }

    public Vec3d applyWithoutOffsetUncentered(Vec3d localVec) {
        Vec3d vec = localVec;
        if (mirror != null)
            vec = VecHelper.mirror(vec, mirror);
        if (rotationAxis != null)
            vec = VecHelper.rotate(vec, angle, rotationAxis);
        return vec;
    }

    public Vec3d applyWithoutOffset(Vec3d localVec) {
        Vec3d vec = localVec;
        if (mirror != null)
            vec = VecHelper.mirrorCentered(vec, mirror);
        if (rotationAxis != null)
            vec = VecHelper.rotateCentered(vec, angle, rotationAxis);
        return vec;
    }

    public Vec3d unapplyWithoutOffset(Vec3d globalVec) {
        Vec3d vec = globalVec;
        if (rotationAxis != null)
            vec = VecHelper.rotateCentered(vec, -angle, rotationAxis);
        if (mirror != null)
            vec = VecHelper.mirrorCentered(vec, mirror);

        return vec;
    }

    public Vec3d apply(Vec3d localVec) {
        return applyWithoutOffset(localVec).add(Vec3d.of(offset));
    }

    public BlockPos applyWithoutOffset(BlockPos localPos) {
        return BlockPos.ofFloored(applyWithoutOffset(VecHelper.getCenterOf(localPos)));
    }

    public BlockPos apply(BlockPos localPos) {
        return applyWithoutOffset(localPos).add(offset);
    }

    public BlockPos unapply(BlockPos globalPos) {
        return unapplyWithoutOffset(globalPos.subtract(offset));
    }

    public BlockPos unapplyWithoutOffset(BlockPos globalPos) {
        return BlockPos.ofFloored(unapplyWithoutOffset(VecHelper.getCenterOf(globalPos)));
    }

    public void apply(BlockEntity be) {
        BlockEntityTransformer transformer = MovedBlockTransformerRegistries.BLOCK_ENTITY_TRANSFORMERS.get(be.getType());
        if (transformer != null) {
            transformer.transform(be, this);
        } else if (be instanceof TransformableBlockEntity itbe) {
            itbe.transform(be, this);
        }
    }

    /**
     * Vanilla does not support block state rotation around axes other than Y. Add
     * specific cases here for vanilla block states so that they can react to rotations
     * around horizontal axes. For Create blocks, implement ITransformableBlock.
     */
    public BlockState apply(BlockState state) {
        Block block = state.getBlock();
        BlockTransformer transformer = MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.get(block);
        if (transformer != null) {
            return transformer.transform(state, this);
        } else if (block instanceof TransformableBlock transformable) {
            return transformable.transform(state, this);
        }

        if (mirror != null)
            state = state.mirror(mirror);

        if (rotationAxis == Axis.Y) {
            if (block instanceof BellBlock) {
                if (state.get(Properties.ATTACHMENT) == Attachment.DOUBLE_WALL)
                    state = state.with(Properties.ATTACHMENT, Attachment.SINGLE_WALL);
                return state.with(BellBlock.FACING, rotation.rotate(state.get(BellBlock.FACING)));
            }

            return state.rotate(rotation);
        }

        if (block instanceof WallMountedBlock) {
            EnumProperty<Direction> facingProperty = WallMountedBlock.FACING;
            EnumProperty<BlockFace> faceProperty = WallMountedBlock.FACE;
            Direction stateFacing = state.get(facingProperty);
            BlockFace stateFace = state.get(faceProperty);
            boolean z = rotationAxis == Axis.Z;
            Direction forcedAxis = z ? Direction.WEST : Direction.SOUTH;

            if (stateFacing.getAxis() == rotationAxis && stateFace == BlockFace.WALL)
                return state;

            for (int i = 0; i < rotation.ordinal(); i++) {
                stateFace = state.get(faceProperty);
                stateFacing = state.get(facingProperty);

                boolean b = state.get(faceProperty) == BlockFace.CEILING;
                state = state.with(facingProperty, b ? forcedAxis : forcedAxis.getOpposite());

                if (stateFace != BlockFace.WALL) {
                    state = state.with(faceProperty, BlockFace.WALL);
                    continue;
                }

                if (stateFacing.getDirection() == (z ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE)) {
                    state = state.with(faceProperty, BlockFace.FLOOR);
                    continue;
                }
                state = state.with(faceProperty, BlockFace.CEILING);
            }

            return state;
        }

        boolean halfTurn = rotation == BlockRotation.CLOCKWISE_180;
        if (block instanceof StairsBlock) {
            state = transformStairs(state, halfTurn);
            return state;
        }

        if (state.contains(FACING)) {
            state = state.with(FACING, rotateFacing(state.get(FACING)));
        } else if (state.contains(AXIS)) {
            state = state.with(AXIS, rotateAxis(state.get(AXIS)));
        } else if (halfTurn) {
            if (state.contains(HORIZONTAL_FACING)) {
                Direction stateFacing = state.get(HORIZONTAL_FACING);
                if (stateFacing.getAxis() == rotationAxis)
                    return state;
            }

            state = state.rotate(rotation);

            if (state.contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) != SlabType.DOUBLE)
                state = state.with(SlabBlock.TYPE, state.get(SlabBlock.TYPE) == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM);
        }

        return state;
    }

    protected BlockState transformStairs(BlockState state, boolean halfTurn) {
        if (state.get(StairsBlock.FACING).getAxis() != rotationAxis) {
            for (int i = 0; i < rotation.ordinal(); i++) {
                Direction direction = state.get(StairsBlock.FACING);
                BlockHalf half = state.get(StairsBlock.HALF);
                if (direction.getDirection() == AxisDirection.POSITIVE ^ half == BlockHalf.BOTTOM ^ direction.getAxis() == Axis.Z)
                    state = state.cycle(StairsBlock.HALF);
                else
                    state = state.with(StairsBlock.FACING, direction.getOpposite());
            }
        } else {
            if (halfTurn) {
                state = state.cycle(StairsBlock.HALF);
            }
        }
        return state;
    }

    public Direction mirrorFacing(Direction facing) {
        if (mirror != null)
            return mirror.apply(facing);
        return facing;
    }

    public Axis rotateAxis(Axis axis) {
        Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
        return rotateFacing(facing).getAxis();
    }

    public Direction rotateFacing(Direction facing) {
        for (int i = 0; i < rotation.ordinal(); i++)
            facing = facing.rotateClockwise(rotationAxis);
        return facing;
    }
}
