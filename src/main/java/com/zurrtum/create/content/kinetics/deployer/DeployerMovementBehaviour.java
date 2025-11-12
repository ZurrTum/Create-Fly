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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

import static com.zurrtum.create.Create.LOGGER;

public class DeployerMovementBehaviour extends MovementBehaviour {
    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING).getUnitVec3i()).scale(2);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        if (context.world.isClientSide())
            return;

        tryGrabbingItem(context);
        DeployerPlayer player = getPlayer(context);
        Mode mode = getMode(context);
        if (mode == Mode.USE && !DeployerHandler.shouldActivate(player.cast().getMainHandItem(), context.world, pos, null))
            return;

        activate(context, pos, player, mode);
        checkForTrackPlacementAdvancement(context, player);
        tryDisposeOfExcess(context);
        context.stall = player.getBlockBreakingProgress() != null;
    }

    public void activate(MovementContext context, BlockPos pos, DeployerPlayer player, Mode mode) {
        Level world = context.world;

        player.setPlacedTracks(false);

        FilterItemStack filter = context.getFilterFromBE();
        if (filter.item().is(AllItems.SCHEMATIC)) {
            activateAsSchematicPrinter(context, pos, player, world, filter.item());
            return;
        }

        Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING).getUnitVec3i());
        facingVec = context.rotation.apply(facingVec);
        Vec3 vec = context.position.subtract(facingVec.scale(2));

        float xRot = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;
        if (Math.abs(xRot) > 89) {
            Vec3 initial = new Vec3(0, 0, 1);
            if (context.contraption.entity instanceof OrientedContraptionEntity oce)
                initial = VecHelper.rotate(initial, oce.getInitialYaw(), Axis.Y);
            if (context.contraption.entity instanceof CarriageContraptionEntity cce)
                initial = VecHelper.rotate(initial, 90, Axis.Y);
            facingVec = context.rotation.apply(initial);
        }

        ServerPlayer serverPlayer = player.cast();
        serverPlayer.setYRot(AbstractContraptionEntity.yawFromVector(facingVec));
        serverPlayer.setXRot(xRot);

        DeployerHandler.activate(player, vec, pos, facingVec, mode);
    }

    protected void checkForTrackPlacementAdvancement(MovementContext context, DeployerPlayer player) {
        if ((context.contraption instanceof MountedContraption || context.contraption instanceof CarriageContraption) && player.getPlacedTracks() && context.blockEntityData != null) {
            context.blockEntityData.read("Owner", UUIDUtil.CODEC).ifPresent(uuid -> {
                if (context.world.getPlayerByUUID(uuid) instanceof ServerPlayer serverPlayer) {
                    AllAdvancements.SELF_DEPLOYING.trigger(serverPlayer);
                }
            });
        }
    }

    protected void activateAsSchematicPrinter(MovementContext context, BlockPos pos, DeployerPlayer player, Level world, ItemStack filter) {
        if (!filter.has(AllDataComponents.SCHEMATIC_ANCHOR))
            return;
        if (!world.getBlockState(pos).canBeReplaced())
            return;

        if (!filter.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false))
            return;
        SchematicLevel schematicWorld = SchematicInstances.get(world, filter);
        if (schematicWorld == null)
            return;
        if (!schematicWorld.getBounds().isInside(pos.subtract(schematicWorld.anchor)))
            return;
        BlockState blockState = schematicWorld.getBlockState(pos);
        ItemRequirement requirement = ItemRequirement.of(blockState, schematicWorld.getBlockEntity(pos));
        if (requirement.isInvalid() || requirement.isEmpty())
            return;
        if (blockState.is(AllBlocks.BELT))
            return;

        List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
        ItemStack contextStack = requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.getFirst().stack;

        if (!context.contraption.hasUniversalCreativeCrate) {
            Container itemHandler = context.contraption.getStorage().getAllItems();
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

        CompoundTag data = BlockHelper.prepareBlockEntityData(world, blockState, schematicWorld.getBlockEntity(pos));
        BlockHelper.placeSchematicBlock(world, blockState, pos, contextStack, data);

        if (blockState.getBlock() instanceof BaseRailBlock || blockState.getBlock() instanceof ITrackBlock)
            player.setPlacedTracks(true);
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide())
            return;
        if (!context.stall)
            return;

        DeployerPlayer player = getPlayer(context);
        Mode mode = getMode(context);

        Pair<BlockPos, Float> blockBreakingProgress = player.getBlockBreakingProgress();
        if (blockBreakingProgress != null) {
            int timer = context.data.getIntOr("Timer", 0);
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
        if (context.world.isClientSide())
            return;

        super.cancelStall(context);
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        if (player.getBlockBreakingProgress() == null)
            return;
        context.world.destroyBlockProgress(player.cast().getId(), player.getBlockBreakingProgress().getKey(), -1);
        player.setBlockBreakingProgress(null);
    }

    @Override
    public void stopMoving(MovementContext context) {
        if (context.world.isClientSide())
            return;

        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;

        ServerPlayer serverPlayer = player.cast();
        cancelStall(context);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(context.contraption.entity.problemPath(), LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, context.world.registryAccess());
            serverPlayer.getInventory().save(view.list("Inventory", ItemStackWithSlot.CODEC));
            context.blockEntityData.store("Inventory", CompoundTag.CODEC, view.buildResult());
        }
        serverPlayer.discard();
    }

    private void tryGrabbingItem(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        ServerPlayer serverPlayer = player.cast();
        if (serverPlayer.getMainHandItem().isEmpty()) {
            FilterItemStack filter = context.getFilterFromBE();
            if (filter.item().is(AllItems.SCHEMATIC))
                return;
            ItemStack held = context.contraption.getStorage().getAllItems().extract(stack -> filter.test(context.world, stack), 1);
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, held);
        }
    }

    private void tryDisposeOfExcess(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        Inventory inv = player.cast().getInventory();
        FilterItemStack filter = context.getFilterFromBE();

        NonNullList<ItemStack> main = inv.getNonEquipmentItems();
        int selected = inv.getSelectedSlot();
        for (int i = 0; i < main.size(); i++) {
            ItemStack stack = main.get(i);
            if (stack.isEmpty() || i == selected && filter.test(context.world, stack))
                continue;
            dropItem(context, stack);
            main.set(i, ItemStack.EMPTY);
        }
        Inventory.EQUIPMENT_SLOT_MAPPING.forEach((slot, equipmentSlot) -> {
            ItemStack stack = inv.getItem(slot);
            if (stack.isEmpty()) {
                return;
            }
            dropItem(context, stack);
            inv.setItem(slot, ItemStack.EMPTY);
        });
    }

    @Override
    public void writeExtraData(MovementContext context) {
        DeployerPlayer player = getPlayer(context);
        if (player == null)
            return;
        ItemStack stack = player.cast().getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        RegistryOps<Tag> ops = context.world.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        context.data.store("HeldItem", ItemStack.CODEC, ops, stack);
    }

    private DeployerPlayer getPlayer(MovementContext context) {
        if (!(context.temporaryData instanceof DeployerPlayer) && context.world instanceof ServerLevel) {
            RegistryAccess registryManager = context.world.registryAccess();
            UUID owner = context.blockEntityData.read("Owner", UUIDUtil.CODEC).orElse(null);
            String ownerName = context.blockEntityData.read("OwnerName", Codec.STRING).orElse(null);
            DeployerPlayer player = DeployerPlayer.create((ServerLevel) context.world, owner, ownerName);
            player.setOnMinecartContraption(context.contraption instanceof MountedContraption);

            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "DeployerMovementBehaviour", LOGGER)) {
                CompoundTag inventory = context.blockEntityData.read("Inventory", CompoundTag.CODEC).orElseGet(CompoundTag::new);
                ValueInput view = TagValueInput.create(logging, registryManager, inventory);
                player.cast().getInventory().load(view.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
            }

            if (context.data.contains("HeldItem"))
                player.cast().setItemInHand(
                    InteractionHand.MAIN_HAND,
                    context.data.read("HeldItem", ItemStack.CODEC, registryManager.createSerializationContext(NbtOps.INSTANCE))
                        .orElse(ItemStack.EMPTY)
                );
            context.blockEntityData.remove("Inventory");
            context.temporaryData = player;
        }
        return (DeployerPlayer) context.temporaryData;
    }

    private Mode getMode(MovementContext context) {
        return context.blockEntityData.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
