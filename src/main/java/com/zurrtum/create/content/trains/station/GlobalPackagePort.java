package com.zurrtum.create.content.trains.station;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.inventory.Inventory;

public class GlobalPackagePort {
    public static final Codec<GlobalPackagePort> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("address").forGetter(i -> i.address),
        ItemStackHandler.CODEC.fieldOf("offlineBuffer").forGetter(i -> i.offlineBuffer),
        Codec.BOOL.fieldOf("primed").forGetter(i -> i.primed),
        Codec.BOOL.fieldOf("restoring").forGetter(i -> i.restoring)
    ).apply(instance, GlobalPackagePort::new));

    public String address;
    public ItemStackHandler offlineBuffer;
    public boolean primed = false;
    private boolean restoring = false;

    public GlobalPackagePort() {
        this("", new ItemStackHandler(18), false, false);
    }

    private GlobalPackagePort(String address, ItemStackHandler offlineBuffer, boolean primed, boolean restoring) {
        this.address = address;
        this.offlineBuffer = offlineBuffer;
        this.primed = primed;
    }

    public void restoreOfflineBuffer(Inventory inventory) {
        if (!primed)
            return;

        restoring = true;

        for (int slot = 0, size = offlineBuffer.size(); slot < size; slot++) {
            inventory.setStack(slot, offlineBuffer.getStack(slot));
        }

        restoring = false;
        primed = false;
    }

    public void saveOfflineBuffer(Inventory inventory) {
        /*
         * Each time restoreOfflineBuffer changes a slot, the inventory
         * calls this method. We must filter out those calls to prevent
         * overwriting later slots which haven't been restored yet and
         * to avoid unnecessary work.
         */
        if (restoring)
            return;

        // TODO: Call save method on individual slots rather than iterating
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            offlineBuffer.setStack(slot, inventory.getStack(slot));
        }

        Create.RAILWAYS.markTracksDirty();
    }
}
