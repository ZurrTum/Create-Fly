package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import static com.zurrtum.create.content.trains.entity.CarriageBogey.UPSIDE_DOWN_KEY;

public abstract class AbstractBogeyBlockEntity extends CachedRenderBBBlockEntity {
    public static final String BOGEY_STYLE_KEY = "BogeyStyle";
    public static final String BOGEY_DATA_KEY = "BogeyData";

    private CompoundTag bogeyData;

    public AbstractBogeyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract BogeyStyle getDefaultStyle();

    public CompoundTag getBogeyData() {
        if (bogeyData == null || !bogeyData.contains(BOGEY_STYLE_KEY))
            bogeyData = createBogeyData();
        return bogeyData;
    }

    public void setBogeyData(@NotNull CompoundTag newData) {
        if (!newData.contains(BOGEY_STYLE_KEY)) {
            newData.store(BOGEY_STYLE_KEY, ResourceLocation.CODEC, getDefaultStyle().id);
        }
        bogeyData = newData;
    }

    public void setBogeyStyle(@NotNull BogeyStyle style) {
        getBogeyData().store(BOGEY_STYLE_KEY, ResourceLocation.CODEC, style.id);
        markUpdated();
    }

    @NotNull
    public BogeyStyle getStyle() {
        CompoundTag data = getBogeyData();
        ResourceLocation currentStyle = data.read(BOGEY_STYLE_KEY, ResourceLocation.CODEC).orElseThrow();
        BogeyStyle style = AllBogeyStyles.BOGEY_STYLES.get(currentStyle);
        if (style == null) {
            setBogeyStyle(getDefaultStyle());
            return getStyle();
        }
        return style;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        CompoundTag data = this.getBogeyData();
        if (data != null)
            view.store(BOGEY_DATA_KEY, CompoundTag.CODEC, data); // Now contains style
        super.saveAdditional(view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        bogeyData = view.read(BOGEY_DATA_KEY, CompoundTag.CODEC).orElseGet(this::createBogeyData);
        super.loadAdditional(view);
    }

    private CompoundTag createBogeyData() {
        CompoundTag nbt = new CompoundTag();
        nbt.store(BOGEY_STYLE_KEY, ResourceLocation.CODEC, getDefaultStyle().id);
        boolean upsideDown = false;
        if (getBlockState().getBlock() instanceof AbstractBogeyBlock<?> bogeyBlock)
            upsideDown = bogeyBlock.isUpsideDown(getBlockState());
        nbt.putBoolean(UPSIDE_DOWN_KEY, upsideDown);
        return nbt;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    // Ponder
    LerpedFloat virtualAnimation = LerpedFloat.angular();

    public float getVirtualAngle(float partialTicks) {
        return virtualAnimation.getValue(partialTicks);
    }

    public void animate(float distanceMoved) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> type))
            return;
        double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
        double newWheelAngle = (virtualAnimation.getValue() - angleDiff) % 360;
        virtualAnimation.setValue(newWheelAngle);
    }

    private void markUpdated() {
        setChanged();
        Level level = getLevel();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }
}
