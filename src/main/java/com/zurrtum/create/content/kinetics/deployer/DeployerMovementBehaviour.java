package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.schematics.SchematicInstances;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

import static com.zurrtum.create.Create.LOGGER;

public class DeployerMovementBehaviour extends MovementBehaviour {
    @Override
    public Vec3d getActiveAreaOffset(MovementContext context) {
        return Vec3d.of(context.state.get(DeployerBlock.FACING).getVector()).multiply(2);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        if (context.world.isClient())
            return;

        tryGrabbingItem(context);
        DeployerPlayer player = getPlayer(context);
        Mode mode = getMode(context);
        if (mode == Mode.USE && !DeployerHandler.shouldActivate(player.cast().getMainHandStack(), context.world, pos, null))
            return;

        activate(context, pos, player, mode);
        checkForTrackPlacementAdvancement(context, player);
        tryDisposeOfExcess(context);
        context.stall = player.getBlockBreakingProgress() != null;
    }

    public void activate(MovementContext context, BlockPos pos, DeployerPlayer player, Mode mode) {
        World world = context.world;

        player.setPlacedTracks(false);

        FilterItemStack filter = context.getFilterFromBE();
        if (filter.item().isOf(AllItems.SCHEMATIC)) {
            activateAsSchematicPrinter(context, pos, player, world, filter.item());
            return;
        }

        Vec3d facingVec = Vec3d.of(context.state.get(DeployerBlock.FACING).getVector());
        facingVec = context.rotation.apply(facingVec);
        Vec3d vec = context.position.subtract(facingVec.multiply(2));

        float xRot = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;
        if (Math.abs(xRot) > 89) {
            Vec3d initial = new Vec3d(0, 0, 1);
            if (context.contraption.entity instanceof OrientedContraptionEntity oce)
                initial = VecHelper.rotate(initial, oce.getInitialYaw(), Axis.Y);
            if (context.contraption.entity instanceof CarriageContraptionEntity cce)
                initial = VecHelper.rotate(initial, 90, Axis.Y);
            facingVec = context.rotation.apply(initial);
        }

        ServerPlayerEntity serverPlayer = player.cast();
        serverPlayer.setYaw(AbstractContraptionEntity.yawFromVector(facingVec));
        serverPlayer.setPitch(xRot);

        DeployerHandler.activate(player, vec, pos, facingVec, mode);
    }

    protected void checkForTrackPlacementAdvancement(MovementContext context, DeployerPlayer player) {
        if ((context.contraption instanceof MountedContraption || context.contraption instanceof CarriageContraption) && player.getPlacedTracks() && context.blockEntityData != null) {
            context.blockEntityData.get("Owner", Uuids.INT_STREAM_CODEC).ifPresent(uuid -> {
                if (context.world.getPlayerByUuid(uuid) instanceof ServerPlayerEntity serverPlayer) {
                    AllAdvancements.SELF_DEPLOYING.trigger(serverPlayer);
                }
            });
        }
    }

    protected void activateAsSchematicPrinter(MovementContext context, BlockPos pos, DeployerPlayer player, World world, ItemStack filter) {
        if (!filter.contains(AllDataComponents.SCHEMATIC_ANCHOR))
            return;
        if (!world.getBlockState(pos).isReplaceable())
            return;

        if (!filter.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false))
            return;
        SchematicLevel schematicWorld = SchematicInstances.get(world, filter);
        if (schematicWorld == null)
            return;
        if (!schematicWorld.getBounds().contains(pos.subtract(schematicWorld.anchor)))
            return;
        BlockState blockState = schematicWorld.getBlockState(pos);
        ItemRequirement requirement = ItemRequirement.of(blockState, schematicWorld.getBlockEntity(pos));
        if (requirement.isInvalid() || requirement.isEmpty())
            return;
        if (blockState.isOf(AllBlocks.BELT))
            return;

        List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
        ItemStack contextStack = requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.getFirst().stack;

        if (!context.contraption.hasUniversalCreativeCrate) {
            Inventory itemHandler = context.contraption.getStorage().getAllItems();
            for (ItemRequirement.StackRequirement required : requiredItems) {
                int count = required.stack.getCount();
                int extract = itemHandler.countAll(required::matches, count);
                if (extract != count) {
                    return;
                }
            }
            for (ItemRequirement.StackRequirement required : requiredItems) {
                contextStack = required.stack;
                itemHandler.extractAll(required::matches, contextStack.getCount());
            }
        }

        NbtCompound data = BlockHelper.prepareBlockEntityData(world, blockState, schematicWorld.getBlockEntity(pos));
        BlockHelper.placeSchematicBlock(world, blockState, pos, contextStack, data);

        if (blockState.getBlock() instanceof AbstractRailBlock || blockState.getBlock() instanceof ITrackBlock)
            player.setPlacedTracks(true);
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClient())
            return;
        if (!context.stall)
            return;

        DeployerPlayer player = getPlayer(context);
        Mode mode = getMode(context);

        Pair<BlockPos, Float> blockBreakingProgress = player.getBlockBreakingProgress();
        if (blockBreakingProgress != null) {
            int timer = context.data.getInt("Timer", 0);
            if (timer < 20) {
                timer++;
                context.data.putInt("Timer", timer);
                return;
            }

            context.data.remove("Timer");
            activate(context, blockBreakingProgress.getKey(), player, mode);
            tryDisposeOfExcess(context);
        }

        context.stall = player.getBlockBreakingProgress() != null;
    }

    @Override
    public void cancelStall(MovementContext context) {
        if (context.world.isClient())
            return;

        super.cancelStall(context);
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        if (player.getBlockBreakingProgress() == null)
            return;
        context.world.setBlockBreakingInfo(player.cast().getId(), player.getBlockBreakingProgress().getKey(), -1);
        player.setBlockBreakingProgress(null);
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.world.isClient())
            return;

        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;

        ServerPlayerEntity serverPlayer = player.cast();
        cancelStall(context);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(context.contraption.entity.getErrorReporterContext(), LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, context.world.getRegistryManager());
            serverPlayer.getInventory().writeData(view.getListAppender("Inventory", StackWithSlot.CODEC));
            context.blockEntityData.put("Inventory", NbtCompound.CODEC, view.getNbt());
        }
        serverPlayer.discard();
    }

    private void tryGrabbingItem(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        ServerPlayerEntity serverPlayer = player.cast();
        if (serverPlayer.getMainHandStack().isEmpty()) {
            FilterItemStack filter = context.getFilterFromBE();
            if (filter.item().isOf(AllItems.SCHEMATIC))
                return;
            ItemStack held = context.contraption.getStorage().getAllItems().extract(stack -> filter.test(context.world, stack), 1);
            serverPlayer.setStackInHand(Hand.MAIN_HAND, held);
        }
    }

    private void tryDisposeOfExcess(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        PlayerInventory inv = player.cast().getInventory();
        FilterItemStack filter = context.getFilterFromBE();

        DefaultedList<ItemStack> main = inv.getMainStacks();
        int selected = inv.getSelectedSlot();
        for (int i = 0; i < main.size(); i++) {
            ItemStack stack = main.get(i);
            if (stack.isEmpty() || i == selected && filter.test(context.world, stack))
                continue;
            dropItem(context, stack);
            main.set(i, ItemStack.EMPTY);
        }
        PlayerInventory.EQUIPMENT_SLOTS.forEach((slot, equipmentSlot) -> {
            ItemStack stack = inv.getStack(slot);
            if (stack.isEmpty()) {
                return;
            }
            dropItem(context, stack);
            inv.setStack(slot, ItemStack.EMPTY);
        });
    }

    @Override
    public void writeExtraData(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        ItemStack stack = player.cast().getMainHandStack();
        if (stack.isEmpty()) {
            return;
        }
        RegistryOps<NbtElement> ops = context.world.getRegistryManager().getOps(NbtOps.INSTANCE);
        context.data.put("HeldItem", ItemStack.CODEC, ops, stack);
    }

    private DeployerPlayer getPlayer(MovementContext context) {
        if (!(context.temporaryData instanceof DeployerPlayer) && context.world instanceof ServerWorld) {
            DynamicRegistryManager registryManager = context.world.getRegistryManager();
            UUID owner = context.blockEntityData.get("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
            String ownerName = context.blockEntityData.get("OwnerName", Codec.STRING).orElse(null);
            DeployerPlayer player = DeployerPlayer.create((ServerWorld) context.world, owner, ownerName);
            player.setOnMinecartContraption(context.contraption instanceof MountedContraption);

            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "DeployerMovementBehaviour", LOGGER)) {
                NbtCompound inventory = context.blockEntityData.get("Inventory", NbtCompound.CODEC).orElseGet(NbtCompound::new);
                ReadView view = NbtReadView.create(logging, registryManager, inventory);
                player.cast().getInventory().readData(view.getTypedListView("Inventory", StackWithSlot.CODEC));
            }

            if (context.data.contains("HeldItem"))
                player.cast().setStackInHand(
                    Hand.MAIN_HAND,
                    context.data.get("HeldItem", ItemStack.CODEC, registryManager.getOps(NbtOps.INSTANCE)).orElse(ItemStack.EMPTY)
                );
            context.blockEntityData.remove("Inventory");
            context.temporaryData = player;
        }
        return (DeployerPlayer) context.temporaryData;
    }

    private Mode getMode(MovementContext context) {
        return context.blockEntityData.get("Mode", Mode.CODEC).orElse(Mode.PUNCH);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
