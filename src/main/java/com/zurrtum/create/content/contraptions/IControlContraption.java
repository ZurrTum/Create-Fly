package com.zurrtum.create.content.contraptions;

import net.minecraft.util.math.BlockPos;

public interface IControlContraption {

    boolean isAttachedTo(AbstractContraptionEntity contraption);

    void attach(ControlledContraptionEntity contraption);

    void onStall();

    boolean isValid();

    BlockPos getBlockPosition();

    enum MovementMode {
        MOVE_PLACE,
        MOVE_PLACE_RETURNED,
        MOVE_NEVER_PLACE;
    }

    enum RotationMode {
        ROTATE_PLACE,
        ROTATE_PLACE_RETURNED,
        ROTATE_NEVER_PLACE;
    }

}