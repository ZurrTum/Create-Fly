package com.zurrtum.create.client.infrastructure.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChuteScenes {

    public static void downward(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chute", "Transporting Items downward via Chutes");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.9f);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        ElementLink<WorldSectionElement> top = scene.world().showIndependentSection(util.select().fromTo(3, 3, 3, 3, 4, 3), Direction.DOWN);
        ElementLink<WorldSectionElement> bottom = scene.world().showIndependentSection(util.select().fromTo(3, 2, 3, 3, 1, 3), Direction.DOWN);
        scene.world().moveSection(bottom, util.vector().of(-2, 0, -1), 0);
        scene.world().moveSection(top, util.vector().of(0, 0, -1), 0);
        scene.idle(20);

        ItemStack stack = new ItemStack(Items.COPPER_BLOCK);
        scene.world().createItemEntity(util.vector().centerOf(util.grid().at(3, 3, 2)), util.vector().of(0, -0.1, 0), stack);
        scene.idle(20);
        ElementLink<EntityElement> remove = scene.world()
            .createItemEntity(util.vector().centerOf(util.grid().at(1, 5, 2)), util.vector().of(0, 0.1, 0), stack);
        scene.idle(15);
        scene.world().modifyEntity(remove, Entity::discard);

        scene.overlay().showText(60).attachKeyFrame().pointAt(util.vector().topOf(util.grid().at(1, 2, 2))).placeNearTarget()
            .text("Chutes can transport items vertically from and to inventories");
        scene.idle(70);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.world().moveSection(bottom, util.vector().of(1, 0, 0), 10);
        scene.world().moveSection(top, util.vector().of(-1, 0, 0), 10);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.NORTH), Pointing.RIGHT, 40).rightClick()
            .withItem(AllItems.WRENCH.getDefaultStack());
        scene.idle(7);
        scene.world().modifyBlock(util.grid().at(3, 3, 3), s -> s.with(ChuteBlock.SHAPE, Shape.WINDOW), false);
        scene.overlay().showText(50).attachKeyFrame().pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST)).placeNearTarget()
            .text("Using the Wrench, a window can be created");

        scene.idle(60);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH), Pointing.RIGHT, 40).rightClick()
            .withItem(AllItems.INDUSTRIAL_IRON_BLOCK.getDefaultStack());
        scene.idle(7);
        scene.world().modifyBlock(util.grid().at(3, 2, 3), s -> s.with(ChuteBlock.SHAPE, Shape.ENCASED), false);
        scene.overlay().showText(50).pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.WEST)).placeNearTarget()
            .text("Using Industrial Iron Blocks, chutes can be encased");

        scene.idle(10);

        for (int i = 0; i < 8; i++) {
            scene.idle(10);
            scene.world().createItemOnBeltLike(util.grid().at(3, 3, 3), Direction.UP, stack);
        }
        scene.idle(20);
        scene.world().hideIndependentSection(bottom, Direction.EAST);
        scene.world().hideIndependentSection(top, Direction.EAST);
        scene.idle(15);
        scene.addKeyframe();

        scene.rotateCameraY(-90);
        scene.world().modifyBlock(util.grid().at(2, 2, 1), s -> s.with(ChuteBlock.SHAPE, Shape.NORMAL), false);
        scene.world().modifyBlock(util.grid().at(2, 3, 2), s -> s.with(ChuteBlock.SHAPE, Shape.INTERSECTION), false);
        scene.world().showSection(util.select().fromTo(2, 1, 1, 2, 2, 1), Direction.DOWN);
        scene.idle(30);
        ItemStack chuteItem = AllItems.CHUTE.getDefaultStack();
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 2, 1), Direction.SOUTH), Pointing.LEFT, 30).rightClick()
            .withItem(chuteItem);
        scene.idle(7);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.NORTH);
        scene.world().restoreBlocks(util.select().position(2, 2, 1));
        scene.idle(15);
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.SOUTH), Pointing.LEFT, 30).rightClick()
            .withItem(chuteItem);
        scene.idle(7);
        scene.world().showSection(util.select().position(2, 4, 3), Direction.NORTH);
        scene.idle(10);
        scene.world().restoreBlocks(util.select().position(2, 3, 2));
        scene.idle(25);

        scene.overlay().showText(70).attachKeyFrame().pointAt(util.vector().blockSurface(util.grid().at(2, 4, 3), Direction.WEST)).placeNearTarget()
            .text("Placing chutes targeting the side faces of another will make it diagonal");
        scene.idle(15);
        scene.rotateCameraY(90);

        scene.idle(35);

        Direction offset = Direction.NORTH;
        for (int i = 0; i < 3; i++) {
            remove = scene.world().createItemEntity(
                util.vector().centerOf(util.grid().at(2, 6, 3).offset(offset)),
                util.vector().of(0, 0.1, 0).add(Vec3d.of(offset.getVector()).multiply(-.1)),
                stack
            );
            scene.idle(12);
            scene.world().createItemOnBeltLike(util.grid().at(2, 4, 3), Direction.UP, stack);
            scene.world().modifyEntity(remove, Entity::discard);
            scene.idle(3);
            offset = offset.rotateYClockwise();
        }

        scene.idle(10);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH), Pointing.RIGHT, 50).withItem(stack);
        scene.markAsFinished();
    }

    public static void upward(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chute_upward", "Transporting Items upward via Chutes");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.9f);
        scene.showBasePlate();
        Selection chute = util.select().fromTo(1, 2, 2, 1, 4, 2);
        scene.world().setBlocks(chute, Blocks.AIR.getDefaultState(), false);
        scene.world().showSection(util.select().position(1, 1, 2), Direction.UP);
        scene.idle(20);

        scene.world().restoreBlocks(chute);
        scene.world().showSection(chute, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(1, 1, 2), 0);
        Vec3d surface = util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.WEST);
        scene.overlay().showText(70).text("Using Encased Fans at the top or bottom, a Chute can move items upward").attachKeyFrame().pointAt(surface)
            .placeNearTarget();
        scene.idle(80);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.NORTH), Pointing.RIGHT, 50)
            .withItem(AllItems.GOGGLES.getDefaultStack());
        scene.overlay().showText(70).text("Inspecting chutes with Engineers' Goggles reveals information about the movement direction")
            .attachKeyFrame().pointAt(surface).placeNearTarget();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(2, 2, 2, 4, 1, 5).add(util.select().position(3, 0, 5)), Direction.DOWN);
        ItemStack stack = new ItemStack(Items.COPPER_BLOCK);
        scene.world().createItemOnBelt(util.grid().at(4, 1, 2), Direction.EAST, stack);
        scene.idle(10);
        scene.rotateCameraY(60);
        scene.overlay().showText(70).text("On the 'blocked' end, items will have to be inserted/taken from the sides").attachKeyFrame()
            .pointAt(util.vector().centerOf(util.grid().at(3, 1, 2)).add(0, 3 / 16f, 0)).placeNearTarget();
        scene.idle(32);
        scene.world().flapFunnel(util.grid().at(2, 2, 2), false);
        scene.world().removeItemsFromBelt(util.grid().at(2, 1, 2));
        scene.world().createItemOnBeltLike(util.grid().at(1, 2, 2), Direction.EAST, stack);
    }

    public static void smart(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("smart_chute", "Filtering Items using Smart Chutes");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.9f);

        Selection lever = util.select().fromTo(0, 1, 2, 1, 3, 2);
        BlockPos smarty = util.grid().at(2, 3, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);

        scene.overlay().showText(60).text("Smart Chutes are vertical chutes with additional control").attachKeyFrame()
            .pointAt(util.vector().blockSurface(smarty, Direction.WEST)).placeNearTarget();
        scene.idle(70);

        Vec3d filter = util.vector().blockSurface(smarty, Direction.NORTH).add(0, 3 / 16f, 0);
        scene.overlay().showFilterSlotInput(filter, Direction.NORTH, 70);
        scene.idle(10);
        scene.rotateCameraY(20);
        scene.overlay().showText(60).text("Items in the filter slot specify what to extract or transfer").attachKeyFrame()
            .pointAt(filter.add(0, 0, 0.125)).placeNearTarget();
        scene.idle(60);

        scene.world().showSection(util.select().position(2, 4, 2), Direction.DOWN);
        scene.idle(15);

        ItemStack copper = new ItemStack(Items.IRON_INGOT);
        scene.overlay().showControls(filter.add(0, 0.125, 0), Pointing.DOWN, 40).rightClick().withItem(copper);
        scene.idle(7);
        scene.world().setFilterData(util.select().position(smarty), SmartChuteBlockEntity.class, copper);

        for (int i = 0; i < 18; i++) {
            scene.idle(10);
            scene.world().createItemOnBeltLike(util.grid().at(2, 2, 2), Direction.UP, copper);
            if (i == 8) {
                scene.rotateCameraY(-20);
                scene.overlay().showControls(filter.add(0, 0.125, 0), Pointing.DOWN, 40).rightClick();
                scene.overlay().showText(50).text("Use the value panel to specify the extracted stack size").attachKeyFrame()
                    .pointAt(filter.add(0, 0, 0.125)).placeNearTarget();
            }
            if (i == 13)
                scene.world().showSection(lever, Direction.NORTH);
        }

        scene.world().toggleRedstonePower(lever.add(util.select().position(smarty)));
        scene.effects().indicateRedstone(util.grid().at(0, 3, 2));
        scene.overlay().showText(50).text("Redstone power will prevent Smart Chutes from acting.").attachKeyFrame().colored(PonderPalette.RED)
            .pointAt(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.UP)).placeNearTarget();
        scene.idle(70);

        scene.world().toggleRedstonePower(lever.add(util.select().position(smarty)));
        scene.markAsFinished();
        for (int i = 0; i < 8; i++) {
            scene.idle(10);
            scene.world().createItemOnBeltLike(util.grid().at(2, 2, 2), Direction.UP, copper);
        }

    }

}