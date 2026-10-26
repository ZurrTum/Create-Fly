package probe;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper.ExtractionCountMode;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class ExtractionLimitProbe implements DedicatedServerModInitializer {
    private int failures;
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var level = server.overworld();
            BlockPos funnel = new BlockPos(112, 151, 48);
            try {
                level.getChunkAt(funnel);
                level.setBlockAndUpdate(funnel, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(funnel.relative(Direction.SOUTH), Blocks.CHEST.defaultBlockState());
                ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(funnel.relative(Direction.SOUTH));
                level.setBlockAndUpdate(funnel, AllBlocks.BRASS_FUNNEL.defaultBlockState()
                    .setValue(FunnelBlock.FACING, Direction.NORTH).setValue(FunnelBlock.EXTRACTING, true));
                InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(level, funnel, InvManipulationBehaviour.TYPE);
                if (behaviour == null) throw new AssertionError("missing funnel behaviour");
                behaviour.findNewCapability();
                if (!behaviour.hasInventory()) throw new AssertionError("missing chest capability");
                check("bucket limit", () -> extract(chest, behaviour, Items.BUCKET, 4, 16, ExtractionCountMode.UPTO, 64, 16));
                check("normal stack", () -> extract(chest, behaviour, Items.COBBLESTONE, 1, 64, ExtractionCountMode.UPTO, 64, 64));
                check("exact legal stack", () -> extract(chest, behaviour, Items.BUCKET, 2, 16, ExtractionCountMode.EXACTLY, 16, 16));
                check("exact insufficient", () -> extract(chest, behaviour, Items.BUCKET, 1, 16, ExtractionCountMode.EXACTLY, 17, 0));
                check("exact selection before cap", () -> extract(chest, behaviour, Items.BUCKET, 4, 16, ExtractionCountMode.EXACTLY, 64, 16));
                check("below limit", () -> extract(chest, behaviour, Items.BUCKET, 1, 16, ExtractionCountMode.UPTO, 7, 7));
            } catch (Throwable failure) {
                failures++;
                failure.printStackTrace(System.out);
            }
            System.out.println("EXTRACTION_LIMIT_PROBE_RESULT failures=" + failures);
        });
    }

    private static void extract(ChestBlockEntity chest, InvManipulationBehaviour behaviour, Item item,
                                int stacks, int stackCount, ExtractionCountMode mode, int request, int expected) {
        chest.clearContent();
        for (int i = 0; i < stacks; i++) chest.setItem(i, new ItemStack(item, stackCount));
        int before = count(chest);
        ItemStack simulated = behaviour.simulate().extract(mode, request);
        if (count(chest) != before) throw new AssertionError("simulation changed inventory");
        ItemStack actual = behaviour.extract(mode, request);
        int remaining = count(chest);
        System.out.println("EXTRACTION_LIMIT_OBS mode=" + mode + " request=" + request + " simulated=" + simulated.getCount()
            + " actual=" + actual.getCount() + " remaining=" + remaining + " before=" + before);
        if (simulated.getCount() != expected || actual.getCount() != expected
            || actual.getCount() > actual.getMaxStackSize() || simulated.getCount() > simulated.getMaxStackSize()
            || actual.getCount() + remaining != before) throw new AssertionError("illegal stack or conservation failure");
        if (expected > 0 && (!simulated.is(item) || !actual.is(item))) throw new AssertionError("wrong item");
    }

    private static int count(ChestBlockEntity chest) {
        int total = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) total += chest.getItem(i).getCount();
        return total;
    }

    private void check(String name, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            failures++;
            System.out.println("EXTRACTION_LIMIT_FAIL " + name);
            failure.printStackTrace(System.out);
        }
    }
}
