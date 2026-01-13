package com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class AnimatedContainerBehaviour<M extends MenuBase<? extends SmartBlockEntity>> extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<AnimatedContainerBehaviour<?>> TYPE = new BehaviourType<>();

    public int openCount;

    private final Class<M> menuClass;
    private Consumer<Boolean> openChanged;

    public AnimatedContainerBehaviour(SmartBlockEntity be, Class<M> menuClass) {
        super(be);
        this.menuClass = menuClass;
        openCount = 0;
    }

    public void onOpenChanged(Consumer<Boolean> openChanged) {
        this.openChanged = openChanged;
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            openCount = view.getInt("OpenCount", 0);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket)
            view.putInt("OpenCount", openCount);
    }

    @Override
    public void lazyTick() {
        updateOpenCount();
        super.lazyTick();
    }

    void updateOpenCount() {
        World level = getWorld();
        if (level.isClient())
            return;
        if (openCount == 0)
            return;

        int prevOpenCount = openCount;
        openCount = 0;

        for (PlayerEntity playerentity : level.getNonSpectatingEntities(PlayerEntity.class, new Box(getPos()).expand(8)))
            if (menuClass.isInstance(playerentity.currentScreenHandler) && menuClass.cast(playerentity.currentScreenHandler).contentHolder == blockEntity)
                openCount++;

        if (prevOpenCount != openCount) {
            if (openChanged != null && prevOpenCount == 0 && openCount > 0)
                openChanged.accept(true);
            if (openChanged != null && prevOpenCount > 0 && openCount == 0)
                openChanged.accept(false);
            blockEntity.sendData();
        }
    }

    public void startOpen(PlayerEntity player) {
        if (player.isSpectator())
            return;
        if (getWorld().isClient())
            return;
        if (openCount < 0)
            openCount = 0;
        openCount++;
        if (openCount == 1 && openChanged != null)
            openChanged.accept(true);
        blockEntity.sendData();
    }

    public void stopOpen(PlayerEntity player) {
        if (player.isSpectator())
            return;
        if (getWorld().isClient())
            return;
        openCount--;
        if (openCount == 0 && openChanged != null)
            openChanged.accept(false);
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
