package com.zurrtum.create.foundation.blockEntity.behaviour.simple;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.function.Supplier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DeferralBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<DeferralBehaviour> TYPE = new BehaviourType<>();

    private boolean needsUpdate;
    private Supplier<Boolean> callback;

    public DeferralBehaviour(SmartBlockEntity be, Supplier<Boolean> callback) {
        super(be);
        this.callback = callback;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putBoolean("NeedsUpdate", needsUpdate);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        needsUpdate = view.getBooleanOr("NeedsUpdate", false);
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        if (needsUpdate && callback.get())
            needsUpdate = false;
    }

    public void scheduleUpdate() {
        needsUpdate = true;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
