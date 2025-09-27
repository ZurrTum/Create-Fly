package com.zurrtum.create.client.infrastructure.ponder.scenes.highLogistics;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.element.ElementLinkImpl;
import com.zurrtum.create.client.ponder.foundation.element.ParrotElementImpl;
import com.zurrtum.create.client.ponder.foundation.instruction.CreateParrotInstruction;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;

import java.util.Iterator;
import java.util.function.Supplier;

public class FrogAndConveyorScenes {

    public static void conveyor(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_conveyor", "Relaying rotational force using Chain Conveyors");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(.75f);
        scene.setSceneOffsetY(-1);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection pole = util.select().fromTo(1, 1, 6, 1, 3, 6);
        Selection cogs = util.select().position(8, 1, 2);
        Selection cogs2 = util.select().fromTo(7, 1, 1, 7, 3, 1);

        BlockPos conv1 = util.grid().at(7, 4, 1);
        BlockPos conv2 = util.grid().at(1, 4, 7);
        BlockPos conv3 = util.grid().at(1, 2, 4);
        BlockPos conv4 = util.grid().at(7, 4, 7);

        connection(builder, conv1, conv2, false);
        connection(builder, conv1, conv3, false);
        connection(builder, conv1, conv4, false);

        Selection pole3 = util.select().position(1, 1, 4);
        Selection pole4 = util.select().fromTo(7, 1, 7, 7, 3, 7);
        Selection cogsBelow = util.select().fromTo(1, 2, 7, 1, 3, 7);
        Selection cogsAbove = util.select().position(1, 5, 7);

        Selection conv1S = util.select().position(conv1);
        Selection conv2S = util.select().position(conv2);
        Selection conv3S = util.select().position(conv3);
        Selection conv4S = util.select().position(conv4);

        scene.world().setKineticSpeed(conv2S, 0);

        scene.idle(5);
        scene.world().showSection(cogs, Direction.EAST);
        scene.idle(5);
        scene.world().showSection(cogs2, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(conv1S, Direction.DOWN);
        ElementLink<WorldSectionElement> poleE = scene.world().showIndependentSection(pole, Direction.DOWN);
        scene.world().moveSection(poleE, util.vector().of(0, 0, 1), 0);
        scene.idle(5);
        scene.world().showSection(conv2S, Direction.DOWN);
        scene.idle(20);

        ItemStack chainItem = new ItemStack(Items.CHAIN);
        scene.overlay().showControls(util.vector().topOf(conv1), Pointing.DOWN, 117).rightClick().withItem(chainItem);

        Vec3d c1 = util.vector().centerOf(conv1);
        Box bb1 = new Box(c1, c1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv1, bb1, 10);
        scene.idle(1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv1, bb1.expand(1, 0.5, 1), 117);
        scene.idle(16);

        scene.overlay().showControls(util.vector().topOf(conv2), Pointing.DOWN, 100).rightClick().withItem(chainItem);

        Vec3d c2 = util.vector().centerOf(conv2);
        Box bb2 = new Box(c2, c2);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv2, bb2, 10);
        scene.idle(1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv2, bb2.expand(1, 0.5, 1), 100);
        scene.idle(10);

        connection(builder, conv1, conv2, true);
        scene.world().setKineticSpeed(conv2S, -32);

        scene.overlay().showText(80).text("Right-click two conveyors with chains to connect them").attachKeyFrame().placeNearTarget()
            .pointAt(util.vector().topOf(conv1.add(-1, 0, -1)));
        scene.idle(90);

        scene.world().showSection(pole3, Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(pole4, Direction.DOWN);
        scene.idle(6);
        scene.world().showSection(conv3S, Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(conv4S, Direction.DOWN);
        scene.idle(12);
        connection(builder, conv1, conv3, true);
        scene.idle(3);
        connection(builder, conv1, conv4, true);
        scene.idle(20);

        scene.overlay().showText(70).text("Chain conveyors relay rotational power between each other..").attachKeyFrame().placeNearTarget()
            .pointAt(util.vector().topOf(conv3.add(-1, 0, -1)));
        scene.idle(60);

        scene.world().hideIndependentSection(poleE, Direction.SOUTH);
        scene.idle(20);
        scene.world().showSection(cogsAbove, Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(cogsBelow, Direction.UP);
        scene.idle(12);

        scene.effects().rotationDirectionIndicator(conv2.up());
        scene.idle(3);
        scene.effects().rotationDirectionIndicator(conv2.down(2));
        scene.idle(10);

        scene.overlay().showText(60).text("..and connect to shafts above or below them").attachKeyFrame().placeNearTarget()
            .pointAt(util.vector().centerOf(util.grid().at(1, 2, 7)));
        scene.idle(60);
        scene.world().hideSection(cogsBelow, Direction.SOUTH);
        scene.idle(15);
        ElementLink<WorldSectionElement> poleE2 = scene.world().showIndependentSection(pole, Direction.EAST);
        scene.world().moveSection(poleE2, util.vector().of(0, 0, 1), 0);
        scene.idle(10);

        scene.overlay().showText(80).text("Right-click holding a wrench to start travelling on the chain").attachKeyFrame().independent(30);

        scene.idle(40);
        ElementLink<ParrotElement> parrot = new ElementLinkImpl<>(ParrotElement.class);
        Vec3d parrotStart = util.vector().centerOf(conv2).add(0, -1.45, 1);
        ChainConveyorParrotElement element = new ChainConveyorParrotElement(parrotStart, ParrotPose.FacePointOfInterestPose::new);
        scene.addInstruction(new CreateParrotInstruction(0, Direction.DOWN, element));
        scene.addInstruction(s -> s.linkElement(element, parrot));
        scene.special().movePointOfInterest(util.grid().at(0, 3, 2));

        scene.idle(20);
        scene.special().moveParrot(parrot, util.vector().of(-1, 0, -1), 14);
        scene.idle(14);
        scene.special().movePointOfInterest(util.grid().at(7, 3, 0));
        scene.special().moveParrot(parrot, util.vector().of(5.75, 0, -5.75), 90);
        scene.idle(65);

        scene.overlay().showText(60).text("At a junction, face towards a chain to follow it").attachKeyFrame().placeNearTarget()
            .pointAt(util.vector().topOf(conv1));

        scene.idle(25);
        scene.special().movePointOfInterest(util.grid().at(9, 3, 1));
        scene.special().moveParrot(parrot, util.vector().of(1, 0, -1), 14);
        scene.idle(14);
        scene.special().movePointOfInterest(util.grid().at(9, 3, 3));
        scene.special().moveParrot(parrot, util.vector().of(0.5, 0, 0), 6);
        scene.idle(6);
        scene.special().movePointOfInterest(util.grid().at(8, 3, 10));
        scene.special().moveParrot(parrot, util.vector().of(0.5, 0, 0.5), 14);
        scene.idle(14);
        scene.special().moveParrot(parrot, util.vector().of(0, 0, 7), 78);
        scene.idle(78);
        scene.special().hideElement(parrot, Direction.SOUTH);
    }

    private static void connection(SceneBuilder builder, BlockPos p1, BlockPos p2, boolean connect) {
        builder.world().modifyBlockEntity(
            p1, ChainConveyorBlockEntity.class, be -> {
                if (connect)
                    be.connections.add(p2.subtract(p1));
                else
                    be.connections.remove(p2.subtract(p1));
            }
        );
        builder.world().modifyBlockEntity(
            p2, ChainConveyorBlockEntity.class, be -> {
                if (connect)
                    be.connections.add(p1.subtract(p2));
                else
                    be.connections.remove(p1.subtract(p2));
            }
        );
    }

    public static class ChainConveyorParrotElement extends ParrotElementImpl {

        private ItemEntity wrench;

        public ChainConveyorParrotElement(Vec3d location, Supplier<? extends ParrotPose> pose) {
            super(location, pose);
        }

        @Override
        protected void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack poseStack, float fade, float pt) {
            MinecraftClient mc = MinecraftClient.getInstance();
            EntityRenderDispatcher entityrenderermanager = mc.getEntityRenderDispatcher();

            if (entity == null) {
                entity = pose.create(world);
                entity.setYaw(entity.lastYaw = 180);
            }

            if (wrench == null) {
                wrench = new ItemEntity(world, 0, 0, 0, AllItems.WRENCH.getDefaultStack());
                wrench.setYaw(wrench.lastYaw = 180);
            }

            double lx = MathHelper.lerp(pt, entity.lastX, entity.getX());
            double ly = MathHelper.lerp(pt, entity.lastY, entity.getY());
            double lz = MathHelper.lerp(pt, entity.lastZ, entity.getZ());
            float angle = AngleHelper.angleLerp(pt, entity.lastYaw, entity.getYaw());

            poseStack.push();
            poseStack.translate(location.x, location.y, location.z);
            poseStack.translate(lx, ly, lz);
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

            poseStack.translate(0, 1.5f, 0);
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin((world.scene.getCurrentTime() + pt) * 0.2f) * 10));
            poseStack.translate(0, -1.5f, 0);

            poseStack.push();
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            poseStack.scale(1.5f, 1.5f, 1.5f);
            poseStack.translate(-0.1, 0.2, -0.6);
            ItemRenderer itemRenderer = mc.getItemRenderer();
            mc.getItemModelManager().clearAndUpdate(itemRenderer.itemRenderState, wrench.getStack(), ItemDisplayContext.GROUND, world, null, 0);
            itemRenderer.itemRenderState.render(poseStack, buffer, lightCoordsFromFade(fade), OverlayTexture.DEFAULT_UV);
            poseStack.pop();

            entity.maxWingDeviation = 2;
            entityrenderermanager.render(entity, 0, 0, 0, pt, poseStack, buffer, lightCoordsFromFade(fade));
            poseStack.pop();
        }

    }

    public static void frogPort(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("package_frogport", "Transporting packages between Frogports");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(.75f);
        scene.setSceneOffsetY(-1);

        BlockPos conv1 = util.grid().at(1, 4, 7);
        BlockPos conv2 = util.grid().at(7, 4, 7);
        BlockPos conv3 = util.grid().at(7, 4, 1);
        Selection conv1S = util.select().position(conv1);
        Selection conv2S = util.select().position(conv2);
        Selection conv3S = util.select().position(conv3);
        Selection largeCog = util.select().position(2, 1, 8);
        Selection shaftPole = util.select().fromTo(1, 1, 7, 1, 3, 7);
        Selection pole2 = util.select().fromTo(7, 1, 7, 7, 3, 7);
        Selection pole3 = util.select().fromTo(7, 1, 1, 7, 3, 1);
        Selection largeCog2 = util.select().position(9, 0, 1);
        Selection fromBelt = util.select().fromTo(9, 1, 0, 6, 1, 0).add(util.select().fromTo(5, 1, 0, 5, 1, 1));
        BlockPos fromFunnel = util.grid().at(5, 2, 1);
        Selection logistics = util.select().fromTo(2, 1, 2, 1, 1, 2);
        Selection toBelt = util.select().fromTo(1, 1, 9, 0, 1, 9).add(util.select().fromTo(0, 1, 8, 0, 1, 6))
            .add(util.select().fromTo(1, 1, 5, 0, 1, 5));
        BlockPos toFunnel = util.grid().at(1, 2, 5);

        BlockPos fromFrog = util.grid().at(5, 2, 2);
        BlockPos toFrog = util.grid().at(2, 2, 5);
        Selection fromFrogS = util.select().position(fromFrog);
        Selection toFrogS = util.select().position(toFrog);

        Selection casing = util.select().position(5, 1, 4);
        Selection fromBarrel = util.select().position(5, 1, 3);
        Selection toBarrel = util.select().position(3, 1, 5);
        Selection fromPackager = util.select().fromTo(7, 1, 2, 5, 1, 2);
        Selection sign = util.select().position(4, 1, 2);
        BlockPos lever = util.grid().at(6, 1, 1);
        Selection toPackager = util.select().fromTo(2, 1, 4, 0, 1, 4);

        scene.world().showSection(util.select().layer(0).substract(largeCog2), Direction.UP);
        scene.idle(10);
        scene.world().showSection(largeCog, Direction.SOUTH);
        scene.idle(2);
        scene.world().showSection(shaftPole, Direction.DOWN);
        scene.idle(2);
        scene.world().showSection(pole2, Direction.DOWN);
        scene.idle(2);
        scene.world().showSection(pole3, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(conv1S, Direction.DOWN);
        scene.world().showSection(conv2S, Direction.DOWN);
        scene.world().showSection(conv3S, Direction.DOWN);
        scene.idle(25);

        Vec3d fromTarget = util.vector().of(6.78, 4.37, 3.5);

        ItemStack frogItem = AllItems.PACKAGE_FROGPORT.getDefaultStack();
        scene.overlay().showControls(fromTarget, Pointing.UP, 50).rightClick().withItem(frogItem);
        scene.idle(5);

        Box bb1 = new Box(fromTarget, fromTarget);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, conv1, bb1, 10);
        scene.idle(1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, conv1, bb1.expand(0.025, 0.025, 0.025), 50);
        scene.idle(26);

        scene.overlay().showText(80).text("Right-click a Chain Conveyor and place the Frogport nearby").attachKeyFrame().placeNearTarget()
            .pointAt(fromTarget);

        scene.idle(40);

        ElementLink<WorldSectionElement> fromFrogE = scene.world().showIndependentSection(fromFrogS, Direction.DOWN);
        scene.world().moveSection(fromFrogE, util.vector().of(0, -1, 0), 0);

        scene.idle(15);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv1, bb1.expand(0.025, 0.025, 0.025), 50);

        Box bb2 = new Box(fromFrog.down()).shrink(0, 0.75, 0);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv2, bb2, 50);
        scene.idle(10);
        scene.overlay().showLine(PonderPalette.GREEN, util.vector().topOf(fromFrog.down()), fromTarget, 40);
        scene.idle(45);

        scene.overlay().showControls(util.vector().topOf(fromFrog.down()), Pointing.DOWN, 40).rightClick();
        scene.idle(7);
        scene.overlay().showOutlineWithText(util.select().position(fromFrog.down()), 70).attachKeyFrame().colored(PonderPalette.BLUE)
            .text("Assign it an address in the inventory UI").pointAt(util.vector().topOf(fromFrog.down())).placeNearTarget();
        scene.idle(80);

        scene.world().moveSection(fromFrogE, util.vector().of(0, 1, 0), 10);
        scene.idle(10);
        ElementLink<WorldSectionElement> casingE = scene.world().showIndependentSection(casing, Direction.NORTH);
        scene.world().moveSection(casingE, util.vector().of(0, 0, -2), 0);
        scene.idle(5);
        scene.world().showSection(largeCog2, Direction.UP);
        scene.world().showSection(fromBelt, Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(fromFunnel), Direction.DOWN);
        scene.idle(10);

        ItemStack box = PackageStyles.getDefaultBox().copy();
        PackageItem.addAddress(box, "Peter");

        scene.world().createItemOnBelt(util.grid().at(5, 1, 0), Direction.NORTH, box);
        scene.idle(5);

        scene.world().multiplyKineticSpeed(util.select().fromTo(9, 0, 1, 5, 1, 0), 1 / 32f);

        scene.overlay().showText(60).attachKeyFrame().text("If the address of an inserted package does not match it..")
            .pointAt(util.vector().topOf(5, 0, 3)).placeNearTarget();

        scene.idle(70);

        scene.overlay().showText(40).colored(PonderPalette.BLUE).text("Albert").pointAt(util.vector().topOf(fromFrog)).placeNearTarget();
        scene.idle(5);
        scene.overlay().showText(40).colored(PonderPalette.OUTPUT).text("→ Peter").pointAt(util.vector().centerOf(util.grid().at(5, 2, 0)))
            .placeNearTarget();

        scene.idle(50);

        scene.world().multiplyKineticSpeed(util.select().fromTo(9, 0, 1, 5, 1, 0), 32f);
        scene.idle(13);
        scene.world().removeItemsFromBelt(util.grid().at(5, 1, 1));
        scene.world().flapFunnel(fromFunnel, false);
        scene.idle(15);
        scene.world().modifyBlockEntity(fromFrog, FrogportBlockEntity.class, be -> be.startAnimation(box, true));
        scene.idle(15);

        scene.overlay().showText(60).text("..the Frogport will place the package on the conveyor").pointAt(fromTarget.add(0, 0, 1.5))
            .placeNearTarget();
        scene.idle(95);

        scene.overlay().showText(60).attachKeyFrame().colored(PonderPalette.RED).text("Packages spin in place if they have no valid destination")
            .pointAt(util.vector().of(6.5, 4.25, 7.5)).placeNearTarget();
        scene.idle(60);

        scene.world().showSection(util.select().position(toFrog.down()), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(toFrogS, Direction.DOWN);
        scene.idle(15);

        Vec3d toTarget = util.vector().of(3.5, 4.37, 6.78);
        Box bb3 = new Box(toTarget, toTarget);
        Box bb4 = new Box(toFrog).shrink(0, 0.75, 0);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, conv3, bb3.expand(0.025, 0.025, 0.025), 80);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, lever, bb4, 80);
        scene.overlay().showLine(PonderPalette.GREEN, util.vector().topOf(toFrog), toTarget, 80);

        scene.idle(10);

        scene.overlay().showText(70).text("More Frogports can be added anywhere on the chain network").attachKeyFrame().placeNearTarget()
            .pointAt(toTarget);

        scene.idle(75);

        scene.overlay().showText(70).colored(PonderPalette.BLUE).text("Peter").pointAt(util.vector().topOf(toFrog)).placeNearTarget();
        scene.idle(30);

        scene.world().modifyBlockEntity(conv2, ChainConveyorBlockEntity.class, be -> boxTransfer(conv1, conv2, be));

        scene.idle(50);
        scene.overlay().showText(70).attachKeyFrame().text("Packages find their path to a matching frog on the chain network")
            .pointAt(util.vector().topOf(toFrog)).placeNearTarget();
        scene.idle(40);

        scene.world().showSection(toBelt, Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().position(toFunnel), Direction.DOWN);
        scene.idle(15);

        scene.world().createItemOnBelt(util.grid().at(1, 1, 5), Direction.EAST, box);
        scene.idle(20);
        scene.world().hideSection(util.select().fromTo(0, 1, 6, 0, 1, 9).add(util.select().position(1, 1, 9)), Direction.SOUTH);
        scene.world().setKineticSpeed(util.select().fromTo(1, 1, 5, 0, 1, 5), 0);

        scene.overlay().showText(50).colored(PonderPalette.BLUE).text("Peter").pointAt(util.vector().topOf(toFrog)).placeNearTarget();
        scene.idle(5);
        scene.overlay().showText(55).colored(PonderPalette.OUTPUT).text("\u2192 Peter").pointAt(util.vector().centerOf(util.grid().at(0, 2, 5)))
            .placeNearTarget();

        scene.idle(60);

        scene.world().hideSection(util.select().fromTo(1, 2, 5, 0, 1, 5), Direction.WEST);
        scene.world().hideSection(util.select().fromTo(5, 2, 1, 5, 1, 0).add(util.select().fromTo(6, 1, 0, 9, 1, 0)), Direction.NORTH);
        scene.world().hideSection(util.select().position(9, 0, 1), Direction.DOWN);

        scene.idle(15);

        scene.world().hideIndependentSection(casingE, Direction.WEST);
        scene.world().hideSection(util.select().position(2, 1, 5), Direction.WEST);
        scene.idle(15);
        ElementLink<WorldSectionElement> fromBarrelE = scene.world().showIndependentSection(fromBarrel, Direction.WEST);
        scene.world().moveSection(fromBarrelE, util.vector().of(0, 0, -1), 0);
        ElementLink<WorldSectionElement> toBarrelE = scene.world().showIndependentSection(toBarrel, Direction.WEST);
        scene.world().moveSection(toBarrelE, util.vector().of(-1, 0, 0), 0);
        scene.idle(20);

        scene.overlay().showOutlineWithText(util.select().position(fromFrog.down()).add(fromFrogS), 70).attachKeyFrame().colored(PonderPalette.BLUE)
            .text("Frogports can directly interface with inventories below them").pointAt(util.vector().topOf(fromFrog.down())).placeNearTarget();

        scene.idle(70);

        scene.world().hideIndependentSection(fromBarrelE, Direction.WEST);
        scene.world().hideIndependentSection(toBarrelE, Direction.EAST);
        scene.idle(15);

        scene.world().showIndependentSection(fromPackager, Direction.WEST);
        ElementLink<WorldSectionElement> toPackagerE = scene.world().showIndependentSection(toPackager, Direction.EAST);
        scene.world().moveSection(toPackagerE, util.vector().of(0, 0, 1), 0);
        ElementLink<WorldSectionElement> leverE = scene.world().showIndependentSection(util.select().position(lever), Direction.DOWN);
        scene.world().moveSection(leverE, util.vector().of(-1, 0, 0), 0);
        scene.idle(15);

        scene.overlay().showText(90).attachKeyFrame().text("This also works with packagers. Items can be packed and shipped directly")
            .pointAt(util.vector().blockSurface(fromFrog.down(), Direction.WEST)).placeNearTarget();
        scene.idle(100);

        scene.world().showSection(sign, Direction.EAST);
        scene.idle(10);

        scene.overlay().showText(80).colored(PonderPalette.BLUE).text("Albert").pointAt(util.vector().topOf(fromFrog)).placeNearTarget();
        scene.overlay().showText(80).colored(PonderPalette.BLUE).text("Peter").pointAt(util.vector().topOf(toFrog)).placeNearTarget();
        scene.idle(20);

        scene.overlay().showOutlineWithText(util.select().position(fromFrog.down()).add(util.select().position(fromFrog.down().west())), 70)
            .colored(PonderPalette.OUTPUT).text("Addresses packages to 'Peter'")
            .pointAt(util.vector().blockSurface(fromFrog.down().west(), Direction.NORTH)).placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(6, 1, 2), Direction.UP).add(0.5, 0, 0), Pointing.DOWN, 40)
            .withItem(new ItemStack(Items.DIAMOND));
        scene.idle(25);

        scene.addKeyframe();

        scene.effects().indicateRedstone(util.grid().at(5, 1, 1));
        scene.world().toggleRedstonePower(util.select().fromTo(5, 1, 2, 6, 1, 1));
        scene.idle(5);

        PonderHilo.packagerCreate(scene, util.grid().at(5, 1, 2), box);
        scene.idle(30);

        scene.world().modifyBlockEntity(
            util.grid().at(5, 1, 2), PackagerBlockEntity.class, be -> {
                be.heldBox = ItemStack.EMPTY;
            }
        );
        scene.world().modifyBlockEntity(fromFrog, FrogportBlockEntity.class, be -> be.startAnimation(box, true));

        scene.idle(40);

        scene.world().modifyBlockEntity(conv2, ChainConveyorBlockEntity.class, be -> boxTransfer(conv1, conv2, be));
        scene.idle(50);

        PonderHilo.packagerUnpack(scene, util.grid().at(2, 1, 4), box);
        scene.idle(20);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(0, 1, 5), Direction.UP).add(0.5, 0, 0), Pointing.DOWN, 40)
            .withItem(new ItemStack(Items.DIAMOND));
        scene.idle(60);

        scene.overlay().showControls(util.vector().centerOf(util.grid().at(2, 2, 5)), Pointing.RIGHT, 40).rightClick()
            .withItem(AllItems.CLIPBOARD.getDefaultStack());
        scene.idle(10);

        scene.overlay().showText(90).attachKeyFrame().text("Right-click Frogports with a clipboard to collect their address")
            .pointAt(util.vector().blockSurface(toFrog, Direction.WEST)).placeNearTarget();
        scene.idle(70);

        scene.world().showSection(logistics, Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(120).text("Clipboards with collected names can help auto-complete address inputs in other UIs")
            .pointAt(util.vector().topOf(util.grid().at(2, 1, 2))).placeNearTarget();
        scene.idle(70);
    }

    public static void boxTransfer(BlockPos to, BlockPos from, ChainConveyorBlockEntity be) {
        for (Iterator<ChainConveyorPackage> iterator = be.getLoopingPackages().iterator(); iterator.hasNext(); ) {
            ChainConveyorPackage chainConveyorPackage = iterator.next();
            chainConveyorPackage.chainPosition = 0;
            be.addTravellingPackage(chainConveyorPackage, to.subtract(from));
            iterator.remove();
        }
    }

}