package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class EntityNameDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockPos pos = context.getSourcePos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        List<SeatEntity> seats = context.level().getNonSpectatingEntities(SeatEntity.class, new Box(x, y - 0.1f, z, x + 1, y + 1, z + 1));

        if (seats.isEmpty())
            return EMPTY_LINE;

        SeatEntity seatEntity = seats.getFirst();
        List<Entity> passengers = seatEntity.getPassengerList();

        if (passengers.isEmpty())
            return EMPTY_LINE;

        return Text.literal(passengers.getFirst().getDisplayName().getString());
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
