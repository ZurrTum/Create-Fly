package com.zurrtum.create.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.zurrtum.create.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.util.datafix.fixes.References.*;

public class V1_21_8 extends NamespacedSchema {
    private static final int WORLD_VERSION = 4440;

    public V1_21_8(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static void register(DataFixerBuilder fixerUpper) {
        Schema schema = fixerUpper.addSchema(WORLD_VERSION, V1_21_8::new);
        fixerUpper.addFixer(new AddNewChoices(schema, "Added Create Entity", ENTITY));
        fixerUpper.addFixer(new AddNewChoices(schema, "Added Create Block Entity", BLOCK_ENTITY));
    }

    @Override
    public void registerTypes(
        Schema schema,
        Map<String, Supplier<TypeTemplate>> entityTypes,
        Map<String, Supplier<TypeTemplate>> blockEntityTypes
    ) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(
            false,
            References.CONTRAPTION,
            () -> DSL.fields("Blocks", DSL.optionalFields("Palette", DSL.list(BLOCK_STATE.in(schema))))
        );
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(
            map,
            "create:contraption",
            () -> DSL.optionalFields("Contraption", References.CONTRAPTION.in(schema))
        );
        schema.register(
            map,
            "create:stationary_contraption",
            () -> DSL.optionalFields("Contraption", References.CONTRAPTION.in(schema))
        );
        schema.register(
            map,
            "create:carriage_contraption",
            () -> DSL.optionalFields("Contraption", References.CONTRAPTION.in(schema))
        );
        schema.register(
            map,
            "create:gantry_contraption",
            () -> DSL.optionalFields("Contraption", References.CONTRAPTION.in(schema))
        );
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.register(map, "create:copycat", () -> DSL.fields("Material", BLOCK_STATE.in(schema)));
        schema.register(map, "create:water_wheel", () -> DSL.fields("Material", BLOCK_STATE.in(schema)));
        schema.register(map, "create:large_water_wheel", () -> DSL.fields("Material", BLOCK_STATE.in(schema)));
        schema.register(map, "create:bracketed_kinetic", () -> DSL.fields("Bracket", BLOCK_STATE.in(schema)));
        schema.register(map, "create:glass_fluid_pipe", () -> DSL.fields("Bracket", BLOCK_STATE.in(schema)));
        schema.register(map, "create:fluid_pipe", () -> DSL.fields("Bracket", BLOCK_STATE.in(schema)));
        return map;
    }
}
