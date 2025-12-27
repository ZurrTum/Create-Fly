package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;

public class ClipboardValueSettingsHandler {

    public static InteractionResult rightClickToCopy(
        Level world,
        Player player,
        ItemStack itemStack,
        InteractionHand hand,
        BlockHitResult hit,
        BlockPos pos
    ) {
        return interact(world, player, itemStack, hit.getDirection(), pos, false);
    }

    public static boolean leftClickToPaste(Level world, Player player, ItemStack itemStack, Direction side, BlockPos pos) {
        return interact(world, player, itemStack, side, pos, true) == InteractionResult.SUCCESS;
    }

    private static InteractionResult interact(Level world, Player player, ItemStack itemStack, Direction side, BlockPos pos, boolean paste) {
        if (!itemStack.is(AllItems.CLIPBOARD) || player.isSpectator() || player.isShiftKeyDown())
            return null;
        if (!(world.getBlockEntity(pos) instanceof SmartBlockEntity smartBE))
            return null;

        ClipboardContent clipboardContent = itemStack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);

        if (smartBE instanceof ClipboardBlockEntity cbe) {
            if (!world.isClientSide()) {
                List<List<ClipboardEntry>> listTo = ClipboardEntry.readAll(clipboardContent);
                List<List<ClipboardEntry>> listFrom = ClipboardEntry.readAll(cbe.components());
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

                    clipboardContent = clipboardContent.setType(ClipboardType.WRITTEN);
                    itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent);
                }

                clipboardContent = clipboardContent.setPages(listTo);
                itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent);
            }

            player.displayClientMessage(
                Component.translatable(
                    "create.clipboard.copied_from_clipboard",
                    world.getBlockState(pos).getBlock().getName().withStyle(ChatFormatting.WHITE)
                ).withStyle(ChatFormatting.GREEN), true
            );
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = null;
        if (paste) {
            tag = clipboardContent.copiedValues().orElse(null);
            if (tag == null) {
                return null;
            }
        }

        boolean anySuccess = false;
        boolean anyValid = false;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(smartBE.problemPath(), LOGGER)) {
            RegistryAccess registryManager = world.registryAccess();
            if (paste) {
                ValueInput readView = TagValueInput.create(logging, registryManager, tag);
                for (BlockEntityBehaviour<?> behaviour : smartBE.getAllBehaviours()) {
                    if (!(behaviour instanceof ClipboardCloneable cc))
                        continue;
                    anyValid = true;
                    anySuccess |= paste(cc, player, readView, side, world.isClientSide());
                }
                if (smartBE instanceof ClipboardCloneable cc) {
                    anyValid = true;
                    anySuccess |= paste(cc, player, readView, side, world.isClientSide());
                }
            } else {
                TagValueOutput writeView = TagValueOutput.createWithContext(logging, registryManager);
                for (BlockEntityBehaviour<?> behaviour : smartBE.getAllBehaviours()) {
                    if (!(behaviour instanceof ClipboardCloneable cc))
                        continue;
                    anyValid = true;
                    anySuccess |= write(cc, registryManager, writeView, side, world.isClientSide());
                }
                if (smartBE instanceof ClipboardCloneable cc) {
                    anyValid = true;
                    anySuccess |= write(cc, registryManager, writeView, side, world.isClientSide());
                }
                if (anySuccess) {
                    tag = writeView.buildResult();
                }
            }
        }

        if (!anyValid)
            return null;

        if (world.isClientSide() || !anySuccess)
            return InteractionResult.SUCCESS;

        player.displayClientMessage(
            Component.translatable(
                paste ? "create.clipboard.pasted_to" : "create.clipboard.copied_from",
                world.getBlockState(pos).getBlock().getName().withStyle(ChatFormatting.WHITE)
            ).withStyle(ChatFormatting.GREEN), true
        );

        if (!paste) {
            clipboardContent = clipboardContent.setType(ClipboardType.WRITTEN);
            clipboardContent = clipboardContent.setCopiedValues(tag);
            itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean paste(ClipboardCloneable cc, Player player, ValueInput readView, Direction side, boolean simulate) {
        return readView.child(cc.getClipboardKey()).map(v -> cc.readFromClipboard(v, player, side, simulate)).orElse(false);
    }

    private static boolean write(
        ClipboardCloneable cc,
        HolderLookup.Provider registryManager,
        ValueOutput writeView,
        Direction side,
        boolean simulate
    ) {
        if (simulate) {
            return cc.canWrite(registryManager, side);
        } else {
            return cc.writeToClipboard(writeView.child(cc.getClipboardKey()), side);
        }
    }
}
