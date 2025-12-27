package com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

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
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            openCount = view.getIntOr("OpenCount", 0);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
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
        Level level = getLevel();
        if (level.isClientSide())
            return;
        if (openCount == 0)
            return;

        int prevOpenCount = openCount;
        openCount = 0;

        for (Player playerentity : level.getEntitiesOfClass(Player.class, new AABB(getPos()).inflate(8)))
            if (menuClass.isInstance(playerentity.containerMenu) && menuClass.cast(playerentity.containerMenu).contentHolder == blockEntity)
                openCount++;

        if (prevOpenCount != openCount) {
            if (openChanged != null && prevOpenCount == 0 && openCount > 0)
                openChanged.accept(true);
            if (openChanged != null && prevOpenCount > 0 && openCount == 0)
                openChanged.accept(false);
            blockEntity.sendData();
        }
    }

    public void startOpen(Player player) {
        if (player.isSpectator())
            return;
        if (getLevel().isClientSide())
            return;
        if (openCount < 0)
            openCount = 0;
        openCount++;
        if (openCount == 1 && openChanged != null)
            openChanged.accept(true);
        blockEntity.sendData();
    }

    public void stopOpen(Player player) {
        if (player.isSpectator())
            return;
        if (getLevel().isClientSide())
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
