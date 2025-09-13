package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.zurrtum.create.content.trains.entity.CarriageBogey.UPSIDE_DOWN_KEY;

public abstract class AbstractBogeyBlockEntity extends CachedRenderBBBlockEntity {
    public static final String BOGEY_STYLE_KEY = "BogeyStyle";
    public static final String BOGEY_DATA_KEY = "BogeyData";

    private NbtCompound bogeyData;

    public AbstractBogeyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract BogeyStyle getDefaultStyle();

    public NbtCompound getBogeyData() {
        if (bogeyData == null || !bogeyData.contains(BOGEY_STYLE_KEY))
            bogeyData = createBogeyData();
        return bogeyData;
    }

    public void setBogeyData(@NotNull NbtCompound newData) {
        if (!newData.contains(BOGEY_STYLE_KEY)) {
            newData.put(BOGEY_STYLE_KEY, Identifier.CODEC, getDefaultStyle().id);
        }
        bogeyData = newData;
    }

    public void setBogeyStyle(@NotNull BogeyStyle style) {
        getBogeyData().put(BOGEY_STYLE_KEY, Identifier.CODEC, style.id);
        markUpdated();
    }

    @NotNull
    public BogeyStyle getStyle() {
        NbtCompound data = getBogeyData();
        Identifier currentStyle = data.get(BOGEY_STYLE_KEY, Identifier.CODEC).orElseThrow();
        BogeyStyle style = AllBogeyStyles.BOGEY_STYLES.get(currentStyle);
        if (style == null) {
            setBogeyStyle(getDefaultStyle());
            return getStyle();
        }
        return style;
    }

    @Override
    protected void writeData(WriteView view) {
        NbtCompound data = this.getBogeyData();
        if (data != null)
            view.put(BOGEY_DATA_KEY, NbtCompound.CODEC, data); // Now contains style
        super.writeData(view);
    }

    @Override
    protected void readData(ReadView view) {
        bogeyData = view.read(BOGEY_DATA_KEY, NbtCompound.CODEC).orElseGet(this::createBogeyData);
        super.readData(view);
    }

    private NbtCompound createBogeyData() {
        NbtCompound nbt = new NbtCompound();
        nbt.put(BOGEY_STYLE_KEY, Identifier.CODEC, getDefaultStyle().id);
        boolean upsideDown = false;
        if (getCachedState().getBlock() instanceof AbstractBogeyBlock<?> bogeyBlock)
            upsideDown = bogeyBlock.isUpsideDown(getCachedState());
        nbt.putBoolean(UPSIDE_DOWN_KEY, upsideDown);
        return nbt;
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(2);
    }

    // Ponder
    LerpedFloat virtualAnimation = LerpedFloat.angular();

    public float getVirtualAngle(float partialTicks) {
        return virtualAnimation.getValue(partialTicks);
    }

    public void animate(float distanceMoved) {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> type))
            return;
        double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
        double newWheelAngle = (virtualAnimation.getValue() - angleDiff) % 360;
        virtualAnimation.setValue(newWheelAngle);
    }

    private void markUpdated() {
        markDirty();
        World level = getWorld();
        if (level != null)
            level.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
    }
}
