package com.zurrtum.create.content.contraptions.behaviour;

import com.google.common.base.Suppliers;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MovementContext {

    public Vec3d position;
    public Vec3d motion;
    public Vec3d relativeMotion;
    public UnaryOperator<Vec3d> rotation;

    public World world;
    public BlockState state;
    public BlockPos localPos;
    public NbtCompound blockEntityData;

    public boolean stall;
    public boolean disabled;
    public boolean firstMovement;
    public NbtCompound data;
    public Contraption contraption;
    public Object temporaryData;

    private FilterItemStack filter;

    private final Supplier<MountedItemStorage> itemStorage;
    private final Supplier<MountedFluidStorage> fluidStorage;

    public MovementContext(World world, StructureBlockInfo info, Contraption contraption) {
        this.world = world;
        this.state = info.state();
        this.blockEntityData = info.nbt();
        this.contraption = contraption;
        localPos = info.pos();

        disabled = false;
        firstMovement = true;
        motion = Vec3d.ZERO;
        relativeMotion = Vec3d.ZERO;
        rotation = v -> v;
        position = null;
        data = new NbtCompound();
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
        if (world.isClient() && contraption.stalled)
            return 700;
        if (Math.abs(length) < 1 / 512f)
            return 0;
        return (((int) (length * modifier + 100 * Math.signum(length))) / 100) * 100;
    }

    public static MovementContext read(World world, StructureBlockInfo info, ReadView view, Contraption contraption) {
        MovementContext context = new MovementContext(world, info, contraption);
        context.motion = view.read("Motion", Vec3d.CODEC).orElseThrow();
        context.relativeMotion = view.read("RelativeMotion", Vec3d.CODEC).orElseThrow();
        view.read("Position", Vec3d.CODEC).ifPresent(position -> context.position = position);
        context.stall = view.getBoolean("Stall", false);
        context.firstMovement = view.getBoolean("FirstMovement", false);
        context.data = view.read("Data", NbtCompound.CODEC).orElseThrow();
        return context;
    }

    public void write(WriteView view) {
        view.put("Motion", Vec3d.CODEC, motion);
        view.put("RelativeMotion", Vec3d.CODEC, relativeMotion);
        if (position != null)
            view.put("Position", Vec3d.CODEC, position);
        view.putBoolean("Stall", stall);
        view.putBoolean("FirstMovement", firstMovement);
        view.put("Data", NbtCompound.CODEC, data);
    }

    public FilterItemStack getFilterFromBE() {
        if (filter != null)
            return filter;
        RegistryOps<NbtElement> ops = world.getRegistryManager().getOps(NbtOps.INSTANCE);
        return filter = blockEntityData.get("Filter", FilterItemStack.CODEC, ops).orElseGet(FilterItemStack::empty);
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
