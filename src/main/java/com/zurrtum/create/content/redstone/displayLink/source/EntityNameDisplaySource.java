package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class EntityNameDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockPos pos = context.getSourcePos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        List<SeatEntity> seats = context.level().getEntitiesOfClass(SeatEntity.class, new AABB(x, y - 0.1f, z, x + 1, y + 1, z + 1));

        if (seats.isEmpty())
            return EMPTY_LINE;

        SeatEntity seatEntity = seats.getFirst();
        List<Entity> passengers = seatEntity.getPassengers();

        if (passengers.isEmpty())
            return EMPTY_LINE;

        return Component.literal(passengers.getFirst().getDisplayName().getString());
    }

    @Override
    protected String getTranslationKey() {
        return "entity_name";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
