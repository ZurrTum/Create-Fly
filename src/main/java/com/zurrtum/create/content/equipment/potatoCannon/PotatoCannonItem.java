package com.zurrtum.create.content.equipment.potatoCannon;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.armor.BacktankUtil;
import com.zurrtum.create.content.equipment.zapper.ShootableGadgetItemMethods;
import com.zurrtum.create.foundation.item.SwingControlItem;
import com.zurrtum.create.foundation.utility.GlobalRegistryAccess;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.PotatoCannonPacket;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PotatoCannonItem extends RangedWeaponItem implements SwingControlItem {
    private static final Predicate<ItemStack> AMMO_PREDICATE = s -> PotatoCannonProjectileType.getTypeForItem(
        GlobalRegistryAccess.getOrThrow(),
        s.getItem()
    ).isPresent();

    public PotatoCannonItem(Settings properties) {
        super(properties);
    }

    @Nullable
    public static Ammo getAmmo(PlayerEntity player, ItemStack heldStack) {
        ItemStack ammoStack = player.getProjectileType(heldStack);
        if (ammoStack.isEmpty()) {
            return null;
        }

        return PotatoCannonProjectileType.getTypeForItem(player.getWorld().getRegistryManager(), ammoStack.getItem())
            .map(r -> new Ammo(ammoStack, r.value())).orElse(null);
    }

    @Override
    protected void shoot(
        LivingEntity shooter,
        ProjectileEntity projectile,
        int index,
        float velocity,
        float inaccuracy,
        float angle,
        @Nullable LivingEntity target
    ) {
    }

    @Override
    protected void shootAll(
        ServerWorld level,
        LivingEntity shooter,
        Hand hand,
        ItemStack weapon,
        List<ItemStack> projectileItems,
        float velocity,
        float inaccuracy,
        boolean isCrit,
        @Nullable LivingEntity target
    ) {
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return use(context.getWorld(), context.getPlayer(), context.getHand());
    }

    @Override
    public ActionResult use(World level, PlayerEntity player, Hand hand) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (ShootableGadgetItemMethods.shouldSwap(player, heldStack, hand, s -> s.getItem() instanceof PotatoCannonItem)) {
            return ActionResult.FAIL;
        }

        Ammo ammo = getAmmo(player, heldStack);
        if (ammo == null) {
            return ActionResult.PASS;
        }
        ItemStack ammoStack = ammo.stack();
        PotatoCannonProjectileType projectileType = ammo.type();

        if (level.isClient) {
            player.clearActiveItem();
            AllClientHandle.INSTANCE.cannonDontAnimateItem(hand);
            return ActionResult.CONSUME.withNewHandStack(heldStack);
        }

        Vec3d barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND, new Vec3d(.75f, -0.15f, 1.5f));
        Vec3d correction = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND, new Vec3d(-.05f, 0, 0))
            .subtract(player.getPos().add(0, player.getStandingEyeHeight(), 0));

        Vec3d lookVec = player.getRotationVector();
        Vec3d motion = lookVec.add(correction).normalize().multiply(2).multiply(projectileType.velocityMultiplier());

        float soundPitch = projectileType.soundPitch() + (level.getRandom().nextFloat() - .5f) / 4f;

        boolean spray = projectileType.split() > 1;
        Vec3d sprayBase = VecHelper.rotate(new Vec3d(0, 0.1, 0), 360 * level.getRandom().nextFloat(), Axis.Z);
        float sprayChange = 360f / projectileType.split();

        ItemStack ammoStackCopy = ammoStack.copy();

        for (int i = 0; i < projectileType.split(); i++) {
            PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(level, SpawnReason.SPAWN_ITEM_USE);
            projectile.setItem(ammoStackCopy);
            projectile.setEnchantmentEffectsFromCannon(heldStack);

            Vec3d splitMotion = motion;
            if (spray) {
                float imperfection = 40 * (level.getRandom().nextFloat() - 0.5f);
                Vec3d sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
                splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, motion));
            }

            if (i != 0)
                projectile.recoveryChance = 0;

            projectile.setPosition(barrelPos.x, barrelPos.y, barrelPos.z);
            projectile.setVelocity(splitMotion);
            projectile.setOwner(player);
            level.spawnEntity(projectile);
        }

        if (!player.isCreative()) {
            ammoStack.decrement(1);
            if (ammoStack.isEmpty())
                player.getInventory().removeOne(ammoStack);
        }

        if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
            heldStack.damage(1, player, LivingEntity.getSlotForHand(hand));

        ShootableGadgetItemMethods.applyCooldown(player, heldStack, hand, s -> s.getItem() instanceof PotatoCannonItem, projectileType.reloadTicks());
        ShootableGadgetItemMethods.sendPackets(player, b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), ammoStack, hand, soundPitch, b));
        player.clearActiveItem();
        return ActionResult.CONSUME.withNewHandStack(heldStack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> tooltip,
        TooltipType tooltipType
    ) {
        if (!AllClientHandle.INSTANCE.isClient()) {
            return;
        }
        PlayerEntity player = AllClientHandle.INSTANCE.getPlayer();
        if (player == null) {
            return;
        }
        Ammo ammo = getAmmo(player, stack);
        if (ammo == null) {
            return;
        }
        ItemStack ammoStack = ammo.stack();
        PotatoCannonProjectileType type = ammo.type();

        RegistryWrapper.WrapperLookup registries = context.getRegistryLookup();
        if (registries == null)
            return;

        RegistryEntryLookup<Enchantment> lookup = registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent enchantments = stack.getEnchantments();
        int power = enchantments.getLevel(lookup.getOrThrow(Enchantments.POWER));
        int punch = enchantments.getLevel(lookup.getOrThrow(Enchantments.PUNCH));
        final float additionalDamageMult = 1 + power * .2f;
        final float additionalKnockback = punch * .5f;

        String _attack = "create.potato_cannon.ammo.attack_damage";
        String _reload = "create.potato_cannon.ammo.reload_ticks";
        String _knockback = "create.potato_cannon.ammo.knockback";

        tooltip.accept(ScreenTexts.EMPTY);
        tooltip.accept(ammoStack.getName().copy().append(Text.literal(":")).formatted(Formatting.GRAY));
        MutableText spacing = ScreenTexts.space();
        Formatting green = Formatting.GREEN;
        Formatting darkGreen = Formatting.DARK_GREEN;

        float damageF = type.damage() * additionalDamageMult;
        MutableText damage = Text.literal(damageF == MathHelper.floor(damageF) ? "" + MathHelper.floor(damageF) : "" + damageF);
        MutableText reloadTicks = Text.literal("" + type.reloadTicks());
        MutableText knockback = Text.literal("" + (type.knockback() + additionalKnockback));

        damage = damage.formatted(additionalDamageMult > 1 ? green : darkGreen);
        knockback = knockback.formatted(additionalKnockback > 0 ? green : darkGreen);
        reloadTicks = reloadTicks.formatted(darkGreen);

        tooltip.accept(spacing.copyContentOnly().append(Text.translatable(_attack, damage).formatted(darkGreen)));
        tooltip.accept(spacing.copyContentOnly().append(Text.translatable(_reload, reloadTicks).formatted(darkGreen)));
        tooltip.accept(spacing.copyContentOnly().append(Text.translatable(_knockback, knockback).formatted(darkGreen)));
    }

    @Override
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity player) {
        return false;
    }

    //TODO
    //    @Override
    //    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    //        return slotChanged || newStack.getItem() != oldStack.getItem();
    //    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return AMMO_PREDICATE;
    }

    @Override
    public int getRange() {
        return 15;
    }

    //TODO
    //    @Override
    //    public boolean supportsEnchantment(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
    //        if (enchantment.is(Enchantments.INFINITY))
    //            return false;
    //        if (enchantment.is(Enchantments.LOOTING))
    //            return true;
    //        return super.supportsEnchantment(stack, enchantment);
    //    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return false;
    }

    public boolean isModelBarVisible(ItemStack stack) {
        return BacktankUtil.isBarVisible(stack, maxUses());
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return BacktankUtil.getBarWidth(stack, maxUses());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return BacktankUtil.getBarColor(stack, maxUses());
    }

    private static int maxUses() {
        return AllConfigs.server().equipment.maxPotatoCannonShots.get();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, Hand hand) {
        return true;
    }

    public record Ammo(ItemStack stack, PotatoCannonProjectileType type) {
    }
}
