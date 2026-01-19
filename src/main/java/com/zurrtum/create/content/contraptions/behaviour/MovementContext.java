package com.zurrtum.create.content.contraptions.behaviour;

import com.google.common.base.Suppliers;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MovementContext {

    public Vec3 position;
    public Vec3 motion;
    public Vec3 relativeMotion;
    public UnaryOperator<Vec3> rotation;

    public Level world;
    public BlockState state;
    public BlockPos localPos;
    public CompoundTag blockEntityData;

    public boolean stall;
    public boolean disabled;
    public boolean firstMovement;
    public CompoundTag data;
    public Contraption contraption;
    public Object temporaryData;

    private FilterItemStack filter;

    private final Supplier<MountedItemStorage> itemStorage;
    private final Supplier<MountedFluidStorage> fluidStorage;

    public MovementContext(Level world, StructureBlockInfo info, Contraption contraption) {
        this.world = world;
        this.state = info.state();
        this.blockEntityData = info.nbt();
        this.contraption = contraption;
        localPos = info.pos();

        disabled = false;
        firstMovement = true;
        motion = Vec3.ZERO;
        relativeMotion = Vec3.ZERO;
        rotation = v -> v;
        position = null;
        data = new CompoundTag();
        stall = false;
        filter = null;
        this.itemStorage = Suppliers.memoize(() -> contraption.getStorage().getAllItemStorages().get(this.localPos));
        this.fluidStorage = Suppliers.memoize(() -> contraption.getStorage().getFluids().storages.get(this.localPos));
    }

    public float getAnimationSpeed() {
        int modifier = 1000;
        double length = -motion.length();
        if (disabled)
            return 0;
        if (world.isClientSide() && contraption.stalled)
            return 700;
        if (Math.abs(length) < 1 / 512f)
            return 0;
        return (((int) (length * modifier + 100 * Math.signum(length))) / 100) * 100;
    }

    public static MovementContext read(Level world, StructureBlockInfo info, ValueInput view, Contraption contraption) {
        MovementContext context = new MovementContext(world, info, contraption);
        context.motion = view.read("Motion", Vec3.CODEC).orElseThrow();
        context.relativeMotion = view.read("RelativeMotion", Vec3.CODEC).orElseThrow();
        view.read("Position", Vec3.CODEC).ifPresent(position -> context.position = position);
        context.stall = view.getBooleanOr("Stall", false);
        context.firstMovement = view.getBooleanOr("FirstMovement", false);
        context.data = view.read("Data", CompoundTag.CODEC).orElseThrow();
        return context;
    }

    public void write(ValueOutput view) {
        view.store("Motion", Vec3.CODEC, motion);
        view.store("RelativeMotion", Vec3.CODEC, relativeMotion);
        if (position != null)
            view.store("Position", Vec3.CODEC, position);
        view.putBoolean("Stall", stall);
        view.putBoolean("FirstMovement", firstMovement);
        view.store("Data", CompoundTag.CODEC, data);
    }

    public FilterItemStack getFilterFromBE() {
        if (filter != null)
            return filter;
        RegistryOps<Tag> ops = world.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return filter = blockEntityData.read("Filter", FilterItemStack.CODEC, ops).orElseGet(FilterItemStack::empty);
    }

    @Nullable
    public MountedItemStorage getItemStorage() {
        return this.itemStorage.get();
    }

    @Nullable
    public MountedFluidStorage getFluidStorage() {
        return this.fluidStorage.get();
    }
}
