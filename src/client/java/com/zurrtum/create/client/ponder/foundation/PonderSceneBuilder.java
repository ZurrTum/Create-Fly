package com.zurrtum.create.client.ponder.foundation;


import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.*;
import com.zurrtum.create.client.ponder.api.element.MinecartElement.MinecartConstructor;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.element.*;
import com.zurrtum.create.client.ponder.foundation.instruction.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Enqueue instructions to the schedule via this object's methods.
 */
public class PonderSceneBuilder implements SceneBuilder {

    private final OverlayInstructions overlay;
    private final WorldInstructions world;
    private final DebugInstructions debug;
    private final EffectInstructions effects;
    private final SpecialInstructions special;

    protected final PonderScene scene;

    public PonderSceneBuilder(PonderScene ponderScene) {
        scene = ponderScene;
        overlay = new PonderOverlayInstructions();
        special = new PonderSpecialInstructions();
        world = new PonderWorldInstructions();
        debug = new PonderDebugInstructions();
        effects = new PonderEffectInstructions();
    }

    @Override
    public OverlayInstructions overlay() {
        return overlay;
    }

    @Override
    public WorldInstructions world() {
        return world;
    }

    @Override
    public DebugInstructions debug() {
        return debug;
    }

    @Override
    public EffectInstructions effects() {
        return effects;
    }

    @Override
    public SpecialInstructions special() {
        return special;
    }

    @Override
    public PonderScene getScene() {
        return scene;
    }

    // General

    @Override
    public void title(String sceneId, String title) {
        scene.sceneId = Identifier.of(scene.getNamespace(), sceneId);
        scene.localization.registerSpecific(scene.sceneId, PonderScene.TITLE_KEY, title);
    }

    @Override
    public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
        scene.basePlateOffsetX = xOffset;
        scene.basePlateOffsetZ = zOffset;
        scene.basePlateSize = basePlateSize;
    }

    @Override
    public void scaleSceneView(float factor) {
        scene.scaleFactor = factor;
    }

    @Override
    public void removeShadow() {
        scene.hidePlatformShadow = true;
    }

    @Override
    public void setSceneOffsetY(float yOffset) {
        scene.yOffset = yOffset;
    }

    @Override
    public void showBasePlate() {
        world.showSection(
            scene.getSceneBuildingUtil().select().cuboid(
                new BlockPos(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ()),
                new Vec3i(scene.getBasePlateSize() - 1, 0, scene.getBasePlateSize() - 1)
            ), Direction.UP
        );
    }

    @Override
    public void addInstruction(PonderInstruction instruction) {
        scene.schedule.add(instruction);
    }

    @Override
    public void addInstruction(Consumer<PonderScene> callback) {
        addInstruction(PonderInstruction.simple(callback));
    }

    @Override
    public void idle(int ticks) {
        addInstruction(new DelayInstruction(ticks));
    }

    @Override
    public void idleSeconds(int seconds) {
        idle(seconds * 20);
    }

    @Override
    public void markAsFinished() {
        addInstruction(new MarkAsFinishedInstruction());
    }

    @Override
    public void setNextUpEnabled(boolean isEnabled) {
        addInstruction(scene -> scene.setNextUpEnabled(isEnabled));
    }

    @Override
    public void rotateCameraY(float degrees) {
        addInstruction(new RotateSceneInstruction(0, degrees, true));
    }

    @Override
    public void addKeyframe() {
        addInstruction(KeyframeInstruction.IMMEDIATE);
    }

    @Override
    public void addLazyKeyframe() {
        addInstruction(KeyframeInstruction.DELAYED);
    }

    public class PonderEffectInstructions implements EffectInstructions {

        @Override
        public void emitParticles(Vec3d location, ParticleEmitter emitter, float amountPerCycle, int cycles) {
            addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
        }

        @Override
        public <T extends ParticleEffect> ParticleEmitter simpleParticleEmitter(T data, Vec3d motion) {
            return (w, x, y, z) -> w.addParticleClient(data, x, y, z, motion.x, motion.y, motion.z);
        }

        @Override
        public <T extends ParticleEffect> ParticleEmitter particleEmitterWithinBlockSpace(T data, Vec3d motion) {
            return (w, x, y, z) -> w.addParticleClient(
                data,
                Math.floor(x) + w.random.nextFloat(),
                Math.floor(y) + w.random.nextFloat(),
                Math.floor(z) + w.random.nextFloat(),
                motion.x,
                motion.y,
                motion.z
            );
        }

        @Override
        public void indicateRedstone(BlockPos pos) {
            createRedstoneParticles(pos, 0xFF0000, 10);
        }

        @Override
        public void indicateSuccess(BlockPos pos) {
            createRedstoneParticles(pos, 0x80FFaa, 10);
        }

        @Override
        public void createRedstoneParticles(BlockPos pos, int color, int amount) {
            int rgb = new Color(color).getRGB();
            addInstruction(new EmitParticlesInstruction(
                VecHelper.getCenterOf(pos),
                effects().particleEmitterWithinBlockSpace(new DustParticleEffect(rgb, 1), Vec3d.ZERO),
                amount,
                2
            ));
        }

    }

    public class PonderOverlayInstructions implements OverlayInstructions {

        @Override
        public TextElementBuilder showText(int duration) {
            TextWindowElement textWindowElement = new TextWindowElement();
            addInstruction(new TextInstruction(textWindowElement, duration));
            return textWindowElement.builder(scene);
        }

        @Override
        public TextElementBuilder showOutlineWithText(Selection selection, int duration) {
            TextWindowElement textWindowElement = new TextWindowElement();
            addInstruction(new TextInstruction(textWindowElement, duration, selection));
            return textWindowElement.builder(scene).pointAt(selection.getCenter());
        }

        @Override
        public InputElementBuilder showControls(Vec3d sceneSpace, Pointing direction, int duration) {
            InputWindowElement inputWindowElement = new InputWindowElement(sceneSpace, direction);
            addInstruction(new ShowInputInstruction(inputWindowElement, duration));
            return inputWindowElement.builder();
        }

        @Override
        public void chaseBoundingBoxOutline(PonderPalette color, Object slot, Box boundingBox, int duration) {
            addInstruction(new ChaseAABBInstruction(color, slot, boundingBox, duration));
        }

        @Override
        public void showCenteredScrollInput(BlockPos pos, Direction side, int duration) {
            showScrollInput(scene.getSceneBuildingUtil().vector().blockSurface(pos, side), side, duration);
        }

        @Override
        public void showScrollInput(Vec3d location, Direction side, int duration) {
            Axis axis = side.getAxis();
            float s = 1 / 16f;
            float q = 1 / 4f;
            Vec3d expands = new Vec3d(axis == Axis.X ? s : q, axis == Axis.Y ? s : q, axis == Axis.Z ? s : q);
            addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
        }

        @Override
        public void showRepeaterScrollInput(BlockPos pos, int duration) {
            float s = 1 / 16f;
            float q = 1 / 6f;
            Vec3d expands = new Vec3d(q, s, q);
            addInstruction(new HighlightValueBoxInstruction(
                scene.getSceneBuildingUtil().vector().blockSurface(pos, Direction.DOWN)
                    .add(0, 3 / 16f, 0), expands, duration
            ));
        }

        @Override
        public void showFilterSlotInput(Vec3d location, int duration) {
            float s = .1f;
            Vec3d expands = new Vec3d(s, s, s);
            addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
        }

        @Override
        public void showFilterSlotInput(Vec3d location, Direction side, int duration) {
            location = location.add(Vec3d.of(side.getVector()).multiply(-3 / 128f));
            Vec3d expands = VecHelper.axisAlingedPlaneOf(side).multiply(11 / 128f);
            addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
        }

        @Override
        public void showLine(PonderPalette color, Vec3d start, Vec3d end, int duration) {
            addInstruction(new LineInstruction(color, start, end, duration, false));
        }

        @Override
        public void showBigLine(PonderPalette color, Vec3d start, Vec3d end, int duration) {
            addInstruction(new LineInstruction(color, start, end, duration, true));
        }

        @Override
        public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
            addInstruction(new OutlineSelectionInstruction(color, slot, selection, duration));
        }

    }

    public class PonderSpecialInstructions implements SpecialInstructions {

        @Override
        public ElementLink<ParrotElement> createBirb(Vec3d location, Supplier<? extends ParrotPose> pose) {
            ElementLink<ParrotElement> link = new ElementLinkImpl<>(ParrotElement.class);
            ParrotElement parrot = ParrotElementImpl.create(location, pose);
            addInstruction(new CreateParrotInstruction(10, Direction.DOWN, parrot));
            addInstruction(scene -> scene.linkElement(parrot, link));
            return link;
        }

        @Override
        public void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose) {
            addInstruction(scene -> scene.resolveOptional(birb).ifPresent(safeBirb -> safeBirb.setPose(pose.get())));
        }

        @Override
        public void movePointOfInterest(Vec3d location) {
            addInstruction(new MovePoiInstruction(location));
        }

        @Override
        public void movePointOfInterest(BlockPos location) {
            movePointOfInterest(VecHelper.getCenterOf(location));
        }

        @Override
        public void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation, int duration) {
            addInstruction(AnimateParrotInstruction.rotate(link, new Vec3d(xRotation, yRotation, zRotation), duration));
        }

        @Override
        public void moveParrot(ElementLink<ParrotElement> link, Vec3d offset, int duration) {
            addInstruction(AnimateParrotInstruction.move(link, offset, duration));
        }

        @Override
        public ElementLink<MinecartElement> createCart(Vec3d location, float angle, MinecartConstructor type) {
            ElementLink<MinecartElement> link = new ElementLinkImpl<>(MinecartElement.class);
            MinecartElement cart = new MinecartElementImpl(location, angle, type);
            addInstruction(new CreateMinecartInstruction(10, Direction.DOWN, cart));
            addInstruction(scene -> scene.linkElement(cart, link));
            return link;
        }

        @Override
        public void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration) {
            addInstruction(AnimateMinecartInstruction.rotate(link, yRotation, duration));
        }

        @Override
        public void moveCart(ElementLink<MinecartElement> link, Vec3d offset, int duration) {
            addInstruction(AnimateMinecartInstruction.move(link, offset, duration));
        }

        @Override
        public <T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, Direction direction) {
            addInstruction(new FadeOutOfSceneInstruction<>(15, direction, link));
        }

    }

    public class PonderWorldInstructions implements WorldInstructions {
        @Override
        public RegistryWrapper.WrapperLookup getHolderLookupProvider() {
            return scene.getWorld().getRegistryManager();
        }

        @Override
        public void incrementBlockBreakingProgress(BlockPos pos) {
            addInstruction(scene -> {
                PonderLevel world = scene.getWorld();
                int progress = world.getBlockBreakingProgressions().getOrDefault(pos, -1) + 1;
                if (progress == 9) {
                    world.addBlockDestroyEffects(pos, world.getBlockState(pos));
                    world.breakBlock(pos, false);
                    world.setBlockBreakingProgress(pos, 0);
                    scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
                } else
                    world.setBlockBreakingProgress(pos, progress + 1);
            });
        }

        @Override
        public void showSection(Selection selection, Direction fadeInDirection) {
            addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection, scene::getBaseWorldSection));
        }

        @Override
        public void showSectionAndMerge(Selection selection, Direction fadeInDirection, ElementLink<WorldSectionElement> link) {
            addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection, () -> scene.resolve(link)));
        }

        @Override
        public void glueBlockOnto(BlockPos position, Direction fadeInDirection, ElementLink<WorldSectionElement> link) {
            addInstruction(new DisplayWorldSectionInstruction(
                15,
                fadeInDirection,
                scene.getSceneBuildingUtil().select().position(position),
                () -> scene.resolve(link),
                position
            ));
        }

        @Override
        public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection) {
            DisplayWorldSectionInstruction instruction = new DisplayWorldSectionInstruction(15, fadeInDirection, selection, null);
            addInstruction(instruction);
            return instruction.createLink(scene);
        }

        @Override
        public ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection) {
            DisplayWorldSectionInstruction instruction = new DisplayWorldSectionInstruction(0, Direction.DOWN, selection, null);
            addInstruction(instruction);
            return instruction.createLink(scene);
        }

        @Override
        public void hideSection(Selection selection, Direction fadeOutDirection) {
            WorldSectionElement worldSectionElement = new WorldSectionElementImpl(selection);
            ElementLink<WorldSectionElement> elementLink = new ElementLinkImpl<>(WorldSectionElement.class);

            addInstruction(scene -> {
                scene.getBaseWorldSection().erase(selection);
                scene.linkElement(worldSectionElement, elementLink);
                scene.addElement(worldSectionElement);
                worldSectionElement.queueRedraw();
            });

            hideIndependentSection(elementLink, fadeOutDirection);
        }

        @Override
        public void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection) {
            addInstruction(new FadeOutOfSceneInstruction<>(15, fadeOutDirection, link));
        }

        @Override
        public void restoreBlocks(Selection selection) {
            addInstruction(scene -> scene.getWorld().restoreBlocks(selection));
        }

        @Override
        public ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection) {
            WorldSectionElementImpl worldSectionElement = new WorldSectionElementImpl(selection);
            ElementLink<WorldSectionElement> elementLink = new ElementLinkImpl<>(WorldSectionElement.class);

            addInstruction(scene -> {
                scene.getBaseWorldSection().erase(selection);
                scene.linkElement(worldSectionElement, elementLink);
                scene.addElement(worldSectionElement);
                worldSectionElement.queueRedraw();
                worldSectionElement.resetAnimatedTransform();
                worldSectionElement.setVisible(true);
                worldSectionElement.forceApplyFade(1);
            });

            return elementLink;
        }

        @Override
        public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation, double zRotation, int duration) {
            addInstruction(AnimateWorldSectionInstruction.rotate(link, new Vec3d(xRotation, yRotation, zRotation), duration));
        }

        @Override
        public void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3d anchor) {
            addInstruction(scene -> scene.resolveOptional(link).ifPresent(safe -> safe.setCenterOfRotation(anchor)));
        }

        @Override
        public void configureStabilization(ElementLink<WorldSectionElement> link, Vec3d anchor) {
            addInstruction(scene -> scene.resolveOptional(link).ifPresent(safe -> safe.stabilizeRotation(anchor)));
        }

        @Override
        public void moveSection(ElementLink<WorldSectionElement> link, Vec3d offset, int duration) {
            addInstruction(AnimateWorldSectionInstruction.move(link, offset, duration));
        }

        @Override
        public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
            addInstruction(new ReplaceBlocksInstruction(selection, $ -> state, true, spawnParticles));
        }

        @Override
        public void destroyBlock(BlockPos pos) {
            setBlock(pos, Blocks.AIR.getDefaultState(), true);
        }

        @Override
        public void setBlock(BlockPos pos, BlockState state, boolean spawnParticles) {
            setBlocks(scene.getSceneBuildingUtil().select().position(pos), state, spawnParticles);
        }

        @Override
        public void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles) {
            modifyBlocks(selection, $ -> state, spawnParticles);
        }

        @Override
        public void modifyBlock(BlockPos pos, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
            modifyBlocks(scene.getSceneBuildingUtil().select().position(pos), stateFunc, spawnParticles);
        }

        @Override
        public void cycleBlockProperty(BlockPos pos, Property<?> property) {
            modifyBlocks(scene.getSceneBuildingUtil().select().position(pos), s -> s.contains(property) ? s.cycle(property) : s, false);
        }

        @Override
        public void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
            addInstruction(new ReplaceBlocksInstruction(selection, stateFunc, false, spawnParticles));
        }

        @Override
        public void toggleRedstonePower(Selection selection) {
            modifyBlocks(
                selection, s -> {
                    if (s.contains(Properties.POWER))
                        s = s.with(Properties.POWER, s.get(Properties.POWER) == 0 ? 15 : 0);
                    if (s.contains(Properties.POWERED))
                        s = s.cycle(Properties.POWERED);
                    if (s.contains(RedstoneTorchBlock.LIT))
                        s = s.cycle(RedstoneTorchBlock.LIT);
                    return s;
                }, false
            );
        }

        @Override
        public <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack) {
            addInstruction(scene -> scene.forEachWorldEntity(entityClass, entityCallBack));
        }

        @Override
        public <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area, Consumer<T> entityCallBack) {
            addInstruction(scene -> scene.forEachWorldEntity(
                entityClass, e -> {
                    if (area.test(e.getBlockPos()))
                        entityCallBack.accept(e);
                }
            ));
        }

        @Override
        public void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack) {
            addInstruction(scene -> {
                EntityElement resolve = scene.resolve(link);
                if (resolve != null)
                    resolve.ifPresent(entityCallBack);
            });
        }

        @Override
        public ElementLink<EntityElement> createEntity(Function<World, Entity> factory) {
            ElementLink<EntityElement> link = new ElementLinkImpl<>(EntityElement.class, UUID.randomUUID());
            addInstruction(scene -> {
                PonderLevel world = scene.getWorld();
                Entity entity = factory.apply(world);
                EntityElement handle = new EntityElementImpl(entity);
                scene.addElement(handle);
                scene.linkElement(handle, link);
                world.spawnEntity(entity);
            });
            return link;
        }

        @Override
        public ElementLink<EntityElement> createItemEntity(Vec3d location, Vec3d motion, ItemStack stack) {
            return createEntity(world -> {
                ItemEntity itemEntity = new ItemEntity(world, location.x, location.y, location.z, stack);
                itemEntity.setVelocity(motion);
                return itemEntity;
            });
        }

        @Override
        public void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> beType, Consumer<NbtCompound> consumer) {
            modifyBlockEntityNBT(selection, beType, consumer, false);
        }

        @Override
        public <T extends BlockEntity> void modifyBlockEntity(BlockPos position, Class<T> beType, Consumer<T> consumer) {
            addInstruction(scene -> {
                BlockEntity blockEntity = scene.getWorld().getBlockEntity(position);
                if (beType.isInstance(blockEntity))
                    consumer.accept(beType.cast(blockEntity));
            });
        }

        @Override
        public void modifyBlockEntityNBT(
            Selection selection,
            Class<? extends BlockEntity> teType,
            Consumer<NbtCompound> consumer,
            boolean reDrawBlocks
        ) {
            addInstruction(new BlockEntityDataInstruction(
                selection, teType, nbt -> {
                consumer.accept(nbt);
                return nbt;
            }, reDrawBlocks
            ));
        }
    }

    public class PonderDebugInstructions implements DebugInstructions {

        @Override
        public void debugSchematic() {
            addInstruction(scene -> scene.addElement(new WorldSectionElementImpl(scene.getSceneBuildingUtil().select().everywhere())));
        }

        @Override
        public void addInstructionInstance(PonderInstruction instruction) {
            addInstruction(instruction);
        }

        @Override
        public void enqueueCallback(Consumer<PonderScene> callback) {
            addInstruction(callback);
        }

    }

}