package com.zurrtum.create.content.contraptions.minecart.capability;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Extended code for Minecarts, this allows for handling stalled carts and
 * coupled trains
 */
public class MinecartController {
    public static final PacketCodec<RegistryByteBuf, MinecartController> PACKET_CODEC = PacketCodec.tuple(
        Couple.streamCodec(StallData.PACKET_CODEC.collect(PacketCodecs::optional)),
        i -> i.stallData,
        Couple.streamCodec(CouplingData.PACKET_CODEC.collect(PacketCodecs::optional)),
        i -> i.couplings,
        MinecartController::new
    );
    private boolean needsEntryRefresh;
    private AbstractMinecartEntity cart;

    /*
     * Stall information, <Internal (waiting couplings), External (stalled
     * contraptions)>
     */
    private Couple<Optional<StallData>> stallData;

    /*
     * Coupling information, <Main (helmed by this cart), Connected (handled by
     * other cart)>
     */
    private Couple<Optional<CouplingData>> couplings;

    public MinecartController(AbstractMinecartEntity minecart) {
        cart = minecart;
        stallData = Couple.create(Optional::empty);
        couplings = Couple.create(Optional::empty);
        needsEntryRefresh = true;
    }

    private MinecartController(Couple<Optional<StallData>> stallData, Couple<Optional<CouplingData>> couplings) {
        this.stallData = stallData;
        this.couplings = couplings;
        needsEntryRefresh = true;
    }

    public AbstractMinecartEntity cart() {
        return cart;
    }

    public void coupleWith(boolean isLeading, UUID coupled, float length, boolean contraption) {
        UUID mainID = isLeading ? cart().getUuid() : coupled;
        UUID connectedID = isLeading ? coupled : cart().getUuid();
        couplings.set(isLeading, Optional.of(new CouplingData(mainID, connectedID, length, contraption)));
        needsEntryRefresh |= isLeading;
        sendData();
    }

    public void decouple() {
        couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {
            UUID idOfOther = cd.idOfCart(!main);
            MinecartController otherCart = CapabilityMinecartController.getIfPresent(cart.getEntityWorld(), idOfOther);
            if (otherCart == null)
                return;

            removeConnection(main);
            otherCart.removeConnection(!main);
        }));
    }

    private void disassemble(AbstractMinecartEntity cart) {
        if (cart instanceof MinecartEntity) {
            return;
        }
        List<Entity> passengers = cart.getPassengerList();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof AbstractContraptionEntity)) {
            return;
        }
        World world = cart.getEntityWorld();
        int i = MathHelper.floor(cart.getX());
        int j = MathHelper.floor(cart.getY());
        int k = MathHelper.floor(cart.getZ());
        if (world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.isIn(BlockTags.RAILS) && blockstate.getBlock() == Blocks.ACTIVATOR_RAIL) {
            if (cart.hasPassengers()) {
                cart.removeAllPassengers();
            }

            if (cart.getDamageWobbleTicks() == 0) {
                cart.setDamageWobbleSide(-cart.getDamageWobbleSide());
                cart.setDamageWobbleTicks(10);
                cart.setDamageWobbleStrength(50.0F);
                cart.velocityModified = true;
            }
        }
    }

    @Nullable
    public UUID getCoupledCart(boolean asMain) {
        Optional<CouplingData> optional = couplings.get(asMain);
        if (optional.isEmpty())
            return null;
        CouplingData couplingData = optional.get();
        return asMain ? couplingData.connectedCartID : couplingData.mainCartID;
    }

    public float getCouplingLength(boolean leading) {
        Optional<CouplingData> optional = couplings.get(leading);
        return optional.map(couplingData -> couplingData.length).orElse(0F);
    }

    public boolean hasContraptionCoupling(boolean current) {
        Optional<CouplingData> optional = couplings.get(current);
        return optional.isPresent() && optional.get().contraption;
    }

    public boolean isConnectedToCoupling() {
        return couplings.get(false).isPresent();
    }

    public boolean isCoupledThroughContraption() {
        return couplings.stream().anyMatch(i -> i.map(CouplingData::getContraption).orElse(false));
    }

    public boolean isFullyCoupled() {
        return isLeadingCoupling() && isConnectedToCoupling();
    }

    public boolean isLeadingCoupling() {
        return couplings.get(true).isPresent();
    }

    public boolean isPresent() {
        return cart.isAlive();
    }

    public boolean isStalled() {
        return isStalled(true) || isStalled(false);
    }

    private boolean isStalled(boolean internal) {
        return stallData.get(internal).isPresent();
    }

    public void prepareForCoupling(boolean isLeading) {
        // reverse existing chain if necessary
        if (isLeading && isLeadingCoupling() || !isLeading && isConnectedToCoupling()) {

            List<MinecartController> cartsToFlip = new ArrayList<>();
            cartsToFlip.add(this);
            MinecartController current = this;
            boolean forward = current.isLeadingCoupling();
            int safetyCount = 1000;

            World world = cart.getEntityWorld();
            while (safetyCount-- > 0) {
                Optional<MinecartController> next = CouplingHandler.getNextInCouplingChain(world, current, forward);
                if (next.isEmpty()) {
                    break;
                }
                current = next.get();
                cartsToFlip.add(current);
            }
            if (safetyCount == 0) {
                Create.LOGGER.warn("Infinite loop in coupling iteration");
                return;
            }

            for (MinecartController minecartController : cartsToFlip) {
                minecartController.couplings.forEachWithContext((opt, leading) -> opt.ifPresent(cd -> {
                    cd.flip();
                    if (!cd.contraption)
                        return;
                    List<Entity> passengers = minecartController.cart().getPassengerList();
                    if (passengers.isEmpty())
                        return;
                    Entity entity = passengers.getFirst();
                    if (!(entity instanceof OrientedContraptionEntity contraption))
                        return;
                    UUID couplingId = contraption.getCouplingId();
                    if (couplingId == cd.mainCartID) {
                        contraption.setCouplingId(cd.connectedCartID);
                        return;
                    }
                    if (couplingId == cd.connectedCartID) {
                        contraption.setCouplingId(cd.mainCartID);
                    }
                }));
                minecartController.couplings = minecartController.couplings.swap();
                minecartController.needsEntryRefresh = true;
                if (minecartController == this)
                    continue;
                minecartController.sendData();
            }
        }
    }

    public void removeConnection(boolean main) {
        if (hasContraptionCoupling(main)) {
            World world = cart.getEntityWorld();
            if (world != null && !world.isClient()) {
                List<Entity> passengers = cart().getPassengerList();
                if (!passengers.isEmpty()) {
                    Entity entity = passengers.getFirst();
                    if (entity instanceof AbstractContraptionEntity abstractContraptionEntity)
                        abstractContraptionEntity.disassemble();
                }
            }
        }

        couplings.set(main, Optional.empty());
        needsEntryRefresh |= main;
        sendData();
    }

    public void sendData() {
        sendData(null);
    }

    public void sendData(@Nullable AbstractMinecartEntity cart) {
        if (cart != null) {
            this.cart = cart;
            needsEntryRefresh = true;
        }
        if (this.cart.getEntityWorld().isClient()) {
            return;
        }
        AllSynchedDatas.MINECART_CONTROLLER.set(this.cart, Optional.of(this), true);
    }

    public void setCart(AbstractMinecartEntity cart) {
        this.cart = cart;
    }

    private void setStalled(boolean stall, boolean internal) {
        if (isStalled(internal) == stall || cart == null)
            return;

        if (stall) {
            stallData.set(internal, Optional.of(new StallData(cart)));
            sendData();
            return;
        }

        if (!isStalled(!internal))
            stallData.get(internal).ifPresent(data -> data.release(cart));
        stallData.set(internal, Optional.empty());

        sendData();
    }

    public void setStalledExternally(boolean stall) {
        setStalled(stall, false);
    }

    public void tick() {
        if (cart == null) {
            return;
        }
        World world = cart.getEntityWorld();
        if (world == null) {
            return;
        }

        if (needsEntryRefresh) {
            CapabilityMinecartController.queuedAdditions.get(world).add(cart);
            needsEntryRefresh = false;
        }

        stallData.forEach(opt -> opt.ifPresent(sd -> sd.tick(cart)));

        MutableBoolean internalStall = new MutableBoolean(false);
        couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {

            UUID idOfOther = cd.idOfCart(!main);
            MinecartController otherCart = CapabilityMinecartController.getIfPresent(world, idOfOther);
            internalStall.setValue(internalStall.booleanValue() || otherCart == null || !otherCart.isPresent() || otherCart.isStalled(false));

        }));
        if (!world.isClient()) {
            setStalled(internalStall.booleanValue(), true);
            disassemble(cart);
        }
    }

    private static class CouplingData {
        public static final PacketCodec<RegistryByteBuf, CouplingData> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            i -> i.mainCartID,
            Uuids.PACKET_CODEC,
            i -> i.connectedCartID,
            PacketCodecs.FLOAT,
            i -> i.length,
            PacketCodecs.BOOLEAN,
            i -> i.contraption,
            CouplingData::new
        );
        private final float length;
        private final boolean contraption;
        private UUID mainCartID;
        private UUID connectedCartID;

        public CouplingData(UUID mainCartID, UUID connectedCartID, float length, boolean contraption) {
            this.mainCartID = mainCartID;
            this.connectedCartID = connectedCartID;
            this.length = length;
            this.contraption = contraption;
        }

        public void flip() {
            UUID swap = mainCartID;
            mainCartID = connectedCartID;
            connectedCartID = swap;
        }

        public boolean getContraption() {
            return contraption;
        }

        public UUID idOfCart(boolean main) {
            return main ? mainCartID : connectedCartID;
        }
    }

    private record StallData(Vec3d position, Vec3d motion, float yaw, float pitch) {
        public static final PacketCodec<RegistryByteBuf, StallData> PACKET_CODEC = PacketCodec.tuple(
            Vec3d.PACKET_CODEC,
            StallData::position,
            Vec3d.PACKET_CODEC,
            StallData::motion,
            PacketCodecs.FLOAT,
            StallData::yaw,
            PacketCodecs.FLOAT,
            StallData::pitch,
            StallData::new
        );

        public StallData(AbstractMinecartEntity entity) {
            this(entity.getPos(), entity.getVelocity(), entity.getYaw(), entity.getPitch());
            tick(entity);
        }

        public void release(AbstractMinecartEntity entity) {
            entity.setVelocity(motion);
        }

        public void tick(AbstractMinecartEntity entity) {
            entity.setVelocity(Vec3d.ZERO);
            entity.setYaw(yaw);
            entity.setPitch(pitch);
        }
    }
}
