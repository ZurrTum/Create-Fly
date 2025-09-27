package com.zurrtum.create.client.infrastructure.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonHeadBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.PistonType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PistonScenes {

    public static void movement(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_piston", "Moving Structures using Mechanical Pistons");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0).add(util.select().position(0, 1, 2)), Direction.UP);

        Selection kinetics = util.select().fromTo(3, 1, 3, 3, 1, 2);
        BlockPos piston = util.grid().at(3, 1, 2);
        BlockPos leverPos = util.grid().at(3, 2, 4);
        BlockPos shaft = util.grid().at(3, 1, 3);

        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(piston), Direction.DOWN);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(util.select().position(3, 1, 1), Direction.DOWN);
        scene.world().moveSection(contraption, util.vector().of(0, 0, 1), 0);
        scene.idle(20);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east()), Direction.DOWN, contraption);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east(2)), Direction.DOWN, contraption);
        scene.world().showSectionAndMerge(util.select().position(piston.north().west()), Direction.DOWN, contraption);
        scene.idle(15);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);
        scene.overlay().showText(55).pointAt(util.vector().topOf(piston)).placeNearTarget().attachKeyFrame()
            .text("Mechanical Pistons can move blocks in front of them");
        scene.idle(65);

        scene.overlay().showText(45).pointAt(util.vector().blockSurface(shaft, Direction.SOUTH)).placeNearTarget()
            .text("Speed and direction of movement depend on the Rotational Input");
        scene.world().setBlock(util.grid().at(2, 1, 1), Blocks.AIR.getDefaultState(), false);
        scene.world().setBlock(util.grid().at(0, 1, 2), Blocks.OAK_PLANKS.getDefaultState(), false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(2, 0, 0), 40);
        scene.idle(60);

        scene.overlay().showControls(util.vector().blockSurface(piston, Direction.WEST), Pointing.DOWN, 30).rightClick()
            .withItem(new ItemStack(Items.SLIME_BALL));
        scene.idle(7);
        scene.world().modifyBlock(piston.north(), s -> s.with(MechanicalPistonHeadBlock.TYPE, PistonType.STICKY), false);
        scene.effects().superGlue(piston, Direction.WEST, true);

        scene.idle(33);
        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);

        scene.idle(25);
        scene.overlay().showText(60).pointAt(util.vector().topOf(piston)).placeNearTarget().attachKeyFrame()
            .text("Sticky Mechanical Pistons can pull the attached blocks back");
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 1, 1), Blocks.OAK_PLANKS.getDefaultState(), false);
        scene.world().setBlock(util.grid().at(0, 1, 2), Blocks.AIR.getDefaultState(), false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(2, 0, 0), 40);

        scene.idle(50);
        scene.world().setBlock(util.grid().at(2, 1, 1), Blocks.AIR.getDefaultState(), false);

        scene.world().replaceBlocks(util.select().fromTo(2, 3, 2, 2, 2, 0), Blocks.OAK_PLANKS.getDefaultState(), false);
        scene.overlay().showOutline(
            PonderPalette.GREEN,
            "glue",
            util.select().fromTo(2, 2, 3, 2, 1, 3).add(util.select().fromTo(2, 1, 3, 2, 1, 1)).add(util.select().position(1, 1, 1)),
            40
        );
        scene.overlay().showControls(util.vector().centerOf(util.grid().at(2, 2, 0)), Pointing.RIGHT, 40)
            .withItem(AllItems.SUPER_GLUE.getDefaultStack());

        ElementLink<WorldSectionElement> chassis = scene.world().showIndependentSection(util.select().fromTo(2, 2, 0, 2, 3, 2), Direction.DOWN);
        scene.world().moveSection(chassis, util.vector().of(0, -1, 1), 0);
        scene.addKeyframe();
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(1, 2, 0), Direction.EAST, chassis);
        scene.idle(15);
        scene.effects().superGlue(piston.west().north(), Direction.WEST, true);
        scene.overlay().showText(80).pointAt(util.vector().topOf(piston.west())).placeNearTarget().sharedText("movement_anchors");

        scene.idle(90);
        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);
        scene.world().moveSection(chassis, util.vector().of(-2, 0, 0), 40);
    }

    public static void poles(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("piston_pole", "Piston Extension Poles");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);

        Selection kinetics = util.select().fromTo(3, 1, 3, 3, 1, 2);
        BlockPos piston = util.grid().at(3, 1, 2);

        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(piston), Direction.DOWN);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(util.select().position(3, 1, 1), Direction.DOWN);
        scene.world().moveSection(contraption, util.vector().of(0, 0, 1), 0);
        scene.idle(20);

        BlockPos leverPos = util.grid().at(3, 2, 4);
        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().setKineticSpeed(kinetics, 16);
        scene.idle(10);

        scene.overlay().showOutlineWithText(util.select().position(piston), 50).colored(PonderPalette.RED).placeNearTarget().attachKeyFrame()
            .text("Without attached Poles, a Mechanical Piston cannot move");
        scene.idle(60);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().setKineticSpeed(kinetics, 0);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east()), Direction.DOWN, contraption);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east(2)), Direction.DOWN, contraption);
        scene.idle(10);

        scene.overlay().showOutline(PonderPalette.RED, new Object(), util.select().fromTo(piston.east(), piston.east(2)), 100);
        scene.overlay().showOutlineWithText(util.select().fromTo(piston.west(), piston.west(2)), 100)
            .text("The Length of pole added at its back determines the Extension Range").attachKeyFrame().placeNearTarget()
            .colored(PonderPalette.GREEN);
        scene.idle(110);

        scene.world().showSectionAndMerge(util.select().position(piston.north().west()), Direction.EAST, contraption);
        scene.idle(10);
        ElementLink<ParrotElement> birb = scene.special().createBirb(util.vector().topOf(piston.west()), ParrotPose.FaceCursorPose::new);
        scene.idle(15);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().setKineticSpeed(kinetics, 16);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);
        scene.special().moveParrot(birb, util.vector().of(-2, 0, 0), 40);

    }

    public static void movementModes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_piston_modes", "Movement Modes of the Mechanical Piston");
        scene.configureBasePlate(0, 0, 5);
        Selection rose = util.select().fromTo(0, 2, 2, 0, 1, 2);
        scene.world().showSection(util.select().layer(0).add(rose), Direction.UP);

        Selection kinetics = util.select().fromTo(3, 1, 3, 3, 1, 2);
        BlockPos piston = util.grid().at(3, 1, 2);
        BlockPos leverPos = util.grid().at(3, 2, 4);
        BlockPos shaft = util.grid().at(3, 1, 3);

        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 2, 5), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(piston), Direction.DOWN);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(util.select().position(3, 1, 1), Direction.DOWN);
        scene.world().moveSection(contraption, util.vector().of(0, 0, 1), 0);
        scene.idle(20);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east()), Direction.DOWN, contraption);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(piston.north().east(2)), Direction.DOWN, contraption);
        scene.world().showSectionAndMerge(util.select().position(piston.north().west()), Direction.DOWN, contraption);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(piston.north().west().up()), Direction.DOWN, contraption);
        scene.idle(15);
        scene.effects().superGlue(piston.west(), Direction.UP, true);
        scene.idle(10);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);
        scene.idle(40);

        scene.world().destroyBlock(util.grid().at(0, 1, 2));
        scene.world().destroyBlock(util.grid().at(0, 2, 2));
        scene.idle(10);
        scene.overlay().showOutlineWithText(rose, 70).text("Whenever Pistons stop moving, the moved structure reverts to blocks").attachKeyFrame()
            .colored(PonderPalette.RED);
        scene.idle(80);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(2, 0, 0), 40);
        scene.world().hideSection(rose, Direction.UP);
        scene.idle(50);

        scene.world().setBlock(util.grid().at(0, 1, 2), Blocks.ROSE_BUSH.getDefaultState(), false);
        scene.world().setBlock(util.grid().at(0, 2, 2), Blocks.ROSE_BUSH.getDefaultState().with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), false);
        scene.world().showIndependentSection(rose, Direction.DOWN);
        Vec3d filter = util.vector().topOf(piston).add(.125, 0, 0);
        scene.overlay().showFilterSlotInput(filter, Direction.UP, 60);
        scene.overlay().showControls(filter.add(0, .125, 0), Pointing.DOWN, 60).rightClick();
        scene.overlay().showText(70).pointAt(filter.add(-.125, 0, 0)).placeNearTarget().attachKeyFrame().sharedText("behaviour_modify_value_panel");
        scene.idle(80);

        scene.effects().indicateRedstone(leverPos);
        scene.world().toggleRedstonePower(util.select().fromTo(leverPos, leverPos.down()));
        scene.world().modifyKineticSpeed(kinetics, f -> -f);
        scene.effects().rotationDirectionIndicator(shaft);
        scene.world().moveSection(contraption, util.vector().of(-2, 0, 0), 40);
        scene.idle(50);
        scene.overlay().showText(120).colored(PonderPalette.GREEN).pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.WEST))
            .placeNearTarget().text("It can be configured never to revert to solid blocks, or only at the location it started at");

    }

}