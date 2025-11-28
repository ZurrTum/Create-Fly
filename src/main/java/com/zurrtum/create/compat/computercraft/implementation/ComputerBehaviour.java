package com.zurrtum.create.compat.computercraft.implementation;

import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
//import com.zurrtum.create.compat.computercraft.implementation.luaObjects.PackageLuaObject;
import com.zurrtum.create.compat.computercraft.implementation.luaObjects.PackageLuaObject;
import com.zurrtum.create.compat.computercraft.implementation.peripherals.*;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ComputerBehaviour extends AbstractComputerBehaviour {

    SyncedPeripheral<?> peripheral;
    Supplier<SyncedPeripheral<?>> peripheralSupplier;
    SmartBlockEntity be;

    public ComputerBehaviour(SmartBlockEntity be) {
        super(be);
        this.peripheralSupplier = getPeripheralFor(be);
        this.be = be;
    }

    public static Supplier<SyncedPeripheral<?>> getPeripheralFor(SmartBlockEntity be) {
//		if (be instanceof SpeedControllerBlockEntity scbe)
//			return () -> new SpeedControllerPeripheral(scbe, scbe.targetSpeed);
		if (be instanceof CreativeMotorBlockEntity cmbe)
			return () -> new CreativeMotorPeripheral(cmbe, cmbe.getGeneratedSpeedBehaviour());
		if (be instanceof DisplayLinkBlockEntity dlbe)
			return () -> new DisplayLinkPeripheral(dlbe);
		if (be instanceof FrogportBlockEntity fpbe)
			return () -> new FrogportPeripheral(fpbe);
//		if (be instanceof PostboxBlockEntity pbbe)
//			return () -> new PostboxPeripheral(pbbe);
		if (be instanceof NixieTubeBlockEntity ntbe)
			return () -> new NixieTubePeripheral(ntbe);
//		if (be instanceof SequencedGearshiftBlockEntity sgbe)
//			return () -> new SequencedGearshiftPeripheral(sgbe);
//		if (be instanceof SignalBlockEntity sbe)
//			return () -> new SignalPeripheral(sbe);
//		if (be instanceof SpeedGaugeBlockEntity sgbe)
//			return () -> new SpeedGaugePeripheral(sgbe);
        if (be instanceof StressGaugeBlockEntity sgbe)
            return () -> new StressGaugePeripheral(sgbe);
//		if (be instanceof StockTickerBlockEntity sgbe)
//			return () -> new StockTickerPeripheral(sgbe);
//		// Has to be before PackagerBlockEntity as it's a subclass
//		if (be instanceof RepackagerBlockEntity rpbe)
//			return () -> new RepackagerPeripheral(rpbe);
//		if (be instanceof PackagerBlockEntity pgbe)
//			return () -> new PackagerPeripheral(pgbe);
//		if (be instanceof RedstoneRequesterBlockEntity rrbe)
//			return () -> new RedstoneRequesterPeripheral(rrbe);
//		if (be instanceof StationBlockEntity sbe)
//			return () -> new StationPeripheral(sbe);
//		if (be instanceof TableClothBlockEntity tcbe)
//			return () -> new TableClothShopPeripheral(tcbe);
//		if (be instanceof StickerBlockEntity sbe)
//			return () -> new StickerPeripheral(sbe);
//		if (be instanceof StationBlockEntity sbe)
//			return () -> new StationPeripheral(sbe);
//		if (be instanceof TrackObserverBlockEntity tobe)
//			return () -> new TrackObserverPeripheral(tobe);

        throw new IllegalArgumentException(
                "No peripheral available for " + Registries.BLOCK_ENTITY_TYPE.getKey(be.getType()));
    }

    public static void registerItemDetailProviders() {
        VanillaDetailRegistries.ITEM_STACK.addProvider((out, stack) -> {
            if (PackageItem.isPackage(stack)) {
				PackageLuaObject packageLuaObject = new PackageLuaObject(null, stack);
				out.put("package", packageLuaObject);
            }
        });
    }

    @Override
    public IPeripheral getPeripheralCapability() {
        if (peripheral == null)
            peripheral = peripheralSupplier.get();
        return peripheral;
    }

    @Override
    public void removePeripheral() {
        // TODO (aster): Check if not needed in fabric?
//		if (peripheral != null)
//			getWorld().invalidateCapabilities(be.getBlockPos());
    }

    @Override
    public void prepareComputerEvent(@NotNull ComputerEvent event) {
        if (peripheral != null)
            peripheral.prepareComputerEvent(event);
    }

}
