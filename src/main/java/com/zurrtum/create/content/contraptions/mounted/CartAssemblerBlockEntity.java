package com.zurrtum.create.content.contraptions.mounted;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import com.zurrtum.create.content.redstone.rail.ControllerRailBlock;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CartAssemblerBlockEntity extends SmartBlockEntity {
    private static final int assemblyCooldown = 8;

    protected ServerScrollOptionBehaviour<CartMovementMode> movementMode;
    private int ticksSinceMinecartUpdate;
    protected @Nullable AssemblyException lastException;
    protected @Nullable AbstractMinecart cartToAssemble;

    public CartAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CART_ASSEMBLER, pos, state);
        ticksSinceMinecartUpdate = assemblyCooldown;
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksSinceMinecartUpdate < assemblyCooldown) {
            ticksSinceMinecartUpdate++;
        }

        tryAssemble(cartToAssemble);
        cartToAssemble = null;
    }

    public void tryAssemble(@Nullable AbstractMinecart cart) {
        if (cart == null)
            return;

        if (!isMinecartUpdateValid())
            return;
        resetTicksSinceMinecartUpdate();

        BlockState state = level.getBlockState(worldPosition);
        if (!state.is(AllBlocks.CART_ASSEMBLER))
            return;
        CartAssemblerBlock.CartAssemblerAction action = CartAssemblerBlock.getActionForCart(state, cart);
        if (action.shouldAssemble())
            assemble(level, worldPosition, cart);
        if (action.shouldDisassemble())
            disassemble(level, worldPosition, cart);
        if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE) {
            if (cart.getDeltaMovement().length() > 1 / 128f) {
                Direction facing = cart.getMotionDirection();
                RailShape railShape = state.getValue(CartAssemblerBlock.RAIL_SHAPE);
                for (Direction d : Iterate.directionsInAxis(railShape == RailShape.EAST_WEST ? Axis.X : Axis.Z))
                    if (level.getBlockState(worldPosition.relative(d)).isRedstoneConductor(level, worldPosition.relative(d)))
                        facing = d.getOpposite();

                double speed = cart.getMaxSpeed((ServerLevel) level);
                cart.setDeltaMovement(facing.getStepX() * speed, facing.getStepY() * speed, facing.getStepZ() * speed);
            }
        }
        if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL) {
            Vec3i accelerationVector = ControllerRailBlock.getAccelerationVector(AllBlocks.CONTROLLER_RAIL.defaultBlockState()
                .setValue(ControllerRailBlock.SHAPE, state.getValue(CartAssemblerBlock.RAIL_SHAPE))
                .setValue(ControllerRailBlock.BACKWARDS, state.getValue(CartAssemblerBlock.BACKWARDS)));
            double speed = cart.getMaxSpeed((ServerLevel) level);
            cart.setDeltaMovement(Vec3.atLowerCornerOf(accelerationVector).scale(speed));
        }
        if (action == CartAssemblerBlock.CartAssemblerAction.DISASSEMBLE_BRAKE) {
            Vec3 diff = VecHelper.getCenterOf(worldPosition).subtract(cart.position());
            cart.setDeltaMovement(diff.x / 16f, 0, diff.z / 16f);
        }
    }

    protected void assemble(Level world, BlockPos pos, AbstractMinecart cart) {
        if (!cart.getPassengers().isEmpty())
            return;

        Optional<MinecartController> value = AllSynchedDatas.MINECART_CONTROLLER.get(cart);
        if (value.map(MinecartController::isCoupledThroughContraption).orElse(false)) {
            return;
        }

        CartMovementMode mode = CartMovementMode.values()[movementMode.getValue()];

        MountedContraption contraption = new MountedContraption(mode);
        try {
            if (!contraption.assemble(world, pos))
                return;

            lastException = null;
            sendData();
        } catch (AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }

        boolean couplingFound = contraption.connectedCart != null;
        Direction initialOrientation = CartAssemblerBlock.getHorizontalDirection(getBlockState());

        if (couplingFound) {
            cart.setPos(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
            if (!CouplingHandler.tryToCoupleCarts(null, world, cart.getId(), contraption.connectedCart.getId()))
                return;
        }

        contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
        contraption.startMoving(world);
        contraption.expandBoundsAroundAxis(Axis.Y);

        if (couplingFound) {
            Vec3 diff = contraption.connectedCart.position().subtract(cart.position());
            initialOrientation = Direction.fromYRot(Mth.atan2(diff.z, diff.x) * 180 / Math.PI);
        }

        OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
        if (couplingFound)
            entity.setCouplingId(cart.getUUID());
        entity.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        world.addFreshEntity(entity);
        entity.startRiding(cart);

        if (cart instanceof MinecartFurnace) {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
                TagValueOutput view = TagValueOutput.createWithContext(logging, world.registryAccess());
                if (cart.save(view)) {
                    view.putDouble("PushZ", 0);
                    view.putDouble("PushX", 0);
                    ValueInput data = TagValueInput.create(logging, world.registryAccess(), view.buildResult());
                    cart.load(data);
                }
            }
        }

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);
    }

    protected void disassemble(Level world, BlockPos pos, AbstractMinecart cart) {
        if (cart.getPassengers().isEmpty())
            return;
        Entity entity = cart.getPassengers().getFirst();
        if (!(entity instanceof OrientedContraptionEntity contraption))
            return;
        UUID couplingId = contraption.getCouplingId();

        if (couplingId == null) {
            contraption.yaw = CartAssemblerBlock.getHorizontalDirection(getBlockState()).toYRot();
            disassembleCart(cart);
            return;
        }

        Couple<MinecartController> coupledCarts = contraption.getCoupledCartsIfPresent();
        if (coupledCarts == null)
            return;

        // Make sure connected cart is present and being disassembled
        for (boolean current : Iterate.trueAndFalse) {
            MinecartController minecartController = coupledCarts.get(current);
            if (minecartController.cart() == cart)
                continue;
            BlockPos otherPos = minecartController.cart().blockPosition();
            BlockState blockState = world.getBlockState(otherPos);
            if (!blockState.is(AllBlocks.CART_ASSEMBLER))
                return;
            if (!CartAssemblerBlock.getActionForCart(blockState, minecartController.cart()).shouldDisassemble())
                return;
        }

        for (boolean current : Iterate.trueAndFalse)
            coupledCarts.get(current).removeConnection(current);
        disassembleCart(cart);
    }

    protected void disassembleCart(AbstractMinecart cart) {
        cart.ejectPassengers();
        if (cart instanceof MinecartFurnace) {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
                TagValueOutput view = TagValueOutput.createWithContext(logging, level.registryAccess());
                cart.saveAsPassenger(view);
                Vec3 velocity = cart.getDeltaMovement();
                view.putDouble("PushX", velocity.x);
                view.putDouble("PushZ", velocity.z);
                ValueInput data = TagValueInput.create(logging, level.registryAccess(), view.buildResult());
                cart.load(data);
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementMode = new ServerScrollOptionBehaviour<>(CartMovementMode.class, this);
        behaviours.add(movementMode);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        AssemblyException.write(view, lastException);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        lastException = AssemblyException.read(view);
        super.read(view, clientPacket);
    }

    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    public enum CartMovementMode implements StringRepresentable {
        ROTATE,
        ROTATE_PAUSED,
        ROTATION_LOCKED;

        public static final Codec<CartMovementMode> CODEC = StringRepresentable.fromEnum(CartMovementMode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public void resetTicksSinceMinecartUpdate() {
        ticksSinceMinecartUpdate = 0;
    }

    public void assembleNextTick(AbstractMinecart cart) {
        if (cartToAssemble == null)
            cartToAssemble = cart;
    }

    public boolean isMinecartUpdateValid() {
        return ticksSinceMinecartUpdate >= assemblyCooldown;
    }

}
