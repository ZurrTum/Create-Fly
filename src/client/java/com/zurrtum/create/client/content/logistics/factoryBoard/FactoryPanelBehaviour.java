package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class FactoryPanelBehaviour extends FilteringBehaviour<ServerFactoryPanelBehaviour> {
    public static final BehaviourType<FactoryPanelBehaviour> TOP_LEFT = new BehaviourType<>();
    public static final BehaviourType<FactoryPanelBehaviour> TOP_RIGHT = new BehaviourType<>();
    public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_LEFT = new BehaviourType<>();
    public static final BehaviourType<FactoryPanelBehaviour> BOTTOM_RIGHT = new BehaviourType<>();

    public FactoryPanelBehaviour(FactoryPanelBlockEntity be, PanelSlot slot) {
        super(be, new FactoryPanelSlotPositioning(slot));
        behaviour = be.panels.get(slot);
    }

    @SuppressWarnings("unchecked")
    public static Function<FactoryPanelBlockEntity, BlockEntityBehaviour<?>>[] allSlot() {
        PanelSlot[] slots = PanelSlot.values();
        Function<FactoryPanelBlockEntity, BlockEntityBehaviour<?>>[] all = new Function[slots.length];
        for (int i = 0, len = slots.length; i < len; i++) {
            PanelSlot slot = slots[i];
            all[i] = be -> new FactoryPanelBehaviour(be, slot);
        }
        return all;
    }

    public static List<FilteringBehaviour<?>> allBehaviours(FactoryPanelBlockEntity be) {
        int size = PanelSlot.values().length;
        List<FilteringBehaviour<?>> list = new ArrayList<>(size);
        int count = 0;
        for (BlockEntityBehaviour<?> behaviour : be.getAllBehaviours()) {
            if (behaviour instanceof FactoryPanelBehaviour factoryPanelBehaviour) {
                list.add(factoryPanelBehaviour);
                if (++count == size) {
                    break;
                }
            }
        }
        return list;
    }

    @Nullable
    public static FactoryPanelBehaviour at(BlockRenderView world, FactoryPanelPosition pos) {
        if (world instanceof World l && !l.isPosLoaded(pos.pos()))
            return null;
        if (!(world.getBlockEntity(pos.pos()) instanceof FactoryPanelBlockEntity fpbe))
            return null;
        FactoryPanelBehaviour factoryPanel = fpbe.getBehaviour(getTypeForSlot(pos.slot()));
        if (factoryPanel != null && factoryPanel.behaviour.isActive()) {
            return factoryPanel;
        }
        return null;
    }

    @Override
    public BehaviourType<? extends BlockEntityBehaviour<?>> getType() {
        return getTypeForSlot(behaviour.slot);
    }

    public static BehaviourType<FactoryPanelBehaviour> getTypeForSlot(PanelSlot slot) {
        return switch (slot) {
            case BOTTOM_LEFT -> BOTTOM_LEFT;
            case TOP_LEFT -> TOP_LEFT;
            case TOP_RIGHT -> TOP_RIGHT;
            case BOTTOM_RIGHT -> BOTTOM_RIGHT;
        };
    }

    @Override
    public void tick() {
        if (behaviour.active) {
            LogisticallyLinkedClientHandler.tickPanel(this);
        }
    }

    public UUID getNetwork() {
        return behaviour.network;
    }

    public FactoryPanelPosition getPanelPosition() {
        return behaviour.getPanelPosition();
    }

    @Override
    public MutableText formatValue(ValueSettings value) {
        if (value.value() == 0) {
            return CreateLang.translateDirect("gui.factory_panel.inactive");
        } else {
            return Text.literal(Math.max(0, value.value()) + ((value.row() == 0) ? "" : "▤"));
        }
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        int maxAmount = 100;
        return new ValueSettingsBoard(
            CreateLang.translate("factory_panel.target_amount").component(), maxAmount, 10, List.of(
            CreateLang.translate("schedule.condition.threshold.items").component(),
            CreateLang.translate("schedule.condition.threshold.stacks").component()
        ), new ValueSettingsFormatter(this::formatValue)
        );
    }

    @Override
    public MutableText getLabel() {
        String key = "";

        if (!behaviour.targetedBy.isEmpty() && behaviour.count == 0)
            return CreateLang.translate("gui.factory_panel.no_target_amount_set").style(Formatting.RED).component();

        if (behaviour.isMissingAddress())
            return CreateLang.translate("gui.factory_panel.address_missing").style(Formatting.RED).component();

        if (getFilter().isEmpty())
            key = "factory_panel.new_factory_task";
        else if (behaviour.waitingForNetwork)
            key = "factory_panel.some_links_unloaded";
        else if (behaviour.getAmount() == 0 || behaviour.targetedBy.isEmpty())
            return behaviour.getFilter().getName().copyContentOnly();
        else {
            key = behaviour.getFilter().getName().getString();
            if (behaviour.redstonePowered)
                key += " " + CreateLang.translate("factory_panel.redstone_paused").string();
            else if (!behaviour.satisfied)
                key += " " + CreateLang.translate("factory_panel.in_progress").string();
            return CreateLang.text(key).component();
        }

        return CreateLang.translate(key).component();
    }

    @Override
    public MutableText getTip() {
        return CreateLang.translateDirect(getFilter().isEmpty() ? "logistics.filter.click_to_set" : "factory_panel.click_to_configure");
    }

    @Override
    public MutableText getAmountTip() {
        return CreateLang.translateDirect("factory_panel.hold_to_set_amount");
    }

    @Override
    public MutableText getCountLabelForValueBox() {
        if (getFilter().isEmpty())
            return Text.empty();
        if (behaviour.waitingForNetwork) {
            return Text.literal("?");
        }

        int levelInStorage = behaviour.getLevelInStorage();
        boolean inf = levelInStorage >= BigItemStack.INF;
        int inStorage = levelInStorage / (behaviour.upTo ? 1 : getFilter().getMaxCount());
        int promised = behaviour.getPromised();
        String stacks = behaviour.upTo ? "" : "▤";

        if (behaviour.count == 0) {
            return CreateLang.text(inf ? "  ∞" : inStorage + stacks).color(0xF1EFE8).component();
        }

        return CreateLang.text(inf ? "  ∞" : "   " + inStorage + stacks)
            .color(behaviour.satisfied ? 0xD7FFA8 : behaviour.promisedSatisfied ? 0xffcd75 : 0xFFBFA8).add(CreateLang.text(promised == 0 ? "" : "⏶"))
            .add(CreateLang.text("/").style(Formatting.WHITE)).add(CreateLang.text(behaviour.count + stacks + "  ").color(0xF1EFE8)).component();
    }

    @Override
    public float getRenderDistance() {
        return 64;
    }
}
