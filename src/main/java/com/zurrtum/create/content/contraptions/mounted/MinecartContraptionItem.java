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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.zurrtum.create.Create.LOGGER;

public class MinecartContraptionItem extends Item {

    private final EntityType<? extends AbstractMinecart> minecartType;

    public static MinecartContraptionItem rideable(Properties builder) {
        return new MinecartContraptionItem(EntityType.MINECART, builder);
    }

    public static MinecartContraptionItem furnace(Properties builder) {
        return new MinecartContraptionItem(EntityType.FURNACE_MINECART, builder);
    }

    public static MinecartContraptionItem chest(Properties builder) {
        return new MinecartContraptionItem(EntityType.CHEST_MINECART, builder);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return AllConfigs.server().kinetics.minecartContraptionInContainers.get();
    }

    private MinecartContraptionItem(EntityType<? extends AbstractMinecart> minecartTypeIn, Properties builder) {
        super(builder);
        this.minecartType = minecartTypeIn;
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    // Taken and adjusted from MinecartItem
    private static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            ServerLevel world = source.level();
            Vec3 vec3 = source.center();
            double d0 = vec3.x() + (double) direction.getStepX() * 1.125D;
            double d1 = Math.floor(vec3.y()) + (double) direction.getStepY();
            double d2 = vec3.z() + (double) direction.getStepZ() * 1.125D;
            BlockPos blockpos = source.pos().relative(direction);
            BlockState blockstate = world.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock abstractRailBlock ? blockstate.getValue(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.is(BlockTags.RAILS)) {
                if (railshape.isSlope()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (!blockstate.isAir() || !world.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }

                BlockState blockstate1 = world.getBlockState(blockpos.below());
                RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock abstractRailBlock ? blockstate1.getValue(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railshape1.isSlope()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }

            AbstractMinecart abstractminecartentity = AbstractMinecart.createMinecart(
                world,
                d0,
                d1 + d3,
                d2,
                ((MinecartContraptionItem) stack.getItem()).minecartType,
                EntitySpawnReason.SPAWN_ITEM_USE,
                stack,
                null
            );
            if (stack.has(DataComponents.CUSTOM_NAME))
                abstractminecartentity.setCustomName(stack.getHoverName());
            world.addFreshEntity(abstractminecartentity);
            addContraptionToMinecart(world, stack, abstractminecartentity, direction);

            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, source.pos(), 0);
        }
    };

    // Taken and adjusted from MinecartItem
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (!blockstate.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemstack = context.getItemInHand();
            if (world instanceof ServerLevel serverlevel) {
                RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock abstractRailBlock ? blockstate.getValue(abstractRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isSlope()) {
                    d0 = 0.5D;
                }

                AbstractMinecart abstractminecartentity = AbstractMinecart.createMinecart(
                    serverlevel,
                    (double) blockpos.getX() + 0.5D,
                    (double) blockpos.getY() + 0.0625D + d0,
                    (double) blockpos.getZ() + 0.5D,
                    this.minecartType,
                    EntitySpawnReason.SPAWN_ITEM_USE,
                    itemstack,
                    null
                );
                if (itemstack.has(DataComponents.CUSTOM_NAME))
                    abstractminecartentity.setCustomName(itemstack.getHoverName());
                Player player = context.getPlayer();
                world.addFreshEntity(abstractminecartentity);
                addContraptionToMinecart(world, itemstack, abstractminecartentity, player == null ? null : player.getDirection());
            }

            itemstack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }

    public static void addContraptionToMinecart(Level world, ItemStack itemstack, AbstractMinecart cart, @Nullable Direction newFacing) {
        if (itemstack.has(AllDataComponents.MINECRAFT_CONTRAPTION_DATA)) {
            CompoundTag contraptionTag = itemstack.get(AllDataComponents.MINECRAFT_CONTRAPTION_DATA);

            Direction intialOrientation = contraptionTag.read("InitialOrientation", Direction.CODEC).orElse(Direction.DOWN);

            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(cart.problemPath(), LOGGER)) {
                Contraption mountedContraption = Contraption.fromData(
                    world,
                    TagValueInput.create(logging, world.registryAccess(), contraptionTag),
                    false
                );
                OrientedContraptionEntity contraptionEntity = newFacing == null ? OrientedContraptionEntity.create(
                    world,
                    mountedContraption,
                    intialOrientation
                ) : OrientedContraptionEntity.createAtYaw(world, mountedContraption, intialOrientation, newFacing.toYRot());

                contraptionEntity.startRiding(cart);
                contraptionEntity.setPos(cart.getX(), cart.getY(), cart.getZ());
                world.addFreshEntity(contraptionEntity);
            }
        }
    }

    public static InteractionResult wrenchCanBeUsedToPickUpMinecartContraptions(Player player, InteractionHand hand, Entity entity) {
        if (player == null || entity == null)
            return null;
        if (!AllConfigs.server().kinetics.survivalContraptionPickup.get() && !player.isCreative())
            return null;

        ItemStack wrench = player.getItemInHand(hand);
        if (!wrench.is(AllItems.WRENCH))
            return null;
        if (entity instanceof AbstractContraptionEntity)
            entity = entity.getVehicle();
        if (!(entity instanceof AbstractMinecart cart))
            return null;
        if (!entity.isAlive())
            return null;
        if (player instanceof DeployerPlayer dfp && dfp.isOnMinecartContraption())
            return null;
        EntityType<?> type = cart.getType();
        if (type != EntityType.MINECART && type != EntityType.FURNACE_MINECART && type != EntityType.CHEST_MINECART)
            return null;
        List<Entity> passengers = cart.getPassengers();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof OrientedContraptionEntity oce))
            return null;
        Contraption contraption = oce.getContraption();

        if (ContraptionMovementSetting.isNoPickup(contraption.getBlocks().values())) {
            player.displayClientMessage(
                Component.translatable("create.contraption.minecart_contraption_illegal_pickup").withStyle(ChatFormatting.RED), true);
            return null;
        }

        Level world = player.level();
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        contraption.stop(world);

        for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors())
            if (MovementBehaviour.REGISTRY.get(pair.left.state()) instanceof PortableStorageInterfaceMovement psim)
                psim.reset(pair.right);

        ItemStack generatedStack = create(type, oce);
        generatedStack.set(DataComponents.CUSTOM_NAME, entity.getCustomName());

        if (!generatedStack.isEmpty()) {
            Optional<Tag> result = ItemStack.CODEC.encodeStart(world.registryAccess().createSerializationContext(NbtOps.INSTANCE), generatedStack)
                .result();
            if (result.isPresent() && ContraptionPickupLimiting.isTooLargeForPickup(result.get())) {
                player.displayClientMessage(
                    Component.translatable("create.contraption.minecart_contraption_too_big").withStyle(ChatFormatting.RED),
                    true
                );
                return null;
            }
        }

        if (contraption.getBlocks().size() > 200 && player instanceof ServerPlayer serverPlayer)
            AllAdvancements.CART_PICKUP.trigger(serverPlayer);

        player.getInventory().placeItemBackInInventory(generatedStack);
        oce.discard();
        entity.discard();
        return InteractionResult.SUCCESS;
    }

    public static ItemStack create(EntityType<?> type, OrientedContraptionEntity entity) {
        ItemStack stack = ItemStack.EMPTY;

        if (type == EntityType.MINECART) {
            stack = AllItems.MINECART_CONTRAPTION.getDefaultInstance();
        } else if (type == EntityType.FURNACE_MINECART) {
            stack = AllItems.FURNACE_MINECART_CONTRAPTION.getDefaultInstance();
        } else if (type == EntityType.CHEST_MINECART) {
            stack = AllItems.CHEST_MINECART_CONTRAPTION.getDefaultInstance();
        }

        if (stack.isEmpty())
            return stack;

        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, entity.registryAccess());
            entity.getContraption().write(view, false);
            view.discard("UUID");
            view.discard("Pos");
            view.discard("Motion");
            view.store("InitialOrientation", Direction.CODEC, entity.getInitialOrientation());
            stack.set(AllDataComponents.MINECRAFT_CONTRAPTION_DATA, view.buildResult());
        }

        return stack;
    }
}
