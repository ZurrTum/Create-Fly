package com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ControlsSlot;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerValueBox;
import com.zurrtum.create.client.content.fluids.pipes.SmartPipeFilterSlot;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerFilterSlot;
import com.zurrtum.create.client.content.kinetics.saw.SawFilterSlot;
import com.zurrtum.create.client.content.logistics.chute.SmartChuteFilterSlotPositioning;
import com.zurrtum.create.client.content.logistics.crate.CrateSlot;
import com.zurrtum.create.client.content.logistics.funnel.FunnelFilterSlotPositioning;
import com.zurrtum.create.client.content.logistics.itemHatch.HatchFilterSlot;
import com.zurrtum.create.client.content.processing.basin.BasinValueBox;
import com.zurrtum.create.client.content.redstone.FilteredDetectorFilterSlot;
import com.zurrtum.create.client.content.trains.observer.ObserverFilterSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.logistics.itemHatch.ItemHatchBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FilteringBehaviour<T extends ServerFilteringBehaviour> extends BlockEntityBehaviour<SmartBlockEntity> implements ValueSettingsBehaviour {
    public static final BehaviourType<FilteringBehaviour<?>> TYPE = new BehaviourType<>();
    protected T behaviour;
    protected ValueBoxTransform slotPositioning;

    public MutableText customLabel;

    @SuppressWarnings("unchecked")
    public FilteringBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be);
        behaviour = (T) blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        slotPositioning = slot;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        if (behaviour == null) {
            behaviour = (T) blockEntity.getBehaviour(ServerFilteringBehaviour.TYPE);
        }
    }

    @Override
    public void tick() {
    }

    public static BlockEntityBehaviour<SmartBlockEntity> saw(SawBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new SawFilterSlot());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> basin(BasinBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new BasinValueBox());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> funnel(FunnelBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new FunnelFilterSlotPositioning());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> chute(SmartChuteBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new SmartChuteFilterSlotPositioning());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> deployer(DeployerBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new DeployerFilterSlot());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> pipe(SmartFluidPipeBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new SmartPipeFilterSlot());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> observer(SmartObserverBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new FilteredDetectorFilterSlot(false));
    }

    public static BlockEntityBehaviour<SmartBlockEntity> threshold(ThresholdSwitchBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new FilteredDetectorFilterSlot(true));
    }

    public static BlockEntityBehaviour<SmartBlockEntity> controls(ContraptionControlsBlockEntity blockEntity) {
        FilteringBehaviour<ServerFilteringBehaviour> filter = new FilteringBehaviour<>(blockEntity, new ControlsSlot());
        filter.setLabel(CreateLang.translateDirect("contraptions.contoller.target"));
        return filter;
    }

    public static BlockEntityBehaviour<SmartBlockEntity> crate(CreativeCrateBlockEntity blockEntity) {
        FilteringBehaviour<ServerFilteringBehaviour> filter = new FilteringBehaviour<>(blockEntity, new CrateSlot());
        filter.setLabel(CreateLang.translateDirect("logistics.creative_crate.supply"));
        return filter;
    }

    public static BlockEntityBehaviour<SmartBlockEntity> observer(TrackObserverBlockEntity blockEntity) {
        FilteringBehaviour<ServerFilteringBehaviour> filter = new FilteringBehaviour<>(blockEntity, new ObserverFilterSlot());
        filter.setLabel(CreateLang.translateDirect("logistics.train_observer.cargo_filter"));
        return filter;
    }

    public static BlockEntityBehaviour<SmartBlockEntity> roller(RollerBlockEntity blockEntity) {
        FilteringBehaviour<ServerFilteringBehaviour> filter = new FilteringBehaviour<>(blockEntity, new RollerValueBox(3));
        filter.setLabel(CreateLang.translateDirect("contraptions.mechanical_roller.pave_material"));
        return filter;
    }

    public static BlockEntityBehaviour<SmartBlockEntity> hatch(ItemHatchBlockEntity blockEntity) {
        return new FilteringBehaviour<>(blockEntity, new HatchFilterSlot());
    }

    public ItemStack getFilter(Direction side) {
        return behaviour.getFilter(side);
    }

    public ItemStack getFilter() {
        return behaviour.getFilter();
    }

    @Override
    public ValueSettings getValueSettings() {
        return behaviour.getValueSettings();
    }

    public boolean isCountVisible() {
        return behaviour.isCountVisible();
    }

    @Override
    public boolean acceptsValueSettings() {
        return behaviour.isCountVisible();
    }

    @Override
    public boolean testHit(Vec3d hit) {
        BlockState state = blockEntity.getCachedState();
        Vec3d localHit = hit.subtract(Vec3d.of(blockEntity.getPos()));
        return slotPositioning.testHit(getWorld(), getPos(), state, localHit);
    }

    public void setLabel(MutableText label) {
        this.customLabel = label;
    }

    @Override
    public BehaviourType<? extends BlockEntityBehaviour<?>> getType() {
        return TYPE;
    }

    @Override
    public boolean isActive() {
        return behaviour.isActive();
    }

    public float getRenderDistance() {
        return AllConfigs.client().filterItemRenderDistance.getF();
    }

    public MutableText getLabel() {
        if (customLabel != null)
            return customLabel;
        return CreateLang.translateDirect(behaviour.isRecipeFilter() ? "logistics.recipe_filter" : behaviour.fluidFilter ? "logistics.fluid_filter" : "logistics.filter");
    }

    public MutableText getTip() {
        return CreateLang.translateDirect(behaviour.getFilter().isEmpty() ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace");
    }

    public MutableText getAmountTip() {
        return CreateLang.translateDirect("logistics.filter.hold_to_set_amount");
    }

    public MutableText getCountLabelForValueBox() {
        return Text.literal(behaviour.isCountVisible() ? behaviour.upTo && behaviour.getMaxStackSize() == behaviour.count ? "*" : String.valueOf(
            behaviour.count) : "");
    }

    @Override
    public ValueBoxTransform getSlotPositioning() {
        return slotPositioning;
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        int maxAmount = behaviour.getMaxStackSize(hitResult.getSide());
        return new ValueSettingsBoard(
            CreateLang.translateDirect("logistics.filter.extracted_amount"),
            maxAmount,
            16,
            CreateLang.translatedOptions("logistics.filter", "up_to", "exactly"),
            new ValueSettingsFormatter(this::formatValue)
        );
    }

    public MutableText formatValue(ValueSettings value) {
        if (value.row() == 0 && value.value() == behaviour.getMaxStackSize())
            return CreateLang.translateDirect("logistics.filter.any_amount_short");
        return Text.literal(((value.row() == 0) ? "≤" : "=") + Math.max(1, value.value()));
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings valueSettings, boolean ctrlDown) {
        behaviour.setValueSettings(player, valueSettings, ctrlDown);
    }

    @Override
    public boolean mayInteract(PlayerEntity player) {
        return behaviour.mayInteract(player);
    }

    @Override
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        behaviour.onShortInteract(player, hand, side, hitResult);
    }

    @Override
    public int netId() {
        return behaviour.netId();
    }
}
