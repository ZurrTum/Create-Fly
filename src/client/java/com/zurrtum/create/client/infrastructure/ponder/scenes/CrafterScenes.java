package com.zurrtum.create.client.infrastructure.ponder.scenes;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class CrafterScenes {

    public static void setup(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_crafter", "Setting up Mechanical Crafters");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().modifyKineticSpeed(util.select().everywhere(), f -> 1.5f * f);

        Selection redstone = util.select().fromTo(3, 1, 0, 3, 1, 1);
        Selection kinetics = util.select().fromTo(4, 1, 2, 4, 1, 5);
        BlockPos depotPos = util.grid().at(0, 1, 2);
        Selection crafters = util.select().fromTo(1, 1, 2, 3, 3, 2);

        scene.world().modifyBlocks(crafters, s -> s.with(MechanicalCrafterBlock.POINTING, Pointing.DOWN), false);
        scene.world().setKineticSpeed(crafters, 0);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                scene.world().showSection(util.select().position(y == 1 ? x + 1 : 3 - x, y + 1, 2), Direction.DOWN);
                scene.idle(2);
            }
        }

        scene.overlay().showText(70).text("An array of Mechanical Crafters can be used to automate any Crafting Recipe")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.WEST)).attachKeyFrame().placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.NORTH), Pointing.RIGHT, 40).rightClick()
            .withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().cycleBlockProperty(util.grid().at(2, 3, 2), MechanicalCrafterBlock.POINTING);
        scene.idle(10);
        scene.overlay().showText(50).text("Using a Wrench, the Crafters' paths can be arranged")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.NORTH)).attachKeyFrame().placeNearTarget();
        scene.idle(60);

        BlockPos[] positions = new BlockPos[]{util.grid().at(3, 1, 2), util.grid().at(2, 1, 2), util.grid().at(1, 1, 2)};

        for (BlockPos pos : positions) {
            scene.overlay().showControls(util.vector().blockSurface(pos, Direction.NORTH), Pointing.RIGHT, 10).rightClick()
                .withItem(AllItems.WRENCH.getDefaultStack());
            scene.idle(7);
            scene.world().cycleBlockProperty(pos, MechanicalCrafterBlock.POINTING);
            scene.idle(15);
        }

        scene.overlay().showText(100).text("For a valid setup, all paths have to converge into one exit at any side")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.WEST).add(0, 0, -.5f)).colored(PonderPalette.GREEN)
            .attachKeyFrame().placeNearTarget();
        scene.idle(60);

        Collection<Couple<BlockPos>> couples = ImmutableList.of(
            Couple.create(util.grid().at(3, 3, 2), util.grid().at(3, 2, 2)),
            Couple.create(util.grid().at(3, 2, 2), util.grid().at(3, 1, 2)),
            Couple.create(util.grid().at(2, 3, 2), util.grid().at(1, 3, 2)),
            Couple.create(util.grid().at(3, 1, 2), util.grid().at(2, 1, 2)),
            Couple.create(util.grid().at(1, 3, 2), util.grid().at(1, 2, 2)),
            Couple.create(util.grid().at(2, 2, 2), util.grid().at(2, 1, 2)),
            Couple.create(util.grid().at(1, 2, 2), util.grid().at(1, 1, 2)),
            Couple.create(util.grid().at(2, 1, 2), util.grid().at(1, 1, 2)),
            Couple.create(util.grid().at(1, 1, 2), util.grid().at(0, 1, 2))
        );

        for (Couple<BlockPos> c : couples) {
            scene.idle(5);
            Vec3d p1 = util.vector().blockSurface(c.getFirst(), Direction.NORTH).add(0, 0, -0.125);
            Vec3d p2 = util.vector().blockSurface(c.getSecond(), Direction.NORTH).add(0, 0, -0.125);
            Box point = new Box(p1, p1);
            Box line = new Box(p1, p2);
            scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, point, 2);
            scene.idle(1);
            scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, line, 30);
        }

        scene.world().showSection(util.select().position(depotPos), Direction.EAST);
        scene.idle(20);
        scene.overlay().showText(60).text("The outputs will be placed into the inventory at the exit")
            .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.NORTH)).placeNearTarget();
        scene.idle(70);

        scene.rotateCameraY(60);
        scene.idle(20);
        scene.world().showSection(kinetics, Direction.NORTH);
        scene.overlay().showText(60).text("Mechanical Crafters require Rotational Force to operate")
            .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.NORTH)).attachKeyFrame().placeNearTarget();
        scene.idle(8);
        scene.world().setKineticSpeed(crafters, -48);
        scene.world()
            .multiplyKineticSpeed(
                util.select().position(3, 2, 2).add(util.select().position(2, 3, 2)).add(util.select().position(1, 2, 2))
                    .add(util.select().position(2, 1, 2)), -1
            );
        scene.idle(55);
        scene.rotateCameraY(-60);

        scene.idle(40);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.NORTH), Pointing.RIGHT, 40).rightClick()
            .withItem(planks);
        scene.idle(7);
        Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
        scene.world().modifyBlockEntity(util.grid().at(1, 3, 2), type, mct -> mct.getInventory().insert(planks.copy()));

        scene.idle(10);
        scene.overlay().showText(50).text("Right-Click the front to insert Items manually")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.NORTH)).attachKeyFrame().placeNearTarget();
        scene.idle(60);

        ItemStack redstoneDust = new ItemStack(Items.REDSTONE);
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack cobble = new ItemStack(Items.COBBLESTONE);

        scene.world().setCraftingResult(util.grid().at(1, 1, 2), new ItemStack(Items.PISTON));

        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), type, mct -> mct.getInventory().insert(planks.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(3, 3, 2), type, mct -> mct.getInventory().insert(planks.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 2), type, mct -> mct.getInventory().insert(cobble.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(1, 2, 2), type, mct -> mct.getInventory().insert(cobble.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), type, mct -> mct.getInventory().insert(cobble.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), type, mct -> mct.getInventory().insert(redstoneDust.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(3, 1, 2), type, mct -> mct.getInventory().insert(cobble.copy()));

        scene.overlay().showText(80).attachKeyFrame().text("Once every slot of a path contains an Item, the crafting process will begin")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.WEST)).placeNearTarget();
        scene.idle(180);

        scene.world().removeItemsFromBelt(depotPos);

        ItemStack stick = new ItemStack(Items.STICK);

        scene.world().setCraftingResult(util.grid().at(1, 1, 2), new ItemStack(Items.IRON_PICKAXE));

        scene.world().modifyBlockEntity(util.grid().at(1, 3, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(2);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(2);
        scene.world().modifyBlockEntity(util.grid().at(3, 3, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(2);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 2), type, mct -> mct.getInventory().insert(stick.copy()));
        scene.idle(2);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), type, mct -> mct.getInventory().insert(stick.copy()));
        scene.world().showSection(redstone, Direction.SOUTH);
        scene.idle(10);

        scene.overlay().showText(90).attachKeyFrame().colored(PonderPalette.RED)
            .text("For recipes not fully occupying the crafter setup, the start can be forced using a Redstone Pulse")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.NORTH)).placeNearTarget();
        scene.idle(100);
        scene.effects().indicateRedstone(util.grid().at(3, 1, 0));
        scene.world().toggleRedstonePower(redstone);
        scene.idle(20);
        scene.world().toggleRedstonePower(redstone);
    }

    public static void connect(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_crafter_connect", "Connecting Inventories of Crafters");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                scene.world().showSection(util.select().position(y == 1 ? x + 1 : 2 - x, y + 1, 2), Direction.DOWN);
                scene.idle(2);
            }
        }

        Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
        BlockPos depotPos = util.grid().at(0, 1, 2);
        Selection funnel = util.select().fromTo(4, 1, 5, 4, 1, 2).add(util.select().fromTo(3, 2, 2, 3, 1, 2));
        Selection kinetics = util.select().position(3, 3, 2).add(util.select().fromTo(3, 3, 3, 3, 1, 3));
        scene.idle(5);

        scene.world().showSection(kinetics, Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(depotPos), Direction.EAST);
        scene.idle(10);
        scene.world().showSection(funnel, Direction.WEST);
        scene.rotateCameraY(60);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        scene.world().createItemOnBelt(util.grid().at(4, 1, 2), Direction.EAST, planks.copy());
        scene.idle(22);

        scene.world().modifyBlockEntity(util.grid().at(2, 2, 2), type, mct -> mct.getInventory().insert(planks.copy()));
        scene.world().removeItemsFromBelt(util.grid().at(3, 1, 2));
        scene.world().flapFunnel(util.grid().at(3, 2, 2), false);

        scene.overlay().showOutlineWithText(util.select().position(2, 2, 2), 70).attachKeyFrame().placeNearTarget()
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH)).text("Items can be inserted to Crafters automatically");
        scene.idle(80);

        scene.rotateCameraY(-60 - 90 - 30);
        scene.idle(40);

        Vec3d v = util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.WEST);
        Box bb = new Box(v, v).expand(.125f, .5, .5);
        v = v.add(0, 0, .5);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, new Object(), bb, 45);
        scene.overlay().showControls(v, Pointing.LEFT, 40).rightClick().withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().connectCrafterInvs(util.grid().at(2, 2, 2), util.grid().at(1, 2, 2));
        scene.idle(40);
        scene.overlay().showOutlineWithText(util.select().fromTo(2, 2, 2, 1, 2, 2), 70).attachKeyFrame().placeNearTarget().pointAt(v)
            .text("Using the Wrench at their backs, Mechanical Crafter inputs can be combined");
        scene.idle(80);
        scene.overlay().showControls(v.add(0, 1, 0), Pointing.LEFT, 20).rightClick().withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().connectCrafterInvs(util.grid().at(2, 3, 2), util.grid().at(1, 3, 2));
        scene.idle(20);
        scene.overlay().showControls(v.add(0, -1, 0), Pointing.LEFT, 20).rightClick().withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().connectCrafterInvs(util.grid().at(2, 1, 2), util.grid().at(1, 1, 2));
        scene.idle(20);
        scene.overlay().showControls(v.add(.5, -.5, 0), Pointing.LEFT, 20).rightClick().withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().connectCrafterInvs(util.grid().at(2, 1, 2), util.grid().at(2, 2, 2));
        scene.idle(10);
        scene.overlay().showControls(v.add(.5, .5, 0), Pointing.LEFT, 20).rightClick().withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().connectCrafterInvs(util.grid().at(2, 2, 2), util.grid().at(2, 3, 2));
        scene.idle(20);

        scene.rotateCameraY(90 + 30);
        scene.idle(40);
        scene.overlay().showOutlineWithText(util.select().fromTo(1, 1, 2, 2, 3, 2), 70).attachKeyFrame().placeNearTarget()
            .text("All connected Crafters can now be accessed by the same input location");
        scene.idle(60);
        scene.overlay().showControls(util.vector().centerOf(util.grid().at(4, 2, 2)), Pointing.DOWN, 40).withItem(planks);
        scene.idle(7);
        scene.world().createItemOnBelt(util.grid().at(4, 1, 2), Direction.EAST, planks.copyWithCount(16));
        scene.idle(22);

        scene.world().removeItemsFromBelt(util.grid().at(3, 1, 2));
        BlockPos[] positions = new BlockPos[]{util.grid().at(2, 3, 2), util.grid().at(1, 3, 2), util.grid().at(1, 2, 2), util.grid().at(
            2,
            1,
            2
        ), util.grid().at(1, 1, 2)};

        scene.world().setCraftingResult(util.grid().at(1, 1, 2), new ItemStack(Items.OAK_DOOR, 3));
        for (BlockPos pos : positions) {
            scene.world().modifyBlockEntity(pos, type, mct -> mct.getInventory().insert(planks.copy()));
            scene.idle(1);
        }

    }

    public static void covers(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_crafter_covers", "Covering slots of Mechanical Crafters");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.world().setBlock(util.grid().at(2, 2, 2), Blocks.AIR.getDefaultState(), false);

        Selection kinetics = util.select().fromTo(3, 1, 2, 3, 1, 5);
        scene.world().setKineticSpeed(util.select().fromTo(1, 2, 2, 3, 1, 2), 0);

        scene.world().showSection(util.select().position(3, 2, 2), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 2, 2), Direction.WEST);
        scene.idle(5);

        ItemStack iron = new ItemStack(Items.IRON_INGOT);

        Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(1, 2, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        scene.idle(5);

        Selection emptyCrafter = util.select().position(2, 2, 2);
        scene.overlay().showOutlineWithText(emptyCrafter, 90).attachKeyFrame().colored(PonderPalette.RED)
            .text("Some recipes will require additional Crafters to bridge gaps in the path").placeNearTarget();
        scene.idle(70);
        scene.world().restoreBlocks(emptyCrafter);
        scene.world().setCraftingResult(util.grid().at(2, 2, 2), new ItemStack(Items.BUCKET));
        scene.world().showSection(emptyCrafter, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.world().showSection(kinetics, Direction.NORTH);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().fromTo(3, 1, 2, 1, 2, 2), -32);
        scene.world().setKineticSpeed(util.select().position(3, 1, 2).add(emptyCrafter), 32);

        scene.idle(20);

        scene.overlay().showText(90).attachKeyFrame().colored(PonderPalette.GREEN)
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH))
            .text("Using Slot Covers, Crafters can be set to act as an Empty Slot in the arrangement").placeNearTarget();
        scene.idle(100);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH).add(0.5, 0, 0), Pointing.RIGHT, 50)
            .withItem(AllItems.CRAFTER_SLOT_COVER.getDefaultStack()).rightClick();
        scene.idle(7);
        scene.world().modifyBlockEntityNBT(emptyCrafter, type, compound -> compound.putBoolean("Cover", true));
        scene.idle(130);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST), Pointing.LEFT, 40)
            .withItem(new ItemStack(Items.BUCKET));
        scene.idle(50);
        scene.world().showSection(util.select().position(4, 2, 2), Direction.DOWN);

        scene.world().connectCrafterInvs(util.grid().at(3, 2, 2), util.grid().at(2, 2, 2));
        scene.idle(5);
        scene.world().connectCrafterInvs(util.grid().at(2, 1, 2), util.grid().at(2, 2, 2));
        scene.idle(5);
        scene.world().connectCrafterInvs(util.grid().at(1, 2, 2), util.grid().at(2, 2, 2));
        scene.idle(10);

        scene.overlay().showOutlineWithText(util.select().fromTo(3, 2, 2, 1, 2, 2).add(util.select().position(2, 1, 2)), 80).attachKeyFrame()
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH))
            .text("Shared Inputs created with the Wrench at the back can also reach across covered Crafters").placeNearTarget();
        scene.idle(60);

        ElementLink<EntityElement> ingot = scene.world().createItemEntity(util.vector().centerOf(4, 4, 2), util.vector().of(0, 0.2, 0), iron);
        scene.idle(17);
        scene.world().modifyEntity(ingot, Entity::discard);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        ingot = scene.world().createItemEntity(util.vector().centerOf(4, 4, 2), util.vector().of(0, 0.2, 0), iron);
        scene.idle(17);
        scene.world().modifyEntity(ingot, Entity::discard);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), type, mct -> mct.getInventory().insert(iron.copy()));
        ingot = scene.world().createItemEntity(util.vector().centerOf(4, 4, 2), util.vector().of(0, 0.2, 0), iron);
        scene.idle(17);
        scene.world().modifyEntity(ingot, Entity::discard);
        scene.world().modifyBlockEntity(util.grid().at(1, 2, 2), type, mct -> mct.getInventory().insert(iron.copy()));

    }

}