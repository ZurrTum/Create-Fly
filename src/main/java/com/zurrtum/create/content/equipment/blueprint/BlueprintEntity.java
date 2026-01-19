package com.zurrtum.create.content.equipment.blueprint;

import com.google.common.cache.Cache;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import com.zurrtum.create.foundation.utility.TickBasedCache;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.packet.s2c.BlueprintPreviewPacket;
import com.zurrtum.create.infrastructure.packet.s2c.NbtSpawnPacket;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class BlueprintEntity extends HangingEntity implements SpecialEntityItemRequirement, IInteractionChecker {
    private static final Cache<String, BlueprintPreviewPacket> PREVIEW_CACHE = new TickBasedCache<>(20, true);
    private static final EntityDataAccessor<CompoundTag> RECIPES = SynchedEntityData.defineId(
        BlueprintEntity.class,
        AllSynchedDatas.NBT_COMPOUND_HANDLER
    );

    public int size;
    protected Direction verticalOrientation;

    public BlueprintEntity(EntityType<? extends BlueprintEntity> p_i50221_1_, Level p_i50221_2_) {
        super(p_i50221_1_, p_i50221_2_);
        size = 1;
    }

    public BlueprintEntity(Level world, BlockPos pos, Direction facing, Direction verticalOrientation) {
        super(AllEntityTypes.CRAFTING_BLUEPRINT, world, pos);

        for (int size = 3; size > 0; size--) {
            this.size = size;
            updateFacingWithBoundingBox(facing, verticalOrientation);
            if (survives())
                break;
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_POSE.equals(data)) {
            refreshDimensions();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RECIPES, new CompoundTag());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        view.store("Orientation", Direction.CODEC, verticalOrientation);
        view.store("Facing", Direction.CODEC, getDirection());
        view.putInt("Size", size);
        view.store("Recipes", CompoundTag.CODEC, entityData.get(RECIPES));
        super.addAdditionalSaveData(view);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        verticalOrientation = view.read("Orientation", Direction.CODEC).orElse(Direction.DOWN);
        size = view.getIntOr("Size", 1);
        view.read("Recipes", CompoundTag.CODEC).ifPresent(nbt -> entityData.set(RECIPES, nbt));
        super.readAdditionalSaveData(view);
        Direction direction = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
        updateFacingWithBoundingBox(direction, verticalOrientation);
    }

    protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
        Objects.requireNonNull(facing);
        setDirectionRaw(facing);
        this.verticalOrientation = verticalOrientation;
        if (facing.getAxis().isHorizontal()) {
            setXRot(0.0F);
            setYRot(facing.get2DDataValue() * 90);
        } else {
            setXRot(-90 * facing.getAxisDirection().getStep());
            setYRot(verticalOrientation.getAxis().isHorizontal() ? 180 + verticalOrientation.toYRot() : 0);
        }

        xRotO = getXRot();
        yRotO = getYRot();
        recalculateBoundingBox();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).withEyeHeight(0);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
        Vec3 pos = Vec3.atLowerCornerOf(getPos()).add(.5, .5, .5).subtract(Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(0.46875));
        double d1 = pos.x;
        double d2 = pos.y;
        double d3 = pos.z;
        setPosRaw(d1, d2, d3);

        Axis axis = direction.getAxis();
        if (size == 2)
            pos = pos.add(Vec3.atLowerCornerOf(axis.isHorizontal() ? direction.getCounterClockWise()
                    .getUnitVec3i() : verticalOrientation.getClockWise().getUnitVec3i()).scale(0.5))
                .add(Vec3.atLowerCornerOf(axis.isHorizontal() ? Direction.UP.getUnitVec3i() : direction == Direction.UP ? verticalOrientation.getUnitVec3i() : verticalOrientation.getOpposite()
                    .getUnitVec3i()).scale(0.5));

        d1 = pos.x;
        d2 = pos.y;
        d3 = pos.z;

        double d4 = getEntityWidth();
        double d5 = getEntityHeight();
        double d6 = d4;
        Axis direction$axis = getDirection().getAxis();
        switch (direction$axis) {
            case X:
                d4 = 1.0D;
                break;
            case Y:
                d5 = 1.0D;
                break;
            case Z:
                d6 = 1.0D;
        }

        d4 = d4 / 32.0D;
        d5 = d5 / 32.0D;
        d6 = d6 / 32.0D;

        return new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6);
    }

    @Override
    protected void recalculateBoundingBox() {
        Direction direction = getDirection();
        if (direction != null && verticalOrientation != null) {
            setBoundingBox(calculateBoundingBox(pos, direction));
        }
    }

    @Override
    public void setPos(double pX, double pY, double pZ) {
        setPosRaw(pX, pY, pZ);
        super.setPos(pX, pY, pZ);
    }

    @Override
    public boolean survives() {
        Level world = level();
        if (!world.noCollision(this))
            return false;

        int i = Math.max(1, getEntityWidth() / 16);
        int j = Math.max(1, getEntityHeight() / 16);
        Direction direction = getDirection();
        BlockPos blockpos = pos.relative(direction.getOpposite());
        Direction upDirection = direction.getAxis()
            .isHorizontal() ? Direction.UP : direction == Direction.UP ? verticalOrientation : verticalOrientation.getOpposite();
        Direction newDirection = direction.getAxis().isVertical() ? verticalOrientation.getClockWise() : direction.getCounterClockWise();
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int k = 0; k < i; ++k) {
            for (int l = 0; l < j; ++l) {
                int i1 = (i - 1) / -2;
                int j1 = (j - 1) / -2;
                blockpos$mutable.set(blockpos).move(newDirection, k + i1).move(upDirection, l + j1);
                BlockState blockstate = world.getBlockState(blockpos$mutable);
                if (Block.canSupportCenter(world, blockpos$mutable, direction))
                    continue;
                if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
                    return false;
                }
            }
        }

        return canCoexist(true);
    }

    public int getEntityWidth() {
        return 16 * size;
    }

    public int getEntityHeight() {
        return 16 * size;
    }

    @Override
    public boolean skipAttackInteraction(Entity source) {
        if (!(source instanceof Player player) || level().isClientSide())
            return super.skipAttackInteraction(source);

        double attrib = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + (player.isCreative() ? 0 : -0.5F);

        Vec3 eyePos = source.getEyePosition(1);
        Vec3 look = source.getViewVector(1);
        Vec3 target = eyePos.add(look.scale(attrib));

        Optional<Vec3> rayTrace = getBoundingBox().clip(eyePos, target);
        if (!rayTrace.isPresent())
            return super.skipAttackInteraction(source);

        Vec3 hitVec = rayTrace.get();
        BlueprintSection sectionAt = getSectionAt(hitVec.subtract(position()));
        ItemStackHandler items = sectionAt.getItems();

        if (items.getItem(9).isEmpty())
            return super.skipAttackInteraction(source);
        for (int i = 0, size = items.getContainerSize(); i < size; i++)
            items.setItem(i, ItemStack.EMPTY);
        sectionAt.save(items);
        return true;
    }

    @Override
    public void dropItem(ServerLevel world, @Nullable Entity p_110128_1_) {
        if (!world.getGameRules().get(GameRules.ENTITY_DROPS))
            return;

        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (p_110128_1_ instanceof Player playerentity) {
            if (playerentity.getAbilities().instabuild)
                return;
        }

        spawnAtLocation(world, AllItems.CRAFTING_BLUEPRINT.getDefaultInstance());
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return AllItems.CRAFTING_BLUEPRINT.getDefaultInstance();
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return new ItemRequirement(ItemUseType.CONSUME, AllItems.CRAFTING_BLUEPRINT);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void snapTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
        this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
    }

    @Override
    public void moveOrInterpolateTo(Vec3 pos, float yaw, float pitch) {
        BlockPos blockpos = this.pos.offset(BlockPos.containing(pos.x() - getX(), pos.y() - getY(), pos.z() - getZ()));
        this.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, registryAccess());
            addAdditionalSaveData(view);
            return new NbtSpawnPacket(this, entityTrackerEntry, view.buildResult());
        }
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        CompoundTag nbt = ((NbtSpawnPacket) packet).getNbt();
        if (nbt == null) {
            return;
        }
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
            readAdditionalSaveData(TagValueInput.create(logging, registryAccess(), nbt));
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (FakePlayerHandler.has(player))
            return InteractionResult.PASS;

        boolean holdingWrench = player.getItemInHand(hand).is(AllItems.WRENCH);
        BlueprintSection section = getSectionAt(vec);
        ItemStackHandler items = section.getItems();

        Level world = level();
        if (!holdingWrench && !world.isClientSide() && !items.getItem(9).isEmpty()) {
            RegistryAccess registryManager = world.registryAccess();
            Inventory playerInv = player.getInventory();
            int size = playerInv.getContainerSize();
            boolean firstPass = true;
            int amountCrafted = 0;
            //TODO
            //            CommonHooks.setCraftingPlayer(player);
            RecipeHolder<CraftingRecipe> recipe = null;
            List<ItemStack> results = null;

            do {
                int[] stacksTaken = new int[size];
                int max = 0;
                List<ItemStack> craftingStacks = new ArrayList<>(9);
                boolean success = true;

                Search:
                for (int i = 0; i < 9; i++) {
                    ItemStack filter = items.getItem(i);
                    if (filter.isEmpty()) {
                        craftingStacks.add(ItemStack.EMPTY);
                        continue;
                    }
                    FilterItemStack requestedItem = FilterItemStack.of(filter);

                    for (int slot = 0; slot < size; slot++) {
                        ItemStack stack = playerInv.getItem(slot);
                        if (!requestedItem.test(world, stack))
                            continue;
                        int used = stacksTaken[slot];
                        if (stack.getCount() == used) {
                            continue;
                        }
                        stacksTaken[slot] = used + 1;
                        craftingStacks.add(stack.copyWithCount(1));
                        if (slot > max) {
                            max = slot;
                        }
                        continue Search;
                    }

                    success = false;
                    break;
                }

                if (success) {
                    CraftingInput input = CraftingInput.of(3, 3, craftingStacks);
                    recipe = ((ServerLevel) world).recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, world, recipe).orElse(null);
                    if (recipe == null) {
                        success = false;
                    } else {
                        CraftingRecipe craftingRecipe = recipe.value();
                        ItemStack result = craftingRecipe.assemble(input, registryManager);
                        if (result.isEmpty() || result.getCount() + amountCrafted > 64) {
                            success = false;
                        } else {
                            amountCrafted += result.getCount();
                            result.onCraftedBy(player, 1);
                            //TODO
                            //                        EventHooks.firePlayerCraftingEvent(player, result, craftingInventory);

                            results = new ArrayList<>();
                            results.add(result);
                            for (ItemStack stack : craftingRecipe.getRemainingItems(input)) {
                                if (stack.isEmpty()) {
                                    continue;
                                }
                                results.add(stack);
                            }

                            if (firstPass) {
                                world.playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.ITEM_PICKUP,
                                    SoundSource.PLAYERS,
                                    .2f,
                                    1f + world.getRandom().nextFloat()
                                );
                                firstPass = false;
                            }
                        }
                    }
                }

                if (success) {
                    for (int slot = 0; slot <= max; slot++) {
                        int used = stacksTaken[slot];
                        if (used == 0) {
                            continue;
                        }
                        ItemStack stack = playerInv.getItem(slot);
                        int count = stack.getCount();
                        if (count == used) {
                            stack = ItemStack.EMPTY;
                        } else {
                            stack.setCount(count - used);
                        }
                        playerInv.setItem(slot, stack);
                    }
                    playerInv.setChanged();
                    for (ItemStack stack : results) {
                        player.getInventory().placeItemBackInInventory(stack);
                    }
                } else {
                    break;
                }

            } while (player.isShiftKeyDown());
            //TODO
            //            CommonHooks.setCraftingPlayer(null);
            PREVIEW_CACHE.invalidate(getId() + "_" + section.index + "_" + player.getId() + (player.isShiftKeyDown() ? "_sneaking" : ""));
            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MenuProvider.openHandledScreen(serverPlayer, section);
        }

        return InteractionResult.SUCCESS;
    }

    public static BlueprintPreviewPacket getPreview(BlueprintEntity be, int index, ServerPlayer player, boolean sneaking) {
        try {
            return PREVIEW_CACHE.get(
                be.getId() + "_" + index + "_" + player.getId() + (sneaking ? "_sneaking" : ""),
                () -> createPreview(be, index, player, sneaking)
            );
        } catch (ExecutionException e) {
            e.printStackTrace();
            return BlueprintPreviewPacket.EMPTY;
        }
    }

    private static BlueprintPreviewPacket createPreview(BlueprintEntity be, int index, ServerPlayer player, boolean sneaking) {
        BlueprintSection section = be.getSection(index);
        ItemStackHandler items = section.getItems();
        if (items.isEmpty()) {
            return BlueprintPreviewPacket.EMPTY;
        }
        ServerLevel world = player.level();
        Inventory playerInv = player.getInventory();
        int size = playerInv.getContainerSize();
        int[] stacksTaken = new int[size];
        List<FilterItemStack> requestedItems = new ArrayList<>(9);
        List<ItemStack> craftingStacks = new ArrayList<>(9);
        Object2IntLinkedOpenCustomHashMap<ItemStack> missingStacks = BlueprintPreviewPacket.createMap();
        Object2IntLinkedOpenCustomHashMap<ItemStack> availableStacks = BlueprintPreviewPacket.createMap();
        Search:
        for (int i = 0; i < 9; i++) {
            FilterItemStack requestedItem = FilterItemStack.of(items.getItem(i));
            if (requestedItem.isEmpty()) {
                requestedItems.add(null);
                craftingStacks.add(ItemStack.EMPTY);
                continue;
            }
            requestedItems.add(requestedItem);
            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = playerInv.getItem(slot);
                if (!requestedItem.test(world, stack))
                    continue;
                int used = stacksTaken[slot];
                if (stack.getCount() == used) {
                    continue;
                }
                stacksTaken[slot] = used + 1;
                craftingStacks.add(stack.copyWithCount(1));
                availableStacks.merge(stack, 1, Integer::sum);
                continue Search;
            }
            missingStacks.merge(requestedItem.item(), 1, Integer::sum);
        }
        if (!missingStacks.isEmpty()) {
            return new BlueprintPreviewPacket(availableStacks, missingStacks, items.getItem(9));
        }
        CraftingInput input = CraftingInput.of(3, 3, craftingStacks);
        Optional<ItemStack> result = world.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, world)
            .map(entry -> entry.value().assemble(input, world.registryAccess())).filter(stack -> !stack.isEmpty());
        if (result.isEmpty()) {
            return new BlueprintPreviewPacket(availableStacks, List.of(), ItemStack.EMPTY);
        }
        ItemStack resultStack = result.get();
        if (sneaking) {
            int max = resultStack.getMaxStackSize();
            int craftingCount = resultStack.getCount();
            if (craftingCount < max) {
                int count = craftingCount;
                Object2IntLinkedOpenCustomHashMap<ItemStack> ingredients = BlueprintPreviewPacket.createMap(availableStacks);
                Outer:
                while (count + craftingCount <= max) {
                    Search:
                    for (int i = 0; i < 9; i++) {
                        FilterItemStack requestedItem = requestedItems.get(i);
                        if (requestedItem == null) {
                            continue;
                        }
                        for (int slot = 0; slot < size; slot++) {
                            ItemStack stack = playerInv.getItem(slot);
                            if (!requestedItem.test(world, stack))
                                continue;
                            int used = stacksTaken[slot];
                            if (stack.getCount() == used) {
                                continue;
                            }
                            stacksTaken[slot] = used + 1;
                            continue Search;
                        }
                        break Outer;
                    }
                    ObjectBidirectionalIterator<Object2IntMap.Entry<ItemStack>> iterator = availableStacks.object2IntEntrySet().fastIterator();
                    do {
                        Object2IntMap.Entry<ItemStack> entry = iterator.next();
                        entry.setValue(entry.getIntValue() + ingredients.getInt(entry.getKey()));
                    } while (iterator.hasNext());
                    count += craftingCount;
                }
                resultStack.setCount(count);
            }
        }
        return new BlueprintPreviewPacket(availableStacks, List.of(), resultStack);
    }

    public BlueprintSection getSectionAt(Vec3 vec) {
        int index = 0;
        if (size > 1) {
            vec = VecHelper.rotate(vec, getYRot(), Axis.Y);
            vec = VecHelper.rotate(vec, -getXRot(), Axis.X);
            vec = vec.add(0.5, 0.5, 0);
            if (size == 3)
                vec = vec.add(1, 1, 0);
            int x = Mth.clamp(Mth.floor(vec.x), 0, size - 1);
            int y = Mth.clamp(Mth.floor(vec.y), 0, size - 1);
            index = x + y * size;
        }

        return getSection(index);
    }

    public Optional<CompoundTag> getRecipeCompound(int index) {
        return entityData.get(RECIPES).getCompound(Integer.toString(index));
    }

    public void putRecipeCompound(int index, CompoundTag compound) {
        CompoundTag recipes = entityData.get(RECIPES);
        recipes.put(Integer.toString(index), compound);
        entityData.set(RECIPES, recipes, true);
    }

    private final Map<Integer, BlueprintSection> sectionCache = new HashMap<>();

    public BlueprintSection getSection(int index) {
        return sectionCache.computeIfAbsent(index, BlueprintSection::new);
    }

    public class BlueprintSection implements MenuProvider, IInteractionChecker {
        private static final Couple<ItemStack> EMPTY_DISPLAY = Couple.create(ItemStack.EMPTY, ItemStack.EMPTY);
        public int index;
        Couple<ItemStack> cachedDisplayItems;
        public boolean inferredIcon = false;

        public BlueprintSection(int index) {
            this.index = index;
        }

        public Couple<ItemStack> getDisplayItems() {
            if (cachedDisplayItems != null)
                return cachedDisplayItems;
            return getRecipeCompound(index).flatMap(nbt -> nbt.read("Inventory", CreateCodecs.ITEM_LIST_CODEC)
                .map(items -> Couple.create(items.get(9), items.get(10)))).orElse(EMPTY_DISPLAY);
        }

        public ItemStackHandler getItems() {
            ItemStackHandler newInv = new ItemStackHandler(11);
            getRecipeCompound(index).ifPresentOrElse(
                nbt -> {
                    try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
                        ValueInput view = TagValueInput.create(logging, registryAccess(), nbt);
                        newInv.readSlots(view);
                        inferredIcon = view.getBooleanOr("InferredIcon", false);
                    }
                }, () -> inferredIcon = false
            );
            return newInv;
        }

        public void save(ItemStackHandler inventory) {
            cachedDisplayItems = null;
            if (!level().isClientSide()) {
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
                    TagValueOutput view = TagValueOutput.createWithContext(logging, registryAccess());
                    inventory.writeSlots(view);
                    view.putBoolean("InferredIcon", inferredIcon);
                    putRecipeCompound(index, view.buildResult());
                }
            }
        }

        public boolean isEntityAlive() {
            return isAlive();
        }

        public Level getBlueprintWorld() {
            return level();
        }

        @Override
        public BlueprintMenu createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
            extraData.writeVarInt(getId());
            extraData.writeVarInt(index);
            return new BlueprintMenu(id, inv, this);
        }

        @Override
        public Component getDisplayName() {
            return AllItems.CRAFTING_BLUEPRINT.getName();
        }

        @Override
        public boolean canPlayerUse(Player player) {
            return BlueprintEntity.this.canPlayerUse(player);
        }

    }

    @Override
    public boolean canPlayerUse(Player player) {
        AABB box = getBoundingBox();

        double dx = 0;
        if (box.minX > player.getX()) {
            dx = box.minX - player.getX();
        } else if (player.getX() > box.maxX) {
            dx = player.getX() - box.maxX;
        }

        double dy = 0;
        if (box.minY > player.getY()) {
            dy = box.minY - player.getY();
        } else if (player.getY() > box.maxY) {
            dy = player.getY() - box.maxY;
        }

        double dz = 0;
        if (box.minZ > player.getZ()) {
            dz = box.minZ - player.getZ();
        } else if (player.getZ() > box.maxZ) {
            dz = player.getZ() - box.maxZ;
        }

        return (dx * dx + dy * dy + dz * dz) <= 64.0D;
    }

}
