package com.zurrtum.create;

import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import com.zurrtum.create.content.contraptions.bearing.ClockworkContraption;
import com.zurrtum.create.content.contraptions.bearing.StabilizedContraption;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.contraptions.gantry.GantryContraption;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.contraptions.piston.PistonContraption;
import com.zurrtum.create.content.contraptions.pulley.PulleyContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllContraptionTypes {
    public static final ContraptionType PISTON = register("piston", PistonContraption::new);
    public static final ContraptionType PULLEY = register("pulley", PulleyContraption::new);
    public static final ContraptionType MOUNTED = register("mounted", MountedContraption::new);
    public static final ContraptionType STABILIZED = register("stabilized", StabilizedContraption::new);
    public static final ContraptionType BEARING = register("bearing", BearingContraption::new);
    public static final ContraptionType GANTRY = register("gantry", GantryContraption::new);
    public static final ContraptionType CLOCKWORK = register("clockwork", ClockworkContraption::new);
    public static final ContraptionType CARRIAGE = register("carriage", CarriageContraption::new);
    public static final ContraptionType ELEVATOR = register("elevator", ElevatorContraption::new);

    private static ContraptionType register(String name, Supplier<? extends Contraption> factory) {
        return Registry.register(CreateRegistries.CONTRAPTION_TYPE, Identifier.of(MOD_ID, name), new ContraptionType(factory));
    }

    public static void register() {
    }
}
