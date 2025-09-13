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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public class ArmInteractionPoint {
    public static Codec<ArmInteractionPoint> getCodec(World world, BlockPos anchor) {
        return RecordCodecBuilder.create(instance -> instance.group(
            CreateRegistries.ARM_INTERACTION_POINT_TYPE.getCodec().fieldOf("Type").forGetter(ArmInteractionPoint::getType),
            BlockPos.CODEC.fieldOf("Pos").forGetter(point -> point.pos.subtract(anchor)),
            Mode.CODEC.fieldOf("Mode").forGetter(ArmInteractionPoint::getMode)
        ).apply(
            instance, (type, pos, mode) -> {
                pos = pos.add(anchor);
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
    protected World level;
    protected BlockPos pos;
    protected Mode mode = Mode.DEPOSIT;

    protected BlockState cachedState;
    protected Supplier<Inventory> cachedHandler;
    protected ArmAngleTarget cachedAngles;

    public ArmInteractionPoint(ArmInteractionPointType type, World level, BlockPos pos, BlockState state) {
        this.type = type;
        this.level = level;
        this.pos = pos;
        this.cachedState = state;
    }

    public ArmInteractionPointType getType() {
        return type;
    }

    public World getLevel() {
        return level;
    }

    public void setLevel(World level) {
        this.level = level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void relativePos(BlockPos pos) {
        this.pos = this.pos.subtract(pos);
    }

    public void absolutePos(BlockPos pos) {
        this.pos = this.pos.add(pos);
    }

    public Mode getMode() {
        return mode;
    }

    public void cycleMode() {
        mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
    }

    protected Vec3d getInteractionPositionVector() {
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
    protected Inventory getHandler(ArmBlockEntity armBlockEntity) {
        if (cachedHandler == null && level instanceof ServerWorld serverLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null)
                return null;
            cachedHandler = ItemHelper.getInventoryCache(serverLevel, pos, Direction.UP, (blockEntity, direction) -> !armBlockEntity.isRemoved());
        }
        return cachedHandler.get();
    }

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        Inventory handler = getHandler(armBlockEntity);
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
        Inventory handler = getHandler(armBlockEntity);
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
        Inventory handler = getHandler(armBlockEntity);
        if (handler == null)
            return 0;
        return handler.size();
    }

    protected void serialize(NbtCompound nbt, BlockPos anchor) {
        NBTHelper.writeEnum(nbt, "Mode", mode);
    }

    protected void deserialize(NbtCompound nbt, BlockPos anchor) {
        mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
    }

    public final NbtCompound serialize(BlockPos anchor) {
        Identifier key = CreateRegistries.ARM_INTERACTION_POINT_TYPE.getId(type);
        if (key == null)
            throw new IllegalArgumentException("Could not get id for ArmInteractionPointType " + type + "!");

        NbtCompound nbt = new NbtCompound();
        nbt.putString("Type", key.toString());
        nbt.put("Pos", BlockPos.CODEC, pos.subtract(anchor));
        serialize(nbt, anchor);
        return nbt;
    }

    @Nullable
    public static ArmInteractionPoint deserialize(NbtCompound nbt, World level, BlockPos anchor) {
        Identifier id = Identifier.tryParse(nbt.getString("Type", ""));
        if (id == null)
            return null;
        ArmInteractionPointType type = CreateRegistries.ARM_INTERACTION_POINT_TYPE.get(id);
        if (type == null)
            return null;
        BlockPos pos = NBTHelper.readBlockPos(nbt, "Pos").add(anchor);
        BlockState state = level.getBlockState(pos);
        if (!type.canCreatePoint(level, pos, state))
            return null;
        ArmInteractionPoint point = type.createPoint(level, pos, state);
        if (point == null)
            return null;
        point.deserialize(nbt, anchor);
        return point;
    }

    public static void transformPos(NbtCompound nbt, StructureTransform transform) {
        BlockPos pos = nbt.get("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        pos = transform.applyWithoutOffset(pos);
        nbt.put("Pos", BlockPos.CODEC, pos);
    }

    public static boolean isInteractable(World level, BlockPos pos, BlockState state) {
        return ArmInteractionPointType.getPrimaryType(level, pos, state) != null;
    }

    @Nullable
    public static ArmInteractionPoint create(World level, BlockPos pos, BlockState state) {
        ArmInteractionPointType type = ArmInteractionPointType.getPrimaryType(level, pos, state);
        if (type == null)
            return null;
        return type.createPoint(level, pos, state);
    }

    public enum Mode implements StringIdentifiable {
        DEPOSIT("create.mechanical_arm.deposit_to", 0xDDC166),
        TAKE("create.mechanical_arm.extract_from", 0x7FCDE0);

        public static final Codec<Mode> CODEC = StringIdentifiable.createCodec(Mode::values);
        public static final PacketCodec<ByteBuf, Mode> PACKET_CODEC = CatnipStreamCodecBuilders.ofEnum(Mode.class);
        private final String translationKey;
        private final int color;

        Mode(String translationKey, int color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        @Override
        public String asString() {
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
