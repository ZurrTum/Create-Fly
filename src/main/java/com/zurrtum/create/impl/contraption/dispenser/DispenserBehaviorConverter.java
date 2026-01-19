package com.zurrtum.create.impl.contraption.dispenser;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedProjectileDispenseBehavior;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public enum DispenserBehaviorConverter implements SimpleRegistry.Provider<Item, MountedDispenseBehavior> {
    INSTANCE;

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public MountedDispenseBehavior get(Item item, Level world) {
        DispenseItemBehavior vanilla = ((DispenserBlock) Blocks.DISPENSER).getDispenseMethod(world, item.getDefaultInstance());
        if (vanilla == null)
            return null;

        // when the default, return null. The default will be used anyway, avoid caching it for no reason.
        if (vanilla.getClass() == DefaultDispenseItemBehavior.class)
            return null;

        // if the item is explicitly blocked from having its behavior wrapped, ignore it
        if (item.builtInRegistryHolder().is(AllItemTags.DISPENSE_BEHAVIOR_WRAP_BLACKLIST))
            return null;

        if (vanilla instanceof ProjectileDispenseBehavior projectile) {
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
        private final DispenseItemBehavior wrapped;
        private boolean hasErrored;

        private FallbackBehavior(Item item, DispenseItemBehavior wrapped) {
            this.item = item;
            this.wrapped = wrapped;
        }

        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
            if (this.hasErrored)
                return stack;

            MinecraftServer server = context.world.getServer();
            ServerLevel serverLevel = server != null ? server.getLevel(context.world.dimension()) : null;

            Direction nearestFacing = MountedDispenseBehavior.getClosestFacingDirection(facing);
            BlockState state = context.state;
            if (state.hasProperty(BlockStateProperties.FACING))
                state = state.setValue(BlockStateProperties.FACING, nearestFacing);

            BlockSource source = new BlockSource(serverLevel, pos, state, null);

            try {
                // use a copy in case of implosion after modifying it
                return this.wrapped.dispense(source, stack.copy());
            } catch (NullPointerException e) {
                // likely due to the lack of a BlockEntity
                Identifier itemId = BuiltInRegistries.ITEM.getKey(this.item);
                String message = "Error dispensing item '" + itemId + "' from contraption, not doing that anymore";
                Create.LOGGER.error(message, e);
                this.hasErrored = true;
                return stack;
            }
        }
    }
}
