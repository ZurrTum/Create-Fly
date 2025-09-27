package com.zurrtum.create.client.foundation.ponder;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.data.FunctionalHelper;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueSelectionHandler;
import com.zurrtum.create.client.foundation.ponder.element.BeltItemElement;
import com.zurrtum.create.client.foundation.ponder.element.ExpandedParrotElement;
import com.zurrtum.create.client.foundation.ponder.instruction.AnimateBlockEntityInstruction;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderSceneBuilder;
import com.zurrtum.create.client.ponder.foundation.element.ElementLinkImpl;
import com.zurrtum.create.client.ponder.foundation.instruction.CreateParrotInstruction;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CreateSceneBuilder extends PonderSceneBuilder {

    private final EffectInstructions effects;
    private final WorldInstructions world;
    private final SpecialInstructions special;

    public CreateSceneBuilder(SceneBuilder baseSceneBuilder) {
        this(baseSceneBuilder.getScene());
    }

    private CreateSceneBuilder(PonderScene ponderScene) {
        super(ponderScene);
        effects = new EffectInstructions();
        world = new WorldInstructions();
        special = new SpecialInstructions();
    }

    public EffectInstructions effects() {
        return effects;
    }

    public WorldInstructions world() {
        return world;
    }

    public SpecialInstructions special() {
        return special;
    }

    public class EffectInstructions extends PonderEffectInstructions {

        public void superGlue(BlockPos pos, Direction side, boolean fullBlock) {
            addInstruction(scene -> SuperGlueSelectionHandler.spawnParticles(scene.getWorld(), pos, side, fullBlock));
        }

        private void rotationIndicator(BlockPos pos, boolean direction, BlockPos displayPos) {
            addInstruction(scene -> {
                BlockState blockState = scene.getWorld().getBlockState(pos);
                BlockEntity blockEntity = scene.getWorld().getBlockEntity(pos);

                if (!(blockState.getBlock() instanceof KineticBlock kb))
                    return;
                if (!(blockEntity instanceof KineticBlockEntity kbe))
                    return;

                Direction.Axis rotationAxis = kb.getRotationAxis(blockState);

                float speed = kbe.getTheoreticalSpeed();
                IRotate.SpeedLevel speedLevel = IRotate.SpeedLevel.of(speed);
                int color = direction ? speed > 0 ? 0xeb5e0b : 0x1687a7 : speedLevel.getColor();
                int particleSpeed = speedLevel.getParticleSpeed();
                particleSpeed *= Math.signum(speed);

                Vec3d location = VecHelper.getCenterOf(displayPos);
                RotationIndicatorParticleData particleData = new RotationIndicatorParticleData(
                    color,
                    particleSpeed,
                    kb.getParticleInitialRadius(),
                    kb.getParticleTargetRadius(),
                    20,
                    rotationAxis
                );

                for (int i = 0; i < 20; i++)
                    scene.getWorld().addParticleClient(particleData, location.x, location.y, location.z, 0, 0, 0);
            });
        }

        public void rotationSpeedIndicator(BlockPos pos) {
            rotationIndicator(pos, false, pos);
        }

        public void rotationDirectionIndicator(BlockPos pos) {
            rotationIndicator(pos, true, pos);
        }


    }

    public class WorldInstructions extends PonderWorldInstructions {

        public void rotateBearing(BlockPos pos, float angle, int duration) {
            addInstruction(AnimateBlockEntityInstruction.bearing(pos, angle, duration));
        }

        public void movePulley(BlockPos pos, float distance, int duration) {
            addInstruction(AnimateBlockEntityInstruction.pulley(pos, distance, duration));
        }

        public void animateBogey(BlockPos pos, float distance, int duration) {
            addInstruction(AnimateBlockEntityInstruction.bogey(pos, distance, duration + 1));
        }

        public void moveDeployer(BlockPos pos, float distance, int duration) {
            addInstruction(AnimateBlockEntityInstruction.deployer(pos, distance, duration));
        }

        public void createItemOnBeltLike(BlockPos location, Direction insertionSide, ItemStack stack) {
            addInstruction(scene -> {
                PonderLevel world = scene.getWorld();
                BlockEntity blockEntity = world.getBlockEntity(location);
                if (!(blockEntity instanceof SmartBlockEntity beltBlockEntity))
                    return;
                DirectBeltInputBehaviour behaviour = beltBlockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
                if (behaviour == null)
                    return;
                behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);
            });
            flapFunnel(location.up(), true);
        }

        public ElementLink<BeltItemElement> createItemOnBelt(BlockPos beltLocation, Direction insertionSide, ItemStack stack) {
            ElementLink<BeltItemElement> link = new ElementLinkImpl<>(BeltItemElement.class);
            addInstruction(scene -> {
                PonderLevel world = scene.getWorld();
                BlockEntity blockEntity = world.getBlockEntity(beltLocation);
                if (!(blockEntity instanceof BeltBlockEntity beltBlockEntity))
                    return;

                DirectBeltInputBehaviour behaviour = beltBlockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
                behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);

                BeltBlockEntity controllerBE = beltBlockEntity.getControllerBE();
                if (controllerBE != null)
                    controllerBE.tick();

                TransportedItemStackHandlerBehaviour transporter = beltBlockEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
                transporter.handleProcessingOnAllItems(tis -> {
                    BeltItemElement tracker = new BeltItemElement(tis);
                    scene.addElement(tracker);
                    scene.linkElement(tracker, link);
                    return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
                });
            });
            flapFunnel(beltLocation.up(), true);
            return link;
        }

        public void removeItemsFromBelt(BlockPos beltLocation) {
            addInstruction(scene -> {
                PonderLevel world = scene.getWorld();
                BlockEntity blockEntity = world.getBlockEntity(beltLocation);
                if (!(blockEntity instanceof SmartBlockEntity beltBlockEntity))
                    return;
                TransportedItemStackHandlerBehaviour transporter = beltBlockEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
                if (transporter == null)
                    return;
                transporter.handleCenteredProcessingOnAllItems(.52f, tis -> TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
            });
        }

        public void stallBeltItem(ElementLink<BeltItemElement> link, boolean stalled) {
            addInstruction(scene -> {
                BeltItemElement resolve = scene.resolve(link);
                if (resolve != null)
                    resolve.ifPresent(tis -> tis.locked = stalled);
            });
        }

        public void changeBeltItemTo(ElementLink<BeltItemElement> link, ItemStack newStack) {
            addInstruction(scene -> {
                BeltItemElement resolve = scene.resolve(link);
                if (resolve != null)
                    resolve.ifPresent(tis -> tis.stack = newStack);
            });
        }

        public void setKineticSpeed(Selection selection, float speed) {
            modifyKineticSpeed(selection, f -> speed);
        }

        public void multiplyKineticSpeed(Selection selection, float modifier) {
            modifyKineticSpeed(selection, f -> f * modifier);
        }

        public void modifyKineticSpeed(Selection selection, UnaryOperator<Float> speedFunc) {
            modifyBlockEntityNBT(
                selection, SpeedGaugeBlockEntity.class, nbt -> {
                    float newSpeed = speedFunc.apply(nbt.getFloat("Speed", 0));
                    nbt.putFloat("Value", SpeedGaugeBlockEntity.getDialTarget(newSpeed));
                }
            );
            modifyBlockEntityNBT(
                selection, KineticBlockEntity.class, nbt -> {
                    nbt.putFloat("Speed", speedFunc.apply(nbt.getFloat("Speed", 0)));
                }
            );
        }

        public void propagatePipeChange(BlockPos pos) {
            modifyBlockEntity(pos, PumpBlockEntity.class, be -> be.onSpeedChanged(0));
        }

        public void setFilterData(Selection selection, Class<? extends BlockEntity> teType, ItemStack filter) {
            modifyBlockEntityNBT(
                selection, teType, nbt -> {
                    if (!filter.isEmpty()) {
                        RegistryOps<NbtElement> ops = world().getHolderLookupProvider().getOps(NbtOps.INSTANCE);
                        nbt.put("Filter", ItemStack.CODEC, ops, filter);
                    }
                }
            );
        }

        public void instructArm(BlockPos armLocation, ArmBlockEntity.Phase phase, ItemStack heldItem, int targetedPoint) {
            modifyBlockEntityNBT(
                scene.getSceneBuildingUtil().select().position(armLocation), ArmBlockEntity.class, compound -> {
                    compound.put("Phase", ArmBlockEntity.Phase.CODEC, phase);
                    if (!heldItem.isEmpty()) {
                        RegistryOps<NbtElement> ops = world().getHolderLookupProvider().getOps(NbtOps.INSTANCE);
                        compound.put("HeldItem", ItemStack.CODEC, ops, heldItem);
                    } else {
                        compound.remove("HeldItem");
                    }
                    compound.putInt("TargetPointIndex", targetedPoint);
                    compound.putFloat("MovementProgress", 0);
                }
            );
        }

        public void flapFunnel(BlockPos position, boolean outward) {
            modifyBlockEntity(position, FunnelBlockEntity.class, funnel -> funnel.flap(!outward));
        }

        public void setCraftingResult(BlockPos crafter, ItemStack output) {
            modifyBlockEntity(crafter, MechanicalCrafterBlockEntity.class, mct -> mct.setScriptedResult(output));
        }

        public void connectCrafterInvs(BlockPos position1, BlockPos position2) {
            addInstruction(s -> {
                ConnectedInputHandler.toggleConnection(s.getWorld(), position1, position2);
                s.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
            });
        }

        public void toggleControls(BlockPos position) {
            cycleBlockProperty(position, ControlsBlock.VIRTUAL);
        }

        public void animateTrainStation(BlockPos position, boolean trainPresent) {
            modifyBlockEntityNBT(
                getScene().getSceneBuildingUtil().select().position(position),
                StationBlockEntity.class,
                c -> c.putBoolean("ForceFlag", trainPresent)
            );
        }

        public void conductorBlaze(BlockPos position, boolean conductor) {
            modifyBlockEntityNBT(
                getScene().getSceneBuildingUtil().select().position(position),
                BlazeBurnerBlockEntity.class,
                c -> c.putBoolean("TrainHat", conductor)
            );
        }

        public void changeSignalState(BlockPos position, SignalBlockEntity.SignalState state) {
            modifyBlockEntityNBT(
                getScene().getSceneBuildingUtil().select().position(position),
                SignalBlockEntity.class,
                c -> c.put("State", SignalBlockEntity.SignalState.CODEC, state)
            );
        }

        public void setDisplayBoardText(BlockPos position, int line, Text text) {
            modifyBlockEntity(position, FlapDisplayBlockEntity.class, t -> t.applyTextManually(line, text));
        }

        public void dyeDisplayBoard(BlockPos position, int line, DyeColor color) {
            modifyBlockEntity(position, FlapDisplayBlockEntity.class, t -> t.setColour(line, color));
        }

        public void flashDisplayLink(BlockPos position) {
            modifyBlockEntity(position, LinkWithBulbBlockEntity.class, LinkWithBulbBlockEntity::pulse);
        }

        @Override
        public void restoreBlocks(Selection selection) {
            super.restoreBlocks(selection);
            markSmartBlockEntityVirtual(selection);
        }

        @Override
        public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
            super.setBlocks(selection, state, spawnParticles);
            markSmartBlockEntityVirtual(selection);
        }

        @Override
        public void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
            super.modifyBlocks(selection, stateFunc, spawnParticles);
            markSmartBlockEntityVirtual(selection);
        }

        private void markSmartBlockEntityVirtual(Selection selection) {
            addInstruction(scene -> selection.forEach(pos -> {
                if (scene.getWorld().getBlockEntity(pos) instanceof SmartBlockEntity smartBlockEntity)
                    smartBlockEntity.markVirtual();
            }));
        }
    }

    public class SpecialInstructions extends PonderSpecialInstructions {

        @Override
        public ElementLink<ParrotElement> createBirb(Vec3d location, Supplier<? extends ParrotPose> pose) {
            ElementLink<ParrotElement> link = new ElementLinkImpl<>(ParrotElement.class);
            ParrotElement parrot = ExpandedParrotElement.create(location, pose);
            addInstruction(new CreateParrotInstruction(10, Direction.DOWN, parrot));
            addInstruction(scene -> scene.linkElement(parrot, link));
            return link;
        }

        public ElementLink<ParrotElement> birbOnTurntable(BlockPos pos) {
            return createBirb(VecHelper.getCenterOf(pos), () -> new ParrotSpinOnComponentPose(pos));
        }

        public ElementLink<ParrotElement> birbOnSpinnyShaft(BlockPos pos) {
            return createBirb(VecHelper.getCenterOf(pos).add(0, 0.5, 0), () -> new ParrotSpinOnComponentPose(pos));
        }

        public void conductorBirb(ElementLink<ParrotElement> birb, boolean conductor) {
            addInstruction(scene -> scene.resolveOptional(birb).map(FunctionalHelper.filterAndCast(ExpandedParrotElement.class))
                .ifPresent(expandedBirb -> expandedBirb.setConductor(conductor)));
        }

        public static class ParrotSpinOnComponentPose extends ParrotPose {
            private final BlockPos componentPos;

            public ParrotSpinOnComponentPose(BlockPos componentPos) {
                this.componentPos = componentPos;
            }

            @Override
            public void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
                BlockEntity blockEntity = scene.getWorld().getBlockEntity(componentPos);
                if (!(blockEntity instanceof KineticBlockEntity))
                    return;
                float rpm = ((KineticBlockEntity) blockEntity).getSpeed();
                entity.lastYaw = entity.getYaw();
                entity.setYaw(entity.lastYaw + (rpm * .3f));
            }
        }
    }

}