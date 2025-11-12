package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class FactoryPanelSupportBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    private static final Codec<List<FactoryPanelPosition>> LINKED_CODEC = Codec.list(FactoryPanelPosition.CODEC);

    public static final BehaviourType<FactoryPanelSupportBehaviour> TYPE = new BehaviourType<>();

    private List<FactoryPanelPosition> linkedPanels;
    private boolean changed;

    private Supplier<Boolean> outputPower;
    private Supplier<Boolean> isOutput;
    private Runnable onNotify;

    public FactoryPanelSupportBehaviour(SmartBlockEntity be, Supplier<Boolean> isOutput, Supplier<Boolean> outputPower, Runnable onNotify) {
        super(be);
        this.isOutput = isOutput;
        this.outputPower = outputPower;
        this.onNotify = onNotify;
        linkedPanels = new ArrayList<>();
    }

    public boolean shouldPanelBePowered() {
        return isOutput() && outputPower.get();
    }

    public boolean isOutput() {
        return isOutput.get();
    }

    public void notifyLink() {
        onNotify.run();
    }

    @Override
    public void destroy() {
        for (FactoryPanelPosition panelPos : linkedPanels) {
            if (!getLevel().isLoaded(panelPos.pos()))
                continue;
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(getLevel(), panelPos);
            behaviour.targetedByLinks.remove(getPos());
            behaviour.blockEntity.notifyUpdate();
        }
        super.destroy();
    }

    public void notifyPanels() {
        if (getLevel().isClientSide())
            return;
        for (Iterator<FactoryPanelPosition> iterator = linkedPanels.iterator(); iterator.hasNext(); ) {
            FactoryPanelPosition panelPos = iterator.next();
            if (!getLevel().isLoaded(panelPos.pos()))
                continue;
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(getLevel(), panelPos);
            if (behaviour == null) {
                iterator.remove();
                changed = true;
                continue;
            }
            behaviour.checkForRedstoneInput();
        }
    }

    @Nullable
    public Boolean shouldBePoweredTristate() {
        for (Iterator<FactoryPanelPosition> iterator = linkedPanels.iterator(); iterator.hasNext(); ) {
            FactoryPanelPosition panelPos = iterator.next();
            if (!getLevel().isLoaded(panelPos.pos()))
                return null;
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(getLevel(), panelPos);
            if (behaviour == null) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (behaviour.isActive() && behaviour.satisfied && behaviour.count != 0)
                return true;
        }
        return false;
    }

    public List<FactoryPanelPosition> getLinkedPanels() {
        return linkedPanels;
    }

    public void connect(ServerFactoryPanelBehaviour panel) {
        FactoryPanelPosition panelPosition = panel.getPanelPosition();
        if (linkedPanels.contains(panelPosition))
            return;
        linkedPanels.add(panelPosition);
        changed = true;
    }

    public void disconnect(ServerFactoryPanelBehaviour panel) {
        linkedPanels.remove(panel.getPanelPosition());
        changed = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (changed) {
            changed = false;
            if (!isOutput())
                notifyLink();
            blockEntity.setChanged();
        }
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.store("LinkedGauges", LINKED_CODEC, linkedPanels);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        linkedPanels.clear();
        view.read("LinkedGauges", LINKED_CODEC).ifPresent(linkedPanels::addAll);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
