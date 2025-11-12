package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlock;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PackageAddressDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
            return EMPTY_LINE;

        InvManipulationBehaviour invManipulationBehaviour = cobe.getBehaviour(InvManipulationBehaviour.TYPE);
        ServerFilteringBehaviour filteringBehaviour = cobe.getBehaviour(ServerFilteringBehaviour.TYPE);
        Container handler = invManipulationBehaviour.getInventory();

        if (handler == null) {
            BlockPos targetPos = cobe.getBlockPos().relative(SmartObserverBlock.getTargetDirection(cobe.getBlockState()));

            if (context.level().getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity ccbe)
                for (ChainConveyorPackage box : ccbe.getLoopingPackages())
                    if (filteringBehaviour.test(box.item))
                        return Component.literal(PackageItem.getAddress(box.item));

            return EMPTY_LINE;
        }

        for (ItemStack stack : handler) {
            if (PackageItem.isPackage(stack) && filteringBehaviour.test(stack))
                return Component.literal(PackageItem.getAddress(stack));
        }

        return EMPTY_LINE;
    }

    @Override
    protected String getTranslationKey() {
        return "read_package_address";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}