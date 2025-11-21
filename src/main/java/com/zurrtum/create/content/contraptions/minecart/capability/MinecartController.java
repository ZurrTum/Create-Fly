package com.zurrtum.create.content.contraptions.minecart.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
    public static final Codec<MinecartController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Couple.optionalCodec(StallData.CODEC).fieldOf("stallData").forGetter(i -> i.stallData),
        Couple.optionalCodec(CouplingData.CODEC).fieldOf("couplings").forGetter(i -> i.couplings)
    ).apply(instance, MinecartController::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinecartController> PACKET_CODEC = StreamCodec.composite(
        Couple.streamCodec(StallData.PACKET_CODEC.apply(ByteBufCodecs::optional)),
        i -> i.stallData,
        Couple.streamCodec(CouplingData.PACKET_CODEC.apply(ByteBufCodecs::optional)),
        i -> i.couplings,
        MinecartController::new
    );
    private boolean needsEntryRefresh;
    private AbstractMinecart cart;

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

    public MinecartController(AbstractMinecart minecart) {
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

    public AbstractMinecart cart() {
        return cart;
    }

    public void coupleWith(boolean isLeading, UUID coupled, float length, boolean contraption) {
        UUID mainID = isLeading ? cart().getUUID() : coupled;
        UUID connectedID = isLeading ? coupled : cart().getUUID();
        couplings.set(isLeading, Optional.of(new CouplingData(mainID, connectedID, length, contraption)));
        needsEntryRefresh |= isLeading;
        sendData();
    }

    public void decouple() {
        couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {
            UUID idOfOther = cd.idOfCart(!main);
            MinecartController otherCart = CapabilityMinecartController.getIfPresent(cart.level(), idOfOther);
            if (otherCart == null)
                return;

            removeConnection(main);
            otherCart.removeConnection(!main);
        }));
    }

    private void disassemble(AbstractMinecart cart) {
        if (cart instanceof Minecart) {
            return;
        }
        List<Entity> passengers = cart.getPassengers();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof AbstractContraptionEntity)) {
            return;
        }
        Level world = cart.level();
        int i = Mth.floor(cart.getX());
        int j = Mth.floor(cart.getY());
        int k = Mth.floor(cart.getZ());
        if (world.getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
            --j;
        }
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(BlockTags.RAILS) && blockstate.getBlock() == Blocks.ACTIVATOR_RAIL) {
            if (cart.isVehicle()) {
                cart.ejectPassengers();
            }

            if (cart.getHurtTime() == 0) {
                cart.setHurtDir(-cart.getHurtDir());
                cart.setHurtTime(10);
                cart.setDamage(50.0F);
                cart.hurtMarked = true;
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

            Level world = cart.level();
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
                    List<Entity> passengers = minecartController.cart().getPassengers();
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
            Level world = cart.level();
            if (world != null && !world.isClientSide()) {
                List<Entity> passengers = cart().getPassengers();
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

    public void sendData(@Nullable AbstractMinecart cart) {
        if (cart != null) {
            this.cart = cart;
            needsEntryRefresh = true;
        }
        if (this.cart.level().isClientSide()) {
            return;
        }
        AllSynchedDatas.MINECART_CONTROLLER.set(this.cart, Optional.of(this), true);
    }

    public void setCart(AbstractMinecart cart) {
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
        Level world = cart.level();
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
        if (!world.isClientSide()) {
            setStalled(internalStall.booleanValue(), true);
            disassemble(cart);
        }
    }

    private static class CouplingData {
        public static final Codec<CouplingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("mainCartID").forGetter(i -> i.mainCartID),
            UUIDUtil.CODEC.fieldOf("connectedCartID").forGetter(i -> i.connectedCartID),
            Codec.FLOAT.fieldOf("length").forGetter(i -> i.length),
            Codec.BOOL.fieldOf("contraption").forGetter(i -> i.contraption)
        ).apply(instance, CouplingData::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CouplingData> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            i -> i.mainCartID,
            UUIDUtil.STREAM_CODEC,
            i -> i.connectedCartID,
            ByteBufCodecs.FLOAT,
            i -> i.length,
            ByteBufCodecs.BOOL,
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

    private record StallData(Vec3 position, Vec3 motion, float yaw, float pitch) {
        public static final Codec<StallData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("position").forGetter(StallData::position),
            Vec3.CODEC.fieldOf("motion").forGetter(StallData::motion),
            Codec.FLOAT.fieldOf("yaw").forGetter(StallData::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(StallData::pitch)
        ).apply(instance, StallData::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StallData> PACKET_CODEC = StreamCodec.composite(
            Vec3.STREAM_CODEC,
            StallData::position,
            Vec3.STREAM_CODEC,
            StallData::motion,
            ByteBufCodecs.FLOAT,
            StallData::yaw,
            ByteBufCodecs.FLOAT,
            StallData::pitch,
            StallData::new
        );

        public StallData(AbstractMinecart entity) {
            this(entity.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
            tick(entity);
        }

        public void release(AbstractMinecart entity) {
            entity.setDeltaMovement(motion);
        }

        public void tick(AbstractMinecart entity) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setYRot(yaw);
            entity.setXRot(pitch);
        }
    }
}
