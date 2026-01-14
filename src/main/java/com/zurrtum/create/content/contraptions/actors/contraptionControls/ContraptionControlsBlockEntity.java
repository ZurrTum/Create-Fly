package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ContraptionControlsBlockEntity extends SmartBlockEntity implements Clearable {

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
        filtering.withPredicate(stack -> stack.isIn(AllItemTags.CONTRAPTION_CONTROLLED));
    }

    public void pressButton() {
        button.setValue(1);
    }

    public void updatePoweredState() {
        if (world.isClient())
            return;
        boolean powered = world.isReceivingRedstonePower(pos);
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
        if (!world.isClient())
            return;
        tickAnimations();
        int value = disabled ? 4 * 45 : 0;
        indicator.setValue(value);
        indicator.updateChaseTarget(value);
    }

    @Override
    public void clear() {
        filtering.setFilter(ItemStack.EMPTY);
    }

    public void tickAnimations() {
        button.tickChaser();
        indicator.tickChaser();
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        disabled = view.getBoolean("Disabled", false);
        powered = view.getBoolean("Powered", false);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Disabled", disabled);
        view.putBoolean("Powered", powered);
    }

    public static void sendStatus(PlayerEntity player, ItemStack filter, boolean enabled) {
        MutableText state = Text.translatable("create.contraption.controls.actor_toggle." + (enabled ? "on" : "off"))
            .withColor(enabled ? 0xA3DF55 : 0xEE9246);

        if (filter.isEmpty()) {
            player.sendMessage(Text.translatable("create.contraption.controls.all_actor_toggle", state), true);
            return;
        }

        player.sendMessage(Text.translatable("create.contraption.controls.specific_actor_toggle", filter.getName().getString(), state), true);
    }
}