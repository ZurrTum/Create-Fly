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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TrackBlock extends Block implements IBE<TrackBlockEntity>, IWrenchable, ITrackBlock, SpecialBlockItemRequirement, ProperWaterloggedBlock {

    public static final EnumProperty<TrackShape> SHAPE = EnumProperty.of("shape", TrackShape.class);
    public static final BooleanProperty HAS_BE = BooleanProperty.of("turn");

    protected final TrackMaterial material;

    public TrackBlock(Settings p_49795_, TrackMaterial material) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(SHAPE, TrackShape.ZO).with(HAS_BE, false).with(WATERLOGGED, false));
        this.material = material;
    }

    public static TrackBlock andesite(Settings settings) {
        return new TrackBlock(settings, AllTrackMaterials.ANDESITE);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> p_49915_) {
        super.appendProperties(p_49915_.add(SHAPE, HAS_BE, WATERLOGGED));
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
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState stateForPlacement = withWater(super.getPlacementState(ctx), ctx);

        if (ctx.getPlayer() == null)
            return stateForPlacement;

        Vec3d lookAngle = ctx.getPlayer().getRotationVector();
        lookAngle = lookAngle.multiply(1, 0, 1);
        if (MathHelper.approximatelyEquals(lookAngle.length(), 0))
            lookAngle = VecHelper.rotate(new Vec3d(0, 0, 1), -ctx.getPlayer().getYaw(), Axis.Y);

        lookAngle = lookAngle.normalize();

        TrackShape best = TrackShape.ZO;
        double bestValue = Float.MAX_VALUE;
        for (TrackShape shape : TrackShape.values()) {
            if (shape.isJunction() || shape.isPortal())
                continue;
            Vec3d axis = shape.getAxes().getFirst();
            double distance = Math.min(axis.squaredDistanceTo(lookAngle), axis.normalize().multiply(-1).squaredDistanceTo(lookAngle));
            if (distance > bestValue)
                continue;
            bestValue = distance;
            best = shape;
        }

        World level = ctx.getWorld();
        Vec3d bestAxis = best.getAxes().getFirst();
        if (bestAxis.lengthSquared() == 1)
            for (boolean neg : Iterate.trueAndFalse) {
                BlockPos offset = ctx.getBlockPos().add(BlockPos.ofFloored(bestAxis.multiply(neg ? -1 : 1)));

                if (level.getBlockState(offset).isSideSolidFullSquare(level, offset, Direction.UP) && !level.getBlockState(offset.up())
                    .isSideSolidFullSquare(level, offset, Direction.DOWN)) {
                    if (best == TrackShape.XO)
                        best = neg ? TrackShape.AW : TrackShape.AE;
                    if (best == TrackShape.ZO)
                        best = neg ? TrackShape.AN : TrackShape.AS;
                }
            }

        return stateForPlacement.with(SHAPE, best);
    }

    @Override
    public BlockState onBreak(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
        super.onBreak(pLevel, pPos, pState, pPlayer);
        if (pLevel.isClient())
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
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.isOf(this)) {
            if (pState.with(HAS_BE, true) == pOldState.with(HAS_BE, true)) {
                return;
            }
            TrackPropagator.onRailRemoved(pLevel, pPos, pState);
        }
        if (pLevel.isClient) {
            return;
        }
        QueryableTickScheduler<Block> blockTicks = pLevel.getBlockTickScheduler();
        if (!blockTicks.isQueued(pPos, this))
            pLevel.scheduleBlockTick(pPos, this, 1);
        updateGirders(pState, pLevel, pPos, blockTicks);
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        withBlockEntityDo(pLevel, pPos, TrackBlockEntity::validateConnections);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld level, BlockPos pos, Random p_60465_) {
        TrackPropagator.onRailAdded(level, pos, state);
        withBlockEntityDo(level, pos, tbe -> tbe.tilt.undoSmoothing());
        if (!state.get(SHAPE).isPortal())
            connectToPortal(level, pos, state);
    }

    protected void connectToPortal(ServerWorld level, BlockPos pos, BlockState state) {
        TrackShape shape = state.get(TrackBlock.SHAPE);
        Axis portalTest = shape == TrackShape.XO ? Axis.X : shape == TrackShape.ZO ? Axis.Z : null;
        if (portalTest == null)
            return;

        boolean pop = false;
        String fail = null;
        BlockPos failPos = null;

        for (Direction d : Iterate.directionsInAxis(portalTest)) {
            BlockPos portalPos = pos.offset(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!PortalTrackProvider.isSupportedPortal(portalState))
                continue;

            pop = true;
            PortalTrackProvider.Exit otherSide = PortalTrackProvider.getOtherSide(level, new BlockFace(pos, d));
            if (otherSide == null) {
                fail = "missing";
                continue;
            }

            ServerWorld otherLevel = otherSide.level();
            BlockFace otherTrack = otherSide.face();
            BlockPos otherTrackPos = otherTrack.getPos();
            BlockState existing = otherLevel.getBlockState(otherTrackPos);
            if (!existing.isReplaceable()) {
                fail = "blocked";
                failPos = otherTrackPos;
                continue;
            }

            level.setBlockState(pos, state.with(SHAPE, TrackShape.asPortal(d)).with(HAS_BE, true), Block.NOTIFY_ALL);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrackBlockEntity tbe)
                tbe.bind(otherLevel.getRegistryKey(), otherTrackPos);

            BlockState otherState = ProperWaterloggedBlock.withWater(
                otherLevel,
                state.with(SHAPE, TrackShape.asPortal(otherTrack.getFace())).with(HAS_BE, true),
                otherTrackPos
            );
            otherLevel.setBlockState(otherTrackPos, otherState, Block.NOTIFY_ALL);
            BlockEntity otherBE = otherLevel.getBlockEntity(otherTrackPos);
            if (otherBE instanceof TrackBlockEntity tbe)
                tbe.bind(level.getRegistryKey(), pos);

            pop = false;
        }

        if (!pop)
            return;

        level.breakBlock(pos, true);

        if (fail == null)
            return;
        PlayerEntity player = level.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10, Predicates.alwaysTrue());
        if (player == null)
            return;
        player.sendMessage(Text.literal("<!> ").append(Text.translatable("create.portal_track.failed")).formatted(Formatting.GOLD), false);
        MutableText component = failPos != null ? Text.translatable(
            "create.portal_track." + fail,
            failPos.getX(),
            failPos.getY(),
            failPos.getZ()
        ) : Text.translatable("create.portal_track." + fail);
        player.sendMessage(Text.literal(" - ").formatted(Formatting.GRAY).append(component.withColor(0xFFD3B4)), false);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView level,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(level, tickView, state, pCurrentPos);
        TrackShape shape = state.get(SHAPE);
        if (!shape.isPortal())
            return state;

        for (Direction d : Iterate.horizontalDirections) {
            if (TrackShape.asPortal(d) != state.get(SHAPE))
                continue;
            if (pDirection != d)
                continue;

            BlockPos portalPos = pCurrentPos.offset(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!PortalTrackProvider.isSupportedPortal(portalState))
                return Blocks.AIR.getDefaultState();
        }

        return state;
    }

    @Override
    public int getYOffsetAt(BlockView world, BlockPos pos, BlockState state, Vec3d end) {
        return getBlockEntityOptional(world, pos).map(tbe -> tbe.tilt.getYOffsetForAxisEnd(end)).orElse(0);
    }

    @Override
    public Collection<DiscoveredLocation> getConnected(
        BlockView worldIn,
        BlockPos pos,
        BlockState state,
        boolean linear,
        TrackNodeLocation connectedTo
    ) {
        Collection<DiscoveredLocation> list;
        BlockView world = connectedTo != null && worldIn instanceof ServerWorld sl ? sl.getServer().getWorld(connectedTo.dimension) : worldIn;

        if (getTrackAxes(world, pos, state).size() > 1) {
            Vec3d center = Vec3d.ofBottomCenter(pos).add(0, getElevationAtCenter(world, pos, state), 0);
            TrackShape shape = state.get(TrackBlock.SHAPE);
            list = new ArrayList<>();
            for (Vec3d axis : getTrackAxes(world, pos, state))
                for (boolean fromCenter : Iterate.trueAndFalse)
                    ITrackBlock.addToListIfConnected(
                        connectedTo,
                        list,
                        (d, b) -> axis.multiply(b ? 0 : fromCenter ? -d : d).add(center),
                        b -> shape.getNormal(),
                        b -> world instanceof World l ? l.getRegistryKey() : World.OVERWORLD,
                        v -> 0,
                        axis,
                        null,
                        (b, v) -> ITrackBlock.getMaterialSimple(world, v)
                    );
        } else
            list = ITrackBlock.super.getConnected(world, pos, state, linear, connectedTo);

        if (!state.get(HAS_BE))
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
            (d, b) -> d == 1 ? Vec3d.of(bc.bePositions.get(b)) : bc.starts.get(b),
            bc.normals::get,
            b -> world instanceof World l ? l.getRegistryKey() : World.OVERWORLD,
            bc::yOffsetAt,
            null,
            bc,
            (b, v) -> ITrackBlock.getMaterialSimple(world, v, bc.getMaterial())
        ));

        if (trackBE.boundLocation == null || !(world instanceof ServerWorld level))
            return list;

        RegistryKey<World> otherDim = trackBE.boundLocation.getFirst();
        ServerWorld otherLevel = level.getServer().getWorld(otherDim);
        if (otherLevel == null)
            return list;
        BlockPos boundPos = trackBE.boundLocation.getSecond();
        BlockState boundState = otherLevel.getBlockState(boundPos);
        if (!boundState.isIn(AllBlockTags.TRACKS))
            return list;

        Vec3d center = Vec3d.ofBottomCenter(pos).add(0, getElevationAtCenter(world, pos, state), 0);
        Vec3d boundCenter = Vec3d.ofBottomCenter(boundPos).add(0, getElevationAtCenter(otherLevel, boundPos, boundState), 0);
        TrackShape shape = state.get(TrackBlock.SHAPE);
        TrackShape boundShape = boundState.get(TrackBlock.SHAPE);
        Vec3d boundAxis = getTrackAxes(otherLevel, boundPos, boundState).getFirst();

        getTrackAxes(world, pos, state).forEach(axis -> {
            ITrackBlock.addToListIfConnected(
                connectedTo,
                list,
                (d, b) -> (b ? axis : boundAxis).multiply(d).add(b ? center : boundCenter),
                b -> (b ? shape : boundShape).getNormal(),
                b -> b ? level.getRegistryKey() : otherLevel.getRegistryKey(),
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
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        TrackPropagator.onRailRemoved(pLevel, pPos, pState);
        if (!pLevel.isClient)
            updateGirders(pState, pLevel, pPos, pLevel.getBlockTickScheduler());
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
        if (level.isClient)
            return ActionResult.SUCCESS;
        for (Map.Entry<BlockPos, BlockBox> entry : StationBlockEntity.assemblyAreas.get(level).entrySet()) {
            if (!entry.getValue().contains(pos))
                continue;
            if (level.getBlockEntity(entry.getKey()) instanceof StationBlockEntity station)
                if (station.trackClicked(player, hand, this, state, pos))
                    return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    private void updateGirders(BlockState pState, World pLevel, BlockPos pPos, QueryableTickScheduler<Block> blockTicks) {
        for (Vec3d vec3 : getTrackAxes(pLevel, pPos, pState)) {
            if (vec3.length() > 1 || vec3.y != 0)
                continue;
            for (int side : Iterate.positiveAndNegative) {
                BlockPos girderPos = pPos.down().add(BlockPos.ofFloored(vec3.z * side, 0, vec3.x * side));
                BlockState girderState = pLevel.getBlockState(girderPos);
                if (girderState.getBlock() instanceof GirderBlock girderBlock && !blockTicks.isQueued(girderPos, girderBlock))
                    pLevel.scheduleBlockTick(girderPos, girderBlock, 1);
            }
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView reader, BlockPos pos) {
        return reader.getBlockState(pos.down()).getBlock() != this;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_60556_, BlockPos p_60557_, ShapeContext p_60558_) {
        return getFullShape(state);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView pLevel, BlockPos pPos) {
        return getFullShape(state);
    }

    private VoxelShape getFullShape(BlockState state) {
        return switch (state.get(SHAPE)) {
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
    public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return switch (pState.get(SHAPE)) {
            case AE, AW, AN, AS -> VoxelShapes.empty();
            default -> AllShapes.TRACK_COLLISION;
        };
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos p_153215_, BlockState state) {
        if (!state.get(HAS_BE))
            return null;
        return AllBlockEntityTypes.TRACK.instantiate(p_153215_, state);
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
    public Vec3d getUpNormal(BlockView world, BlockPos pos, BlockState state) {
        return state.get(SHAPE).getNormal();
    }

    @Override
    public List<Vec3d> getTrackAxes(BlockView world, BlockPos pos, BlockState state) {
        return state.get(SHAPE).getAxes();
    }

    @Override
    public Vec3d getCurveStart(BlockView world, BlockPos pos, BlockState state, Vec3d axis) {
        boolean vertical = axis.y != 0;
        return VecHelper.getCenterOf(pos).add(0, (vertical ? 0 : -.5f), 0).add(axis.multiply(.5));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World level = context.getWorld();
        if (!level.isClient && !player.isCreative() && state.get(HAS_BE)) {
            BlockEntity blockEntity = level.getBlockEntity(context.getBlockPos());
            if (blockEntity instanceof TrackBlockEntity trackBE) {
                trackBE.cancelDrops = true;
                trackBE.connections.values().forEach(bc -> bc.addItemsToPlayer(player));
            }
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public BlockState overlay(BlockView world, BlockPos pos, BlockState existing, BlockState placed) {
        if (placed.getBlock() != this)
            return existing;

        TrackShape existingShape = existing.get(SHAPE);
        TrackShape placedShape = placed.get(SHAPE);
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
            existing = existing.with(SHAPE, combinedShape);
        return existing;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation pRotation) {
        return state.with(SHAPE, state.get(SHAPE).rotate(pRotation));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror pMirror) {
        return state.with(SHAPE, state.get(SHAPE).mirror(pMirror));
    }

    @Override
    public BlockState getBogeyAnchor(BlockView world, BlockPos pos, BlockState state) {
        return AllBlocks.SMALL_BOGEY.getDefaultState().with(Properties.HORIZONTAL_AXIS, state.get(SHAPE) == TrackShape.XO ? Axis.X : Axis.Z);
    }

    @Override
    public boolean trackEquals(BlockState state1, BlockState state2) {
        return state1.getBlock() == this && state2.getBlock() == this && state1.with(HAS_BE, false) == state2.with(HAS_BE, false);
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
