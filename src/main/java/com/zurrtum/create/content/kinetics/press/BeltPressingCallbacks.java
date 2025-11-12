package com.zurrtum.create.content.kinetics.press;

import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour.Mode;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

public class BeltPressingCallbacks {

    static ProcessingResult onItemReceived(
        TransportedItemStack transported,
        TransportedItemStackHandlerBehaviour handler,
        PressingBehaviour behaviour
    ) {
        if (behaviour.specifics.getKineticSpeed() == 0)
            return PASS;
        if (behaviour.running)
            return HOLD;
        if (!behaviour.specifics.tryProcessOnBelt(transported, null, true))
            return PASS;

        behaviour.start(Mode.BELT);
        return HOLD;
    }

    static ProcessingResult whenItemHeld(
        TransportedItemStack transported,
        TransportedItemStackHandlerBehaviour handler,
        PressingBehaviour behaviour
    ) {

        if (behaviour.specifics.getKineticSpeed() == 0)
            return PASS;
        if (!behaviour.running)
            return PASS;
        if (behaviour.runningTicks != PressingBehaviour.CYCLE / 2)
            return HOLD;

        behaviour.particleItems.clear();
        ArrayList<ItemStack> results = new ArrayList<>();
        if (!behaviour.specifics.tryProcessOnBelt(transported, results, false))
            return PASS;

        boolean bulk = behaviour.specifics.canProcessInBulk() || transported.stack.getCount() == 1;

        transported.clearFanProcessingData();

        List<TransportedItemStack> collect = results.stream().map(stack -> {
            TransportedItemStack copy = transported.copy();
            boolean centered = BeltHelper.isItemUpright(stack);
            copy.stack = stack;
            copy.locked = true;
            copy.angle = centered ? 180 : behaviour.getLevel().random.nextInt(360);
            return copy;
        }).collect(Collectors.toList());

        if (bulk) {
            if (collect.isEmpty())
                handler.handleProcessingOnItem(transported, TransportedResult.removeItem());
            else
                handler.handleProcessingOnItem(transported, TransportedResult.convertTo(collect));

        } else {
            TransportedItemStack left = transported.copy();
            left.stack.shrink(1);

            if (collect.isEmpty())
                handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
            else
                handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));
        }

        behaviour.blockEntity.sendData();
        return HOLD;
    }

}