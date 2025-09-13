package com.zurrtum.create.content.contraptions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class AssemblyException extends Exception {
    public static final Codec<AssemblyException> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TextCodecs.CODEC.fieldOf("Component").forGetter(i -> i.component),
        BlockPos.CODEC.optionalFieldOf("Position").forGetter(i -> Optional.ofNullable(i.getPosition()))
    ).apply(instance, AssemblyException::new));

    private static final long serialVersionUID = 1L;
    public final Text component;
    private BlockPos position = null;

    public static void write(WriteView view, AssemblyException exception) {
        if (exception == null)
            return;

        WriteView lastException = view.get("LastException");
        lastException.put("Component", TextCodecs.CODEC, exception.component);
        if (exception.hasPosition())
            lastException.put("Position", BlockPos.CODEC, exception.getPosition());
    }

    public static AssemblyException read(ReadView view) {
        return view.getOptionalReadView("LastException").map(lastException -> {
            Text component = lastException.read("Component", TextCodecs.CODEC).orElse(ScreenTexts.EMPTY);
            AssemblyException exception = new AssemblyException(component);
            lastException.read("Position", BlockPos.CODEC).ifPresent(position -> {
                exception.position = position;
            });
            return exception;
        }).orElse(null);
    }

    public AssemblyException(Text component) {
        this.component = component;
    }

    public AssemblyException(String langKey, Object... objects) {
        this(Text.translatable("create.gui.assembly.exception." + langKey, objects));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private AssemblyException(Text component, Optional<BlockPos> position) {
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

    public BlockPos getPosition() {
        return position;
    }
}
