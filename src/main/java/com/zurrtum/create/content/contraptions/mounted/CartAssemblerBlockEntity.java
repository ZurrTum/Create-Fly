package com.zurrtum.create.content.contraptions.mounted;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CartAssemblerBlockEntity extends SmartBlockEntity {
    private static final int assemblyCooldown = 8;

    protected ServerScrollOptionBehaviour<CartMovementMode> movementMode;
    private int ticksSinceMinecartUpdate;
    protected AssemblyException lastException;
    protected AbstractMinecartEntity cartToAssemble;

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

    public void tryAssemble(AbstractMinecartEntity cart) {
        if (cart == null)
            return;

        if (!isMinecartUpdateValid())
            return;
        resetTicksSinceMinecartUpdate();

        BlockState state = world.getBlockState(pos);
        if (!state.isOf(AllBlocks.CART_ASSEMBLER))
            return;
        CartAssemblerBlock.CartAssemblerAction action = CartAssemblerBlock.getActionForCart(state, cart);
        if (action.shouldAssemble())
            assemble(world, pos, cart);
        if (action.shouldDisassemble())
            disassemble(world, pos, cart);
        if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE) {
            if (cart.getVelocity().length() > 1 / 128f) {
                Direction facing = cart.getMovementDirection();
                RailShape railShape = state.get(CartAssemblerBlock.RAIL_SHAPE);
                for (Direction d : Iterate.directionsInAxis(railShape == RailShape.EAST_WEST ? Axis.X : Axis.Z))
                    if (world.getBlockState(pos.offset(d)).isSolidBlock(world, pos.offset(d)))
                        facing = d.getOpposite();

                double speed = cart.getMaxSpeed((ServerWorld) world);
                cart.setVelocity(facing.getOffsetX() * speed, facing.getOffsetY() * speed, facing.getOffsetZ() * speed);
            }
        }
        if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL) {
            Vec3i accelerationVector = ControllerRailBlock.getAccelerationVector(AllBlocks.CONTROLLER_RAIL.getDefaultState()
                .with(ControllerRailBlock.SHAPE, state.get(CartAssemblerBlock.RAIL_SHAPE))
                .with(ControllerRailBlock.BACKWARDS, state.get(CartAssemblerBlock.BACKWARDS)));
            double speed = cart.getMaxSpeed((ServerWorld) world);
            cart.setVelocity(Vec3d.of(accelerationVector).multiply(speed));
        }
        if (action == CartAssemblerBlock.CartAssemblerAction.DISASSEMBLE_BRAKE) {
            Vec3d diff = VecHelper.getCenterOf(pos).subtract(cart.getPos());
            cart.setVelocity(diff.x / 16f, 0, diff.z / 16f);
        }
    }

    protected void assemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
        if (!cart.getPassengerList().isEmpty())
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
        Direction initialOrientation = CartAssemblerBlock.getHorizontalDirection(getCachedState());

        if (couplingFound) {
            cart.setPosition(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
            if (!CouplingHandler.tryToCoupleCarts(null, world, cart.getId(), contraption.connectedCart.getId()))
                return;
        }

        contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
        contraption.startMoving(world);
        contraption.expandBoundsAroundAxis(Axis.Y);

        if (couplingFound) {
            Vec3d diff = contraption.connectedCart.getPos().subtract(cart.getPos());
            initialOrientation = Direction.fromHorizontalDegrees(MathHelper.atan2(diff.z, diff.x) * 180 / Math.PI);
        }

        OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
        if (couplingFound)
            entity.setCouplingId(cart.getUuid());
        entity.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        world.spawnEntity(entity);
        entity.startRiding(cart);

        if (cart instanceof FurnaceMinecartEntity) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getReporterContext(), Create.LOGGER)) {
                NbtWriteView view = NbtWriteView.create(logging, world.getRegistryManager());
                if (cart.saveData(view)) {
                    view.putDouble("PushZ", 0);
                    view.putDouble("PushX", 0);
                    ReadView data = NbtReadView.create(logging, world.getRegistryManager(), view.getNbt());
                    cart.readData(data);
                }
            }
        }

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);
    }

    protected void disassemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
        if (cart.getPassengerList().isEmpty())
            return;
        Entity entity = cart.getPassengerList().getFirst();
        if (!(entity instanceof OrientedContraptionEntity contraption))
            return;
        UUID couplingId = contraption.getCouplingId();

        if (couplingId == null) {
            contraption.yaw = CartAssemblerBlock.getHorizontalDirection(getCachedState()).getPositiveHorizontalDegrees();
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
            BlockPos otherPos = minecartController.cart().getBlockPos();
            BlockState blockState = world.getBlockState(otherPos);
            if (!blockState.isOf(AllBlocks.CART_ASSEMBLER))
                return;
            if (!CartAssemblerBlock.getActionForCart(blockState, minecartController.cart()).shouldDisassemble())
                return;
        }

        for (boolean current : Iterate.trueAndFalse)
            coupledCarts.get(current).removeConnection(current);
        disassembleCart(cart);
    }

    protected void disassembleCart(AbstractMinecartEntity cart) {
        cart.removeAllPassengers();
        if (cart instanceof FurnaceMinecartEntity) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getReporterContext(), Create.LOGGER)) {
                NbtWriteView view = NbtWriteView.create(logging, world.getRegistryManager());
                cart.saveSelfData(view);
                Vec3d velocity = cart.getVelocity();
                view.putDouble("PushX", velocity.x);
                view.putDouble("PushZ", velocity.z);
                ReadView data = NbtReadView.create(logging, world.getRegistryManager(), view.getNbt());
                cart.readData(data);
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
    public void write(WriteView view, boolean clientPacket) {
        AssemblyException.write(view, lastException);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        lastException = AssemblyException.read(view);
        super.read(view, clientPacket);
    }

    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    public enum CartMovementMode implements StringIdentifiable {
        ROTATE,
        ROTATE_PAUSED,
        ROTATION_LOCKED;

        public static final Codec<CartMovementMode> CODEC = StringIdentifiable.createCodec(CartMovementMode::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public void resetTicksSinceMinecartUpdate() {
        ticksSinceMinecartUpdate = 0;
    }

    public void assembleNextTick(AbstractMinecartEntity cart) {
        if (cartToAssemble == null)
            cartToAssemble = cart;
    }

    public boolean isMinecartUpdateValid() {
        return ticksSinceMinecartUpdate >= assemblyCooldown;
    }

}
