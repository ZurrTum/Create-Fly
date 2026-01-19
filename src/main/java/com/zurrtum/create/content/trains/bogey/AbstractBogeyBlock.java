package com.zurrtum.create.content.trains.bogey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public abstract class AbstractBogeyBlock<T extends AbstractBogeyBlockEntity> extends Block implements IBE<T>, ProperWaterloggedBlock, SpecialBlockItemRequirement, IWrenchable {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbstractBogeyBlock<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.BLOCK)
        .map(block -> (AbstractBogeyBlock<?>) block, Function.identity());
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    static final List<Identifier> BOGEYS = Util.make(
        new ArrayList<>(), list -> {
            list.add(Identifier.fromNamespaceAndPath(MOD_ID, "block/small_bogey"));
            list.add(Identifier.fromNamespaceAndPath(MOD_ID, "block/large_bogey"));
        }
    );
    public BogeySize size;

    public AbstractBogeyBlock(Properties pProperties, BogeySize size) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
        this.size = size;
    }

    public boolean isOnIncompatibleTrack(Carriage carriage, boolean leading) {
        TravellingPoint point = leading ? carriage.getLeadingPoint() : carriage.getTrailingPoint();
        CarriageBogey bogey = leading ? carriage.leadingBogey() : carriage.trailingBogey();
        TrackEdge currentEdge = point.edge;
        if (currentEdge == null)
            return false;
        return currentEdge.getTrackMaterial().getId() != getTrackType(bogey.getStyle());
    }

    public Set<Identifier> getValidPathfindingTypes(BogeyStyle style) {
        return ImmutableSet.of(getTrackType(style));
    }

    public abstract Identifier getTrackType(BogeyStyle style);

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    static final EnumSet<Direction> STICKY_X = EnumSet.of(Direction.EAST, Direction.WEST);
    static final EnumSet<Direction> STICKY_Z = EnumSet.of(Direction.SOUTH, Direction.NORTH);

    public EnumSet<Direction> getStickySurfaces(BlockGetter world, BlockPos pos, BlockState state) {
        return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? STICKY_X : STICKY_Z;
    }

    public abstract double getWheelPointSpacing();

    public abstract double getWheelRadius();

    public Vec3 getConnectorAnchorOffset(boolean upsideDown) {
        return getConnectorAnchorOffset();
    }

    /**
     * This should be implemented, but not called directly
     */
    protected abstract Vec3 getConnectorAnchorOffset();

    public boolean allowsSingleBogeyCarriage() {
        return true;
    }

    public abstract BogeyStyle getDefaultStyle();

    /**
     * Legacy system doesn't capture bogey block entities when constructing a train
     */
    public boolean captureBlockEntityForTrain() {
        return false;
    }

    public BogeySize getSize() {
        return this.size;
    }

    public Direction getBogeyUpDirection() {
        return Direction.UP;
    }

    public boolean isTrackAxisAlongFirstCoordinate(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.X;
    }

    @Nullable
    public BlockState getMatchingBogey(Direction upDirection, boolean axisAlongFirst) {
        if (upDirection != Direction.UP)
            return null;
        return defaultBlockState().setValue(AXIS, axisAlongFirst ? Direction.Axis.X : Direction.Axis.Z);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (level.isClientSide())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!player.isShiftKeyDown() && stack.is(AllItems.WRENCH) && !player.getCooldowns()
            .isOnCooldown(stack) && AllBogeyStyles.BOGEY_STYLES.size() > 1) {

            BlockEntity be = level.getBlockEntity(pos);

            if (!(be instanceof AbstractBogeyBlockEntity sbbe))
                return InteractionResult.FAIL;

            player.getCooldowns().addCooldown(stack, 20);
            BogeyStyle currentStyle = sbbe.getStyle();

            BogeySize size = getSize();

            BogeyStyle style = this.getNextStyle(currentStyle);
            if (style == currentStyle)
                return InteractionResult.TRY_WITH_EMPTY_HAND;

            Set<BogeySize> validSizes = style.validSizes();

            for (int i = 0; i < AllBogeySizes.all().size(); i++) {
                if (validSizes.contains(size))
                    break;
                size = size.nextBySize();
            }

            sbbe.setBogeyStyle(style);

            CompoundTag defaultData = style.defaultData;
            sbbe.setBogeyData(sbbe.getBogeyData().merge(defaultData));

            if (size == getSize()) {
                if (state.getBlock() != style.getBlockForSize(size)) {
                    CompoundTag oldData = sbbe.getBogeyData();
                    level.setBlockAndUpdate(pos, copyProperties(state, getStateOfSize(sbbe, size)));
                    if (!(level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity bogeyBlockEntity))
                        return InteractionResult.FAIL;
                    bogeyBlockEntity.setBogeyData(oldData);
                }
                player.displayClientMessage(Component.translatable("create.bogey.style.updated_style").append(": ").append(style.displayName), true);
            } else {
                CompoundTag oldData = sbbe.getBogeyData();
                level.setBlockAndUpdate(pos, getStateOfSize(sbbe, size));
                if (!(level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity bogeyBlockEntity))
                    return InteractionResult.FAIL;
                bogeyBlockEntity.setBogeyData(oldData);
                player.displayClientMessage(
                    Component.translatable("create.bogey.style.updated_style_and_size").append(": ").append(style.displayName), true);
            }

            return InteractionResult.CONSUME;
        }

        return onInteractWithBogey(state, level, pos, player, hand, hitResult);
    }

    // Allows for custom interactions with bogey block to be added simply
    protected InteractionResult onInteractWithBogey(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * If, instead of using the style-based cycling system you prefer to use separate blocks, return them from this method
     */
    protected List<Identifier> getBogeyBlockCycle() {
        return BOGEYS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
        Block block = state.getBlock();
        List<Identifier> bogeyCycle = getBogeyBlockCycle();
        int indexOf = bogeyCycle.indexOf(RegisteredObjectsHelper.getKeyOrThrow(block));
        if (indexOf == -1)
            return state;
        int index = (indexOf + 1) % bogeyCycle.size();
        Direction bogeyUpDirection = getBogeyUpDirection();
        boolean trackAxisAlongFirstCoordinate = isTrackAxisAlongFirstCoordinate(state);

        while (index != indexOf) {
            Identifier id = bogeyCycle.get(index);
            Block newBlock = BuiltInRegistries.BLOCK.getValue(id);
            if (newBlock instanceof AbstractBogeyBlock<?> bogey) {
                BlockState matchingBogey = bogey.getMatchingBogey(bogeyUpDirection, trackAxisAlongFirstCoordinate);
                if (matchingBogey != null)
                    return copyProperties(state, matchingBogey);
            }
            index = (index + 1) % bogeyCycle.size();
        }

        return state;
    }

    public BlockState getNextSize(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractBogeyBlockEntity sbbe)
            return this.getNextSize(sbbe);
        return level.getBlockState(pos);
    }

    /**
     * List of BlockState Properties to copy between sizes
     */
    public List<Property<?>> propertiesToCopy() {
        return ImmutableList.of(WATERLOGGED, AXIS);
    }

    // generic method needed to satisfy Property and BlockState's generic requirements
    private <V extends Comparable<V>> BlockState copyProperty(BlockState source, BlockState target, Property<V> property) {
        if (source.hasProperty(property) && target.hasProperty(property)) {
            return target.setValue(property, source.getValue(property));
        }
        return target;
    }

    private BlockState copyProperties(BlockState source, BlockState target) {
        for (Property<?> property : propertiesToCopy())
            target = copyProperty(source, target, property);
        return target;
    }

    public BlockState getNextSize(AbstractBogeyBlockEntity sbbe) {
        BogeySize size = this.getSize();
        BogeyStyle style = sbbe.getStyle();
        BlockState nextBlock = style.getNextBlock(size).defaultBlockState();
        nextBlock = copyProperties(sbbe.getBlockState(), nextBlock);
        return nextBlock;
    }

    public BlockState getStateOfSize(AbstractBogeyBlockEntity sbbe, BogeySize size) {
        BogeyStyle style = sbbe.getStyle();
        BlockState state = style.getBlockForSize(size).defaultBlockState();
        return copyProperties(sbbe.getBlockState(), state);
    }

    public BogeyStyle getNextStyle(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof AbstractBogeyBlockEntity sbbe)
            return this.getNextStyle(sbbe.getStyle());
        return getDefaultStyle();
    }

    public BogeyStyle getNextStyle(BogeyStyle style) {
        Collection<BogeyStyle> allStyles = style.getCycleGroup().values();
        if (allStyles.size() <= 1)
            return style;
        List<BogeyStyle> list = new ArrayList<>(allStyles);
        return Iterate.cycleValue(list, style);
    }


    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return switch (pRotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> pState.cycle(AXIS);
            default -> pState;
        };
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, AllItems.RAILWAY_CASING.getDefaultInstance());
    }

    public boolean canBeUpsideDown() {
        return false;
    }

    public boolean isUpsideDown(BlockState state) {
        return false;
    }

    public BlockState getVersion(BlockState base, boolean upsideDown) {
        return base;
    }
}
