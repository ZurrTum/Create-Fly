package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.depot.EntityLauncher;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorPlacementPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class EjectorTargetHandler {

    static BlockPos currentSelection;
    static ItemStack currentItem;
    static long lastHoveredBlockPos = -1;
    static EntityLauncher launcher;

    public static ActionResult rightClickingBlocksSelectsThem(World world, ClientPlayerEntity player, Hand hand, BlockHitResult ray) {
        if (currentItem == null)
            return null;
        BlockPos pos = ray.getBlockPos();
        if (player.isSpectator() || !player.isSneaking())
            return null;

        String key = "weighted_ejector.target_set";
        player.sendMessage(CreateLang.translateDirect(key).formatted(Formatting.GOLD), true);
        currentSelection = pos;
        launcher = null;
        return ActionResult.SUCCESS;
    }

    public static boolean leftClickingBlocksDeselectsThem(ClientPlayerEntity player, BlockPos pos) {
        if (currentItem == null)
            return false;
        if (!player.isSneaking())
            return false;
        if (pos.equals(currentSelection)) {
            currentSelection = null;
            launcher = null;
            return true;
        }
        return false;
    }

    public static void flushSettings(ClientPlayNetworkHandler listener, BlockPos pos) {
        int h = 0;
        int v = 0;

        ClientPlayerEntity player = listener.client.player;
        String key = "weighted_ejector.target_not_valid";
        Formatting colour = Formatting.WHITE;

        if (currentSelection == null)
            key = "weighted_ejector.no_target";

        Direction validTargetDirection = getValidTargetDirection(pos);
        if (validTargetDirection == null) {
            player.sendMessage(CreateLang.translateDirect(key).formatted(colour), true);
            currentItem = null;
            currentSelection = null;
            return;
        }

        key = "weighted_ejector.targeting";
        colour = Formatting.GREEN;

        player.sendMessage(
            CreateLang.translateDirect(key, currentSelection.getX(), currentSelection.getY(), currentSelection.getZ())
                .formatted(colour), true
        );

        BlockPos diff = pos.subtract(currentSelection);
        h = Math.abs(diff.getX() + diff.getZ());
        v = -diff.getY();

        listener.sendPacket(new EjectorPlacementPacket(h, v, pos, validTargetDirection));
        currentSelection = null;
        currentItem = null;

    }

    public static Direction getValidTargetDirection(BlockPos pos) {
        if (currentSelection == null)
            return null;
        if (VecHelper.onSameAxis(pos, currentSelection, Axis.Y))
            return null;

        int xDiff = currentSelection.getX() - pos.getX();
        int zDiff = currentSelection.getZ() - pos.getZ();
        int max = AllConfigs.server().kinetics.maxEjectorDistance.get();

        if (Math.abs(xDiff) > max || Math.abs(zDiff) > max)
            return null;

        if (xDiff == 0)
            return Direction.get(zDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.Z);
        if (zDiff == 0)
            return Direction.get(xDiff < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, Axis.X);

        return null;
    }

    public static void tick(MinecraftClient mc) {
        ItemStack heldItemMainhand = mc.player.getMainHandStack();
        if (!heldItemMainhand.isOf(AllItems.WEIGHTED_EJECTOR)) {
            currentItem = null;
        } else {
            if (heldItemMainhand != currentItem) {
                currentSelection = null;
                currentItem = heldItemMainhand;
            }
            drawOutline(mc.world, currentSelection);
        }

        boolean wrench = heldItemMainhand.isOf(AllItems.WRENCH);
        if (wrench) {
            checkForWrench(mc);
        }
        drawArc(mc, wrench);
    }

    protected static void drawArc(MinecraftClient mc, boolean wrench) {
        if (currentSelection == null)
            return;
        if (currentItem == null && !wrench)
            return;

        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult blockRayTraceResult))
            return;
        if (blockRayTraceResult.getType() == Type.MISS)
            return;

        BlockPos pos = blockRayTraceResult.getBlockPos();
        if (!wrench)
            pos = pos.offset(blockRayTraceResult.getSide());

        int xDiff = currentSelection.getX() - pos.getX();
        int yDiff = currentSelection.getY() - pos.getY();
        int zDiff = currentSelection.getZ() - pos.getZ();
        int validX = Math.abs(zDiff) > Math.abs(xDiff) ? 0 : xDiff;
        int validZ = Math.abs(zDiff) < Math.abs(xDiff) ? 0 : zDiff;

        BlockPos validPos = currentSelection.add(validX, yDiff, validZ);
        Direction d = getValidTargetDirection(validPos);
        if (d == null)
            return;
        if (launcher == null || lastHoveredBlockPos != pos.asLong()) {
            lastHoveredBlockPos = pos.asLong();
            launcher = new EntityLauncher(Math.abs(validX + validZ), yDiff);
        }

        double totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
        int segments = (((int) totalFlyingTicks) / 3) + 1;
        double tickOffset = totalFlyingTicks / segments;
        boolean valid = xDiff == validX && zDiff == validZ;
        int intColor = valid ? 0x9ede73 : 0xff7171;
        DustParticleEffect data = new DustParticleEffect(intColor, 1);
        ClientWorld world = mc.world;

        Box bb = new Box(0, 0, 0, 1, 0, 1).offset(currentSelection.add(-validX, -yDiff, -validZ));
        Outliner.getInstance().chaseAABB("valid", bb).colored(intColor).lineWidth(1 / 16f);

        for (int i = 0; i < segments; i++) {
            double ticks = ((AnimationTickHolder.getRenderTime() / 3) % tickOffset) + i * tickOffset;
            Vec3d vec = launcher.getGlobalPos(ticks, d, pos).add(xDiff - validX, 0, zDiff - validZ);
            world.addParticleClient(data, vec.x, vec.y, vec.z, 0, 0, 0);
        }
    }

    private static void checkForWrench(MinecraftClient mc) {
        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult result))
            return;
        BlockPos pos = result.getBlockPos();

        BlockEntity be = mc.world.getBlockEntity(pos);
        if (!(be instanceof EjectorBlockEntity)) {
            lastHoveredBlockPos = -1;
            currentSelection = null;
            return;
        }

        if (lastHoveredBlockPos == -1 || lastHoveredBlockPos != pos.asLong()) {
            EjectorBlockEntity ejector = (EjectorBlockEntity) be;
            if (!ejector.getTargetPosition().equals(ejector.getPos()))
                currentSelection = ejector.getTargetPosition();
            lastHoveredBlockPos = pos.asLong();
            launcher = null;
        }

        if (lastHoveredBlockPos != -1)
            drawOutline(mc.world, currentSelection);
    }

    public static void drawOutline(ClientWorld world, BlockPos pos) {
        if (pos == null)
            return;
        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(world, pos);
        Box boundingBox = shape.isEmpty() ? new Box(BlockPos.ORIGIN) : shape.getBoundingBox();
        Outliner.getInstance().showAABB("target", boundingBox.offset(pos)).colored(0xffcb74).lineWidth(1 / 16f);
    }

}
