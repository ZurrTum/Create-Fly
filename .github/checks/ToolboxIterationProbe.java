package probe;

import com.mojang.authlib.GameProfile;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.content.equipment.toolbox.ToolboxMenu;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ToolboxIterationProbe implements DedicatedServerModInitializer {
    private static int failures;
    private static final BlockPos POS = new BlockPos(104, 100, 104);

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.overworld();
            check(level, "unequip two players deposits their items", box -> {
                FakePlayerEntity a = player(level, box, "RemoveA", 8);
                FakePlayerEntity b = player(level, box, "RemoveB", 12);
                box.unequipTracked();
                expectUnlinked(a);
                expectUnlinked(b);
                expect(a.getInventory().getItem(0).isEmpty() && b.getInventory().getItem(0).isEmpty(), "items returned");
                expect(stored(box) == 20, "deposits conserved");
                expect(a.getInventory().getItem(8).getCount() == 3, "unrelated inventory preserved");
            });
            check(level, "menu replenishment unlinks two players safely", box -> {
                box.inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
                FakePlayerEntity a = player(level, box, "RefillA", 0);
                FakePlayerEntity b = player(level, box, "RefillB", 0);
                keepOtherLink(a, box);
                a.containerMenu = new ToolboxMenu(1, a.getInventory(), box);
                b.containerMenu = new ToolboxMenu(2, b.getInventory(), box);
                tickPlayers(box);
                expectUnlinked(a);
                expectUnlinked(b);
                expect(AllSynchedDatas.TOOLBOX.get(a).contains("1"), "other compartment link preserved");
                expect(stored(box) == 64 && a.getInventory().getItem(0).isEmpty()
                    && b.getInventory().getItem(0).isEmpty(), "menu replenishment leaves items in toolbox");
            });
            check(level, "menu deposit unlinks two players and keeps carried items", box -> {
                FakePlayerEntity a = player(level, box, "DepositA", 64);
                FakePlayerEntity b = player(level, box, "DepositB", 64);
                a.containerMenu = new ToolboxMenu(1, a.getInventory(), box);
                b.containerMenu = new ToolboxMenu(2, b.getInventory(), box);
                tickPlayers(box);
                expectUnlinked(a);
                expectUnlinked(b);
                expect(a.getInventory().getItem(0).getCount() == 64
                    && b.getInventory().getItem(0).getCount() == 64 && stored(box) == 0, "carried stacks preserved");
            });
            check(level, "normal balancing keeps both links", box -> {
                box.inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
                FakePlayerEntity a = player(level, box, "BalanceA", 0);
                FakePlayerEntity b = player(level, box, "BalanceB", 64);
                tickPlayers(box);
                expect(a.getInventory().getItem(0).getCount() == 32
                    && b.getInventory().getItem(0).getCount() == 32 && stored(box) == 64, "normal balance conserved");
                expect(AllSynchedDatas.TOOLBOX.get(a).contains("0")
                    && AllSynchedDatas.TOOLBOX.get(b).contains("0"), "normal links preserved");
            });
            check(level, "incompatible carried stack disconnects without moving items", box -> {
                FakePlayerEntity a = player(level, box, "MismatchA", 0);
                FakePlayerEntity b = player(level, box, "MismatchB", 0);
                a.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 4));
                b.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
                tickPlayers(box);
                expectUnlinked(a);
                expectUnlinked(b);
                expect(a.getInventory().getItem(0).getCount() == 4
                    && b.getInventory().getItem(0).getCount() == 5 && stored(box) == 0, "incompatible items preserved");
            });
            System.out.println("TOOLBOX_ITERATION_PROBE_RESULT failures=" + failures);
        });
    }

    private static FakePlayerEntity player(ServerLevel level, ToolboxBlockEntity box, String name, int count) {
        FakePlayerEntity player = new FakePlayerEntity(level, new GameProfile(UUID.randomUUID(), name));
        player.setPos(POS.getX() + .5, POS.getY(), POS.getZ() + .5);
        player.getInventory().setItem(0, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.IRON_INGOT, count));
        player.getInventory().setItem(8, new ItemStack(Items.GOLD_INGOT, 3));
        link(player, box, 0, 0);
        return player;
    }

    private static void keepOtherLink(FakePlayerEntity player, ToolboxBlockEntity box) {
        box.inventory.filters.set(1, new ItemStack(Items.DIAMOND));
        link(player, box, 1, 1);
    }

    private static void link(FakePlayerEntity player, ToolboxBlockEntity box, int compartment, int hotbarSlot) {
        CompoundTag link = new CompoundTag();
        link.store("Pos", BlockPos.CODEC, POS);
        link.putInt("Slot", compartment);
        AllSynchedDatas.TOOLBOX.get(player).put(Integer.toString(hotbarSlot), link);
        box.connectPlayer(compartment, player, hotbarSlot);
    }

    private static void tickPlayers(ToolboxBlockEntity box) throws Exception {
        var tick = ToolboxBlockEntity.class.getDeclaredMethod("tickPlayers");
        tick.setAccessible(true);
        try {
            tick.invoke(box);
        } catch (InvocationTargetException exception) {
            throw new AssertionError("tickPlayers failed", exception.getCause());
        }
    }

    private static int stored(ToolboxBlockEntity box) {
        int count = 0;
        for (int i = 0; i < ToolboxInventory.STACKS_PER_COMPARTMENT; i++)
            count += box.inventory.getItem(i).getCount();
        return count;
    }

    private static void expectUnlinked(FakePlayerEntity player) {
        expect(!AllSynchedDatas.TOOLBOX.get(player).contains("0"), "toolbox link removed for " + player.getName().getString());
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void check(ServerLevel level, String name, Check action) {
        try {
            ColorCollection<? extends Block> toolboxes = AllBlocks.TOOLBOX;
            level.setBlockAndUpdate(POS, toolboxes.pick(DyeColor.BROWN).defaultBlockState());
            if (!(level.getBlockEntity(POS) instanceof ToolboxBlockEntity box)) throw new AssertionError("toolbox missing");
            box.inventory.filters.set(0, new ItemStack(Items.IRON_INGOT));
            action.run(box);
            System.out.println("TOOLBOX_ITERATION_PROBE PASS " + name);
        } catch (Throwable failure) {
            failures++;
            System.out.println("TOOLBOX_ITERATION_PROBE FAIL " + name);
            failure.printStackTrace(System.out);
        } finally {
            level.setBlockAndUpdate(POS, Blocks.AIR.defaultBlockState());
        }
    }

    private interface Check {
        void run(ToolboxBlockEntity box) throws Exception;
    }
}
