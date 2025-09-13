package com.zurrtum.create;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.schematics.SchematicProcessor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllStructureProcessorTypes {
    public static final StructureProcessorType<SchematicProcessor> SCHEMATIC = register("schematic", SchematicProcessor.CODEC);

    public static <P extends StructureProcessor> StructureProcessorType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.STRUCTURE_PROCESSOR, Identifier.of(MOD_ID, id), () -> codec);
    }

    public static void register() {
    }
}
