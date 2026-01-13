package com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue;

import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

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
        value = MathHelper.clamp(value, min, max);
        if (value == this.value)
            return;
        this.value = value;
        callback.accept(value);
        blockEntity.markDirty();
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
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("ScrollValue", value);
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        value = view.getInt("ScrollValue", 0);
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
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        if (FakePlayerHandler.has(player))
            blockEntity.getCachedState().onUseWithItem(player.getStackInHand(hand), getWorld(), player, hand, hitResult);
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlDown) {
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
