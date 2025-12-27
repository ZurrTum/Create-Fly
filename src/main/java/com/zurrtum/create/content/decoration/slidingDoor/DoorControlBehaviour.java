package com.zurrtum.create.content.decoration.slidingDoor;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DoorControlBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<DoorControlBehaviour> TYPE = new BehaviourType<>();

    public DoorControl mode;

    public DoorControlBehaviour(SmartBlockEntity be) {
        super(be);
        mode = DoorControl.ALL;
    }

    public void set(DoorControl mode) {
        if (this.mode == mode)
            return;
        this.mode = mode;
        blockEntity.notifyUpdate();
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.store("DoorControl", DoorControl.CODEC, mode);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        mode = view.read("DoorControl", DoorControl.CODEC).orElse(DoorControl.ALL);
        super.read(view, clientPacket);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
