package com.zurrtum.create.client.infrastructure.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class MechanicalSawScenes {

    public static void processing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_saw_processing", "Processing Items on the Mechanical Saw");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos shaftPos = util.grid().at(2, 1, 3);
        scene.world().setBlock(shaftPos, AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, Axis.Z), false);

        BlockPos sawPos = util.grid().at(2, 1, 2);
        Selection sawSelect = util.select().position(sawPos);
        scene.world().modifyBlockEntityNBT(sawSelect, SawBlockEntity.class, nbt -> nbt.putInt("RecipeIndex", 0));

        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 5), Direction.DOWN);
        scene.idle(10);
        scene.effects().rotationDirectionIndicator(shaftPos);
        scene.world().showSection(sawSelect, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50).attachKeyFrame().text("Upward facing Mechanical Saws can process a variety of items")
            .pointAt(util.vector().blockSurface(sawPos, Direction.WEST)).placeNearTarget();
        scene.idle(45);

        ItemStack log = new ItemStack(Items.OAK_LOG);
        ItemStack strippedLog = new ItemStack(Items.STRIPPED_OAK_LOG);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);

        Vec3d itemSpawn = util.vector().centerOf(sawPos.up().west());
        ElementLink<EntityElement> logItem = scene.world().createItemEntity(itemSpawn, util.vector().of(0, 0, 0), log);
        scene.idle(12);

        scene.overlay().showControls(itemSpawn, Pointing.DOWN, 20).withItem(log);
        scene.idle(10);

        scene.world().modifyEntity(logItem, e -> e.setVelocity(util.vector().of(0.05, 0.2, 0)));
        scene.idle(12);

        scene.world().modifyEntity(logItem, Entity::discard);
        scene.world().createItemOnBeltLike(sawPos, Direction.WEST, log);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.remainingTime = inventory.recipeDuration = 50;
                inventory.appliedRecipe = false;
            }
        );
        scene.idle(34);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(0, ItemStack.EMPTY);
                inventory.setStack(1, strippedLog);
                inventory.remainingTime = inventory.recipeDuration = 20;
                inventory.appliedRecipe = true;
            }
        );
        scene.idle(16);

        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(1, ItemStack.EMPTY);
                inventory.remainingTime = -1;
            }
        );
        logItem = scene.world().createItemEntity(util.vector().topOf(sawPos).add(0.5, -.1, 0), util.vector().of(0.05, 0.18, 0), strippedLog);
        scene.idle(12);
        scene.overlay().showControls(itemSpawn.add(2, 0, 0), Pointing.DOWN, 20).withItem(strippedLog);
        scene.idle(30);

        scene.overlay().showText(60).attachKeyFrame().text("The processed item always moves against the rotational input to the saw")
            .pointAt(util.vector().blockSurface(sawPos, Direction.UP)).placeNearTarget();
        scene.idle(70);

        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -2 * f);
        scene.effects().rotationDirectionIndicator(shaftPos);
        scene.world().modifyEntity(logItem, e -> e.setVelocity(util.vector().of(-0.05, 0.2, 0)));
        scene.idle(12);

        scene.world().modifyEntity(logItem, Entity::discard);
        scene.world().createItemOnBeltLike(sawPos, Direction.EAST, strippedLog);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.remainingTime = inventory.recipeDuration = 50;
                inventory.appliedRecipe = false;
            }
        );
        scene.idle(17);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(0, ItemStack.EMPTY);
                inventory.setStack(1, planks);
                inventory.remainingTime = inventory.recipeDuration = 20;
                inventory.appliedRecipe = true;
            }
        );
        scene.idle(8);

        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(1, ItemStack.EMPTY);
                inventory.remainingTime = -1;
            }
        );
        logItem = scene.world().createItemEntity(util.vector().topOf(sawPos).add(-0.5, -.1, 0), util.vector().of(-0.05, 0.18, 0), planks);
        scene.idle(22);

        Selection otherBelt = util.select().fromTo(3, 1, 3, 4, 1, 2);
        Selection belt = util.select().fromTo(0, 1, 2, 1, 1, 3);

        scene.world().setKineticSpeed(otherBelt, 0);
        scene.world().setKineticSpeed(belt, 0);
        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);
        scene.world().modifyEntity(logItem, Entity::discard);
        scene.world().setBlock(shaftPos, AllBlocks.COGWHEEL.getDefaultState().with(ShaftBlock.AXIS, Axis.Z), true);
        scene.idle(3);
        scene.addKeyframe();

        scene.world().multiplyKineticSpeed(util.select().everywhere(), .5f);

        ElementLink<WorldSectionElement> beltSection = scene.world().showIndependentSection(belt, Direction.EAST);
        scene.world().moveSection(beltSection, util.vector().of(0, 100, 0), 0);
        scene.idle(1);
        scene.world().removeItemsFromBelt(util.grid().at(1, 1, 2));
        scene.idle(1);
        scene.world().setKineticSpeed(belt, -64);
        scene.idle(1);
        scene.world().moveSection(beltSection, util.vector().of(0, -100, 0), 0);
        scene.idle(3);

        ElementLink<WorldSectionElement> otherBeltSection = scene.world().showIndependentSection(otherBelt, Direction.WEST);
        scene.world().moveSection(otherBeltSection, util.vector().of(0, 100, 0), 0);
        scene.idle(1);
        scene.world().removeItemsFromBelt(util.grid().at(3, 1, 2));
        scene.idle(1);
        scene.world().setKineticSpeed(otherBelt, -64);
        scene.idle(1);
        scene.world().moveSection(otherBeltSection, util.vector().of(0, -100, 0), 0);
        scene.idle(3);

        ItemStack stone = new ItemStack(Blocks.STONE);
        BlockPos firstBelt = util.grid().at(0, 1, 2);
        scene.overlay().showText(60).text("Saws can work in-line with Mechanical Belts")
            .pointAt(util.vector().blockSurface(firstBelt, Direction.WEST)).placeNearTarget();
        scene.idle(40);
        scene.world().createItemOnBelt(firstBelt, Direction.WEST, stone);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.remainingTime = inventory.recipeDuration = 50;
                inventory.appliedRecipe = false;
            }
        );
        scene.idle(34);
        ItemStack bricks = new ItemStack(Blocks.STONE_BRICKS);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(0, ItemStack.EMPTY);
                inventory.setStack(1, bricks);
                inventory.remainingTime = inventory.recipeDuration = 20;
                inventory.appliedRecipe = true;
            }
        );
        Vec3d filter = util.vector().of(2.5, 1 + 13 / 16f, 2 + 5 / 16f);
        scene.overlay().showFilterSlotInput(filter, Direction.UP, 80);
        scene.overlay().showText(80).attachKeyFrame().text("When an ingredient has multiple possible outcomes, the filter slot can specify it")
            .pointAt(filter).placeNearTarget();
        scene.idle(15);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(1, ItemStack.EMPTY);
                DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(saw.getWorld(), util.grid().at(3, 1, 2), DirectBeltInputBehaviour.TYPE);
                behaviour.handleInsertion(bricks, Direction.EAST, false);
            }
        );
        scene.idle(75);

        scene.overlay().showControls(filter, Pointing.DOWN, 30).withItem(bricks);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.idle(7);
        scene.world().setFilterData(util.select().position(sawPos), SawBlockEntity.class, bricks);
        scene.idle(10);
        scene.world().createItemOnBelt(firstBelt, Direction.WEST, stone);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.remainingTime = inventory.recipeDuration = 50;
                inventory.appliedRecipe = false;
            }
        );
        scene.idle(34);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(0, ItemStack.EMPTY);
                inventory.setStack(1, bricks);
                inventory.remainingTime = inventory.recipeDuration = 20;
                inventory.appliedRecipe = true;
            }
        );
        scene.idle(15);
        scene.world().modifyBlockEntity(
            sawPos, SawBlockEntity.class, saw -> {
                ProcessingInventory inventory = saw.inventory;
                inventory.setStack(1, ItemStack.EMPTY);
                DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(saw.getWorld(), util.grid().at(3, 1, 2), DirectBeltInputBehaviour.TYPE);
                behaviour.handleInsertion(bricks, Direction.EAST, false);
            }
        );

        scene.markAsFinished();
        scene.overlay().showText(100).text("Without filter, the Saw would cycle through all outcomes instead").colored(PonderPalette.RED)
            .pointAt(filter).placeNearTarget();
        scene.idle(65);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
    }

    public static void treeCutting(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_saw_breaker", "Cutting Trees with the Mechanical Saw");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.9f);
        scene.world().setBlock(util.grid().at(2, 0, 2), Blocks.GRASS_BLOCK.getDefaultState(), false);
        scene.world().showSection(util.select().layer(0).add(util.select().position(3, 1, 1)).add(util.select().position(1, 1, 2)), Direction.UP);

        scene.world().setKineticSpeed(util.select().position(5, 0, 1), -8);
        scene.world().setKineticSpeed(util.select().fromTo(3, 1, 2, 5, 1, 2), 16);

        scene.idle(5);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(3, 1, 2), Direction.DOWN);

        scene.idle(20);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 3, 2), Direction.UP);
        scene.world().showSection(util.select().layersFrom(4), Direction.UP);

        BlockPos breakingPos = util.grid().at(2, 1, 2);
        scene.idle(5);
        for (int i = 0; i < 10; i++) {
            scene.idle(10);
            scene.world().incrementBlockBreakingProgress(breakingPos);
            if (i == 1) {
                scene.overlay().showText(80).attachKeyFrame().placeNearTarget().pointAt(util.vector().blockSurface(breakingPos, Direction.WEST))
                    .text("When given Rotational Force, a Mechanical Saw will cut trees directly in front of it");
            }
        }

        scene.world().replaceBlocks(util.select().fromTo(2, 2, 2, 2, 6, 2), Blocks.AIR.getDefaultState(), true);

        scene.world().destroyBlock(util.grid().at(3, 5, 0));
        scene.world().destroyBlock(util.grid().at(0, 4, 1));
        scene.world().destroyBlock(util.grid().at(2, 6, 1));
        scene.world().destroyBlock(util.grid().at(1, 4, 0));
        scene.world().destroyBlock(util.grid().at(1, 6, 2));
        scene.world().destroyBlock(util.grid().at(1, 5, 3));
        scene.world().destroyBlock(util.grid().at(0, 4, 3));

        scene.world().replaceBlocks(util.select().layersFrom(4), Blocks.AIR.getDefaultState(), false);

        for (int i = 0; i < 5; i++) {
            Vec3d dropPos = util.vector().centerOf(breakingPos.up(i));
            float distance = (float) dropPos.distanceTo(util.vector().centerOf(breakingPos));
            scene.world().createItemEntity(dropPos, util.vector().of(-distance / 20, 0, 0), new ItemStack(Items.OAK_LOG));
        }

        scene.idle(35);
        scene.world().destroyBlock(util.grid().at(1, 1, 2));
        scene.world().hideSection(util.select().layersFrom(2).add(util.select().fromTo(2, 1, 2, 1, 1, 3)), Direction.UP);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.idle(15);
        scene.world().setBlocks(util.select().fromTo(2, 1, 2, 1, 20, 3), Blocks.JUNGLE_LOG.getDefaultState(), false);
        scene.world().showSection(util.select().layersFrom(2).add(util.select().fromTo(2, 1, 2, 1, 1, 3)), Direction.UP);
        scene.idle(15);

        scene.world().hideSection(util.select().fromTo(2, 1, 2, 1, 1, 3).substract(util.select().position(breakingPos)), Direction.WEST);
        scene.idle(10);
        scene.overlay().showOutlineWithText(util.select().position(breakingPos), 90).attachKeyFrame().colored(PonderPalette.GREEN).placeNearTarget()
            .text("In order to cut the tree fully, the Saw has to break the last block connecting it to the ground");

        scene.idle(25);
        for (int i = 0; i < 10; i++) {
            scene.idle(10);
            scene.world().incrementBlockBreakingProgress(breakingPos);
        }

        for (int i = 0; i < 30; i++) {
            scene.world().replaceBlocks(util.select().fromTo(2, i + 1, 2, 1, i + 1, 3), Blocks.AIR.getDefaultState(), true);
            for (int x = 1; x <= 2; x++) {
                for (int z = 2; z <= 3; z++) {
                    Vec3d dropPos = util.vector().centerOf(x, i + 1, z);
                    float distance = (float) dropPos.distanceTo(util.vector().centerOf(breakingPos));
                    scene.world().createItemEntity(dropPos, util.vector().of(-distance / 20, 0, 0), new ItemStack(Items.JUNGLE_LOG));
                }
            }
            scene.idle(1);
        }
    }

    public static void contraption(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_saw_contraption", "Using Mechanical Saws on Contraptions");
        scene.configureBasePlate(1, 0, 6);
        scene.scaleSceneView(.9f);
        scene.world().setBlock(util.grid().at(2, 0, 3), Blocks.GRASS_BLOCK.getDefaultState(), false);
        scene.world().showSection(
            util.select().layer(0).add(util.select().position(3, 1, 1)).add(util.select().position(1, 1, 2)).add(util.select().position(2, 1, 4)),
            Direction.UP
        );

        Selection kinetics = util.select().fromTo(6, 1, 2, 6, 1, 6);

        scene.idle(5);
        ElementLink<WorldSectionElement> pistonHead = scene.world().showIndependentSection(util.select().fromTo(6, 1, 1, 8, 1, 1), Direction.DOWN);
        scene.world().moveSection(pistonHead, util.vector().of(0, 0, 1), 0);
        scene.world().showSection(kinetics, Direction.DOWN);
        scene.idle(5);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(util.select().fromTo(5, 1, 3, 5, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(4, 1, 3), Direction.EAST, contraption);
        scene.idle(5);
        scene.world().showSectionAndMerge(util.select().position(4, 1, 2), Direction.EAST, contraption);
        scene.idle(5);

        scene.overlay().showText(60).attachKeyFrame().placeNearTarget().pointAt(util.vector().topOf(util.grid().at(4, 1, 3)))
            .text("Whenever Saws are moved as part of an animated Contraption...");
        scene.idle(70);

        Selection saws = util.select().fromTo(4, 1, 2, 4, 1, 3);

        Selection tree = util.select().fromTo(2, 1, 3, 2, 7, 3).add(util.select().layersFrom(3));
        scene.world().showSection(tree, Direction.UP);
        scene.world().setKineticSpeed(util.select().position(5, 0, 6), -8);
        scene.world().setKineticSpeed(kinetics, 16);
        scene.world().setKineticSpeed(saws, 16);
        scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
        scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
        scene.idle(20);

        BlockPos breakingPos = util.grid().at(2, 1, 3);

        for (int i = 0; i < 10; i++) {
            scene.idle(3);
            scene.world().incrementBlockBreakingProgress(breakingPos);
            if (i == 2) {
                scene.overlay().showText(80).placeNearTarget().pointAt(util.vector().blockSurface(breakingPos, Direction.WEST))
                    .text("...they will cut any trees the contraption runs them into");
            }
        }

        scene.world().replaceBlocks(util.select().fromTo(2, 2, 3, 2, 6, 3), Blocks.AIR.getDefaultState(), true);
        scene.world().destroyBlock(util.grid().at(4, 5, 1));
        scene.world().destroyBlock(util.grid().at(1, 4, 2));
        scene.world().destroyBlock(util.grid().at(3, 6, 2));
        scene.world().destroyBlock(util.grid().at(2, 4, 1));
        scene.world().destroyBlock(util.grid().at(2, 6, 3));
        scene.world().destroyBlock(util.grid().at(2, 5, 2));
        scene.world().destroyBlock(util.grid().at(1, 4, 2));
        scene.world().replaceBlocks(util.select().layersFrom(4), Blocks.AIR.getDefaultState(), false);

        for (int i = 0; i < 5; i++) {
            Vec3d dropPos = util.vector().centerOf(breakingPos.up(i));
            float distance = (float) dropPos.distanceTo(util.vector().centerOf(breakingPos));
            scene.world().createItemEntity(dropPos, util.vector().of(-distance / 20, 0, 0), new ItemStack(Items.OAK_LOG));
        }

        scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
        scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
        scene.idle(20);
        scene.world().setKineticSpeed(saws, 0);
        scene.idle(20);

        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);
        scene.world().moveSection(pistonHead, util.vector().of(2, 0, 0), 40);
        scene.world().moveSection(contraption, util.vector().of(2, 0, 0), 40);
        scene.world().hideSection(tree, Direction.UP);
        scene.idle(40);

        scene.world().restoreBlocks(tree);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.world().glueBlockOnto(util.grid().at(5, 2, 2), Direction.DOWN, contraption);

        scene.overlay().showText(60).attachKeyFrame().placeNearTarget().pointAt(util.vector().blockSurface(util.grid().at(5, 2, 2), Direction.WEST))
            .sharedText("storage_on_contraption");
        scene.idle(70);

        scene.world().showSection(tree, Direction.DOWN);
        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> -f);
        scene.world().setKineticSpeed(saws, 16);
        scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
        scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
        scene.idle(20);

        for (int i = 0; i < 10; i++) {
            scene.idle(3);
            scene.world().incrementBlockBreakingProgress(breakingPos);
        }

        scene.world().replaceBlocks(util.select().fromTo(2, 2, 3, 2, 6, 3), Blocks.AIR.getDefaultState(), true);
        scene.world().destroyBlock(util.grid().at(4, 5, 1));
        scene.world().destroyBlock(util.grid().at(1, 4, 2));
        scene.world().destroyBlock(util.grid().at(3, 6, 2));
        scene.world().destroyBlock(util.grid().at(2, 4, 1));
        scene.world().destroyBlock(util.grid().at(2, 6, 3));
        scene.world().destroyBlock(util.grid().at(2, 5, 2));
        scene.world().destroyBlock(util.grid().at(1, 4, 2));
        scene.world().replaceBlocks(util.select().layersFrom(4), Blocks.AIR.getDefaultState(), false);

        scene.world().moveSection(pistonHead, util.vector().of(-1, 0, 0), 20);
        scene.world().moveSection(contraption, util.vector().of(-1, 0, 0), 20);
        scene.idle(20);
        scene.world().setKineticSpeed(saws, 0);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(3, 2, 2), Pointing.DOWN, 60).withItem(new ItemStack(Blocks.OAK_LOG));
        scene.idle(20);
    }

}