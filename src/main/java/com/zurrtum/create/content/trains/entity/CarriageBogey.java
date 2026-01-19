package com.zurrtum.create.content.trains.entity;

import com.mojang.serialization.*;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

import static com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity.BOGEY_DATA_KEY;
import static com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity.BOGEY_STYLE_KEY;

public class CarriageBogey {
    public static final StreamCodec<RegistryFriendlyByteBuf, CarriageBogey> STREAM_CODEC = StreamCodec.composite(
        AbstractBogeyBlock.STREAM_CODEC,
        bogey -> bogey.type,
        ByteBufCodecs.BOOL,
        bogey -> bogey.upsideDown,
        ByteBufCodecs.COMPOUND_TAG,
        bogey -> bogey.bogeyData,
        CarriageBogey::new
    );

    public static final Random RANDOM = new Random();
    public static final String UPSIDE_DOWN_KEY = "UpsideDown";

    public Carriage carriage;
    public boolean isLeading;

    public CompoundTag bogeyData;

    public AbstractBogeyBlock<?> type;
    boolean upsideDown;
    Couple<TravellingPoint> points;

    public LerpedFloat wheelAngle;
    public LerpedFloat yaw;
    public LerpedFloat pitch;

    public Couple<Vec3> couplingAnchors;

    int derailAngle;

    public CarriageBogey(AbstractBogeyBlock<?> type, boolean upsideDown, CompoundTag bogeyData) {
        this(type, upsideDown, bogeyData, new TravellingPoint(), new TravellingPoint());
    }

    public CarriageBogey(AbstractBogeyBlock<?> type, boolean upsideDown, CompoundTag bogeyData, TravellingPoint point, TravellingPoint point2) {
        this.type = type;
        this.upsideDown = type.canBeUpsideDown() && upsideDown;
        point.upsideDown = this.upsideDown;
        point2.upsideDown = this.upsideDown;
        if (bogeyData == null || bogeyData.isEmpty())
            bogeyData = this.createBogeyData(); // Prevent Crash When Updating
        bogeyData.putBoolean(UPSIDE_DOWN_KEY, upsideDown);
        this.bogeyData = bogeyData;
        points = Couple.create(point, point2);
        wheelAngle = LerpedFloat.angular();
        yaw = LerpedFloat.angular();
        pitch = LerpedFloat.angular();
        derailAngle = RANDOM.nextInt(60) - 30;
        couplingAnchors = Couple.create(null, null);
    }

    public ResourceKey<Level> getDimension() {
        TravellingPoint leading = leading();
        TravellingPoint trailing = trailing();
        if (leading.edge == null || trailing.edge == null)
            return null;
        if (leading.edge.isInterDimensional() || trailing.edge.isInterDimensional())
            return null;
        ResourceKey<Level> dimension1 = leading.node1.getLocation().dimension;
        ResourceKey<Level> dimension2 = trailing.node1.getLocation().dimension;
        if (dimension1.equals(dimension2))
            return dimension1;
        return null;
    }

    public void updateAngles(CarriageContraptionEntity entity, double distanceMoved) {
        double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());

        float xRot = 0;
        float yRot = 0;

        if (leading().edge == null || carriage.train.derailed) {
            yRot = -90 + entity.yaw - derailAngle;
        } else if (!entity.level().dimension().equals(getDimension())) {
            yRot = -90 + entity.yaw;
            xRot = 0;
        } else {
            Vec3 positionVec = leading().getPosition(carriage.train.graph);
            Vec3 coupledVec = trailing().getPosition(carriage.train.graph);
            double diffX = positionVec.x - coupledVec.x;
            double diffY = positionVec.y - coupledVec.y;
            double diffZ = positionVec.z - coupledVec.z;
            yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
            xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));
        }

        double newWheelAngle = (wheelAngle.getValue() - angleDiff) % 360;

        for (boolean twice : Iterate.trueAndFalse) {
            if (twice && !entity.firstPositionUpdate)
                continue;
            wheelAngle.setValue(newWheelAngle);
            pitch.setValue(xRot);
            yaw.setValue(-yRot);
        }
    }

    public TravellingPoint leading() {
        TravellingPoint point = points.getFirst();
        point.upsideDown = isUpsideDown();
        return point;
    }

    public TravellingPoint trailing() {
        TravellingPoint point = points.getSecond();
        point.upsideDown = isUpsideDown();
        return point;
    }

    public double getStress() {
        if (getDimension() == null)
            return 0;
        if (carriage.train.derailed)
            return 0;
        return type.getWheelPointSpacing() - leading().getPosition(carriage.train.graph).distanceTo(trailing().getPosition(carriage.train.graph));
    }

    @Nullable
    public Vec3 getAnchorPosition() {
        return getAnchorPosition(false);
    }

    @Nullable
    public Vec3 getAnchorPosition(boolean flipUpsideDown) {
        if (leading().edge == null)
            return null;
        return points.getFirst().getPosition(carriage.train.graph, flipUpsideDown)
            .add(points.getSecond().getPosition(carriage.train.graph, flipUpsideDown)).scale(.5);
    }

    public void updateCouplingAnchor(Vec3 entityPos, float entityXRot, float entityYRot, int bogeySpacing, float yaw, float pitch, boolean leading) {
        boolean selfUpsideDown = isUpsideDown();
        boolean leadingUpsideDown = carriage.leadingBogey().isUpsideDown();
        Vec3 thisOffset = type.getConnectorAnchorOffset(selfUpsideDown);
        thisOffset = thisOffset.multiply(1, 1, leading ? -1 : 1);

        thisOffset = VecHelper.rotate(thisOffset, pitch, Axis.X);
        thisOffset = VecHelper.rotate(thisOffset, yaw, Axis.Y);
        thisOffset = VecHelper.rotate(thisOffset, -entityYRot - 90, Axis.Y);
        thisOffset = VecHelper.rotate(thisOffset, entityXRot, Axis.X);
        thisOffset = VecHelper.rotate(thisOffset, -180, Axis.Y);
        thisOffset = thisOffset.add(0, 0, leading ? 0 : -bogeySpacing);
        thisOffset = VecHelper.rotate(thisOffset, 180, Axis.Y);
        thisOffset = VecHelper.rotate(thisOffset, -entityXRot, Axis.X);
        thisOffset = VecHelper.rotate(thisOffset, entityYRot + 90, Axis.Y);
        if (selfUpsideDown != leadingUpsideDown)
            thisOffset = thisOffset.add(0, selfUpsideDown ? -2 : 2, 0);

        couplingAnchors.set(leading, entityPos.add(thisOffset));
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        view.store("Type", CreateCodecs.BLOCK_CODEC, type);
        ValueOutput.ValueOutputList list = view.childrenList("Points");
        points.getFirst().write(list.addChild(), dimensions);
        points.getSecond().write(list.addChild(), dimensions);
        view.putBoolean("UpsideDown", upsideDown);
        bogeyData.putBoolean(UPSIDE_DOWN_KEY, upsideDown);
        bogeyData.store(BOGEY_STYLE_KEY, Identifier.CODEC, getStyle().id);
        view.store(BOGEY_DATA_KEY, CompoundTag.CODEC, bogeyData);
    }

    public static <T> DataResult<T> encode(final CarriageBogey input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Type", input.type, CreateCodecs.BLOCK_CODEC);
        ListBuilder<T> list = ops.listBuilder();
        list.add(TravellingPoint.encode(input.points.getFirst(), ops, empty, dimensions));
        list.add(TravellingPoint.encode(input.points.getSecond(), ops, empty, dimensions));
        map.add("Points", list.build(empty));
        map.add("UpsideDown", ops.createBoolean(input.upsideDown));
        input.bogeyData.putBoolean(UPSIDE_DOWN_KEY, input.upsideDown);
        input.bogeyData.store(BOGEY_STYLE_KEY, Identifier.CODEC, input.getStyle().id);
        map.add(BOGEY_DATA_KEY, input.bogeyData, CompoundTag.CODEC);
        return map.build(empty);
    }

    public static CarriageBogey read(ValueInput view, TrackGraph graph, DimensionPalette dimensions) {
        AbstractBogeyBlock<?> type = (AbstractBogeyBlock<?>) view.read("Type", CreateCodecs.BLOCK_CODEC).orElseThrow();
        boolean upsideDown = view.getBooleanOr("UpsideDown", false);
        Iterator<ValueInput> iterator = view.childrenListOrEmpty("Points").iterator();
        TravellingPoint point1 = TravellingPoint.read(iterator.next(), graph, dimensions);
        TravellingPoint point2 = TravellingPoint.read(iterator.next(), graph, dimensions);
        CompoundTag data = view.read(BOGEY_DATA_KEY, CompoundTag.CODEC).orElseThrow();
        return new CarriageBogey(type, upsideDown, data, point1, point2);
    }

    public static <T> CarriageBogey decode(DynamicOps<T> ops, T input, TrackGraph graph, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        AbstractBogeyBlock<?> type = (AbstractBogeyBlock<?>) CreateCodecs.BLOCK_CODEC.parse(ops, map.get("Type")).getOrThrow();
        boolean upsideDown = ops.getBooleanValue(map.get("UpsideDown")).getOrThrow();
        Iterator<T> iterator = ops.getStream(map.get("Points")).getOrThrow().iterator();
        TravellingPoint point1 = TravellingPoint.decode(ops, iterator.next(), graph, dimensions);
        TravellingPoint point2 = TravellingPoint.decode(ops, iterator.next(), graph, dimensions);
        CompoundTag data = CompoundTag.CODEC.parse(ops, map.get(BOGEY_DATA_KEY)).getOrThrow();
        return new CarriageBogey(type, upsideDown, data, point1, point2);
    }

    public BogeyStyle getStyle() {
        Optional<Identifier> location = bogeyData.read(BOGEY_STYLE_KEY, Identifier.CODEC);
        if (location.isEmpty()) {
            return AllBogeyStyles.STANDARD;
        }
        BogeyStyle style = AllBogeyStyles.BOGEY_STYLES.get(location.get());
        return style != null ? style : AllBogeyStyles.STANDARD; // just for safety
    }

    public BogeySize getSize() {
        return type.getSize();
    }

    private CompoundTag createBogeyData() {
        BogeyStyle style = type != null ? type.getDefaultStyle() : AllBogeyStyles.STANDARD;
        CompoundTag nbt = style.defaultData != null ? style.defaultData : new CompoundTag();
        nbt.store(BOGEY_STYLE_KEY, Identifier.CODEC, style.id);
        nbt.putBoolean(UPSIDE_DOWN_KEY, isUpsideDown());
        return nbt;
    }

    void setLeading() {
        isLeading = true;
    }

    public boolean isUpsideDown() {
        return type.canBeUpsideDown() && upsideDown;
    }
}
