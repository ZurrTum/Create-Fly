package com.zurrtum.create.content.redstone.nixieTube;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class DoubleFaceAttachedBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<DoubleFaceAttachedBlock> CODEC = simpleCodec(DoubleFaceAttachedBlock::new);

    public enum DoubleAttachFace implements StringRepresentable {
        FLOOR,
        WALL,
        WALL_REVERSED,
        CEILING;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public int xRot() {
            return this == FLOOR ? 0 : this == CEILING ? 180 : 90;
        }
    }

    public static final EnumProperty<NixieTubeBlock.DoubleAttachFace> FACE = EnumProperty.create(
        "double_face",
        NixieTubeBlock.DoubleAttachFace.class
    );

    public DoubleFaceAttachedBlock(Properties p_53182_) {
        super(p_53182_);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for (Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = defaultBlockState().setValue(
                    FACE,
                    direction == Direction.UP ? NixieTubeBlock.DoubleAttachFace.CEILING : NixieTubeBlock.DoubleAttachFace.FLOOR
                ).setValue(FACING, pContext.getHorizontalDirection());
            } else {
                Vec3 n = Vec3.atLowerCornerOf(direction.getClockWise().getUnitVec3i());
                NixieTubeBlock.DoubleAttachFace face = NixieTubeBlock.DoubleAttachFace.WALL;
                if (pContext.getPlayer() != null) {
                    Vec3 lookAngle = pContext.getPlayer().getLookAngle();
                    if (lookAngle.dot(n) < 0)
                        face = NixieTubeBlock.DoubleAttachFace.WALL_REVERSED;
                }
                blockstate = defaultBlockState().setValue(FACE, face).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
                return blockstate;
            }
        }

        return null;
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        return switch (pState.getValue(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> pState.getValue(FACING);
        };
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
