package probe;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BasinArmOutputProbe implements DedicatedServerModInitializer {
    private static int failures;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.overworld();
            BlockPos pos = new BlockPos(144, 120, 144);
            level.getChunkAt(pos);
            level.setBlockAndUpdate(pos, AllBlocks.BASIN.defaultBlockState());
            BasinBlockEntity basin = (BasinBlockEntity) level.getBlockEntity(pos);
            ArmBlockEntity arm = new ArmBlockEntity(pos.above(), AllBlocks.MECHANICAL_ARM.defaultBlockState());
            arm.setLevel(level);
            ArmInteractionPoint point = AllArmInteractionPointTypes.BASIN.createPoint(level, pos, level.getBlockState(pos));
            check("basin outputs only", () -> {
                basin.itemCapability.setItem(0, new ItemStack(Items.QUARTZ, 8));
                basin.itemCapability.setItem(9, new ItemStack(Items.GRANITE, 8));
                expect(point.extract(arm, 0, 1, true).isEmpty(), "ingredient preview");
                expect(point.extract(arm, 0, 1, false).isEmpty(), "ingredient extraction");
                ItemStack preview = point.extract(arm, 9, 1, true);
                expect(preview.is(Items.GRANITE) && preview.getCount() == 1, "output preview");
                expect(basin.itemCapability.getItem(9).getCount() == 8, "preview mutated output");
                ItemStack actual = point.extract(arm, 9, 1, false);
                expect(actual.is(Items.GRANITE) && actual.getCount() == 1, "output extraction");
                expect(basin.itemCapability.getItem(0).getCount() == 8, "ingredient changed");
                expect(basin.itemCapability.getItem(9).getCount() == 7, "output remainder");
            });
            check("identical input and output remain separate", () -> {
                basin.itemCapability.setItem(0, new ItemStack(Items.QUARTZ, 8));
                basin.itemCapability.setItem(9, new ItemStack(Items.QUARTZ, 8));
                expect(point.extract(arm, 0, false).isEmpty(), "ingredient overload bypass");
                expect(point.extract(arm, 9, 3, false).getCount() == 3, "output amount");
                expect(basin.itemCapability.getItem(0).getCount() == 8, "identical ingredient changed");
                expect(basin.itemCapability.getItem(9).getCount() == 5, "identical output remainder");
            });
            SimpleContainer inventory = new SimpleContainer(2);
            ArmInteractionPoint generic = point(level, inventory);
            check("requested generic slot and simulation", () -> {
                inventory.setItem(0, new ItemStack(Items.QUARTZ, 8));
                inventory.setItem(1, new ItemStack(Items.GRANITE, 8));
                expect(generic.extract(arm, 1, 3, true).is(Items.GRANITE), "wrong generic preview");
                expect(inventory.getItem(1).getCount() == 8, "generic preview mutated");
                expect(generic.extract(arm, 1, 3, false).getCount() == 3, "generic amount");
                expect(inventory.getItem(0).getCount() == 8 && inventory.getItem(1).getCount() == 5, "wrong slot changed");
            });
            check("identical generic slots", () -> {
                inventory.setItem(0, new ItemStack(Items.QUARTZ, 8));
                inventory.setItem(1, new ItemStack(Items.QUARTZ, 8));
                expect(generic.extract(arm, 1, 3, false).getCount() == 3, "identical generic amount");
                expect(inventory.getItem(0).getCount() == 8 && inventory.getItem(1).getCount() == 5, "identical wrong slot changed");
            });
            check("empty slot and invalid requests", () -> {
                inventory.setItem(0, new ItemStack(Items.QUARTZ, 8));
                inventory.setItem(1, ItemStack.EMPTY);
                for (boolean simulate : new boolean[]{true, false}) {
                    expect(generic.extract(arm, 1, 1, simulate).isEmpty(), "empty requested slot");
                    expect(generic.extract(arm, -1, 1, simulate).isEmpty(), "negative slot");
                    expect(generic.extract(arm, 2, 1, simulate).isEmpty(), "past last slot");
                    expect(generic.extract(arm, 0, 0, simulate).isEmpty(), "zero amount");
                }
                expect(inventory.getItem(0).getCount() == 8, "invalid request changed inventory");
            });
            check("sided slot visibility and extraction permission", () -> {
                GuardedInventory guarded = new GuardedInventory();
                guarded.setItem(0, new ItemStack(Items.QUARTZ, 8));
                guarded.setItem(1, new ItemStack(Items.GRANITE, 8));
                ArmInteractionPoint sided = point(level, guarded);
                for (boolean simulate : new boolean[]{true, false}) {
                    expect(sided.extract(arm, 0, 1, simulate).isEmpty(), "hidden face slot");
                    expect(sided.extract(arm, 1, 1, simulate).isEmpty(), "denied face extraction");
                }
                guarded.allow = true;
                expect(sided.extract(arm, 1, 1, false).is(Items.GRANITE), "allowed face extraction");
                expect(guarded.getItem(0).getCount() == 8 && guarded.getItem(1).getCount() == 7, "sided inventory changed wrong slot");
            });
            System.out.println("BASIN_ARM_OUTPUT_PROBE_RESULT failures=" + failures);
        });
    }

    private static ArmInteractionPoint point(ServerLevel level, Container inventory) {
        return new ArmInteractionPoint(AllArmInteractionPointTypes.DEPOT, level, BlockPos.ZERO, AllBlocks.DEPOT.defaultBlockState()) {
            @Override protected Container getHandler(ArmBlockEntity arm) { return inventory; }
        };
    }

    private static class GuardedInventory extends SimpleContainer implements WorldlyContainer {
        boolean allow;
        GuardedInventory() { super(2); }
        @Override public int[] getSlotsForFace(Direction side) { return new int[]{1}; }
        @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
        @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return allow && side == Direction.UP; }
    }

    private static void expect(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void check(String name, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            failures++;
            System.out.println("BASIN_ARM_OUTPUT_PROBE FAIL " + name);
            failure.printStackTrace(System.out);
        }
    }
}
