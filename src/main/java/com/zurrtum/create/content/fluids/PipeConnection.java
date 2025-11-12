package com.zurrtum.create.content.fluids;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PipeConnection {

    public Direction side;

    // Layer I
    Couple<Float> pressure; // [inbound, outward]
    Optional<FlowSource> source;
    Optional<FlowSource> previousSource;

    // Layer II
    Optional<Flow> flow;
    boolean particleSplashNextTick;

    // Layer III
    Optional<FluidNetwork> network; // not serialized

    public PipeConnection(Direction side) {
        this.side = side;
        pressure = Couple.create(() -> 0f);
        flow = Optional.empty();
        previousSource = Optional.empty();
        source = Optional.empty();
        network = Optional.empty();
        particleSplashNextTick = false;
    }

    public FluidStack getProvidedFluid() {
        FluidStack empty = FluidStack.EMPTY;
        if (!hasFlow())
            return empty;
        Flow flow = this.flow.get();
        if (!flow.inbound)
            return empty;
        if (!flow.complete)
            return empty;
        return flow.fluid;
    }

    public boolean flipFlowsIfPressureReversed() {
        if (!hasFlow())
            return false;
        boolean singlePressure = comparePressure() != 0 && (getInboundPressure() == 0 || getOutwardPressure() == 0);
        Flow flow = this.flow.get();
        if (!singlePressure || comparePressure() < 0 == flow.inbound)
            return false;
        flow.inbound = !flow.inbound;
        if (!flow.complete)
            this.flow = Optional.empty();
        return true;
    }

    public void manageSource(Level world, BlockPos pos, BlockEntity blockEntity) {
        if (!source.isPresent() && !determineSource(world, pos))
            return;
        FlowSource flowSource = source.get();
        flowSource.manageSource(world, blockEntity);
    }

    public boolean manageFlows(Level world, BlockPos pos, FluidStack internalFluid, Predicate<FluidStack> extractionPredicate) {

        // Only keep network if still valid
        Optional<FluidNetwork> retainedNetwork = network;
        network = Optional.empty();

        // chunk border
        if (!source.isPresent() && !determineSource(world, pos))
            return false;
        FlowSource flowSource = source.get();

        if (!hasFlow()) {
            if (!hasPressure())
                return false;

            // Try starting a new flow
            boolean prioritizeInbound = comparePressure() < 0;
            for (boolean trueFalse : Iterate.trueAndFalse) {
                boolean inbound = prioritizeInbound == trueFalse;
                if (pressure.get(inbound) == 0)
                    continue;
                if (tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid))
                    return true;
            }
            return false;
        }

        // Manage existing flow
        Flow flow = this.flow.get();
        FluidStack provided = flow.inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid;
        if (!hasPressure() || provided.isEmpty() || !FluidStack.areFluidsAndComponentsEqual(provided, flow.fluid)) {
            this.flow = Optional.empty();
            return true;
        }

        // Overwrite existing flow
        if (flow.inbound != comparePressure() < 0) {
            boolean inbound = !flow.inbound;
            if (inbound && !provided.isEmpty() || !inbound && !internalFluid.isEmpty()) {
                FluidPropagator.resetAffectedFluidNetworks(world, pos, side);
                tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid);
                return true;
            }
        }

        flowSource.whileFlowPresent(world, flow.inbound);

        if (!flowSource.isEndpoint())
            return false;
        if (!flow.inbound)
            return false;

        // Layer III
        network = retainedNetwork;
        if (!hasNetwork())
            network = Optional.of(new FluidNetwork(world, new BlockFace(pos, side), flowSource::provideHandler));
        network.get().tick();

        return false;
    }

    private boolean tryStartingNewFlow(boolean inbound, FluidStack providedFluid) {
        if (providedFluid.isEmpty())
            return false;
        Flow flow = new Flow(inbound, providedFluid);
        this.flow = Optional.of(flow);
        return true;
    }

    public boolean determineSource(Level world, BlockPos pos) {
        BlockPos relative = pos.relative(side);
        // cannot use world.isLoaded because it always returns true on client
        if (world.getChunk(relative.getX() >> 4, relative.getZ() >> 4, ChunkStatus.FULL, false) == null)
            return false;

        BlockFace location = new BlockFace(pos, side);
        if (FluidPropagator.isOpenEnd(world, pos, side)) {
            if (previousSource.orElse(null) instanceof OpenEndedPipe)
                source = previousSource;
            else
                source = Optional.of(new OpenEndedPipe(location));
            return true;
        }

        if (FluidPropagator.hasFluidCapability(world, location.getConnectedPos(), side.getOpposite())) {
            source = Optional.of(new FlowSource.FluidHandler(location));
            return true;
        }

        FluidTransportBehaviour behaviour = BlockEntityBehaviour.get(world, relative, FluidTransportBehaviour.TYPE);
        source = Optional.of(behaviour == null ? new FlowSource.Blocked(location) : new FlowSource.OtherPipe(location));
        return true;
    }

    public void tickFlowProgress(Level world, BlockPos pos) {
        if (!hasFlow())
            return;
        Flow flow = this.flow.get();
        if (flow.fluid.isEmpty())
            return;

        if (world.isClientSide()) {
            if (!source.isPresent())
                determineSource(world, pos);

            boolean openEnd = hasOpenEnd();
            int amount = 1;
            if (particleSplashNextTick) {
                amount += PipeConnection.SPLASH_PARTICLE_AMOUNT;
            }
            AllClientHandle.INSTANCE.spawnPipeParticles(world, pos, flow, openEnd, side, amount);
            particleSplashNextTick = false;
        }

        float flowSpeed = 1 / 32f + Mth.clamp(pressure.get(flow.inbound) / 128f, 0, 1) * 31 / 32f;
        flow.progress.setValue(Math.min(flow.progress.getValue() + flowSpeed, 1));
        if (flow.progress.getValue() >= 1)
            flow.complete = true;
    }

    public void write(ValueOutput view, BlockPos blockEntityPos, boolean clientPacket) {
        if (hasPressure()) {
            view.store("Pressure", CreateCodecs.FLOAT_LIST_CODEC, List.of(getInboundPressure(), getOutwardPressure()));
        }

        if (source.orElse(null) instanceof OpenEndedPipe openEndedPipe)
            view.store("OpenEnd", OpenEndedPipe.codec(blockEntityPos), openEndedPipe);

        flow.ifPresent(flow -> {
            ValueOutput flowData = view.child("Flow");
            if (!flow.fluid.isEmpty()) {
                flowData.store("Fluid", FluidStack.CODEC, flow.fluid);
            }
            flowData.putBoolean("In", flow.inbound);
            if (!flow.complete)
                flow.progress.write(flowData.child("Progress"));
        });

    }

    private boolean hasOpenEnd() {
        return source.orElse(null) instanceof OpenEndedPipe;
    }

    public void read(ValueInput view, BlockPos blockEntityPos, boolean clientPacket) {
        view.read("Pressure", CreateCodecs.FLOAT_LIST_CODEC).ifPresentOrElse(
            list -> {
                pressure = Couple.create(list.getFirst(), list.getLast());
            }, () -> pressure.replace((f -> 0f))
        );

        source = Optional.ofNullable(view.read("OpenEnd", OpenEndedPipe.codec(blockEntityPos)).orElse(null));

        view.child("Flow").ifPresentOrElse(
            flowData -> {
                boolean inbound = flowData.getBooleanOr("In", false);
                FluidStack fluid = flowData.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
                Flow flow;
                if (this.flow.isEmpty()) {
                    flow = new Flow(inbound, fluid);
                    this.flow = Optional.of(flow);
                    if (clientPacket)
                        particleSplashNextTick = true;
                } else {
                    flow = this.flow.get();
                    flow.fluid = fluid;
                    flow.inbound = inbound;
                }
                flowData.child("Progress").ifPresentOrElse(
                    progress -> {
                        flow.complete = false;
                        flow.progress.read(progress, clientPacket);
                    }, () -> {
                        flow.complete = true;
                        if (flow.progress.getValue() == 0)
                            flow.progress.startWithValue(1);
                        flow.progress.setValue(1);
                    }
                );
            }, () -> flow = Optional.empty()
        );
    }

    /**
     * @return zero if outward == inbound <br>
     * positive if outward {@literal >} inbound <br>
     * negative if outward {@literal <} inbound
     */
    public float comparePressure() {
        return getOutwardPressure() - getInboundPressure();
    }

    public void wipePressure() {
        this.pressure.replace(f -> 0f);
        if (this.source.isPresent())
            this.previousSource = this.source;
        this.source = Optional.empty();
        resetNetwork();
    }

    public FluidStack provideOutboundFlow() {
        if (!hasFlow())
            return FluidStack.EMPTY;
        Flow flow = this.flow.get();
        if (!flow.complete || flow.inbound)
            return FluidStack.EMPTY;
        return flow.fluid;
    }

    public void addPressure(boolean inbound, float pressure) {
        this.pressure = this.pressure.mapWithContext((f, in) -> in == inbound ? f + pressure : f);
    }

    public Couple<Float> getPressure() {
        return pressure;
    }

    public boolean hasPressure() {
        return getInboundPressure() != 0 || getOutwardPressure() != 0;
    }

    private float getOutwardPressure() {
        return pressure.getSecond();
    }

    private float getInboundPressure() {
        return pressure.getFirst();
    }

    public boolean hasFlow() {
        return flow.isPresent();
    }

    public boolean hasNetwork() {
        return network.isPresent();
    }

    public void resetNetwork() {
        network.ifPresent(FluidNetwork::reset);
    }

    public class Flow {

        public boolean complete;
        public boolean inbound;
        public LerpedFloat progress;
        public FluidStack fluid;

        public Flow(boolean inbound, FluidStack fluid) {
            this.inbound = inbound;
            this.fluid = fluid;
            this.progress = LerpedFloat.linear().startWithValue(0);
            this.complete = false;
        }

    }

    public static final int MAX_PARTICLE_RENDER_DISTANCE = 20;
    public static final int SPLASH_PARTICLE_AMOUNT = 1;
    public static final float IDLE_PARTICLE_SPAWN_CHANCE = 1 / 1000f;
    public static final float RIM_RADIUS = 1 / 4f + 1 / 64f;

    //	void visualizePressure(BlockPos pos) {
    //		if (!hasPressure())
    //			return;
    //
    //		pressure.forEachWithContext((pressure, inbound) -> {
    //			if (inbound)
    //				return;
    //
    //			Vector3d directionVec = new Vector3d(side.getDirectionVec());
    //			Vector3d scaleVec = directionVec.scale(-.25f * side.getAxisDirection()
    //				.getOffset());
    //			directionVec = directionVec.scale(inbound ? .35f : .45f);
    //			CreateClient.outliner.chaseAABB("pressure" + pos.toShortString() + side.getName() + String.valueOf(inbound),
    //				FluidPropagator.smallCenter.offset(directionVec.add(new Vector3d(pos)))
    //					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
    //					.expand(0, pressure / 64f, 0)
    //					.grow(1 / 64f));
    //		});
    //	}
    //
    //	void visualizeFlow(BlockPos pos) {
    //		if (!hasFlow())
    //			return;
    //
    //		Vector3d directionVec = new Vector3d(side.getDirectionVec());
    //		float size = 1 / 4f;
    //		float length = .5f;
    //		Flow flow = this.flow.get();
    //		boolean inbound = flow.inbound;
    //		FluidStack fluid = flow.fluid;
    //
    //		if (flow.progress == null)
    //			return;
    //		float value = flow.progress.getValue();
    //		Vector3d start = directionVec.scale(inbound ? .5 : .5f - length);
    //		Vector3d offset = directionVec.scale(length * (inbound ? -1 : 1))
    //			.scale(value);
    //
    //		Vector3d scale = new Vector3d(1, 1, 1).subtract(directionVec.scale(side.getAxisDirection()
    //			.getOffset()))
    //			.scale(size);
    //		AABB bb = new AABB(start, start.add(offset)).offset(VecHelper.getCenterOf(pos))
    //			.grow(scale.x, scale.y, scale.z);
    //
    //		int color = 0x7fdbda;
    //		if (!fluid.isEmpty()) {
    //			Fluid fluid2 = fluid.getFluid();
    //			if (fluid2 == Fluids.WATER)
    //				color = 0x1D4D9B;
    //			else if (fluid2 == Fluids.LAVA)
    //				color = 0xFF773D;
    //			else
    //				color = fluid2.getAttributes()
    //					.getColor(fluid);
    //		}
    //
    //		CreateClient.outliner.chaseAABB(this, bb)
    //			.withFaceTexture(AllSpecialTextures.SELECTION)
    //			.colored(color)
    //			.lineWidth(0);
    //	}

}
