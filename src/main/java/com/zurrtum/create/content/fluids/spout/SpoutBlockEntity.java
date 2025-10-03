package com.zurrtum.create.content.fluids.spout;

import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

public class SpoutBlockEntity extends SmartBlockEntity {

    public static final int FILLING_TIME = 20;
    protected BeltProcessingBehaviour beltProcessing;

    public int processingTicks;
    public boolean sendSplash;
    public BlockSpoutingBehaviour customProcess;

    public SmartFluidTankBehaviour tank;

    private boolean createdSweetRoll, createdHoneyApple, createdChocolateBerries;

    public SpoutBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SPOUT, pos, state);
        processingTicks = -1;
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().stretch(0, -2, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, BucketFluidInventory.CAPACITY, SpoutFluidHandler::new);
        behaviours.add(tank);

        beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived).whileItemHeld(this::whenItemHeld);
        behaviours.add(beltProcessing);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.SPOUT, AllAdvancements.FOODS);
    }

    protected ProcessingResult onItemReceived(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (handler.blockEntity.isVirtual())
            return PASS;
        if (!FillingBySpout.canItemBeFilled(world, transported.stack))
            return PASS;
        if (tank.isEmpty())
            return HOLD;
        if (FillingBySpout.getRequiredAmountForItem((ServerWorld) world, transported.stack, getCurrentFluidInTank()) == -1)
            return PASS;
        return HOLD;
    }

    protected ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (processingTicks != -1 && processingTicks != 5)
            return HOLD;
        if (!FillingBySpout.canItemBeFilled(world, transported.stack))
            return PASS;
        if (tank.isEmpty())
            return HOLD;
        FluidStack fluid = getCurrentFluidInTank();
        int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem((ServerWorld) world, transported.stack, fluid.copy());
        if (requiredAmountForItem <= 0)
            return PASS;
        if (requiredAmountForItem > fluid.getAmount())
            return HOLD;

        if (processingTicks == -1) {
            processingTicks = FILLING_TIME;
            notifyUpdate();
            AllSoundEvents.SPOUTING.playOnServer(world, pos, 0.75f, 0.9f + 0.2f * (float) Math.random());
            return HOLD;
        }

        // Process finished
        ItemStack out = FillingBySpout.fillItem((ServerWorld) world, requiredAmountForItem, transported.stack, fluid);
        if (!out.isEmpty()) {
            transported.clearFanProcessingData();
            List<TransportedItemStack> outList = new ArrayList<>();
            TransportedItemStack held = null;
            TransportedItemStack result = transported.copy();
            result.stack = out;
            if (!transported.stack.isEmpty())
                held = transported.copy();
            outList.add(result);
            handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, held));
        }

        award(AllAdvancements.SPOUT);
        if (trackFoods()) {
            createdChocolateBerries |= out.isOf(AllItems.CHOCOLATE_BERRIES);
            createdHoneyApple |= out.isOf(AllItems.HONEYED_APPLE);
            createdSweetRoll |= out.isOf(AllItems.SWEET_ROLL);
            if (createdChocolateBerries && createdHoneyApple && createdSweetRoll)
                award(AllAdvancements.FOODS);
        }

        SmartFluidTankBehaviour.TankSegment primaryHandler = tank.getPrimaryHandler();
        primaryHandler.setFluid(fluid);
        primaryHandler.markDirty();
        sendSplash = true;
        notifyUpdate();
        return HOLD;
    }

    private FluidStack getCurrentFluidInTank() {
        return tank.getPrimaryHandler().getFluid();
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);

        view.putInt("ProcessingTicks", processingTicks);
        if (sendSplash && clientPacket) {
            view.putBoolean("Splash", true);
            sendSplash = false;
        }

        if (!trackFoods())
            return;
        if (createdChocolateBerries)
            view.putBoolean("ChocolateBerries", true);
        if (createdHoneyApple)
            view.putBoolean("HoneyApple", true);
        if (createdSweetRoll)
            view.putBoolean("SweetRoll", true);
    }

    private boolean trackFoods() {
        AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
        return behaviour != null && behaviour.isOwnerPresent();
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        processingTicks = view.getInt("ProcessingTicks", 0);

        createdChocolateBerries = view.getBoolean("ChocolateBerries", false);
        createdHoneyApple = view.getBoolean("HoneyApple", false);
        createdSweetRoll = view.getBoolean("SweetRoll", false);

        if (!clientPacket)
            return;
        if (view.getBoolean("Splash", false))
            spawnSplash(tank.getPrimaryTank().getRenderedFluid());
    }

    public void tick() {
        super.tick();

        FluidStack currentFluidInTank = getCurrentFluidInTank();
        if (processingTicks == -1 && (isVirtual() || !world.isClient()) && !currentFluidInTank.isEmpty()) {
            BlockPos filling = pos.down(2);
            BlockSpoutingBehaviour behavior = BlockSpoutingBehaviour.get(world, filling);
            if (behavior != null && behavior.fillBlock(world, filling, this, currentFluidInTank.copy(), true) > 0) {
                processingTicks = FILLING_TIME;
                customProcess = behavior;
                notifyUpdate();
            }
        }

        if (processingTicks >= 0) {
            processingTicks--;
            if (processingTicks == 5 && customProcess != null) {
                int fillBlock = customProcess.fillBlock(world, pos.down(2), this, currentFluidInTank.copy(), false);
                customProcess = null;
                if (fillBlock > 0) {
                    tank.getPrimaryHandler()
                        .setFluid(FluidHelper.copyStackWithAmount(currentFluidInTank, currentFluidInTank.getAmount() - fillBlock));
                    sendSplash = true;
                    notifyUpdate();
                }
            }
        }

        if (processingTicks >= 8 && world.isClient) {
            spawnProcessingParticles(tank.getPrimaryTank().getRenderedFluid());
        }
    }

    protected void spawnProcessingParticles(FluidStack fluid) {
        if (isVirtual() || fluid.isEmpty())
            return;
        Vec3d vec = VecHelper.getCenterOf(pos);
        vec = vec.subtract(0, 8 / 16f, 0);
        ParticleEffect particle = new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, fluid.getFluid(), fluid.getComponentChanges());
        world.addImportantParticleClient(particle, vec.x, vec.y, vec.z, 0, -.1f, 0);
    }

    protected static int SPLASH_PARTICLE_COUNT = 20;

    protected void spawnSplash(FluidStack fluid) {
        if (isVirtual() || fluid.isEmpty())
            return;
        Vec3d vec = VecHelper.getCenterOf(pos);
        vec = vec.subtract(0, 2 - 5 / 16f, 0);
        ParticleEffect particle = new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, fluid.getFluid(), fluid.getComponentChanges());
        for (int i = 0; i < SPLASH_PARTICLE_COUNT; i++) {
            Vec3d m = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, 0.125f);
            m = new Vec3d(m.x, Math.abs(m.y), m.z);
            world.addImportantParticleClient(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
    }

    public static class SpoutFluidHandler extends SmartFluidTankBehaviour.InternalFluidHandler {
        private static final int[] EMPTY_SLOTS = new int[0];

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public SpoutFluidHandler(SmartFluidTankBehaviour behaviour, boolean enforceVariety, Optional<Integer> max) {
            super(behaviour, enforceVariety, max);
        }

        @Override
        public int[] getAvailableSlots(@Nullable Direction side) {
            if (side == Direction.DOWN) {
                return EMPTY_SLOTS;
            }
            return super.getAvailableSlots(side);
        }
    }
}
