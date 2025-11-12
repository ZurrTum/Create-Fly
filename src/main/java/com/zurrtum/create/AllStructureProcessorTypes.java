package com.zurrtum.create;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.schematics.SchematicProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import static com.zurrtum.create.Create.MOD_ID;

public class AllStructureProcessorTypes {
    public static final StructureProcessorType<SchematicProcessor> SCHEMATIC = register("schematic", SchematicProcessor.CODEC);

    public static <P extends StructureProcessor> StructureProcessorType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, Identifier.fromNamespaceAndPath(MOD_ID, id), () -> codec);
    }

    public static void register() {
    }
}
