package com.zurrtum.create.client.ponder.foundation.content;

import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class DebugScenes {

    private static int index;

    public static void registerAll(PonderSceneRegistrationHelper<Identifier> helper) {
        index = 1;
        add(helper, DebugScenes::coordinateScene);
        add(helper, DebugScenes::blocksScene);
        add(helper, DebugScenes::fluidsScene);
        add(helper, DebugScenes::offScreenScene);
        add(helper, DebugScenes::particleScene);
        add(helper, DebugScenes::controlsScene);
        add(helper, DebugScenes::birbScene);
        add(helper, DebugScenes::sectionsScene);
        //add(DebugScenes::itemScene);
    }

    private static void add(PonderSceneRegistrationHelper<Identifier> helper, PonderStoryBoard sb) {
        String schematicPath = "debug/scene_" + index;
        helper.addStoryBoard(Identifier.of("spyglass"), schematicPath, sb).highlightAllTags();
        index++;
    }

    public static void empty(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_empty", "Missing Content");
        scene.showBasePlate();
        scene.idle(5);
    }

    public static void coordinateScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_coords", "Coordinate Space");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);

        Selection xAxis = util.select().fromTo(2, 1, 1, 4, 1, 1);
        Selection yAxis = util.select().fromTo(1, 2, 1, 1, 4, 1);
        Selection zAxis = util.select().fromTo(1, 1, 2, 1, 1, 4);

        scene.idle(10);
        scene.overlay().showOutlineWithText(xAxis, 20).colored(PonderPalette.RED).text("Das X axis");
        scene.idle(20);
        scene.overlay().showOutlineWithText(yAxis, 20).colored(PonderPalette.GREEN).text("Das Y axis");
        scene.idle(20);
        scene.overlay().showOutlineWithText(zAxis, 20).colored(PonderPalette.BLUE).text("Das Z axis");
    }

    public static void blocksScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_blocks", "Changing Blocks");
        scene.showBasePlate();
        scene.scaleSceneView(0.75f);
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(1000).independent(10).text("Blocks can be modified");
        scene.idle(20);
        scene.world().replaceBlocks(util.select().fromTo(1, 1, 3, 2, 2, 4), Blocks.WHITE_CONCRETE.getDefaultState(), true);
        scene.idle(10);
        scene.addKeyframe();
        scene.world().replaceBlocks(util.select().position(3, 1, 1), Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.POWER, 15), true);
        scene.rotateCameraY(180);

        for (int i = 0; i < 20; i++) {
            scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 1));
            scene.idle(10);
        }

        scene.markAsFinished();
    }

    public static void fluidsScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_fluids", "Showing Fluids");
        scene.showBasePlate();
        scene.idle(10);
        Vec3d parrotPos = util.vector().topOf(1, 0, 1);
        scene.special().createBirb(parrotPos, ParrotPose.FacePointOfInterestPose::new);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.overlay().showText(1000).text("Fluid rendering test.").pointAt(new Vec3d(1, 2.5, 4.5));
        scene.markAsFinished();

        Object outlineSlot = new Object();

        Vec3d vec1 = util.vector().topOf(1, 0, 0);
        Vec3d vec2 = util.vector().topOf(0, 0, 1);
        Box boundingBox1 = new Box(vec1, vec1).stretch(0, 2.5, 0).expand(.15, 0, .15);
        Box boundingBox2 = new Box(vec2, vec2).stretch(0, .125, 0).expand(.45, 0, .45);
        Vec3d poi1 = boundingBox1.getCenter();
        Vec3d poi2 = boundingBox2.getCenter();

        for (int i = 0; i < 10; i++) {
            scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, outlineSlot, i % 2 == 0 ? boundingBox1 : boundingBox2, 15);
            scene.idle(3);
            scene.special().movePointOfInterest(i % 2 == 0 ? poi1 : poi2);
            scene.idle(12);
        }

        scene.idle(12);
        scene.special().movePointOfInterest(util.grid().at(-4, 5, 4));
        scene.overlay().showText(40).colored(PonderPalette.RED).text("wut?").pointAt(parrotPos.add(-.25f, 0.25f, .25f));

    }

    public static void offScreenScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_baseplate", "Out of bounds / configureBasePlate");
        scene.configureBasePlate(1, 0, 6);
        scene.showBasePlate();

        Selection out1 = util.select().fromTo(7, 0, 0, 8, 0, 5);
        Selection out2 = util.select().fromTo(0, 0, 0, 0, 0, 5);
        Selection blocksExceptBasePlate = util.select().layersFrom(1).add(out1).add(out2);

        scene.addKeyframe();
        scene.idle(10);
        scene.world().showSection(blocksExceptBasePlate, Direction.DOWN);
        scene.idle(10);
        scene.addKeyframe();
        scene.idle(20);
        scene.addKeyframe();
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showOutlineWithText(out1, 100).colored(PonderPalette.BLACK).text("Blocks outside of the base plate do not affect scaling");
        scene.overlay().showOutlineWithText(out2, 100).colored(PonderPalette.BLACK).text("configureBasePlate() makes sure of that.");
        scene.markAsFinished();
    }

    public static void particleScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_particles", "Emitting particles");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        Vec3d emitterPos = util.vector().of(2.5, 2.25, 2.5);
        ParticleEmitter emitter = scene.effects().simpleParticleEmitter(ParticleTypes.LAVA, util.vector().of(0, .1, 0));
        ParticleEmitter rotation = scene.effects().simpleParticleEmitter(ParticleTypes.BUBBLE_COLUMN_UP, util.vector().of(0, .1, 0));

        scene.overlay().showText(20).text("Incoming...").pointAt(emitterPos);
        scene.idle(30);
        scene.effects().emitParticles(emitterPos, emitter, 1, 60);
        scene.effects().emitParticles(emitterPos, rotation, 20, 1);
        scene.idle(30);
        scene.rotateCameraY(180);
    }

    public static void controlsScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_controls", "Basic player interaction");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(util.select().layer(2), Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(util.select().layer(3), Direction.DOWN);
        scene.idle(10);

        BlockPos shaftPos = util.grid().at(3, 1, 1);
        Selection shaftSelection = util.select().position(shaftPos);
        scene.overlay().showControls(util.vector().topOf(shaftPos), Pointing.DOWN, 40).rightClick().whileSneaking()
            .withItem(Items.SALMON.getDefaultStack());
        scene.idle(20);
        scene.world().replaceBlocks(shaftSelection, Blocks.BIRCH_SIGN.getDefaultState(), true);

        scene.idle(20);
        scene.world().hideSection(shaftSelection, Direction.UP);

        scene.idle(20);

        scene.overlay().showControls(util.vector().of(1, 4.5, 3.5), Pointing.LEFT, 20).rightClick().withItem(new ItemStack(Blocks.POLISHED_ANDESITE));
        scene.world().showSection(util.select().layer(4), Direction.DOWN);

        scene.idle(40);

        BlockPos chassis = util.grid().at(1, 1, 3);
        Vec3d chassisSurface = util.vector().blockSurface(chassis, Direction.NORTH);

        Object chassisValueBoxHighlight = new Object();
        Object chassisEffectHighlight = new Object();

        Box point = new Box(chassisSurface, chassisSurface);
        Box expanded = point.expand(1 / 4f, 1 / 4f, 1 / 16f);

        Selection singleBlock = util.select().position(1, 2, 3);
        Selection twoBlocks = util.select().fromTo(1, 2, 3, 1, 3, 3);
        Selection threeBlocks = util.select().fromTo(1, 2, 3, 1, 4, 3);

        Selection singleRow = util.select().fromTo(1, 2, 3, 3, 2, 3);
        Selection twoRows = util.select().fromTo(1, 2, 3, 3, 3, 3);
        Selection threeRows = twoRows.copy().add(threeBlocks);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, point, 1);
        scene.idle(1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, expanded, 120);
        scene.overlay().showControls(chassisSurface, Pointing.UP, 40).scroll().withItem(Items.SALMON.getDefaultStack());

        PonderPalette white = PonderPalette.WHITE;
        scene.overlay().showOutline(white, chassisEffectHighlight, singleBlock, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, twoBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, threeBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, twoBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, singleBlock, 10);
        scene.idle(10);

        scene.idle(30);
        scene.overlay().showControls(chassisSurface, Pointing.UP, 40).whileCTRL().scroll().withItem(Items.SALMON.getDefaultStack());

        scene.overlay().showOutline(white, chassisEffectHighlight, singleRow, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, twoRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, threeRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, twoRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(white, chassisEffectHighlight, singleRow, 10);
        scene.idle(10);

        scene.markAsFinished();
    }

    public static void birbScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_birbs", "Birbs");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        BlockPos pos = new BlockPos(1, 2, 3);
        scene.special().createBirb(util.vector().blockSurface(pos, Direction.UP), ParrotPose.FaceCursorPose::new);
        //scene.special.birbOnSpinnyShaft(pos);
        scene.overlay().showText(100).colored(PonderPalette.GREEN).text("More birbs = More interesting").pointAt(util.vector().topOf(pos));

        scene.idle(10);
        scene.special().createBirb(util.vector().topOf(0, 1, 2), ParrotPose.DancePose::new);
        scene.idle(10);

        scene.special().createBirb(util.vector().centerOf(3, 1, 3).add(0, 0.25f, 0), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        BlockPos poi1 = util.grid().at(4, 1, 0);
        BlockPos poi2 = util.grid().at(0, 1, 4);

        scene.world().setBlock(poi1, Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.special().movePointOfInterest(poi1);
        scene.idle(20);

        scene.world().setBlock(poi2, Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.special().movePointOfInterest(poi2);
        scene.overlay().showText(20).text("Point of Interest").pointAt(util.vector().centerOf(poi2));
        scene.idle(20);

        scene.world().destroyBlock(poi1);
        scene.special().movePointOfInterest(poi1);
        scene.idle(20);

        scene.world().destroyBlock(poi2);
        scene.special().movePointOfInterest(poi2);
    }

    public static void sectionsScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_sections", "Sections");
        scene.showBasePlate();
        scene.idle(10);
        scene.rotateCameraY(95);

        BlockPos mergePos = util.grid().at(1, 1, 1);
        BlockPos independentPos = util.grid().at(3, 1, 1);
        Selection toMerge = util.select().position(mergePos);
        Selection independent = util.select().position(independentPos);
        Selection start = util.select().layersFrom(1).substract(toMerge).substract(independent);

        scene.world().showSection(start, Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(toMerge, Direction.DOWN);
        ElementLink<WorldSectionElement> link = scene.world().showIndependentSection(independent, Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(40).colored(PonderPalette.GREEN).text("This Section got merged to base.").pointAt(util.vector().topOf(mergePos));
        scene.idle(10);
        scene.overlay().showText(40).colored(PonderPalette.RED).text("This Section renders independently.")
            .pointAt(util.vector().topOf(independentPos));

        scene.idle(40);

        scene.world().hideIndependentSection(link, Direction.DOWN);
        scene.world().hideSection(util.select().fromTo(mergePos, util.grid().at(1, 1, 4)), Direction.DOWN);

        scene.idle(20);

        Selection hiddenReplaceArea = util.select().fromTo(2, 1, 2, 4, 1, 4).substract(util.select().position(4, 1, 3))
            .substract(util.select().position(2, 1, 3));

        scene.world().hideSection(hiddenReplaceArea, Direction.UP);
        scene.idle(20);
        scene.world().setBlocks(hiddenReplaceArea, Blocks.BLACK_CONCRETE.getDefaultState(), false);
        scene.world().showSection(hiddenReplaceArea, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showOutlineWithText(hiddenReplaceArea, 30).colored(PonderPalette.BLUE).text("Seamless substitution of blocks");

        scene.idle(40);

        ElementLink<WorldSectionElement> helicopter = scene.world().makeSectionIndependent(hiddenReplaceArea);
        scene.world().rotateSection(helicopter, 50, 5 * 360, 0, 60);
        scene.world().moveSection(helicopter, util.vector().of(0, 4, 5), 50);
        scene.overlay().showText(30).colored(PonderPalette.BLUE).text("Up, up and away.").independent(30);

        scene.idle(40);
        scene.world().hideIndependentSection(helicopter, Direction.UP);

    }

	/*public static void itemScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_items", "Manipulating Items");
		scene.configureBasePlate(0, 0, 6);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		ItemStack brassItem = AllItems.BRASS_INGOT.asStack();
		ItemStack copperItem = new ItemStack(Items.COPPER_INGOT);

		for (int z = 4; z >= 2; z--) {
			scene.world.createItemEntity(util.vector.centerOf(0, 4, z), Vec3.ZERO, brassItem.copy());
			scene.idle(10);
		}

		BlockPos beltPos = util.grid.at(2, 1, 3);
		ElementLink<BeltItemElement> itemOnBelt =
			scene.world.createItemOnBelt(beltPos, Direction.EAST, copperItem.copy());

		scene.idle(10);
		scene.world.stallBeltItem(itemOnBelt, true);
		scene.idle(5);
		scene.overlay.showText(40)
			.colored(PonderPalette.FAST)
			.text("Belt Items can only be force-stalled on the belt they were created on.")
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(45);
		scene.world.stallBeltItem(itemOnBelt, false);
		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (copperItem.sameItem(entity.getItem()))
				entity.setNoGravity(true);
		});

		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (brassItem.sameItem(entity.getItem()))
				entity.setDeltaMovement(util.vector.of(-.15f, .5f, 0));
		});

		scene.idle(27);

		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
	}*/

}