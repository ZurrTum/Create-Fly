package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.ContraptionMovementSetting;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.data.ContraptionPickupLimiting;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.zurrtum.create.Create.LOGGER;

public class MinecartContraptionItem extends Item {

    private final EntityType<? extends AbstractMinecartEntity> minecartType;

    public static MinecartContraptionItem rideable(Settings builder) {
        return new MinecartContraptionItem(EntityType.MINECART, builder);
    }

    public static MinecartContraptionItem furnace(Settings builder) {
        return new MinecartContraptionItem(EntityType.FURNACE_MINECART, builder);
    }

    public static MinecartContraptionItem chest(Settings builder) {
        return new MinecartContraptionItem(EntityType.CHEST_MINECART, builder);
    }

    @Override
    public boolean canBeNested() {
        return AllConfigs.server().kinetics.minecartContraptionInContainers.get();
    }

    private MinecartContraptionItem(EntityType<? extends AbstractMinecartEntity> minecartTypeIn, Settings builder) {
        super(builder);
        this.minecartType = minecartTypeIn;
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    // Taken and adjusted from MinecartItem
    private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
        private final ItemDispenserBehavior behaviourDefaultDispenseItem = new ItemDispenserBehavior();

        @Override
        public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
            Direction direction = source.state().get(DispenserBlock.FACING);
            ServerWorld world = source.world();
            Vec3d vec3 = source.centerPos();
            double d0 = vec3.getX() + (double) direction.getOffsetX() * 1.125D;
            double d1 = Math.floor(vec3.getY()) + (double) direction.getOffsetY();
            double d2 = vec3.getZ() + (double) direction.getOffsetZ() * 1.125D;
            BlockPos blockpos = source.pos().offset(direction);
            BlockState blockstate = world.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock abstractRailBlock ? blockstate.get(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.isIn(BlockTags.RAILS)) {
                if (railshape.isAscending()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (!blockstate.isAir() || !world.getBlockState(blockpos.down()).isIn(BlockTags.RAILS)) {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }

                BlockState blockstate1 = world.getBlockState(blockpos.down());
                RailShape railshape1 = blockstate1.getBlock() instanceof AbstractRailBlock abstractRailBlock ? blockstate1.get(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railshape1.isAscending()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }

            AbstractMinecartEntity abstractminecartentity = AbstractMinecartEntity.create(
                world,
                d0,
                d1 + d3,
                d2,
                ((MinecartContraptionItem) stack.getItem()).minecartType,
                SpawnReason.SPAWN_ITEM_USE,
                stack,
                null
            );
            if (stack.contains(DataComponentTypes.CUSTOM_NAME))
                abstractminecartentity.setCustomName(stack.getName());
            world.spawnEntity(abstractminecartentity);
            addContraptionToMinecart(world, stack, abstractminecartentity, direction);

            stack.decrement(1);
            return stack;
        }

        @Override
        protected void playSound(BlockPointer source) {
            source.world().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, source.pos(), 0);
        }
    };

    // Taken and adjusted from MinecartItem
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockpos = context.getBlockPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (!blockstate.isIn(BlockTags.RAILS)) {
            return ActionResult.FAIL;
        } else {
            ItemStack itemstack = context.getStack();
            if (world instanceof ServerWorld serverlevel) {
                RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock abstractRailBlock ? blockstate.get(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isAscending()) {
                    d0 = 0.5D;
                }

                AbstractMinecartEntity abstractminecartentity = AbstractMinecartEntity.create(
                    serverlevel,
                    (double) blockpos.getX() + 0.5D,
                    (double) blockpos.getY() + 0.0625D + d0,
                    (double) blockpos.getZ() + 0.5D,
                    this.minecartType,
                    SpawnReason.SPAWN_ITEM_USE,
                    itemstack,
                    null
                );
                if (itemstack.contains(DataComponentTypes.CUSTOM_NAME))
                    abstractminecartentity.setCustomName(itemstack.getName());
                PlayerEntity player = context.getPlayer();
                world.spawnEntity(abstractminecartentity);
                addContraptionToMinecart(world, itemstack, abstractminecartentity, player == null ? null : player.getHorizontalFacing());
            }

            itemstack.decrement(1);
            return ActionResult.SUCCESS;
        }
    }

    public static void addContraptionToMinecart(World world, ItemStack itemstack, AbstractMinecartEntity cart, @Nullable Direction newFacing) {
        if (itemstack.contains(AllDataComponents.MINECRAFT_CONTRAPTION_DATA)) {
            NbtCompound contraptionTag = itemstack.get(AllDataComponents.MINECRAFT_CONTRAPTION_DATA);

            Direction intialOrientation = contraptionTag.get("InitialOrientation", Direction.CODEC).orElse(Direction.DOWN);

            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(cart.getErrorReporterContext(), LOGGER)) {
                Contraption mountedContraption = Contraption.fromData(
                    world,
                    NbtReadView.create(logging, world.getRegistryManager(), contraptionTag),
                    false
                );
                OrientedContraptionEntity contraptionEntity = newFacing == null ? OrientedContraptionEntity.create(
                    world,
                    mountedContraption,
                    intialOrientation
                ) : OrientedContraptionEntity.createAtYaw(world, mountedContraption, intialOrientation, newFacing.getPositiveHorizontalDegrees());

                contraptionEntity.startRiding(cart);
                contraptionEntity.setPosition(cart.getX(), cart.getY(), cart.getZ());
                world.spawnEntity(contraptionEntity);
            }
        }
    }

    public static ActionResult wrenchCanBeUsedToPickUpMinecartContraptions(PlayerEntity player, Hand hand, Entity entity) {
        if (player == null || entity == null)
            return null;
        if (!AllConfigs.server().kinetics.survivalContraptionPickup.get() && !player.isCreative())
            return null;

        ItemStack wrench = player.getStackInHand(hand);
        if (!wrench.isOf(AllItems.WRENCH))
            return null;
        if (entity instanceof AbstractContraptionEntity)
            entity = entity.getVehicle();
        if (!(entity instanceof AbstractMinecartEntity cart))
            return null;
        if (!entity.isAlive())
            return null;
        if (player instanceof DeployerPlayer dfp && dfp.isOnMinecartContraption())
            return null;
        EntityType<?> type = cart.getType();
        if (type != EntityType.MINECART && type != EntityType.FURNACE_MINECART && type != EntityType.CHEST_MINECART)
            return null;
        List<Entity> passengers = cart.getPassengerList();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof OrientedContraptionEntity oce))
            return null;
        Contraption contraption = oce.getContraption();

        if (ContraptionMovementSetting.isNoPickup(contraption.getBlocks().values())) {
            player.sendMessage(Text.translatable("create.contraption.minecart_contraption_illegal_pickup").formatted(Formatting.RED), true);
            return null;
        }

        World world = player.getWorld();
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        contraption.stop(world);

        for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors())
            if (MovementBehaviour.REGISTRY.get(pair.left.state()) instanceof PortableStorageInterfaceMovement psim)
                psim.reset(pair.right);

        ItemStack generatedStack = create(type, oce);
        generatedStack.set(DataComponentTypes.CUSTOM_NAME, entity.getCustomName());

        if (!generatedStack.isEmpty()) {
            Optional<NbtElement> result = ItemStack.CODEC.encodeStart(world.getRegistryManager().getOps(NbtOps.INSTANCE), generatedStack).result();
            if (result.isPresent() && ContraptionPickupLimiting.isTooLargeForPickup(result.get())) {
                player.sendMessage(Text.translatable("create.contraption.minecart_contraption_too_big").formatted(Formatting.RED), true);
                return null;
            }
        }

        if (contraption.getBlocks().size() > 200 && player instanceof ServerPlayerEntity serverPlayer)
            AllAdvancements.CART_PICKUP.trigger(serverPlayer);

        player.getInventory().offerOrDrop(generatedStack);
        oce.discard();
        entity.discard();
        return ActionResult.SUCCESS;
    }

    public static ItemStack create(EntityType<?> type, OrientedContraptionEntity entity) {
        ItemStack stack = ItemStack.EMPTY;

        if (type == EntityType.MINECART) {
            stack = AllItems.MINECART_CONTRAPTION.getDefaultStack();
        } else if (type == EntityType.FURNACE_MINECART) {
            stack = AllItems.FURNACE_MINECART_CONTRAPTION.getDefaultStack();
        } else if (type == EntityType.CHEST_MINECART) {
            stack = AllItems.CHEST_MINECART_CONTRAPTION.getDefaultStack();
        }

        if (stack.isEmpty())
            return stack;

        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, entity.getRegistryManager());
            entity.getContraption().write(view, false);
            view.remove("UUID");
            view.remove("Pos");
            view.remove("Motion");
            view.put("InitialOrientation", Direction.CODEC, entity.getInitialOrientation());
            stack.set(AllDataComponents.MINECRAFT_CONTRAPTION_DATA, view.getNbt());
        }

        return stack;
    }
}
