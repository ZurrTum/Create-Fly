package com.zurrtum.create.content.trains.track;

import com.google.common.base.Predicates;
import com.zurrtum.create.*;
import com.zurrtum.create.api.contraption.train.PortalTrackProvider;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TrackBlock extends Block implements IBE<TrackBlockEntity>, IWrenchable, ITrackBlock, SpecialBlockItemRequirement, ProperWaterloggedBlock {

    public static final EnumProperty<TrackShape> SHAPE = EnumProperty.create("shape", TrackShape.class);
    public static final BooleanProperty HAS_BE = BooleanProperty.create("turn");

    protected final TrackMaterial material;

    public TrackBlock(Properties p_49795_, TrackMaterial material) {
        super(p_49795_);
        registerDefaultState(defaultBlockState().setValue(SHAPE, TrackShape.ZO).setValue(HAS_BE, false).setValue(WATERLOGGED, false));
        this.material = material;
    }

    public static TrackBlock andesite(Properties settings) {
        return new TrackBlock(settings, AllTrackMaterials.ANDESITE);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_.add(SHAPE, HAS_BE, WATERLOGGED));
    }

    //TODO
    //    @Override
    //    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
    //        return PathType.RAIL;
    //    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState stateForPlacement = withWater(super.getStateForPlacement(ctx), ctx);

        if (ctx.getPlayer() == null)
            return stateForPlacement;

        Vec3 lookAngle = ctx.getPlayer().getLookAngle();
        lookAngle = lookAngle.multiply(1, 0, 1);
        if (Mth.equal(lookAngle.length(), 0))
            lookAngle = VecHelper.rotate(new Vec3(0, 0, 1), -ctx.getPlayer().getYRot(), Axis.Y);

        lookAngle = lookAngle.normalize();

        TrackShape best = TrackShape.ZO;
        double bestValue = Float.MAX_VALUE;
        for (TrackShape shape : TrackShape.values()) {
            if (shape.isJunction() || shape.isPortal())
                continue;
            Vec3 axis = shape.getAxes().getFirst();
            double distance = Math.min(axis.distanceToSqr(lookAngle), axis.normalize().scale(-1).distanceToSqr(lookAngle));
            if (distance > bestValue)
                continue;
            bestValue = distance;
            best = shape;
        }

        Level level = ctx.getLevel();
        Vec3 bestAxis = best.getAxes().getFirst();
        if (bestAxis.lengthSqr() == 1)
            for (boolean neg : Iterate.trueAndFalse) {
                BlockPos offset = ctx.getClickedPos().offset(BlockPos.containing(bestAxis.scale(neg ? -1 : 1)));

                if (level.getBlockState(offset).isFaceSturdy(level, offset, Direction.UP) && !level.getBlockState(offset.above())
                    .isFaceSturdy(level, offset, Direction.DOWN)) {
                    if (best == TrackShape.XO)
                        best = neg ? TrackShape.AW : TrackShape.AE;
                    if (best == TrackShape.ZO)
                        best = neg ? TrackShape.AN : TrackShape.AS;
                }
            }

        return stateForPlacement.setValue(SHAPE, best);
    }

    @Override
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        if (pLevel.isClientSide())
            return pState;
        if (!pPlayer.isCreative())
            return pState;
        withBlockEntityDo(
            pLevel, pPos, be -> {
                be.cancelDrops = true;
                be.removeInboundConnections(true);
            }
        );

        return pState;
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.is(this)) {
            if (pState.setValue(HAS_BE, true) == pOldState.setValue(HAS_BE, true)) {
                return;
            }
            TrackPropagator.onRailRemoved(pLevel, pPos, pState);
        }
        if (pLevel.isClientSide()) {
            return;
        }
        LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pPos, this))
            pLevel.scheduleTick(pPos, this, 1);
        updateGirders(pState, pLevel, pPos, blockTicks);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        withBlockEntityDo(pLevel, pPos, TrackBlockEntity::validateConnections);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource p_60465_) {
        TrackPropagator.onRailAdded(level, pos, state);
        withBlockEntityDo(level, pos, tbe -> tbe.tilt.undoSmoothing());
        if (!state.getValue(SHAPE).isPortal())
            connectToPortal(level, pos, state);
    }

    protected void connectToPortal(ServerLevel level, BlockPos pos, BlockState state) {
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        Axis portalTest = shape == TrackShape.XO ? Axis.X : shape == TrackShape.ZO ? Axis.Z : null;
        if (portalTest == null)
            return;

        boolean pop = false;
        String fail = null;
        BlockPos failPos = null;

        for (Direction d : Iterate.directionsInAxis(portalTest)) {
            BlockPos portalPos = pos.relative(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!PortalTrackProvider.isSupportedPortal(portalState))
                continue;

            pop = true;
            PortalTrackProvider.Exit otherSide = PortalTrackProvider.getOtherSide(level, new BlockFace(pos, d));
            if (otherSide == null) {
                fail = "missing";
                continue;
            }

            ServerLevel otherLevel = otherSide.level();
            BlockFace otherTrack = otherSide.face();
            BlockPos otherTrackPos = otherTrack.getPos();
            BlockState existing = otherLevel.getBlockState(otherTrackPos);
            if (!existing.canBeReplaced()) {
                fail = "blocked";
                failPos = otherTrackPos;
                continue;
            }

            level.setBlock(pos, state.setValue(SHAPE, TrackShape.asPortal(d)).setValue(HAS_BE, true), Block.UPDATE_ALL);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrackBlockEntity tbe)
                tbe.bind(otherLevel.dimension(), otherTrackPos);

            BlockState otherState = ProperWaterloggedBlock.withWater(
                otherLevel,
                state.setValue(SHAPE, TrackShape.asPortal(otherTrack.getFace())).setValue(HAS_BE, true),
                otherTrackPos
            );
            otherLevel.setBlock(otherTrackPos, otherState, Block.UPDATE_ALL);
            BlockEntity otherBE = otherLevel.getBlockEntity(otherTrackPos);
            if (otherBE instanceof TrackBlockEntity tbe)
                tbe.bind(level.dimension(), pos);

            pop = false;
        }

        if (!pop)
            return;

        level.destroyBlock(pos, true);

        if (fail == null)
            return;
        Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10, Predicates.alwaysTrue());
        if (player == null)
            return;
        player.displayClientMessage(
            Component.literal("<!> ").append(Component.translatable("create.portal_track.failed")).withStyle(ChatFormatting.GOLD), false);
        MutableComponent component = failPos != null ? Component.translatable(
            "create.portal_track." + fail,
            failPos.getX(),
            failPos.getY(),
            failPos.getZ()
        ) : Component.translatable("create.portal_track." + fail);
        player.displayClientMessage(Component.literal(" - ").withStyle(ChatFormatting.GRAY).append(component.withColor(0xFFD3B4)), false);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(level, tickView, state, pCurrentPos);
        TrackShape shape = state.getValue(SHAPE);
        if (!shape.isPortal())
            return state;

        for (Direction d : Iterate.horizontalDirections) {
            if (TrackShape.asPortal(d) != state.getValue(SHAPE))
                continue;
            if (pDirection != d)
                continue;

            BlockPos portalPos = pCurrentPos.relative(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!PortalTrackProvider.isSupportedPortal(portalState))
                return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public int getYOffsetAt(BlockGetter world, BlockPos pos, BlockState state, Vec3 end) {
        return getBlockEntityOptional(world, pos).map(tbe -> tbe.tilt.getYOffsetForAxisEnd(end)).orElse(0);
    }

    @Override
    public Collection<DiscoveredLocation> getConnected(
        BlockGetter worldIn,
        BlockPos pos,
        BlockState state,
        boolean linear,
        TrackNodeLocation connectedTo
    ) {
        Collection<DiscoveredLocation> list;
        BlockGetter world = connectedTo != null && worldIn instanceof ServerLevel sl ? sl.getServer().getLevel(connectedTo.dimension) : worldIn;

        if (getTrackAxes(world, pos, state).size() > 1) {
            Vec3 center = Vec3.atBottomCenterOf(pos).add(0, getElevationAtCenter(world, pos, state), 0);
            TrackShape shape = state.getValue(TrackBlock.SHAPE);
            list = new ArrayList<>();
            for (Vec3 axis : getTrackAxes(world, pos, state))
                for (boolean fromCenter : Iterate.trueAndFalse)
                    ITrackBlock.addToListIfConnected(
                        connectedTo,
                        list,
                        (d, b) -> axis.scale(b ? 0 : fromCenter ? -d : d).add(center),
                        b -> shape.getNormal(),
                        b -> world instanceof Level l ? l.dimension() : Level.OVERWORLD,
                        v -> 0,
                        axis,
                        null,
                        (b, v) -> ITrackBlock.getMaterialSimple(world, v)
                    );
        } else
            list = ITrackBlock.super.getConnected(world, pos, state, linear, connectedTo);

        if (!state.getValue(HAS_BE))
            return list;
        if (linear)
            return list;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TrackBlockEntity trackBE))
            return list;

        Map<BlockPos, BezierConnection> connections = trackBE.getConnections();
        connections.forEach((connectedPos, bc) -> ITrackBlock.addToListIfConnected(
            connectedTo,
            list,
            (d, b) -> d == 1 ? Vec3.atLowerCornerOf(bc.bePositions.get(b)) : bc.starts.get(b),
            bc.normals::get,
            b -> world instanceof Level l ? l.dimension() : Level.OVERWORLD,
            bc::yOffsetAt,
            null,
            bc,
            (b, v) -> ITrackBlock.getMaterialSimple(world, v, bc.getMaterial())
        ));

        if (trackBE.boundLocation == null || !(world instanceof ServerLevel level))
            return list;

        ResourceKey<Level> otherDim = trackBE.boundLocation.getFirst();
        ServerLevel otherLevel = level.getServer().getLevel(otherDim);
        if (otherLevel == null)
            return list;
        BlockPos boundPos = trackBE.boundLocation.getSecond();
        BlockState boundState = otherLevel.getBlockState(boundPos);
        if (!boundState.is(AllBlockTags.TRACKS))
            return list;

        Vec3 center = Vec3.atBottomCenterOf(pos).add(0, getElevationAtCenter(world, pos, state), 0);
        Vec3 boundCenter = Vec3.atBottomCenterOf(boundPos).add(0, getElevationAtCenter(otherLevel, boundPos, boundState), 0);
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        TrackShape boundShape = boundState.getValue(TrackBlock.SHAPE);
        Vec3 boundAxis = getTrackAxes(otherLevel, boundPos, boundState).getFirst();

        getTrackAxes(world, pos, state).forEach(axis -> {
            ITrackBlock.addToListIfConnected(
                connectedTo,
                list,
                (d, b) -> (b ? axis : boundAxis).scale(d).add(b ? center : boundCenter),
                b -> (b ? shape : boundShape).getNormal(),
                b -> b ? level.dimension() : otherLevel.dimension(),
                v -> 0,
                axis,
                null,
                (b, v) -> ITrackBlock.getMaterialSimple(b ? level : otherLevel, v)
            );
        });

        return list;
    }

    //TODO if needed
    //    @Override
    //    public void randomDisplayTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
    //        if (!pState.get(SHAPE).isPortal())
    //            return;
    //        Vec3d v = Vec3d.of(pPos).subtract(.125f, 0, .125f);
    //        CubeParticleData data = new CubeParticleData(1, pRand.nextFloat(), 1, .0125f + .0625f * pRand.nextFloat(), 30, false);
    //        pLevel.addParticleClient(data, v.x + pRand.nextFloat() * 1.5f, v.y + .25f, v.z + pRand.nextFloat() * 1.5f, 0.0D, 0.04D, 0.0D);
    //    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving) {
        TrackPropagator.onRailRemoved(pLevel, pPos, pState);
        if (!pLevel.isClientSide())
            updateGirders(pState, pLevel, pPos, pLevel.getBlockTicks());
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
            return InteractionResult.SUCCESS;
        for (Map.Entry<BlockPos, BoundingBox> entry : StationBlockEntity.assemblyAreas.get(level).entrySet()) {
            if (!entry.getValue().isInside(pos))
                continue;
            if (level.getBlockEntity(entry.getKey()) instanceof StationBlockEntity station)
                if (station.trackClicked(player, hand, this, state, pos))
                    return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private void updateGirders(BlockState pState, Level pLevel, BlockPos pPos, LevelTickAccess<Block> blockTicks) {
        for (Vec3 vec3 : getTrackAxes(pLevel, pPos, pState)) {
            if (vec3.length() > 1 || vec3.y != 0)
                continue;
            for (int side : Iterate.positiveAndNegative) {
                BlockPos girderPos = pPos.below().offset(BlockPos.containing(vec3.z * side, 0, vec3.x * side));
                BlockState girderState = pLevel.getBlockState(girderPos);
                if (girderState.getBlock() instanceof GirderBlock girderBlock && !blockTicks.hasScheduledTick(girderPos, girderBlock))
                    pLevel.scheduleTick(girderPos, girderBlock, 1);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
        return reader.getBlockState(pos.below()).getBlock() != this;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return getFullShape(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter pLevel, BlockPos pPos) {
        return getFullShape(state);
    }

    private VoxelShape getFullShape(BlockState state) {
        return switch (state.getValue(SHAPE)) {
            case AE -> AllShapes.TRACK_ASC.get(Direction.EAST);
            case AW -> AllShapes.TRACK_ASC.get(Direction.WEST);
            case AN -> AllShapes.TRACK_ASC.get(Direction.NORTH);
            case AS -> AllShapes.TRACK_ASC.get(Direction.SOUTH);
            case CR_D -> AllShapes.TRACK_CROSS_DIAG;
            case CR_NDX -> AllShapes.TRACK_CROSS_ORTHO_DIAG.get(Direction.SOUTH);
            case CR_NDZ -> AllShapes.TRACK_CROSS_DIAG_ORTHO.get(Direction.SOUTH);
            case CR_O -> AllShapes.TRACK_CROSS;
            case CR_PDX -> AllShapes.TRACK_CROSS_DIAG_ORTHO.get(Direction.EAST);
            case CR_PDZ -> AllShapes.TRACK_CROSS_ORTHO_DIAG.get(Direction.EAST);
            case ND -> AllShapes.TRACK_DIAG.get(Direction.SOUTH);
            case PD -> AllShapes.TRACK_DIAG.get(Direction.EAST);
            case XO -> AllShapes.TRACK_ORTHO.get(Direction.EAST);
            case ZO -> AllShapes.TRACK_ORTHO.get(Direction.SOUTH);
            case TE -> AllShapes.TRACK_ORTHO_LONG.get(Direction.EAST);
            case TW -> AllShapes.TRACK_ORTHO_LONG.get(Direction.WEST);
            case TS -> AllShapes.TRACK_ORTHO_LONG.get(Direction.SOUTH);
            case TN -> AllShapes.TRACK_ORTHO_LONG.get(Direction.NORTH);
            default -> AllShapes.TRACK_FALLBACK;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(SHAPE)) {
            case AE, AW, AN, AS -> Shapes.empty();
            default -> AllShapes.TRACK_COLLISION;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState state) {
        if (!state.getValue(HAS_BE))
            return null;
        return AllBlockEntityTypes.TRACK.create(p_153215_, state);
    }

    @Override
    public Class<TrackBlockEntity> getBlockEntityClass() {
        return TrackBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrackBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK;
    }

    @Override
    public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state) {
        return state.getValue(SHAPE).getNormal();
    }

    @Override
    public List<Vec3> getTrackAxes(BlockGetter world, BlockPos pos, BlockState state) {
        return state.getValue(SHAPE).getAxes();
    }

    @Override
    public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis) {
        boolean vertical = axis.y != 0;
        return VecHelper.getCenterOf(pos).add(0, (vertical ? 0 : -.5f), 0).add(axis.scale(.5));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide() && !player.isCreative() && state.getValue(HAS_BE)) {
            BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
            if (blockEntity instanceof TrackBlockEntity trackBE) {
                trackBE.cancelDrops = true;
                trackBE.connections.values().forEach(bc -> bc.addItemsToPlayer(player));
            }
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public BlockState overlay(BlockGetter world, BlockPos pos, BlockState existing, BlockState placed) {
        if (placed.getBlock() != this)
            return existing;

        TrackShape existingShape = existing.getValue(SHAPE);
        TrackShape placedShape = placed.getValue(SHAPE);
        TrackShape combinedShape = null;

        for (boolean flip : Iterate.trueAndFalse) {
            TrackShape s1 = flip ? existingShape : placedShape;
            TrackShape s2 = flip ? placedShape : existingShape;
            if (s1 == TrackShape.XO && s2 == TrackShape.ZO)
                combinedShape = TrackShape.CR_O;
            if (s1 == TrackShape.PD && s2 == TrackShape.ND)
                combinedShape = TrackShape.CR_D;
            if (s1 == TrackShape.XO && s2 == TrackShape.PD)
                combinedShape = TrackShape.CR_PDX;
            if (s1 == TrackShape.ZO && s2 == TrackShape.PD)
                combinedShape = TrackShape.CR_PDZ;
            if (s1 == TrackShape.XO && s2 == TrackShape.ND)
                combinedShape = TrackShape.CR_NDX;
            if (s1 == TrackShape.ZO && s2 == TrackShape.ND)
                combinedShape = TrackShape.CR_NDZ;
        }

        if (combinedShape != null)
            existing = existing.setValue(SHAPE, combinedShape);
        return existing;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRotation) {
        return state.setValue(SHAPE, state.getValue(SHAPE).rotate(pRotation));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return state.setValue(SHAPE, state.getValue(SHAPE).mirror(pMirror));
    }

    @Override
    public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state) {
        return AllBlocks.SMALL_BOGEY.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_AXIS, state.getValue(SHAPE) == TrackShape.XO ? Axis.X : Axis.Z);
    }

    @Override
    public boolean trackEquals(BlockState state1, BlockState state2) {
        return state1.getBlock() == this && state2.getBlock() == this && state1.setValue(HAS_BE, false) == state2.setValue(HAS_BE, false);
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        int sameTypeTrackAmount = 1;
        Object2IntMap<TrackMaterial> otherTrackAmounts = new Object2IntArrayMap<>();
        int girderAmount = 0;

        if (be instanceof TrackBlockEntity track) {
            for (BezierConnection bezierConnection : track.getConnections().values()) {
                if (!bezierConnection.isPrimary())
                    continue;
                TrackMaterial material = bezierConnection.getMaterial();
                if (material == getMaterial()) {
                    sameTypeTrackAmount += bezierConnection.getTrackItemCost();
                } else {
                    otherTrackAmounts.put(material, otherTrackAmounts.getOrDefault(material, 0) + 1);
                }
                girderAmount += bezierConnection.getGirderItemCost();
            }
        }

        List<ItemStack> stacks = new ArrayList<>();
        while (sameTypeTrackAmount > 0) {
            stacks.add(new ItemStack(state.getBlock(), Math.min(sameTypeTrackAmount, 64)));
            sameTypeTrackAmount -= 64;
        }
        for (TrackMaterial material : otherTrackAmounts.keySet()) {
            int amt = otherTrackAmounts.getOrDefault(material, 0);
            while (amt > 0) {
                stacks.add(new ItemStack(material, Math.min(amt, 64)));
                amt -= 64;
            }
        }
        while (girderAmount > 0) {
            stacks.add(new ItemStack(AllItems.METAL_GIRDER, Math.min(girderAmount, 64)));
            girderAmount -= 64;
        }

        return new ItemRequirement(ItemUseType.CONSUME, stacks);
    }

    @Override
    public TrackMaterial getMaterial() {
        return material;
    }
}
