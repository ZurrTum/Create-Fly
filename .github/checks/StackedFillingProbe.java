package probe;

import com.zurrtum.create.content.fluids.spout.FillingBySpout;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/** Run only in a disposable dedicated server: registers fixture storage on paper and writable books. */
public class StackedFillingProbe implements DedicatedServerModInitializer {
    private static final int BUCKET = 81_000;
    private int checks;
    private int failures;

    @Override
    public void onInitializeServer() {
        FluidStorage.ITEM.registerForItems((stack, context) ->
            new EmptyItemFluidStorage(context, Items.WRITABLE_BOOK, Fluids.WATER, BUCKET), Items.PAPER);
        FluidStorage.ITEM.registerForItems((stack, context) ->
            new FullItemFluidStorage(context, Items.PAPER, FluidVariant.of(Fluids.WATER), BUCKET),
            Items.WRITABLE_BOOK);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                ServerLevel world = server.overworld();
                for (int count : new int[]{1, 5, Items.PAPER.getDefaultMaxStackSize()}) {
                    checkStack(world, count);
                }
                checkRejected(world, new ItemStack(Items.WRITABLE_BOOK), new FluidStack(Fluids.WATER, BUCKET));
                checkRejected(world, new ItemStack(Items.PAPER, 5), new FluidStack(Fluids.WATER, BUCKET - 1));
                checkRejected(world, new ItemStack(Items.PAPER, 5), new FluidStack(Fluids.LAVA, BUCKET));
                check(!FillingBySpout.canItemBeFilled(world, new ItemStack(Items.WRITABLE_BOOK)), "full gate");
                checkBottle(world);
            } catch (Throwable error) {
                checks++;
                failures++;
                error.printStackTrace();
            }
            System.out.println("STACKED_FILLING_PROBE_RESULT failures=" + failures + " checks=" + checks);
        });
    }

    private void checkStack(ServerLevel world, int count) {
        ItemStack input = new ItemStack(Items.PAPER, count);
        input.set(DataComponents.CUSTOM_NAME, Component.literal("stacked filling fixture"));
        ItemStack before = input.copy();
        FluidStack water = new FluidStack(Fluids.WATER, 2 * BUCKET);
        water.set(DataComponents.CUSTOM_NAME, Component.literal("fixture water"));
        FluidStack waterBefore = water.copy();
        check(GenericItemFilling.canItemBeFilled(world, input), "generic gate count=" + count);
        check(FillingBySpout.canItemBeFilled(world, input), "spout gate count=" + count);
        int demand = GenericItemFilling.getRequiredAmountForItem(world, input, water);
        check(demand == BUCKET, "generic demand count=" + count + " actual=" + demand);
        check(FillingBySpout.getRequiredAmountForItem(world, input, water) == BUCKET,
            "spout demand count=" + count);
        check(GenericItemFilling.getRequiredAmountForItem(world, input, water) == BUCKET,
            "query rollback count=" + count);
        check(ItemStack.matches(input, before), "query preserves item count=" + count);
        check(sameFluid(water, waterBefore), "query preserves fluid count=" + count);

        ItemStack output = FillingBySpout.fillItem(world, demand, input, water);
        check(output.is(Items.WRITABLE_BOOK) && output.getCount() == 1, "one output count=" + count);
        check(input.getCount() == count - 1, "one input consumed count=" + count);
        check(count == 1 ? input.isEmpty() : input.getComponents().equals(before.getComponents()),
            "remainder components count=" + count);
        check(output.getComponentsPatch().equals(before.getComponentsPatch()), "output components count=" + count);
        check(water.getAmount() == BUCKET, "fluid consumption agrees with demand count=" + count);
        check(water.getComponents().equals(waterBefore.getComponents()), "fluid components count=" + count);
    }

    private void checkRejected(ServerLevel world, ItemStack input, FluidStack fluid) {
        ItemStack before = input.copy();
        FluidStack fluidBefore = fluid.copy();
        String label = input.getItem() + "/" + fluid.getFluid() + "/" + fluid.getAmount();
        int demand = GenericItemFilling.getRequiredAmountForItem(world, input, fluid);
        check(demand == 0, "rejected demand " + label);
        check(ItemStack.matches(input, before) && sameFluid(fluid, fluidBefore), "rejected query preserves " + label);
        check(GenericItemFilling.fillItem(world, demand, input, fluid).isEmpty(), "rejected output " + label);
        check(ItemStack.matches(input, before), "rejected fill preserves item " + label);
        check(sameFluid(fluid, fluidBefore), "rejected fill preserves fluid " + label);
    }

    private void checkBottle(ServerLevel world) {
        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 5);
        ItemStack before = bottles.copy();
        FluidStack water = new FluidStack(Fluids.WATER, BUCKET);
        int demand = GenericItemFilling.getRequiredAmountForItem(world, bottles, water);
        check(demand == BUCKET / 3, "internal bottle demand");
        check(ItemStack.matches(bottles, before) && water.getAmount() == BUCKET, "internal bottle query preserves");
        ItemStack output = GenericItemFilling.fillItem(world, demand, bottles, water);
        check(output.is(Items.POTION) && output.getCount() == 1, "internal bottle output");
        check(bottles.getCount() == 4, "internal bottle consumes one");
        check(water.getAmount() == BUCKET - demand, "internal bottle fluid consumption");
    }

    private static boolean sameFluid(FluidStack actual, FluidStack expected) {
        return actual.getAmount() == expected.getAmount()
            && FluidStack.areFluidsAndComponentsEqual(actual, expected);
    }

    private void check(boolean condition, String label) {
        checks++;
        if (!condition) {
            failures++;
            System.out.println("STACKED_FILLING_PROBE_FAIL " + label);
        }
    }
}
