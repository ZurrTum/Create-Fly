package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.ChainConveyorConnectionPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ChainConveyorConnectionHandler {

    private static BlockPos firstPos;
    private static RegistryKey<World> firstDim;

    public static boolean onRightClick(MinecraftClient mc) {
        if (!isChain(mc.player.getMainHandStack()))
            return false;
        if (firstPos == null)
            return false;
        boolean missed = false;
        if (mc.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() != Type.MISS)
            if (!(mc.world.getBlockEntity(bhr.getBlockPos()) instanceof ChainConveyorBlockEntity))
                missed = true;
        if (!mc.player.isSneaking() && !missed)
            return false;
        firstPos = null;
        CreateLang.translate("chain_conveyor.selection_cleared").sendStatus(mc.player);
        return true;
    }

    public static ActionResult onItemUsedOnBlock(World level, ClientPlayerEntity player, Hand hand, BlockHitResult ray) {
        ItemStack itemStack = player.getStackInHand(hand);
        BlockPos pos = ray.getBlockPos();
        BlockState blockState = level.getBlockState(pos);

        if (!blockState.isOf(AllBlocks.CHAIN_CONVEYOR) || !isChain(itemStack) || !player.canModifyBlocks() || FakePlayerHandler.has(player)) {
            return null;
        }

        if (level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity ccbe && ccbe.connections.size() >= AllConfigs.server().kinetics.maxChainConveyorConnections.get()) {
            CreateLang.translate("chain_conveyor.cannot_add_more_connections").style(Formatting.RED).sendStatus(player);
            return ActionResult.CONSUME;
        }

        if (firstPos == null || firstDim != level.getRegistryKey()) {
            firstPos = pos;
            firstDim = level.getRegistryKey();
            player.swingHand(hand);
            return ActionResult.CONSUME;
        }

        boolean success = validateAndConnect(level, pos, player, itemStack, false);
        firstPos = null;

        if (!success) {
            AllSoundEvents.DENY.play(level, player, pos);
            return ActionResult.CONSUME;
        }

        BlockSoundGroup soundtype = Blocks.CHAIN.getDefaultState().getSoundGroup();
        if (soundtype != null)
            level.playSound(
                player,
                pos,
                soundtype.getPlaceSound(),
                SoundCategory.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
            );
        return ActionResult.CONSUME;
    }

    private static boolean isChain(ItemStack itemStack) {
        return itemStack.isOf(Items.CHAIN); // Replace with tag? generic renderer?
    }

    public static void clientTick(MinecraftClient mc) {
        if (firstPos == null)
            return;

        ClientPlayerEntity player = mc.player;
        ClientWorld level = mc.world;
        BlockEntity sourceLift = level.getBlockEntity(firstPos);

        if (firstDim != level.getRegistryKey() || !(sourceLift instanceof ChainConveyorBlockEntity)) {
            firstPos = null;
            CreateLang.translate("chain_conveyor.selection_cleared").sendStatus(player);
            return;
        }

        ItemStack stack = player.getMainHandStack();
        HitResult hitResult = mc.crosshairTarget;

        if (!isChain(stack)) {
            stack = player.getOffHandStack();
            if (!isChain(stack))
                return;
        }

        if (hitResult == null || hitResult.getType() != Type.BLOCK) {
            highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
            return;
        }

        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);

        if (pos.equals(firstPos)) {
            highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
            CreateLang.translate("chain_conveyor.select_second").sendStatus(player);
            return;
        }

        if (!(hitState.getBlock() instanceof ChainConveyorBlock)) {
            highlightConveyor(firstPos, 0xFFFFFF, "chain_connect");
            return;
        }

        boolean success = validateAndConnect(level, pos, player, stack, true);

        if (success)
            CreateLang.translate("chain_conveyor.valid_connection").style(Formatting.GREEN).sendStatus(player);

        int color = success ? 0x95CD41 : 0xEA5C2B;

        highlightConveyor(firstPos, color, "chain_connect");
        highlightConveyor(pos, color, "chain_connect_to");

        Vec3d from = Vec3d.ofCenter(pos);
        Vec3d to = Vec3d.ofCenter(firstPos);
        Vec3d diff = from.subtract(to);

        if (diff.length() < 1)
            return;

        from = from.subtract(diff.normalize().multiply(.5));
        to = to.add(diff.normalize().multiply(.5));

        Vec3d normal = diff.crossProduct(new Vec3d(0, 1, 0)).normalize().multiply(.875);

        Outliner.getInstance().showLine("chain_connect_line", from.add(normal), to.add(normal)).lineWidth(1 / 16f).colored(color);
        Outliner.getInstance().showLine("chain_connect_line_1", from.subtract(normal), to.subtract(normal)).lineWidth(1 / 16f).colored(color);

    }

    private static void highlightConveyor(BlockPos pos, int color, String key) {
        for (int y : Iterate.zeroAndOne) {
            Vec3d prevV = VecHelper.rotate(new Vec3d(0, .125 + y * .75, 1.25), -22.5, Axis.Y).add(Vec3d.ofBottomCenter(pos));
            for (int i = 0; i < 8; i++) {
                Vec3d v = VecHelper.rotate(new Vec3d(0, .125 + y * .75, 1.25), 22.5 + i * 45, Axis.Y).add(Vec3d.ofBottomCenter(pos));
                Outliner.getInstance().showLine(key + y + i, prevV, v).lineWidth(1 / 16f).colored(color);
                prevV = v;
            }
        }
    }

    public static boolean validateAndConnect(WorldAccess level, BlockPos pos, ClientPlayerEntity player, ItemStack chain, boolean simulate) {
        if (!simulate && player.isSneaking()) {
            CreateLang.translate("chain_conveyor.selection_cleared").sendStatus(player);
            return false;
        }

        if (pos.equals(firstPos))
            return false;
        if (!pos.isWithinDistance(firstPos, AllConfigs.server().kinetics.maxChainConveyorLength.get()))
            return fail("chain_conveyor.too_far", player);
        if (pos.isWithinDistance(firstPos, 2.5))
            return fail("chain_conveyor.too_close", player);

        Vec3d diff = Vec3d.of(pos.subtract(firstPos));
        double horizontalDistance = diff.multiply(1, 0, 1).length() - 1.5;

        if (horizontalDistance <= 0)
            return fail("chain_conveyor.cannot_connect_vertically", player);
        if (Math.abs(diff.y) / horizontalDistance > 1)
            return fail("chain_conveyor.too_steep", player);

        ChainConveyorBlock chainConveyorBlock = AllBlocks.CHAIN_CONVEYOR;
        ChainConveyorBlockEntity sourceLift = chainConveyorBlock.getBlockEntity(level, firstPos);
        ChainConveyorBlockEntity targetLift = chainConveyorBlock.getBlockEntity(level, pos);

        if (targetLift.connections.size() >= AllConfigs.server().kinetics.maxChainConveyorConnections.get())
            return fail("chain_conveyor.cannot_add_more_connections", player);
        if (targetLift.connections.contains(firstPos.subtract(pos)))
            return fail("chain_conveyor.already_connected", player);
        if (sourceLift == null || targetLift == null)
            return fail("chain_conveyor.blocks_invalid", player);

        if (!player.isCreative()) {
            int chainCost = ChainConveyorBlockEntity.getChainCost(pos.subtract(firstPos));
            boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, true);
            if (simulate)
                BlueprintOverlayRenderer.displayChainRequirements(chain.getItem(), chainCost, hasEnough);
            if (!hasEnough)
                return fail("chain_conveyor.not_enough_chains", player);
        }

        if (simulate)
            return true;

        player.networkHandler.sendPacket(new ChainConveyorConnectionPacket(firstPos, pos, chain, true));

        CreateLang.text("") // Clear status message
            .sendStatus(player);
        firstPos = null;
        firstDim = null;
        return true;
    }

    private static boolean fail(String message, PlayerEntity player) {
        CreateLang.translate(message).style(Formatting.RED).sendStatus(player);
        return false;
    }

}
