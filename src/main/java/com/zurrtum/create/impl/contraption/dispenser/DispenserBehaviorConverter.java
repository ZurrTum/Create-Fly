package com.zurrtum.create.impl.contraption.dispenser;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedProjectileDispenseBehavior;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum DispenserBehaviorConverter implements SimpleRegistry.Provider<Item, MountedDispenseBehavior> {
    INSTANCE;

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public MountedDispenseBehavior get(Item item, World world) {
        DispenserBehavior vanilla = ((DispenserBlock) Blocks.DISPENSER).getBehaviorForItem(world, item.getDefaultStack());
        if (vanilla == null)
            return null;

        // when the default, return null. The default will be used anyway, avoid caching it for no reason.
        if (vanilla.getClass() == ItemDispenserBehavior.class)
            return null;

        // if the item is explicitly blocked from having its behavior wrapped, ignore it
        if (item.getRegistryEntry().isIn(AllItemTags.DISPENSE_BEHAVIOR_WRAP_BLACKLIST))
            return null;

        if (vanilla instanceof ProjectileDispenserBehavior projectile) {
            return MountedProjectileDispenseBehavior.of(projectile);
        }

        // other behaviors are more dangerous due to BlockPointer providing a BlockEntity, which contraptions can't do.
        // wrap in a fallback that will watch for errors.
        return new FallbackBehavior(item, vanilla);
    }

    @Override
    @Nullable
    public MountedDispenseBehavior get(Item item) {
        Create.LOGGER.warn("Requires World parameter");
        return null;
    }

    @Override
    public void onRegister(Runnable invalidate) {
        // invalidate if the blacklist tag might've changed
        //TODO
        //        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
        //            if (event.shouldUpdateStaticData()) {
        //                invalidate.run();
        //            }
        //        });
    }

    private static final class FallbackBehavior extends DefaultMountedDispenseBehavior {
        private final Item item;
        private final DispenserBehavior wrapped;
        private boolean hasErrored;

        private FallbackBehavior(Item item, DispenserBehavior wrapped) {
            this.item = item;
            this.wrapped = wrapped;
        }

        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            if (this.hasErrored)
                return stack;

            MinecraftServer server = context.world.getServer();
            ServerWorld serverLevel = server != null ? server.getWorld(context.world.getRegistryKey()) : null;

            Direction nearestFacing = MountedDispenseBehavior.getClosestFacingDirection(facing);
            BlockState state = context.state;
            if (state.contains(Properties.FACING))
                state = state.with(Properties.FACING, nearestFacing);

            BlockPointer source = new BlockPointer(serverLevel, pos, state, null);

            try {
                // use a copy in case of implosion after modifying it
                return this.wrapped.dispense(source, stack.copy());
            } catch (NullPointerException e) {
                // likely due to the lack of a BlockEntity
                Identifier itemId = Registries.ITEM.getId(this.item);
                String message = "Error dispensing item '" + itemId + "' from contraption, not doing that anymore";
                Create.LOGGER.error(message, e);
                this.hasErrored = true;
                return stack;
            }
        }
    }
}
