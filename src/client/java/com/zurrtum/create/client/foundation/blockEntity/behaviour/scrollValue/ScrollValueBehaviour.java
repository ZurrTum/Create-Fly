package com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.content.logistics.depot.EjectorSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerBulkScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScrollValueBehaviour<B extends SmartBlockEntity, T extends ServerScrollValueBehaviour> extends BlockEntityBehaviour<B> implements ValueSettingsBehaviour {
    public static final BehaviourType<ScrollValueBehaviour<?, ?>> TYPE = new BehaviourType<>();
    protected T behaviour;
    ValueBoxTransform slotPositioning;

    public Component label;
    protected boolean needsWrench;
    protected Function<Integer, String> formatter = i -> Integer.toString(i);
    protected Supplier<Boolean> isActive = () -> true;

    @SuppressWarnings("unchecked")
    public ScrollValueBehaviour(Component label, B be, ValueBoxTransform slotPositioning) {
        super(be);
        this.label = label;
        this.slotPositioning = slotPositioning;
        behaviour = (T) blockEntity.getBehaviour(ServerScrollValueBehaviour.TYPE);
    }

    @Nullable
    public List<? extends SmartBlockEntity> getBulk() {
        if (behaviour instanceof ServerBulkScrollValueBehaviour bulkBehaviour) {
            return bulkBehaviour.getBulk();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        if (behaviour == null) {
            behaviour = (T) blockEntity.getBehaviour(ServerScrollValueBehaviour.TYPE);
        }
    }

    @Override
    public void tick() {
    }

    public static BlockEntityBehaviour<EjectorBlockEntity> ejector(EjectorBlockEntity blockEntity) {
        return new ScrollValueBehaviour<>(
            CreateLang.translateDirect("weighted_ejector.stack_size"),
            blockEntity,
            new EjectorSlot(blockEntity)
        ).withFormatter(i -> i == 0 ? "*" : String.valueOf(i));
    }

    public ScrollValueBehaviour<B, T> withFormatter(Function<Integer, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    public ScrollValueBehaviour<B, T> onlyActiveWhen(Supplier<Boolean> condition) {
        isActive = condition;
        return this;
    }

    public String formatValue() {
        return formatter.apply(behaviour.getValue());
    }

    @Override
    public boolean isActive() {
        return isActive.get();
    }

    @Override
    public boolean testHit(Vec3 hit) {
        BlockState state = behaviour.blockEntity.getBlockState();
        Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(behaviour.blockEntity.getBlockPos()));
        return slotPositioning.testHit(behaviour.getLevel(), behaviour.getPos(), state, localHit);
    }

    public void setLabel(Component label) {
        this.label = label;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static class StepContext {
        public int currentValue;
        public boolean forward;
        public boolean shift;
        public boolean control;
    }

    @Override
    public ValueBoxTransform getSlotPositioning() {
        return slotPositioning;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, behaviour.getMax(), 10, ImmutableList.of(Component.literal("Value")), new ValueSettingsFormatter());
    }

    @Override
    public ValueSettings getValueSettings() {
        return behaviour.getValueSettings();
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        behaviour.setValueSettings(player, valueSetting, ctrlDown);
    }

    @Override
    public boolean mayInteract(Player player) {
        return behaviour.mayInteract(player);
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        behaviour.onShortInteract(player, hand, side, hitResult);
    }

    @Override
    public int netId() {
        return behaviour.netId();
    }

    @Override
    public boolean onlyVisibleWithWrench() {
        return needsWrench;
    }

}
