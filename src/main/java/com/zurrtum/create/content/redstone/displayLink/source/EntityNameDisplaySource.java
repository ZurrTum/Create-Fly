package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EntityNameDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        List<SeatEntity> seats = context.level().getEntitiesOfClass(SeatEntity.class, new AABB(context.getSourcePos()));

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
