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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public abstract class AbstractBogeyBlock<T extends AbstractBogeyBlockEntity> extends Block implements IBE<T>, ProperWaterloggedBlock, SpecialBlockItemRequirement, IWrenchable {
    public static final PacketCodec<RegistryByteBuf, AbstractBogeyBlock<?>> STREAM_CODEC = PacketCodecs.registryValue(RegistryKeys.BLOCK)
        .xmap(block -> (AbstractBogeyBlock<?>) block, Function.identity());
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    static final List<Identifier> BOGEYS = Util.make(
        new ArrayList<>(), list -> {
            list.add(Identifier.of(MOD_ID, "block/small_bogey"));
            list.add(Identifier.of(MOD_ID, "block/large_bogey"));
        }
    );
    public BogeySize size;

    public AbstractBogeyBlock(Settings pProperties, BogeySize size) {
        super(pProperties);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
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
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(AXIS, WATERLOGGED);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
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

    public EnumSet<Direction> getStickySurfaces(BlockView world, BlockPos pos, BlockState state) {
        return state.get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X ? STICKY_X : STICKY_Z;
    }

    public abstract double getWheelPointSpacing();

    public abstract double getWheelRadius();

    public Vec3d getConnectorAnchorOffset(boolean upsideDown) {
        return getConnectorAnchorOffset();
    }

    /**
     * This should be implemented, but not called directly
     */
    protected abstract Vec3d getConnectorAnchorOffset();

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
        return state.get(AXIS) == Direction.Axis.X;
    }

    @Nullable
    public BlockState getMatchingBogey(Direction upDirection, boolean axisAlongFirst) {
        if (upDirection != Direction.UP)
            return null;
        return getDefaultState().with(AXIS, axisAlongFirst ? Direction.Axis.X : Direction.Axis.Z);
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
        if (level.isClient())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (!player.isSneaking() && stack.isOf(AllItems.WRENCH) && !player.getItemCooldownManager()
            .isCoolingDown(stack) && AllBogeyStyles.BOGEY_STYLES.size() > 1) {

            BlockEntity be = level.getBlockEntity(pos);

            if (!(be instanceof AbstractBogeyBlockEntity sbbe))
                return ActionResult.FAIL;

            player.getItemCooldownManager().set(stack, 20);
            BogeyStyle currentStyle = sbbe.getStyle();

            BogeySize size = getSize();

            BogeyStyle style = this.getNextStyle(currentStyle);
            if (style == currentStyle)
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

            Set<BogeySize> validSizes = style.validSizes();

            for (int i = 0; i < AllBogeySizes.all().size(); i++) {
                if (validSizes.contains(size))
                    break;
                size = size.nextBySize();
            }

            sbbe.setBogeyStyle(style);

            NbtCompound defaultData = style.defaultData;
            sbbe.setBogeyData(sbbe.getBogeyData().copyFrom(defaultData));

            if (size == getSize()) {
                if (state.getBlock() != style.getBlockForSize(size)) {
                    NbtCompound oldData = sbbe.getBogeyData();
                    level.setBlockState(pos, copyProperties(state, getStateOfSize(sbbe, size)));
                    if (!(level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity bogeyBlockEntity))
                        return ActionResult.FAIL;
                    bogeyBlockEntity.setBogeyData(oldData);
                }
                player.sendMessage(Text.translatable("create.bogey.style.updated_style").append(": ").append(style.displayName), true);
            } else {
                NbtCompound oldData = sbbe.getBogeyData();
                level.setBlockState(pos, getStateOfSize(sbbe, size));
                if (!(level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity bogeyBlockEntity))
                    return ActionResult.FAIL;
                bogeyBlockEntity.setBogeyData(oldData);
                player.sendMessage(Text.translatable("create.bogey.style.updated_style_and_size").append(": ").append(style.displayName), true);
            }

            return ActionResult.CONSUME;
        }

        return onInteractWithBogey(state, level, pos, player, hand, hitResult);
    }

    // Allows for custom interactions with bogey block to be added simply
    protected ActionResult onInteractWithBogey(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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
            Block newBlock = Registries.BLOCK.get(id);
            if (newBlock instanceof AbstractBogeyBlock<?> bogey) {
                BlockState matchingBogey = bogey.getMatchingBogey(bogeyUpDirection, trackAxisAlongFirstCoordinate);
                if (matchingBogey != null)
                    return copyProperties(state, matchingBogey);
            }
            index = (index + 1) % bogeyCycle.size();
        }

        return state;
    }

    public BlockState getNextSize(World level, BlockPos pos) {
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
        if (source.contains(property) && target.contains(property)) {
            return target.with(property, source.get(property));
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
        BlockState nextBlock = style.getNextBlock(size).getDefaultState();
        nextBlock = copyProperties(sbbe.getCachedState(), nextBlock);
        return nextBlock;
    }

    public BlockState getStateOfSize(AbstractBogeyBlockEntity sbbe, BogeySize size) {
        BogeyStyle style = sbbe.getStyle();
        BlockState state = style.getBlockForSize(size).getDefaultState();
        return copyProperties(sbbe.getCachedState(), state);
    }

    public BogeyStyle getNextStyle(World level, BlockPos pos) {
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
    public @NotNull BlockState rotate(@NotNull BlockState pState, BlockRotation pRotation) {
        return switch (pRotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> pState.cycle(AXIS);
            default -> pState;
        };
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, AllItems.RAILWAY_CASING.getDefaultStack());
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
