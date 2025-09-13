package com.zurrtum.create.content.redstone.nixieTube;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class DoubleFaceAttachedBlock extends HorizontalFacingBlock {

    public static final MapCodec<DoubleFaceAttachedBlock> CODEC = createCodec(DoubleFaceAttachedBlock::new);

    public enum DoubleAttachFace implements StringIdentifiable {
        FLOOR,
        WALL,
        WALL_REVERSED,
        CEILING;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public int xRot() {
            return this == FLOOR ? 0 : this == CEILING ? 180 : 90;
        }
    }

    public static final EnumProperty<NixieTubeBlock.DoubleAttachFace> FACE = EnumProperty.of("double_face", NixieTubeBlock.DoubleAttachFace.class);

    public DoubleFaceAttachedBlock(Settings p_53182_) {
        super(p_53182_);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        for (Direction direction : pContext.getPlacementDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = getDefaultState().with(
                    FACE,
                    direction == Direction.UP ? NixieTubeBlock.DoubleAttachFace.CEILING : NixieTubeBlock.DoubleAttachFace.FLOOR
                ).with(FACING, pContext.getHorizontalPlayerFacing());
            } else {
                Vec3d n = Vec3d.of(direction.rotateYClockwise().getVector());
                NixieTubeBlock.DoubleAttachFace face = NixieTubeBlock.DoubleAttachFace.WALL;
                if (pContext.getPlayer() != null) {
                    Vec3d lookAngle = pContext.getPlayer().getRotationVector();
                    if (lookAngle.dotProduct(n) < 0)
                        face = NixieTubeBlock.DoubleAttachFace.WALL_REVERSED;
                }
                blockstate = getDefaultState().with(FACE, face).with(FACING, direction.getOpposite());
            }

            if (blockstate.canPlaceAt(pContext.getWorld(), pContext.getBlockPos())) {
                return blockstate;
            }
        }

        return null;
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        switch ((DoubleAttachFace) pState.get(FACE)) {
            case CEILING:
                return Direction.DOWN;
            case FLOOR:
                return Direction.UP;
            default:
                return pState.get(FACING);
        }
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
