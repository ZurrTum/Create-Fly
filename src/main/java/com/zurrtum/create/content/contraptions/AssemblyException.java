package com.zurrtum.create.content.contraptions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class AssemblyException extends Exception {
    public static final Codec<AssemblyException> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ComponentSerialization.CODEC.fieldOf("Component").forGetter(i -> i.component),
        BlockPos.CODEC.optionalFieldOf("Position").forGetter(i -> Optional.ofNullable(i.getPosition()))
    ).apply(instance, AssemblyException::new));

    private static final long serialVersionUID = 1L;
    public final Component component;
    private @Nullable BlockPos position = null;

    public static void write(ValueOutput view, @Nullable AssemblyException exception) {
        if (exception == null)
            return;

        ValueOutput lastException = view.child("LastException");
        lastException.store("Component", ComponentSerialization.CODEC, exception.component);
        if (exception.position != null)
            lastException.store("Position", BlockPos.CODEC, exception.position);
    }

    @Nullable
    public static AssemblyException read(ValueInput view) {
        return view.child("LastException").map(lastException -> {
            Component component = lastException.read("Component", ComponentSerialization.CODEC).orElse(CommonComponents.EMPTY);
            AssemblyException exception = new AssemblyException(component);
            lastException.read("Position", BlockPos.CODEC).ifPresent(position -> {
                exception.position = position;
            });
            return exception;
        }).orElse(null);
    }

    public AssemblyException(Component component) {
        this.component = component;
    }

    public AssemblyException(String langKey, Object... objects) {
        this(Component.translatable("create.gui.assembly.exception." + langKey, objects));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private AssemblyException(Component component, Optional<BlockPos> position) {
        this.component = component;
        this.position = position.orElse(null);
    }

    public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
        AssemblyException e = new AssemblyException("unmovableBlock", pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName());
        e.position = pos;
        return e;
    }

    public static AssemblyException unloadedChunk(BlockPos pos) {
        AssemblyException e = new AssemblyException("chunkNotLoaded", pos.getX(), pos.getY(), pos.getZ());
        e.position = pos;
        return e;
    }

    public static AssemblyException structureTooLarge() {
        return new AssemblyException("structureTooLarge", AllConfigs.server().kinetics.maxBlocksMoved.get());
    }

    public static AssemblyException tooManyPistonPoles() {
        return new AssemblyException("tooManyPistonPoles", AllConfigs.server().kinetics.maxPistonPoles.get());
    }

    public static AssemblyException noPistonPoles() {
        return new AssemblyException("noPistonPoles");
    }

    public static AssemblyException notEnoughSails(int sails) {
        return new AssemblyException("not_enough_sails", sails, AllConfigs.server().kinetics.minimumWindmillSails.get());
    }

    public boolean hasPosition() {
        return position != null;
    }

    @Nullable
    public BlockPos getPosition() {
        return position;
    }
}
