package com.zurrtum.create.content.logistics.packagerLink;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.packager.IdentifiedInventory;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagingRequest;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PackagerLinkBlockEntity extends LinkWithBulbBlockEntity {

    public LogisticallyLinkedBehaviour behaviour;
    public UUID placedBy;

    public PackagerLinkBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PACKAGER_LINK, pos, state);
        setLazyTickRate(10);
        placedBy = null;
    }

    public InventorySummary fetchSummaryFromPackager(@Nullable IdentifiedInventory ignoredHandler) {
        PackagerBlockEntity packager = getPackager();
        if (packager == null)
            return InventorySummary.EMPTY;
        if (packager.isTargetingSameInventory(ignoredHandler))
            return InventorySummary.EMPTY;
        return packager.getAvailableItems();
    }

    public void playEffect() {
        AllSoundEvents.STOCK_LINK.playAt(world, pos, 0.75f, 1.25f, false);
        Vec3d vec3 = Vec3d.ofCenter(pos);

        BlockState state = getCachedState();
        float f = 1;

        BlockFace face = state.get(PackagerLinkBlock.FACE, BlockFace.FLOOR);
        if (face != BlockFace.FLOOR)
            f = -1;
        if (face == BlockFace.WALL)
            vec3 = vec3.add(0, 0.25, 0);

        vec3 = vec3.add(Vec3d.of(state.get(PackagerLinkBlock.FACING, Direction.SOUTH).getVector()).multiply(f * 0.125));

        pulse();
        world.addParticleClient(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, face == BlockFace.CEILING ? -1 : 1, 1);
    }

    public Pair<PackagerBlockEntity, PackagingRequest> processRequest(
        ItemStack stack,
        int amount,
        String address,
        int linkIndex,
        MutableBoolean finalLink,
        int orderId,
        @Nullable PackageOrderWithCrafts context,
        @Nullable IdentifiedInventory ignoredHandler
    ) {
        PackagerBlockEntity packager = getPackager();
        if (packager == null)
            return null;
        if (packager.isTargetingSameInventory(ignoredHandler))
            return null;

        InventorySummary summary = packager.getAvailableItems();
        int availableCount = summary.getCountOf(stack);
        if (availableCount == 0)
            return null;
        int toWithdraw = Math.min(amount, availableCount);
        return Pair.of(packager, PackagingRequest.create(stack, toWithdraw, address, linkIndex, finalLink, 0, orderId, context));
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (placedBy != null)
            view.put("PlacedBy", Uuids.INT_STREAM_CODEC, placedBy);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        placedBy = view.read("PlacedBy", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, true));
    }

    @Override
    public void initialize() {
        super.initialize();
        behaviour.redstonePowerChanged(PackagerLinkBlock.getPower(getCachedState(), world, pos));
        PackagerBlockEntity packager = getPackager();
        if (packager != null)
            packager.recheckIfLinksPresent();
    }

    @Nullable
    public PackagerBlockEntity getPackager() {
        BlockState blockState = getCachedState();
        if (behaviour.redstonePower == 15)
            return null;
        BlockPos source = pos.offset(PackagerLinkBlock.getConnectedDirection(blockState).getOpposite());
        if (!(world.getBlockEntity(source) instanceof PackagerBlockEntity packager))
            return null;
        if (packager instanceof RepackagerBlockEntity)
            return null;
        return packager;
    }

    @Override
    public Direction getBulbFacing(BlockState state) {
        return PackagerLinkBlock.getConnectedDirection(state);
    }

    private static final Map<BlockState, Vec3d> bulbOffsets = new HashMap<>();

    @Override
    public Vec3d getBulbOffset(BlockState state) {
        return bulbOffsets.computeIfAbsent(
            state, s -> {
                Vec3d offset = VecHelper.voxelSpace(5, 6, 11);
                Vec3d wallOffset = VecHelper.voxelSpace(11, 6, 5);
                BlockFace face = s.get(PackagerLinkBlock.FACE);
                Vec3d vec = face == BlockFace.WALL ? wallOffset : offset;
                float angle = AngleHelper.horizontalAngle(s.get(PackagerLinkBlock.FACING));
                if (face == BlockFace.CEILING)
                    angle = -angle;
                if (face == BlockFace.WALL)
                    angle = 0;
                return VecHelper.rotateCentered(vec, angle, Axis.Y);
            }
        );
    }

}
