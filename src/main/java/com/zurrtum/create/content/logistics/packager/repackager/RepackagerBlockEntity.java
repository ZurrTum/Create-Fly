package com.zurrtum.create.content.logistics.packager.repackager;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.compat.computercraft.events.PackageEvent;
import com.zurrtum.create.compat.computercraft.events.RepackageEvent;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.crate.BottomlessItemHandler;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerItemHandler;
import com.zurrtum.create.content.logistics.packager.PackagingRequest;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RepackagerBlockEntity extends PackagerBlockEntity {

    public PackageRepackageHelper repackageHelper;

    public RepackagerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.REPACKAGER, pos, state);
        repackageHelper = new PackageRepackageHelper();
    }

    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;

        Inventory targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv instanceof PackagerItemHandler)
            return false;

        boolean targetIsCreativeCrate = targetInv instanceof BottomlessItemHandler;
        boolean anySpace;
        if (simulate) {
            int count = box.getCount();
            anySpace = targetInv.countSpace(box, count) == count;
        } else {
            anySpace = targetInv.preciseInsert(box);
        }

        if (!targetIsCreativeCrate && !anySpace)
            return false;
        if (simulate)
            return true;

        computerBehaviour.prepareComputerEvent(new PackageEvent(box, "package_received"));
        previouslyUnwrapped = box;
        animationInward = true;
        animationTicks = CYCLE;
        notifyUpdate();
        return true;
    }

    @Override
    public void recheckIfLinksPresent() {
    }

    @Override
    public boolean redstoneModeActive() {
        return true;
    }

    public void attemptToSend(List<PackagingRequest> queuedRequests) {
        if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
            return;
        if (!queuedExitingPackages.isEmpty())
            return;

        Inventory targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv instanceof PackagerItemHandler)
            return;

        attemptToRepackage(targetInv);
        if (heldBox.isEmpty())
            return;

        updateSignAddress();
        if (!signBasedAddress.isBlank())
            PackageItem.addAddress(heldBox, signBasedAddress);
    }

    protected void attemptToRepackage(Inventory targetInv) {
        repackageHelper.clear();
        int completedOrderId = -1;

        for (ItemStack stack : targetInv) {
            if (stack.isEmpty() || !PackageItem.isPackage(stack))
                continue;

            if (!repackageHelper.isFragmented(stack)) {
                targetInv.extract(stack, 1);
                heldBox = stack.copy();
                animationInward = false;
                animationTicks = CYCLE;
                notifyUpdate();
                return;
            }

            completedOrderId = repackageHelper.addPackageFragment(stack);
            if (completedOrderId != -1)
                break;
        }

        if (completedOrderId == -1)
            return;

        List<BigItemStack> boxesToExport = repackageHelper.repack(completedOrderId, world.getRandom());
        if (boxesToExport.isEmpty())
            return;

        if (computerBehaviour.hasAttachedComputer()) {
            for (BigItemStack box : boxesToExport) {
                computerBehaviour.prepareComputerEvent(new RepackageEvent(box.stack, box.count));
            }
        }

        targetInv.extract(repackageHelper.collectedPackages.get(completedOrderId));
        queuedExitingPackages.addAll(boxesToExport);
        notifyUpdate();
    }

}
