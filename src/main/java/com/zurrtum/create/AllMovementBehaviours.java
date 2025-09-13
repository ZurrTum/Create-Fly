package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour;
import com.zurrtum.create.content.contraptions.bearing.StabilizedBearingMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.BellMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.CampfireMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.dispenser.DispenserMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.dispenser.DropperMovementBehaviour;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorMovementBehaviour;
import com.zurrtum.create.content.equipment.bell.HauntedBellMovementBehaviour;
import com.zurrtum.create.content.fluids.tank.FluidTankMovementBehavior;
import com.zurrtum.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.zurrtum.create.content.kinetics.drill.DrillMovementBehaviour;
import com.zurrtum.create.content.kinetics.saw.SawMovementBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotMovementBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelMovementBehaviour;
import com.zurrtum.create.content.processing.basin.BasinMovementBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerMovementBehaviour;
import com.zurrtum.create.content.redstone.contact.ContactMovementBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class AllMovementBehaviours {
    public static final BasinMovementBehaviour BASIN = new BasinMovementBehaviour();
    public static final BlazeBurnerMovementBehaviour BLAZE_BURNER = new BlazeBurnerMovementBehaviour();
    public static final FluidTankMovementBehavior FLUID_TANK = new FluidTankMovementBehavior();
    public static final PortableStorageInterfaceMovement PORTABLE_FLUID_INTERFACE = new PortableStorageInterfaceMovement();
    public static final StabilizedBearingMovementBehaviour MECHANICAL_BEARING = new StabilizedBearingMovementBehaviour();
    public static final ContraptionControlsMovement CONTRAPTION_CONTROLS = new ContraptionControlsMovement();
    public static final DrillMovementBehaviour MECHANICAL_DRILL = new DrillMovementBehaviour();
    public static final SawMovementBehaviour MECHANICAL_SAW = new SawMovementBehaviour();
    public static final DeployerMovementBehaviour DEPLOYER = new DeployerMovementBehaviour();
    public static final PortableStorageInterfaceMovement PORTABLE_STORAGE_INTERFACE = new PortableStorageInterfaceMovement();
    public static final ContactMovementBehaviour REDSTONE_CONTACT = new ContactMovementBehaviour();
    public static final HarvesterMovementBehaviour MECHANICAL_HARVESTER = new HarvesterMovementBehaviour();
    public static final PloughMovementBehaviour MECHANICAL_PLOUGH = new PloughMovementBehaviour();
    public static final RollerMovementBehaviour MECHANICAL_ROLLER = new RollerMovementBehaviour();
    public static final ControlsMovementBehaviour TRAIN_CONTROLS = new ControlsMovementBehaviour();
    public static final FunnelMovementBehaviour ANDESITE_FUNNEL = FunnelMovementBehaviour.andesite();
    public static final FunnelMovementBehaviour BRASS_FUNNEL = FunnelMovementBehaviour.brass();
    public static final BellMovementBehaviour BELL = new BellMovementBehaviour();
    public static final HauntedBellMovementBehaviour HAUNTED_BELL = new HauntedBellMovementBehaviour();
    public static final SlidingDoorMovementBehaviour SLIDING_DOOR = new SlidingDoorMovementBehaviour();
    public static final SeatMovementBehaviour SEAT = new SeatMovementBehaviour();
    public static final CampfireMovementBehaviour CAMPFIRE = new CampfireMovementBehaviour();
    public static final DispenserMovementBehaviour DISPENSER = new DispenserMovementBehaviour();
    public static final DropperMovementBehaviour DROPPER = new DropperMovementBehaviour();
    public static final DepotMovementBehaviour DEPOT = new DepotMovementBehaviour();

    public static void register(MovementBehaviour behaviour, Block... blocks) {
        for (Block block : blocks) {
            MovementBehaviour.REGISTRY.register(block, behaviour);
        }
    }

    public static void register() {
        register(BASIN, AllBlocks.BASIN);
        register(FLUID_TANK, AllBlocks.FLUID_TANK);
        register(BLAZE_BURNER, AllBlocks.BLAZE_BURNER);
        register(PORTABLE_FLUID_INTERFACE, AllBlocks.PORTABLE_FLUID_INTERFACE);
        register(MECHANICAL_BEARING, AllBlocks.MECHANICAL_BEARING);
        register(CONTRAPTION_CONTROLS, AllBlocks.CONTRAPTION_CONTROLS);
        register(MECHANICAL_DRILL, AllBlocks.MECHANICAL_DRILL);
        register(MECHANICAL_SAW, AllBlocks.MECHANICAL_SAW);
        register(DEPLOYER, AllBlocks.DEPLOYER);
        register(PORTABLE_STORAGE_INTERFACE, AllBlocks.PORTABLE_STORAGE_INTERFACE);
        register(REDSTONE_CONTACT, AllBlocks.REDSTONE_CONTACT);
        register(MECHANICAL_HARVESTER, AllBlocks.MECHANICAL_HARVESTER);
        register(MECHANICAL_PLOUGH, AllBlocks.MECHANICAL_PLOUGH);
        register(MECHANICAL_ROLLER, AllBlocks.MECHANICAL_ROLLER);
        register(TRAIN_CONTROLS, AllBlocks.TRAIN_CONTROLS);
        register(ANDESITE_FUNNEL, AllBlocks.ANDESITE_FUNNEL);
        register(BRASS_FUNNEL, AllBlocks.BRASS_FUNNEL);
        register(BELL, Blocks.BELL, AllBlocks.PECULIAR_BELL, AllBlocks.DESK_BELL);
        register(HAUNTED_BELL, AllBlocks.HAUNTED_BELL);
        register(
            SLIDING_DOOR,
            AllBlocks.ANDESITE_DOOR,
            AllBlocks.BRASS_DOOR,
            AllBlocks.COPPER_DOOR,
            AllBlocks.TRAIN_DOOR,
            AllBlocks.FRAMED_GLASS_DOOR
        );
        register(
            SEAT,
            AllBlocks.WHITE_SEAT,
            AllBlocks.ORANGE_SEAT,
            AllBlocks.MAGENTA_SEAT,
            AllBlocks.LIGHT_BLUE_SEAT,
            AllBlocks.YELLOW_SEAT,
            AllBlocks.LIME_SEAT,
            AllBlocks.PINK_SEAT,
            AllBlocks.GRAY_SEAT,
            AllBlocks.LIGHT_GRAY_SEAT,
            AllBlocks.CYAN_SEAT,
            AllBlocks.PURPLE_SEAT,
            AllBlocks.BLUE_SEAT,
            AllBlocks.BROWN_SEAT,
            AllBlocks.GREEN_SEAT,
            AllBlocks.RED_SEAT,
            AllBlocks.BLACK_SEAT
        );
        register(CAMPFIRE, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        register(DISPENSER, Blocks.DISPENSER);
        register(DROPPER, Blocks.DROPPER);
        register(DEPOT, AllBlocks.DEPOT);
    }
}