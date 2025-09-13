package com.zurrtum.create.content.contraptions.actors.trainControls;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.*;

public class ControlsServerHandler {

    public static WorldAttached<Map<UUID, ControlsContext>> receivedInputs = new WorldAttached<>($ -> new HashMap<>());
    static final int TIMEOUT = 30;

    public static void tick(WorldAccess world) {
        Map<UUID, ControlsContext> map = receivedInputs.get(world);
        for (Iterator<Map.Entry<UUID, ControlsContext>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {

            Map.Entry<UUID, ControlsContext> entry = iterator.next();
            ControlsContext ctx = entry.getValue();
            Collection<ManuallyPressedKey> list = ctx.keys;

            if (ctx.entity.isRemoved()) {
                iterator.remove();
                continue;
            }

            for (Iterator<ManuallyPressedKey> entryIterator = list.iterator(); entryIterator.hasNext(); ) {
                ManuallyPressedKey pressedKey = entryIterator.next();
                pressedKey.decrement();
                if (!pressedKey.isAlive())
                    entryIterator.remove(); // key released
            }

            PlayerEntity player = world.getPlayerByUuid(entry.getKey());
            if (player == null) {
                ctx.entity.stopControlling(ctx.controlsLocalPos);
                iterator.remove();
                continue;
            }

            if (!ctx.entity.control(ctx.controlsLocalPos, list.stream().map(ManuallyPressedKey::getSecond).toList(), player)) {
                ctx.entity.stopControlling(ctx.controlsLocalPos);
            }

            if (list.isEmpty())
                iterator.remove();
        }
    }

    public static void receivePressed(
        WorldAccess world,
        AbstractContraptionEntity entity,
        BlockPos controlsPos,
        UUID uniqueID,
        Collection<Integer> collect,
        boolean pressed
    ) {
        Map<UUID, ControlsContext> map = receivedInputs.get(world);

        if (map.containsKey(uniqueID) && map.get(uniqueID).entity != entity)
            map.remove(uniqueID);

        ControlsContext ctx = map.computeIfAbsent(uniqueID, $ -> new ControlsContext(entity, controlsPos));
        Collection<ManuallyPressedKey> list = ctx.keys;

        WithNext:
        for (Integer activated : collect) {
            for (ManuallyPressedKey entry : list) {
                Integer inputType = entry.getSecond();
                if (inputType.equals(activated)) {
                    if (!pressed)
                        entry.setFirst(0);
                    else
                        entry.keepAlive();
                    continue WithNext;
                }
            }

            if (!pressed)
                continue;

            list.add(new ManuallyPressedKey(activated)); // key newly pressed
        }
    }

    public static class ControlsContext {

        Collection<ManuallyPressedKey> keys;
        AbstractContraptionEntity entity;
        BlockPos controlsLocalPos;

        public ControlsContext(AbstractContraptionEntity entity, BlockPos controlsPos) {
            this.entity = entity;
            controlsLocalPos = controlsPos;
            keys = new ArrayList<>();
        }

    }

    static class ManuallyPressedKey extends IntAttached<Integer> {

        public ManuallyPressedKey(Integer second) {
            super(TIMEOUT, second);
        }

        public void keepAlive() {
            setFirst(TIMEOUT);
        }

        public boolean isAlive() {
            return getFirst() > 0;
        }

    }

}
