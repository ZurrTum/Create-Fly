package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

public class ServerScrollValueBehaviour extends BlockEntityBehaviour<SmartBlockEntity> implements ValueSettingsHandleBehaviour {
    public static final BehaviourType<ServerScrollValueBehaviour> TYPE = new BehaviourType<>();
    protected int value = 0;
    protected Consumer<Integer> callback = i -> {
    };
    protected int min = 0;
    protected int max = 1;

    public ServerScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void setValue(int value) {
        value = Mth.clamp(value, min, max);
        if (value == this.value)
            return;
        this.value = value;
        callback.accept(value);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    public int getValue() {
        return value;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("ScrollValue", value);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        value = view.getIntOr("ScrollValue", 0);
        super.read(view, clientPacket);
    }

    public ServerScrollValueBehaviour withCallback(Consumer<Integer> valueCallback) {
        callback = valueCallback;
        return this;
    }

    public ServerScrollValueBehaviour between(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        if (FakePlayerHandler.has(player))
            blockEntity.getBlockState().useItemOn(player.getItemInHand(hand), getLevel(), player, hand, hitResult);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (valueSetting.equals(getValueSettings()))
            return;
        setValue(valueSetting.value());
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, value);
    }
}
