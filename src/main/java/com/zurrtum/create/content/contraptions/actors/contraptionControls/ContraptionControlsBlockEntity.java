package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ContraptionControlsBlockEntity extends SmartBlockEntity {

    public ServerFilteringBehaviour filtering;
    public boolean disabled;
    public boolean powered;

    public LerpedFloat indicator;
    public LerpedFloat button;

    public ContraptionControlsBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CONTRAPTION_CONTROLS, pos, state);
        indicator = LerpedFloat.angular().startWithValue(0);
        button = LerpedFloat.linear().startWithValue(0).chase(0, 0.125f, Chaser.EXP);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this));
        filtering.withPredicate(stack -> stack.is(AllItemTags.CONTRAPTION_CONTROLLED));
    }

    public void pressButton() {
        button.setValue(1);
    }

    public void updatePoweredState() {
        if (level.isClientSide())
            return;
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (this.powered == powered)
            return;
        this.powered = powered;
        this.disabled = powered;
        notifyUpdate();
    }

    @Override
    public void initialize() {
        super.initialize();
        updatePoweredState();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide())
            return;
        tickAnimations();
        int value = disabled ? 4 * 45 : 0;
        indicator.setValue(value);
        indicator.updateChaseTarget(value);
    }

    public void tickAnimations() {
        button.tickChaser();
        indicator.tickChaser();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        disabled = view.getBooleanOr("Disabled", false);
        powered = view.getBooleanOr("Powered", false);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Disabled", disabled);
        view.putBoolean("Powered", powered);
    }

    public static void sendStatus(Player player, ItemStack filter, boolean enabled) {
        MutableComponent state = Component.translatable("create.contraption.controls.actor_toggle." + (enabled ? "on" : "off"))
            .withColor(enabled ? 0xA3DF55 : 0xEE9246);

        if (filter.isEmpty()) {
            player.displayClientMessage(Component.translatable("create.contraption.controls.all_actor_toggle", state), true);
            return;
        }

        player.displayClientMessage(
            Component.translatable(
                "create.contraption.controls.specific_actor_toggle",
                filter.getHoverName().getString(),
                state
            ), true
        );
    }
}