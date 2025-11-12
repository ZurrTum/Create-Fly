package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovingInteraction;
import com.zurrtum.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsInteractionBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.DoorMovingInteraction;
import com.zurrtum.create.content.contraptions.behaviour.LeverMovingInteraction;
import com.zurrtum.create.content.contraptions.behaviour.TrapdoorMovingInteraction;
import com.zurrtum.create.content.kinetics.deployer.DeployerMovingInteraction;
import com.zurrtum.create.content.logistics.depot.MountedDepotInteractionBehaviour;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class AllInteractionBehaviours {
    public static final ConductorBlockInteractionBehavior.BlazeBurner BLAZE_BURNER = new ConductorBlockInteractionBehavior.BlazeBurner();
    public static final MountedDepotInteractionBehaviour DEPOT = new MountedDepotInteractionBehaviour();
    public static final ContraptionControlsMovingInteraction CONTRAPTION_CONTROLS = new ContraptionControlsMovingInteraction();
    public static final DeployerMovingInteraction DEPLOYER = new DeployerMovingInteraction();
    public static final ControlsInteractionBehaviour CONTROLS = new ControlsInteractionBehaviour();
    public static final DoorMovingInteraction DOOR = new DoorMovingInteraction();
    public static final SeatInteractionBehaviour SEAT = new SeatInteractionBehaviour();
    public static final LeverMovingInteraction LEVER = new LeverMovingInteraction();
    public static final TrapdoorMovingInteraction TRAPDOOR = new TrapdoorMovingInteraction();

    public static void register(MovingInteractionBehaviour behaviour, Block... blocks) {
        for (Block block : blocks) {
            MovingInteractionBehaviour.REGISTRY.register(block, behaviour);
        }
    }

    @SuppressWarnings("deprecation")
    public static void register(MovingInteractionBehaviour behaviour, TagKey<Block> tag) {
        MovingInteractionBehaviour.REGISTRY.registerProvider(block -> block.builtInRegistryHolder().is(tag) ? behaviour : null);
    }

    public static void register() {
        register(BLAZE_BURNER, AllBlocks.BLAZE_BURNER);
        register(DEPOT, AllBlocks.DEPOT);
        register(CONTRAPTION_CONTROLS, AllBlocks.CONTRAPTION_CONTROLS);
        register(DEPLOYER, AllBlocks.DEPLOYER);
        register(CONTROLS, AllBlocks.TRAIN_CONTROLS);
        register(DOOR, AllBlocks.ANDESITE_DOOR, AllBlocks.BRASS_DOOR, AllBlocks.COPPER_DOOR, AllBlocks.TRAIN_DOOR, AllBlocks.FRAMED_GLASS_DOOR);
        register(DOOR, BlockTags.WOODEN_DOORS);
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
        register(LEVER, Blocks.LEVER);
        register(TRAPDOOR, AllBlocks.TRAIN_TRAPDOOR, AllBlocks.FRAMED_GLASS_TRAPDOOR);
        register(TRAPDOOR, BlockTags.WOODEN_TRAPDOORS);
        register(TRAPDOOR, BlockTags.FENCE_GATES);
    }
}
