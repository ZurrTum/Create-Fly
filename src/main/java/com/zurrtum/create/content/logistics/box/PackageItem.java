package com.zurrtum.create.content.logistics.box;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Glob;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.box.PackageStyles.PackageStyle;
import com.zurrtum.create.foundation.item.EntityItem;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.component.PackageOrderData;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PackageItem extends Item implements EntityItem {
    public static final int SLOTS = 9;

    public PackageStyle style;

    public PackageItem(Settings properties, PackageStyle style) {
        super(properties);
        this.style = style;
        PackageStyles.ALL_BOXES.add(this);
        (style.rare() ? PackageStyles.RARE_BOXES : PackageStyles.STANDARD_BOXES).add(this);
    }

    public static Function<Settings, PackageItem> styled(PackageStyle style) {
        return properties -> new PackageItem(properties, style);
    }

    public static boolean isPackage(ItemStack stack) {
        return stack.getItem() instanceof PackageItem;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return PackageEntity.fromDroppedItem(world, location, itemstack);
    }

    public static ItemStack containing(List<ItemStack> stacks) {
        ItemStackHandler newInv = new ItemStackHandler(9);
        newInv.insert(stacks);
        return containing(newInv);
    }

    public static ItemStack containing(ItemStackHandler stacks) {
        ItemStack box = PackageStyles.getRandomBox();
        box.set(AllDataComponents.PACKAGE_CONTENTS, ItemHelper.containerContentsFromHandler(stacks));
        return box;
    }

    public static void clearAddress(ItemStack box) {
        box.remove(AllDataComponents.PACKAGE_ADDRESS);
    }

    public static void addAddress(ItemStack box, String address) {
        box.set(AllDataComponents.PACKAGE_ADDRESS, address);
    }

    public static void setOrder(
        ItemStack box,
        int orderId,
        int linkIndex,
        boolean isFinalLink,
        int fragmentIndex,
        boolean isFinal,
        @Nullable PackageOrderWithCrafts orderContext
    ) {
        PackageOrderData order = new PackageOrderData(orderId, linkIndex, isFinalLink, fragmentIndex, isFinal, orderContext);
        box.set(AllDataComponents.PACKAGE_ORDER_DATA, order);
    }

    public static int getOrderId(ItemStack box) {
        if (box.contains(AllDataComponents.PACKAGE_ORDER_DATA)) {
            //noinspection DataFlowIssue
            return box.get(AllDataComponents.PACKAGE_ORDER_DATA).orderId();
        } else {
            return -1;
        }
    }

    public static boolean hasOrderData(ItemStack box) {
        return box.contains(AllDataComponents.PACKAGE_ORDER_DATA);
    }

    public static int getIndex(ItemStack box) {
        if (box.contains(AllDataComponents.PACKAGE_ORDER_DATA)) {
            //noinspection DataFlowIssue
            return box.get(AllDataComponents.PACKAGE_ORDER_DATA).fragmentIndex();
        } else {
            return -1;
        }
    }

    public static boolean isFinal(ItemStack box) {
        //noinspection DataFlowIssue
        return box.contains(AllDataComponents.PACKAGE_ORDER_DATA) && box.get(AllDataComponents.PACKAGE_ORDER_DATA).isFinal();
    }

    public static int getLinkIndex(ItemStack box) {
        if (box.contains(AllDataComponents.PACKAGE_ORDER_DATA)) {
            //noinspection DataFlowIssue
            return box.get(AllDataComponents.PACKAGE_ORDER_DATA).linkIndex();
        } else {
            return -1;
        }
    }

    public static boolean isFinalLink(ItemStack box) {
        //noinspection DataFlowIssue
        return box.contains(AllDataComponents.PACKAGE_ORDER_DATA) && box.get(AllDataComponents.PACKAGE_ORDER_DATA).isFinalLink();
    }

    /**
     * Ordered items and their amount in the original, combined request\n
     * (Present in all non-redstone packages)
     */
    @Nullable
    public static PackageOrderWithCrafts getOrderContext(ItemStack box) {
        if (box.contains(AllDataComponents.PACKAGE_ORDER_DATA)) {
            PackageOrderData data = box.get(AllDataComponents.PACKAGE_ORDER_DATA);
            //noinspection DataFlowIssue
            return data.orderContext();
        } else if (box.contains(AllDataComponents.PACKAGE_ORDER_CONTEXT)) {
            return box.get(AllDataComponents.PACKAGE_ORDER_CONTEXT);
        } else {
            return null;
        }
    }

    public static void addOrderContext(ItemStack box, PackageOrderWithCrafts orderContext) {
        box.set(AllDataComponents.PACKAGE_ORDER_CONTEXT, orderContext);
    }

    public static boolean matchAddress(ItemStack box, String address) {
        return matchAddress(getAddress(box), address);
    }

    public static boolean matchAddress(String boxAddress, String address) {
        if (address.isBlank())
            return boxAddress.isBlank();
        if (address.equals("*") || boxAddress.equals("*"))
            return true;
        if (address.equals(boxAddress))
            return true;
        return address.matches(Glob.toRegexPattern(boxAddress, "")) || boxAddress.matches(Glob.toRegexPattern(address, ""));
    }

    public static String getAddress(ItemStack box) {
        return box.getOrDefault(AllDataComponents.PACKAGE_ADDRESS, "");
    }

    public static float getWidth(ItemStack box) {
        if (box.getItem() instanceof PackageItem pi)
            return pi.style.width() / 16f;
        return 1;
    }

    public static float getHeight(ItemStack box) {
        if (box.getItem() instanceof PackageItem pi)
            return pi.style.height() / 16f;
        return 1;
    }

    public static float getHookDistance(ItemStack box) {
        if (box.getItem() instanceof PackageItem pi)
            return pi.style.riggingOffset() / 16f;
        return 1;
    }

    public static ItemStackHandler getContents(ItemStack box) {
        ItemStackHandler newInv = new ItemStackHandler(9);
        ContainerComponent contents = box.getOrDefault(AllDataComponents.PACKAGE_CONTENTS, ContainerComponent.DEFAULT);
        ItemHelper.fillItemStackHandler(contents, newInv);
        return newInv;
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        TooltipContext tooltipContext,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        super.appendTooltip(stack, tooltipContext, displayComponent, textConsumer, type);

        if (stack.contains(AllDataComponents.PACKAGE_ADDRESS))
            textConsumer.accept(Text.literal("→ " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).formatted(Formatting.GOLD));

        /*
         * Debug Fragmentation Data if (tag.contains("Fragment")) { CompoundTag
         * fragTag = tag.getCompound("Fragment");
         * pTooltipComponents.add(Component.literal("Order Information (Temporary)")
         * .withStyle(ChatFormatting.GREEN)); pTooltipComponents.add(Components
         * .literal(" Link " + fragTag.getInt("LinkIndex") +
         * (fragTag.getBoolean("IsFinalLink") ? " Final" : "") + " | Fragment " +
         * fragTag.getInt("Index") + (fragTag.getBoolean("IsFinal") ? " Final" : ""))
         * .withStyle(ChatFormatting.DARK_GREEN)); if (fragTag.contains("OrderContext"))
         * pTooltipComponents.add(Component.literal("Has Context!")
         * .withStyle(ChatFormatting.DARK_GREEN)); }
         */

        // From stack nbt
        if (!stack.contains(AllDataComponents.PACKAGE_CONTENTS))
            return;

        int visibleNames = 0;
        int skippedNames = 0;
        ItemStackHandler contents = getContents(stack);
        for (int i = 0, size = contents.size(); i < size; i++) {
            ItemStack itemstack = contents.getStack(i);
            if (itemstack.isEmpty())
                continue;
            if (itemstack.getItem() instanceof SpawnEggItem)
                continue;
            if (visibleNames > 2) {
                skippedNames++;
                continue;
            }

            visibleNames++;
            textConsumer.accept(Text.translatable("item.container.item_count", itemstack.getName(), itemstack.getCount()).formatted(Formatting.GRAY));
        }

        if (skippedNames > 0)
            textConsumer.accept(Text.translatable("item.container.more_items", skippedNames).formatted(Formatting.ITALIC));
    }

    // Throwing stuff

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public ActionResult open(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack box = playerIn.getStackInHand(handIn);
        ItemStackHandler contents = getContents(box);
        ItemStack particle = box.copy();

        playerIn.setStackInHand(handIn, box.getCount() <= 1 ? ItemStack.EMPTY : box.copyWithCount(box.getCount() - 1));

        if (!worldIn.isClient()) {
            for (int i = 0, size = contents.size(); i < size; i++) {
                ItemStack itemstack = contents.getStack(i);
                if (itemstack.isEmpty())
                    continue;

                if (itemstack.getItem() instanceof SpawnEggItem sei && worldIn instanceof ServerWorld sl) {
                    EntityType<?> entitytype = sei.getEntityType(itemstack);
                    Entity entity = entitytype.spawnFromItemStack(
                        sl,
                        itemstack,
                        null,
                        BlockPos.ofFloored(playerIn.getEntityPos().add(playerIn.getRotationVector().multiply(1, 0, 1).normalize())),
                        SpawnReason.SPAWN_ITEM_USE,
                        false,
                        false
                    );
                    if (entity != null)
                        itemstack.decrement(1);
                }

                playerIn.getInventory().offerOrDrop(itemstack.copy());
            }
        }

        Vec3d position = playerIn.getEntityPos();
        AllSoundEvents.PACKAGE_POP.playOnServer(worldIn, playerIn.getBlockPos());

        if (worldIn.isClient()) {
            for (int i = 0; i < 10; i++) {
                Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, worldIn.getRandom(), .125f);
                Vec3d pos = position.add(0, 0.5, 0).add(playerIn.getRotationVector().multiply(.5)).add(motion.multiply(4));
                worldIn.addParticleClient(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, particle),
                    pos.x,
                    pos.y,
                    pos.z,
                    motion.x,
                    motion.y,
                    motion.z
                );
            }
        }

        return ActionResult.SUCCESS.withNewHandStack(box);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer().isSneaking()) {
            return open(context.getWorld(), context.getPlayer(), context.getHand());
        }

        Vec3d point = context.getHitPos();
        float h = style.height() / 16f;
        float r = style.width() / 2f / 16f;

        if (context.getSide() == Direction.DOWN)
            point = point.subtract(0, h + .25f, 0);
        else if (context.getSide().getAxis().isHorizontal())
            point = point.add(Vec3d.of(context.getSide().getVector()).multiply(r));

        Box scanBB = new Box(point, point).expand(r, 0, r).stretch(0, h, 0);
        World world = context.getWorld();
        if (!world.getEntitiesByType(AllEntityTypes.PACKAGE, scanBB, e -> true).isEmpty())
            return super.useOnBlock(context);

        PackageEntity packageEntity = new PackageEntity(world, point.x, point.y, point.z);
        ItemStack itemInHand = context.getStack();
        packageEntity.setBox(itemInHand.copy());
        world.spawnEntity(packageEntity);
        itemInHand.decrement(1);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (player.isSneaking())
            return open(world, player, hand);
        ItemStack itemstack = player.getStackInHand(hand);
        player.setCurrentHand(hand);
        return ActionResult.SUCCESS.withNewHandStack(itemstack);
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity entity, int ticks) {
        if (!(entity instanceof PlayerEntity player))
            return false;
        int i = this.getMaxUseTime(stack, entity) - ticks;
        if (i < 0)
            return false;

        float f = getPackageVelocity(i);
        if (f < 0.1D)
            return false;
        if (world.isClient())
            return false;

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.5F);

        ItemStack copy = stack.copy();
        if (!player.getAbilities().creativeMode)
            stack.decrement(1);

        Vec3d vec = new Vec3d(entity.getX(), entity.getY() + entity.getBoundingBox().getLengthY() / 2f, entity.getZ());
        Vec3d motion = entity.getRotationVector().multiply(f * 2);
        vec = vec.add(motion);

        PackageEntity packageEntity = new PackageEntity(world, vec.x, vec.y, vec.z);
        packageEntity.setBox(copy);
        packageEntity.setVelocity(motion);
        packageEntity.tossedBy = new WeakReference<>(player);
        world.spawnEntity(packageEntity);
        return false;
    }

    public static float getPackageVelocity(int p_185059_0_) {
        float f = (float) p_185059_0_ / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F)
            f = 1.0F;
        return f;
    }
}