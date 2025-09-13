package com.zurrtum.create.content.kinetics.belt;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltSlicer.Feedback;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.zurrtum.create.content.kinetics.belt.transport.BeltTunnelInteractionHandler;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.tick.ScheduledTickView;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BeltBlock extends HorizontalKineticBlock implements IBE<BeltBlockEntity>, SpecialBlockItemRequirement, TransformableBlock, ProperWaterloggedBlock, ItemInventoryProvider<BeltBlockEntity> {

    public static final EnumProperty<BeltSlope> SLOPE = EnumProperty.of("slope", BeltSlope.class);
    public static final EnumProperty<BeltPart> PART = EnumProperty.of("part", BeltPart.class);
    public static final BooleanProperty CASING = BooleanProperty.of("casing");

    public BeltBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(SLOPE, BeltSlope.HORIZONTAL).with(PART, BeltPart.START).with(CASING, false).with(WATERLOGGED, false));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, BeltBlockEntity blockEntity, Direction context) {
        if (!BeltBlock.canTransportObjects(blockEntity.getCachedState()))
            return null;
        if (!blockEntity.isRemoved() && blockEntity.itemHandler == null)
            blockEntity.initializeItemHandler();
        return blockEntity.itemHandler;
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return super.areStatesKineticallyEquivalent(oldState, newState) && oldState.get(PART) == newState.get(PART);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        if (face.getAxis() != getRotationAxis(state))
            return false;
        return getBlockEntityOptional(world, pos).map(BeltBlockEntity::hasPulley).orElse(false);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        if (state.get(SLOPE) == BeltSlope.SIDEWAYS)
            return Axis.Y;
        return state.get(HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.BELT_CONNECTOR.getDefaultStack();
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        List<ItemStack> drops = super.getDroppedStacks(state, builder);
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof BeltBlockEntity && ((BeltBlockEntity) blockEntity).hasPulley())
            drops.addAll(AllBlocks.SHAFT.getDefaultState().getDroppedStacks(builder));
        return drops;
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld worldIn, BlockPos pos, ItemStack p_220062_4_, boolean b) {
        BeltBlockEntity controllerBE = BeltHelper.getControllerBE(worldIn, pos);
        if (controllerBE != null)
            controllerBE.getInventory().ejectAll();
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        BlockPos entityPosition = entityIn.getBlockPos();
        BlockPos beltPos = null;

        if (worldIn.getBlockState(entityPosition).isOf(AllBlocks.BELT))
            beltPos = entityPosition;
        else if (worldIn.getBlockState(entityPosition.down()).isOf(AllBlocks.BELT))
            beltPos = entityPosition.down();
        if (beltPos == null)
            return;
        if (!(worldIn instanceof World world))
            return;

        onEntityCollision(worldIn.getBlockState(beltPos), world, beltPos, entityIn, EntityCollisionHandler.DUMMY);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, EntityCollisionHandler handler) {
        if (!canTransportObjects(state))
            return;
        if (entityIn instanceof PlayerEntity player) {
            if (player.isSneaking() && !player.getEquippedStack(EquipmentSlot.FEET).isOf(AllItems.CARDBOARD_BOOTS))
                return;
            if (player.getAbilities().flying)
                return;
        }

        if (DivingBootsItem.isWornBy(entityIn))
            return;

        BeltBlockEntity belt = BeltHelper.getSegmentBE(worldIn, pos);
        if (belt == null)
            return;
        ItemStack asItem = ItemHelper.fromItemEntity(entityIn);
        if (!asItem.isEmpty()) {
            if (worldIn.isClient)
                return;
            if (entityIn.getVelocity().y > 0)
                return;
            Vec3d targetLocation = VecHelper.getCenterOf(pos).add(0, 5 / 16f, 0);
            if (!PackageEntity.centerPackage(entityIn, targetLocation))
                return;
            if (BeltTunnelInteractionHandler.getTunnelOnPosition(worldIn, pos) != null)
                return;
            withBlockEntityDo(
                worldIn, pos, be -> {
                    Inventory inventory = ItemHelper.getInventory(worldIn, pos, state, be, null);
                    if (inventory == null)
                        return;
                    int insert = inventory.insert(asItem);
                    if (asItem.getCount() == insert) {
                        entityIn.discard();
                    } else if (entityIn instanceof ItemEntity itemEntity && insert != 0) {
                        asItem.decrement(insert);
                        itemEntity.setStack(asItem);
                    }
                }
            );
            return;
        }

        BeltBlockEntity controller = BeltHelper.getControllerBE(worldIn, pos);
        if (controller == null || controller.passengers == null)
            return;
        if (controller.passengers.containsKey(entityIn)) {
            TransportedEntityInfo info = controller.passengers.get(entityIn);
            if (info.getTicksSinceLastCollision() != 0 || pos.equals(entityIn.getBlockPos()))
                info.refresh(pos, state);
        } else {
            controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
            entityIn.setOnGround(true);
        }
    }

    public static boolean canTransportObjects(BlockState state) {
        if (!state.isOf(AllBlocks.BELT))
            return false;
        BeltSlope slope = state.get(SLOPE);
        return slope != BeltSlope.VERTICAL && slope != BeltSlope.SIDEWAYS;
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (player.isSneaking() || !player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        boolean isWrench = stack.isOf(AllItems.WRENCH);
        boolean isConnector = stack.isOf(AllItems.BELT_CONNECTOR);
        boolean isShaft = stack.isOf(AllItems.SHAFT);
        boolean isDye = stack.isIn(AllItemTags.DYES);
        boolean hasWater = !stack.isEmpty() && GenericItemEmptying.emptyItem(level, stack, true).getFirst().getFluid().matchesType(Fluids.WATER);
        boolean isHand = stack.isEmpty() && hand == Hand.MAIN_HAND;

        if (isDye || hasWater)
            return onBlockEntityUseItemOn(
                level,
                pos,
                be -> be.applyColor(AllItemTags.getDyeColor(stack)) ? ActionResult.SUCCESS : ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION
            );

        if (isConnector)
            return BeltSlicer.useConnector(state, level, pos, player, hand, hitResult, new Feedback());
        if (isWrench)
            return BeltSlicer.useWrench(state, level, pos, player, hand, hitResult, new Feedback());

        BeltBlockEntity belt = BeltHelper.getSegmentBE(level, pos);
        if (belt == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (PackageItem.isPackage(stack)) {
            ItemStack toInsert = stack.copy();
            Inventory handler = ItemHelper.getInventory(level, belt.getPos(), null);
            if (handler == null)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            int insert = handler.insert(toInsert);
            if (insert != 0) {
                stack.decrement(insert);
                return ActionResult.SUCCESS;
            }
        }

        if (isHand) {
            BeltBlockEntity controllerBelt = belt.getControllerBE();
            if (controllerBelt == null)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            if (level.isClient)
                return ActionResult.SUCCESS;
            MutableBoolean success = new MutableBoolean(false);
            controllerBelt.getInventory().applyToEachWithin(
                belt.index + .5f, .55f, (transportedItemStack) -> {
                    player.getInventory().offerOrDrop(transportedItemStack.stack);
                    success.setTrue();
                    return TransportedResult.removeItem();
                }
            );
            if (success.isTrue())
                level.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, 1f + level.random.nextFloat());
        }

        if (isShaft) {
            if (state.get(PART) != BeltPart.MIDDLE)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            if (level.isClient)
                return ActionResult.SUCCESS;
            if (!player.isCreative())
                stack.decrement(1);
            KineticBlockEntity.switchToBlockState(level, pos, state.with(PART, BeltPart.PULLEY));
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(AllItems.BRASS_CASING)) {
            withBlockEntityDo(level, pos, be -> be.setCasingType(CasingType.BRASS));
            updateCoverProperty(level, pos, level.getBlockState(pos));

            BlockSoundGroup soundType = AllBlocks.BRASS_CASING.getDefaultState().getSoundGroup();
            level.playSound(
                null,
                pos,
                soundType.getPlaceSound(),
                SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F
            );

            return ActionResult.SUCCESS;
        }

        if (stack.isOf(AllItems.ANDESITE_CASING)) {
            withBlockEntityDo(level, pos, be -> be.setCasingType(CasingType.ANDESITE));
            updateCoverProperty(level, pos, level.getBlockState(pos));

            BlockSoundGroup soundType = AllBlocks.ANDESITE_CASING.getDefaultState().getSoundGroup();
            level.playSound(
                null,
                pos,
                soundType.getPlaceSound(),
                SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F
            );

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();

        if (state.get(CASING)) {
            if (world.isClient)
                return ActionResult.SUCCESS;
            withBlockEntityDo(world, pos, be -> be.setCasingType(CasingType.NONE));
            return ActionResult.SUCCESS;
        }

        if (state.get(PART) == BeltPart.PULLEY) {
            if (world.isClient)
                return ActionResult.SUCCESS;
            KineticBlockEntity.switchToBlockState(world, pos, state.with(PART, BeltPart.MIDDLE));
            if (player != null && !player.isCreative())
                player.getInventory().offerOrDrop(AllItems.SHAFT.getDefaultStack());
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(SLOPE, PART, CASING, WATERLOGGED);
        super.appendProperties(builder);
    }

    //TODO
    //    @Override
    //    public PathType getBlockPathType(BlockState state, BlockView world, BlockPos pos, Mob entity) {
    //        return PathType.RAIL;
    //    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return BeltShapes.getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        if (state.getBlock() != this)
            return VoxelShapes.empty();

        VoxelShape shape = getOutlineShape(state, worldIn, pos, context);
        if (!(context instanceof EntityShapeContext))
            return shape;

        return getBlockEntityOptional(worldIn, pos).map(be -> {
            Entity entity = ((EntityShapeContext) context).getEntity();
            if (entity == null)
                return shape;

            BeltBlockEntity controller = be.getControllerBE();
            if (controller == null)
                return shape;
            if (controller.passengers == null || !controller.passengers.containsKey(entity))
                return BeltShapes.getCollisionShape(state);
            return shape;

        }).orElse(shape);
    }

    public static void initBelt(World world, BlockPos pos) {
        if (world.isClient)
            return;
        if (world instanceof ServerWorld serverWorld && serverWorld.getChunkManager().getChunkGenerator() instanceof DebugChunkGenerator)
            return;

        BlockState state = world.getBlockState(pos);
        if (!state.isOf(AllBlocks.BELT))
            return;
        // Find controller
        int limit = 1000;
        BlockPos currentPos = pos;
        while (limit-- > 0) {
            BlockState currentState = world.getBlockState(currentPos);
            if (!currentState.isOf(AllBlocks.BELT)) {
                world.breakBlock(pos, true);
                return;
            }
            BlockPos nextSegmentPosition = nextSegmentPosition(currentState, currentPos, false);
            if (nextSegmentPosition == null)
                break;
            if (!world.isPosLoaded(nextSegmentPosition))
                return;
            currentPos = nextSegmentPosition;
        }

        // Init belts
        int index = 0;
        List<BlockPos> beltChain = getBeltChain(world, currentPos);
        if (beltChain.size() < 2) {
            world.breakBlock(currentPos, true);
            return;
        }

        for (BlockPos beltPos : beltChain) {
            BlockEntity blockEntity = world.getBlockEntity(beltPos);
            BlockState currentState = world.getBlockState(beltPos);

            if (blockEntity instanceof BeltBlockEntity be && currentState.isOf(AllBlocks.BELT)) {
                be.setController(currentPos);
                be.beltLength = beltChain.size();
                be.index = index;
                be.attachKinetics();
                be.markDirty();
                be.sendData();

                if (be.isController() && !canTransportObjects(currentState))
                    be.getInventory().ejectAll();
            } else {
                world.breakBlock(currentPos, true);
                return;
            }
            index++;
        }

    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        super.onStateReplaced(state, world, pos, isMoving);

        if (world.isClient)
            return;
        if (isMoving)
            return;

        // Destroy chain
        for (boolean forward : Iterate.trueAndFalse) {
            BlockPos currentPos = nextSegmentPosition(state, pos, forward);
            if (currentPos == null)
                continue;
            BlockState currentState = world.getBlockState(currentPos);
            if (!currentState.isOf(AllBlocks.BELT))
                continue;

            boolean hasPulley = false;
            BlockEntity blockEntity = world.getBlockEntity(currentPos);
            if (blockEntity instanceof BeltBlockEntity belt) {
                if (belt.isController())
                    belt.getInventory().ejectAll();

                hasPulley = belt.hasPulley();
            }

            world.removeBlockEntity(currentPos);
            BlockState shaftState = AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, getRotationAxis(currentState));
            world.setBlockState(
                currentPos,
                ProperWaterloggedBlock.withWater(world, hasPulley ? shaftState : Blocks.AIR.getDefaultState(), currentPos),
                Block.NOTIFY_ALL
            );
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, currentPos, Block.getRawIdFromState(currentState));
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction side,
        BlockPos p_196271_6_,
        BlockState p_196271_3_,
        Random random
    ) {
        updateWater(world, tickView, state, pos);
        if (side.getAxis().isHorizontal())
            updateTunnelConnections((WorldAccess) world, pos.up());
        if (side == Direction.UP)
            updateCoverProperty(world, pos, state);
        return state;
    }

    public void updateCoverProperty(WorldView world, BlockPos pos, BlockState state) {
        if (world.isClient())
            return;
        if (state.get(CASING) && state.get(SLOPE) == BeltSlope.HORIZONTAL)
            withBlockEntityDo(world, pos, bbe -> bbe.setCovered(isBlockCoveringBelt(world, pos.up())));
    }

    public static boolean isBlockCoveringBelt(WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        VoxelShape collisionShape = blockState.getCollisionShape(world, pos);
        if (collisionShape.isEmpty())
            return false;
        Box bounds = collisionShape.getBoundingBox();
        if (bounds.getLengthX() < .5f || bounds.getLengthZ() < .5f)
            return false;
        if (bounds.minY > 0)
            return false;
        if (blockState.isOf(AllBlocks.CRUSHING_WHEEL_CONTROLLER))
            return false;
        if (FunnelBlock.isFunnel(blockState) && FunnelBlock.getFunnelFacing(blockState) != Direction.UP)
            return false;
        return !(blockState.getBlock() instanceof BeltTunnelBlock);
    }

    private void updateTunnelConnections(WorldAccess world, BlockPos pos) {
        Block tunnelBlock = world.getBlockState(pos).getBlock();
        if (tunnelBlock instanceof BeltTunnelBlock)
            ((BeltTunnelBlock) tunnelBlock).updateTunnel(world, pos);
    }

    public static List<BlockPos> getBeltChain(WorldView world, BlockPos controllerPos) {
        List<BlockPos> positions = new LinkedList<>();

        BlockState blockState = world.getBlockState(controllerPos);
        if (!blockState.isOf(AllBlocks.BELT))
            return positions;

        int limit = 1000;
        BlockPos current = controllerPos;
        while (limit-- > 0 && current != null) {
            BlockState state = world.getBlockState(current);
            if (!state.isOf(AllBlocks.BELT))
                break;
            positions.add(current);
            current = nextSegmentPosition(state, current, true);
        }

        return positions;
    }

    public static BlockPos nextSegmentPosition(BlockState state, BlockPos pos, boolean forward) {
        Direction direction = state.get(HORIZONTAL_FACING);
        BeltSlope slope = state.get(SLOPE);
        BeltPart part = state.get(PART);

        int offset = forward ? 1 : -1;

        if (part == BeltPart.END && forward || part == BeltPart.START && !forward)
            return null;
        if (slope == BeltSlope.VERTICAL)
            return pos.up(direction.getDirection() == AxisDirection.POSITIVE ? offset : -offset);
        pos = pos.offset(direction, offset);
        if (slope != BeltSlope.HORIZONTAL && slope != BeltSlope.SIDEWAYS)
            return pos.up(slope == BeltSlope.UPWARD ? offset : -offset);
        return pos;
    }

    @Override
    public Class<BeltBlockEntity> getBlockEntityClass() {
        return BeltBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeltBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BELT;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        List<ItemStack> required = new ArrayList<>();
        if (state.get(PART) != BeltPart.MIDDLE)
            required.add(AllItems.SHAFT.getDefaultStack());
        if (state.get(PART) == BeltPart.START)
            required.add(AllItems.BELT_CONNECTOR.getDefaultStack());
        if (required.isEmpty())
            return ItemRequirement.NONE;
        return new ItemRequirement(ItemUseType.CONSUME, required);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        BlockState rotate = super.rotate(state, rot);

        if (state.get(SLOPE) != BeltSlope.VERTICAL)
            return rotate;
        if (state.get(HORIZONTAL_FACING).getDirection() != rotate.get(HORIZONTAL_FACING).getDirection()) {
            if (state.get(PART) == BeltPart.START)
                return rotate.with(PART, BeltPart.END);
            if (state.get(PART) == BeltPart.END)
                return rotate.with(PART, BeltPart.START);
        }

        return rotate;
    }

    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Direction.Axis.Y) {
            return rotate(state, transform.rotation);
        }
        return transformInner(state, transform);
    }

    protected BlockState transformInner(BlockState state, StructureTransform transform) {
        boolean halfTurn = transform.rotation == BlockRotation.CLOCKWISE_180;

        Direction initialDirection = state.get(HORIZONTAL_FACING);
        boolean diagonal = state.get(SLOPE) == BeltSlope.DOWNWARD || state.get(SLOPE) == BeltSlope.UPWARD;

        if (!diagonal) {
            for (int i = 0; i < transform.rotation.ordinal(); i++) {
                Direction direction = state.get(HORIZONTAL_FACING);
                BeltSlope slope = state.get(SLOPE);
                boolean vertical = slope == BeltSlope.VERTICAL;
                boolean horizontal = slope == BeltSlope.HORIZONTAL;
                boolean sideways = slope == BeltSlope.SIDEWAYS;

                Direction newDirection = direction.getOpposite();
                BeltSlope newSlope = BeltSlope.VERTICAL;

                if (vertical) {
                    if (direction.getAxis() == transform.rotationAxis) {
                        newDirection = direction.rotateYCounterclockwise();
                        newSlope = BeltSlope.SIDEWAYS;
                    } else {
                        newSlope = BeltSlope.HORIZONTAL;
                        newDirection = direction;
                        if (direction.getAxis() == Axis.Z)
                            newDirection = direction.getOpposite();
                    }
                }

                if (sideways) {
                    newDirection = direction;
                    if (direction.getAxis() == transform.rotationAxis)
                        newSlope = BeltSlope.HORIZONTAL;
                    else
                        newDirection = direction.rotateYCounterclockwise();
                }

                if (horizontal) {
                    newDirection = direction;
                    if (direction.getAxis() == transform.rotationAxis)
                        newSlope = BeltSlope.SIDEWAYS;
                    else if (direction.getAxis() != Axis.Z)
                        newDirection = direction.getOpposite();
                }

                state = state.with(HORIZONTAL_FACING, newDirection);
                state = state.with(SLOPE, newSlope);
            }

        } else if (initialDirection.getAxis() != transform.rotationAxis) {
            for (int i = 0; i < transform.rotation.ordinal(); i++) {
                Direction direction = state.get(HORIZONTAL_FACING);
                Direction newDirection = direction.getOpposite();
                BeltSlope slope = state.get(SLOPE);
                boolean upward = slope == BeltSlope.UPWARD;
                boolean downward = slope == BeltSlope.DOWNWARD;

                // Rotate diagonal
                if (direction.getDirection() == AxisDirection.POSITIVE ^ downward ^ direction.getAxis() == Axis.Z) {
                    state = state.with(SLOPE, upward ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
                } else {
                    state = state.with(HORIZONTAL_FACING, newDirection);
                }
            }

        } else if (halfTurn) {
            Direction direction = state.get(HORIZONTAL_FACING);
            Direction newDirection = direction.getOpposite();
            BeltSlope slope = state.get(SLOPE);
            boolean vertical = slope == BeltSlope.VERTICAL;

            if (diagonal) {
                state = state.with(SLOPE, slope == BeltSlope.UPWARD ? BeltSlope.DOWNWARD : slope == BeltSlope.DOWNWARD ? BeltSlope.UPWARD : slope);
            } else if (vertical) {
                state = state.with(HORIZONTAL_FACING, newDirection);
            }
        }

        return state;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }
}
