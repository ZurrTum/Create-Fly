package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;

public class ClipboardValueSettingsHandler {

    public static ActionResult rightClickToCopy(World world, PlayerEntity player, ItemStack itemStack, Hand hand, BlockHitResult hit, BlockPos pos) {
        return interact(world, player, itemStack, hit.getSide(), pos, false);
    }

    public static boolean leftClickToPaste(World world, PlayerEntity player, ItemStack itemStack, Direction side, BlockPos pos) {
        return interact(world, player, itemStack, side, pos, true) == ActionResult.SUCCESS;
    }

    private static ActionResult interact(World world, PlayerEntity player, ItemStack itemStack, Direction side, BlockPos pos, boolean paste) {
        if (!itemStack.isOf(AllItems.CLIPBOARD) || player.isSpectator() || player.isSneaking())
            return null;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return null;

        if (smartBE instanceof ClipboardBlockEntity cbe) {
            if (!world.isClient()) {
                List<List<ClipboardEntry>> listTo = ClipboardEntry.readAll(itemStack);
                List<List<ClipboardEntry>> listFrom = ClipboardEntry.readAll(cbe.dataContainer);
                List<ClipboardEntry> toAdd = new ArrayList<>();

                for (List<ClipboardEntry> page : listFrom) {
                    Copy:
                    for (ClipboardEntry entry : page) {
                        String entryToAdd = entry.text.getString();
                        for (List<ClipboardEntry> pageTo : listTo)
                            for (ClipboardEntry existing : pageTo)
                                if (entryToAdd.equals(existing.text.getString()))
                                    continue Copy;
                        toAdd.add(new ClipboardEntry(entry.checked, entry.text));
                    }
                }

                for (ClipboardEntry entry : toAdd) {
                    List<ClipboardEntry> page = null;
                    for (List<ClipboardEntry> freePage : listTo) {
                        if (freePage.size() > 11)
                            continue;
                        page = freePage;
                        break;
                    }
                    if (page == null) {
                        page = new ArrayList<>();
                        listTo.add(page);
                    }
                    page.add(entry);
                    ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
                }

                ClipboardEntry.saveAll(listTo, itemStack);
            }

            player.sendMessage(
                Text.translatable("create.clipboard.copied_from_clipboard", world.getBlockState(pos).getBlock().getName().formatted(Formatting.WHITE))
                    .formatted(Formatting.GREEN), true
            );
            return ActionResult.SUCCESS;
        }

        NbtCompound tag = null;
        if (paste) {
            tag = itemStack.get(AllDataComponents.CLIPBOARD_COPIED_VALUES);
            if (tag == null) {
                return null;
            }
        }

        boolean anySuccess = false;
        boolean anyValid = false;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(smartBE.getReporterContext(), LOGGER)) {
            DynamicRegistryManager registryManager = world.getRegistryManager();
            ReadView readView = paste ? NbtReadView.create(logging, registryManager, tag) : null;
            NbtWriteView writeView = paste ? null : NbtWriteView.create(logging, registryManager);
            if (smartBE instanceof ClipboardCloneable cc) {
                anyValid = true;
                if (paste) {
                    anySuccess = paste(cc, player, readView, side, world.isClient());
                } else {
                    anySuccess = write(cc, registryManager, writeView, side, world.isClient());
                    if (anySuccess) {
                        tag = writeView.getNbt();
                    }
                }
            }

            if (!anySuccess) {
                for (BehaviourType<? extends BlockEntityBehaviour<SmartBlockEntity>> type : List.of(
                    ServerScrollValueBehaviour.TYPE,
                    ServerFilteringBehaviour.TYPE,
                    ServerLinkBehaviour.TYPE
                )) {
                    if (!(smartBE.getBehaviour(type) instanceof ClipboardCloneable cc))
                        continue;
                    anyValid = true;
                    if (paste) {
                        anySuccess = paste(cc, player, readView, side, world.isClient());
                    } else {
                        anySuccess = write(cc, registryManager, writeView, side, world.isClient());
                        if (anySuccess) {
                            tag = writeView.getNbt();
                            break;
                        }
                    }
                }
            }
        }

        if (!anyValid)
            return null;

        if (world.isClient() || !anySuccess)
            return ActionResult.SUCCESS;

        player.sendMessage(
            Text.translatable(
                paste ? "create.clipboard.pasted_to" : "create.clipboard.copied_from",
                world.getBlockState(pos).getBlock().getName().formatted(Formatting.WHITE)
            ).formatted(Formatting.GREEN), true
        );

        if (!paste) {
            ClipboardOverrides.switchTo(ClipboardType.WRITTEN, itemStack);
            itemStack.set(AllDataComponents.CLIPBOARD_COPIED_VALUES, tag);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean paste(ClipboardCloneable cc, PlayerEntity player, ReadView readView, Direction side, boolean simulate) {
        return readView.getOptionalReadView(cc.getClipboardKey()).map(v -> cc.readFromClipboard(v, player, side, simulate)).orElse(false);
    }

    private static boolean write(
        ClipboardCloneable cc,
        RegistryWrapper.WrapperLookup registryManager,
        WriteView writeView,
        Direction side,
        boolean simulate
    ) {
        String clipboardKey = cc.getClipboardKey();
        if (simulate) {
            return cc.canWrite(registryManager, side);
        } else {
            return cc.writeToClipboard(writeView.get(clipboardKey), side);
        }
    }
}
