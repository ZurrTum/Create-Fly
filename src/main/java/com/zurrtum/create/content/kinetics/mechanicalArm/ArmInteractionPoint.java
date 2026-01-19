package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.foundation.item.ItemHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public class ArmInteractionPoint {
    public static Codec<ArmInteractionPoint> getCodec(Level world, BlockPos anchor) {
        return RecordCodecBuilder.create(instance -> instance.group(
            CreateRegistries.ARM_INTERACTION_POINT_TYPE.byNameCodec().fieldOf("Type").forGetter(ArmInteractionPoint::getType),
            BlockPos.CODEC.fieldOf("Pos").forGetter(point -> point.pos.subtract(anchor)),
            Mode.CODEC.fieldOf("Mode").forGetter(ArmInteractionPoint::getMode)
        ).apply(
            instance, (type, pos, mode) -> {
                pos = pos.offset(anchor);
                BlockState state = world.getBlockState(pos);
                if (!type.canCreatePoint(world, pos, state))
                    return null;
                ArmInteractionPoint point = type.createPoint(world, pos, state);
                if (point == null)
                    return null;
                point.mode = mode;
                return point;
            }
        ));
    }

    protected final ArmInteractionPointType type;
    protected Level level;
    protected BlockPos pos;
    protected Mode mode = Mode.DEPOSIT;

    protected BlockState cachedState;
    protected Supplier<Container> cachedHandler;
    protected ArmAngleTarget cachedAngles;

    public ArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        this.type = type;
        this.level = level;
        this.pos = pos;
        this.cachedState = state;
    }

    public ArmInteractionPointType getType() {
        return type;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void relativePos(BlockPos pos) {
        this.pos = this.pos.subtract(pos);
    }

    public void absolutePos(BlockPos pos) {
        this.pos = this.pos.offset(pos);
    }

    public Mode getMode() {
        return mode;
    }

    public void cycleMode() {
        mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
    }

    protected Vec3 getInteractionPositionVector() {
        return VecHelper.getCenterOf(pos);
    }

    protected Direction getInteractionDirection() {
        return Direction.DOWN;
    }

    public ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
        if (cachedAngles == null)
            cachedAngles = new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection(), ceiling);

        return cachedAngles;
    }

    public void updateCachedState() {
        cachedState = level.getBlockState(pos);
    }

    public boolean isValid() {
        updateCachedState();
        return type.canCreatePoint(level, pos, cachedState);
    }

    public void keepAlive() {
    }

    @Nullable
    protected Container getHandler(ArmBlockEntity armBlockEntity) {
        if (cachedHandler == null && level instanceof ServerLevel serverLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null)
                return null;
            cachedHandler = ItemHelper.getInventoryCache(serverLevel, pos, Direction.UP, (blockEntity, direction) -> !armBlockEntity.isRemoved());
        }
        return cachedHandler.get();
    }

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        Container handler = getHandler(armBlockEntity);
        if (handler == null)
            return stack;
        int insert;
        if (simulate) {
            insert = handler.countSpace(stack, Direction.UP);
        } else {
            insert = handler.insert(stack, Direction.UP);
        }
        if (insert == 0) {
            return stack;
        }
        int count = stack.getCount();
        if (insert == count) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(count - insert);
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        Container handler = getHandler(armBlockEntity);
        if (handler == null)
            return ItemStack.EMPTY;
        if (simulate) {
            return handler.count(stack -> true, amount, Direction.UP);
        }
        return handler.extract(stack -> true, amount, Direction.UP);
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, boolean simulate) {
        return extract(armBlockEntity, slot, 64, simulate);
    }

    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        Container handler = getHandler(armBlockEntity);
        if (handler == null)
            return 0;
        return handler.getContainerSize();
    }

    protected void serialize(CompoundTag nbt, BlockPos anchor) {
        NBTHelper.writeEnum(nbt, "Mode", mode);
    }

    protected void deserialize(CompoundTag nbt, BlockPos anchor) {
        mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
    }

    public final CompoundTag serialize(BlockPos anchor) {
        Identifier key = CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(type);
        if (key == null)
            throw new IllegalArgumentException("Could not get id for ArmInteractionPointType " + type + "!");

        CompoundTag nbt = new CompoundTag();
        nbt.putString("Type", key.toString());
        nbt.store("Pos", BlockPos.CODEC, pos.subtract(anchor));
        serialize(nbt, anchor);
        return nbt;
    }

    @Nullable
    public static ArmInteractionPoint deserialize(CompoundTag nbt, Level level, BlockPos anchor) {
        Identifier id = Identifier.tryParse(nbt.getStringOr("Type", ""));
        if (id == null)
            return null;
        ArmInteractionPointType type = CreateRegistries.ARM_INTERACTION_POINT_TYPE.getValue(id);
        if (type == null)
            return null;
        BlockPos pos = NBTHelper.readBlockPos(nbt, "Pos").offset(anchor);
        BlockState state = level.getBlockState(pos);
        if (!type.canCreatePoint(level, pos, state))
            return null;
        ArmInteractionPoint point = type.createPoint(level, pos, state);
        if (point == null)
            return null;
        point.deserialize(nbt, anchor);
        return point;
    }

    public static void transformPos(CompoundTag nbt, StructureTransform transform) {
        BlockPos pos = nbt.read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        pos = transform.applyWithoutOffset(pos);
        nbt.store("Pos", BlockPos.CODEC, pos);
    }

    public static boolean isInteractable(Level level, BlockPos pos, BlockState state) {
        return ArmInteractionPointType.getPrimaryType(level, pos, state) != null;
    }

    @Nullable
    public static ArmInteractionPoint create(Level level, BlockPos pos, BlockState state) {
        ArmInteractionPointType type = ArmInteractionPointType.getPrimaryType(level, pos, state);
        if (type == null)
            return null;
        return type.createPoint(level, pos, state);
    }

    public enum Mode implements StringRepresentable {
        DEPOSIT("create.mechanical_arm.deposit_to", 0xDDC166),
        TAKE("create.mechanical_arm.extract_from", 0x7FCDE0);

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<ByteBuf, Mode> PACKET_CODEC = CatnipStreamCodecBuilders.ofEnum(Mode.class);
        private final String translationKey;
        private final int color;

        Mode(String translationKey, int color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public int getColor() {
            return color;
        }
    }

}
