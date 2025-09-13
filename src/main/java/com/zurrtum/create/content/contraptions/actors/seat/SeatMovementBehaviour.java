package com.zurrtum.create.content.contraptions.actors.seat;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SeatMovementBehaviour extends MovementBehaviour {
    @Override
    public void startMoving(MovementContext context) {
        super.startMoving(context);
        int indexOf = context.contraption.getSeats().indexOf(context.localPos);
        context.data.putInt("SeatIndex", indexOf);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        super.visitNewPosition(context, pos);

        AbstractContraptionEntity contraptionEntity = context.contraption.entity;
        if (contraptionEntity == null)
            return;
        int index = context.data.getInt("SeatIndex", 0);
        if (index == -1)
            return;

        Map<UUID, Integer> seatMapping = context.contraption.getSeatMapping();
        BlockState blockState = context.world.getBlockState(pos);
        boolean slab = blockState.getBlock() instanceof SlabBlock && blockState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
        boolean solid = blockState.isOpaque() || slab;

        // Occupied
        if (!seatMapping.containsValue(index))
            return;
        if (!solid)
            return;
        Entity toDismount = null;
        for (Map.Entry<UUID, Integer> entry : seatMapping.entrySet()) {
            if (entry.getValue() != index)
                continue;
            for (Entity entity : contraptionEntity.getPassengerList()) {
                if (!entry.getKey().equals(entity.getUuid()))
                    continue;
                toDismount = entity;
            }
        }
        if (toDismount == null)
            return;
        toDismount.stopRiding();
        Vec3d position = VecHelper.getCenterOf(pos).add(0, slab ? .5f : 1f, 0);
        toDismount.requestTeleport(position.x, position.y, position.z);
        if (toDismount instanceof LivingEntity entity) {
            AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(entity, Optional.empty());
        }
    }

}
