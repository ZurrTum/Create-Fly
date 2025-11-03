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
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class BlueprintEntity extends AbstractDecorationEntity implements SpecialEntityItemRequirement, IInteractionChecker {
    private static final Cache<String, BlueprintPreviewPacket> PREVIEW_CACHE = new TickBasedCache<>(20, true);
    private static final TrackedData<NbtCompound> RECIPES = DataTracker.registerData(BlueprintEntity.class, AllSynchedDatas.NBT_COMPOUND_HANDLER);

    public int size;
    protected Direction verticalOrientation;

    public BlueprintEntity(EntityType<? extends BlueprintEntity> p_i50221_1_, World p_i50221_2_) {
        super(p_i50221_1_, p_i50221_2_);
        size = 1;
    }

    public BlueprintEntity(World world, BlockPos pos, Direction facing, Direction verticalOrientation) {
        super(AllEntityTypes.CRAFTING_BLUEPRINT, world, pos);

        for (int size = 3; size > 0; size--) {
            this.size = size;
            updateFacingWithBoundingBox(facing, verticalOrientation);
            if (canStayAttached())
                break;
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (POSE.equals(data)) {
            calculateDimensions();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(RECIPES, new NbtCompound());
    }

    @Override
    public void writeCustomData(WriteView view) {
        view.put("Orientation", Direction.CODEC, verticalOrientation);
        view.put("Facing", Direction.CODEC, getHorizontalFacing());
        view.putInt("Size", size);
        view.put("Recipes", NbtCompound.CODEC, dataTracker.get(RECIPES));
        super.writeCustomData(view);
    }

    @Override
    public void readCustomData(ReadView view) {
        verticalOrientation = view.read("Orientation", Direction.CODEC).orElse(Direction.DOWN);
        size = view.getInt("Size", 1);
        view.read("Recipes", NbtCompound.CODEC).ifPresent(nbt -> dataTracker.set(RECIPES, nbt));
        super.readCustomData(view);
        Direction direction = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
        updateFacingWithBoundingBox(direction, verticalOrientation);
    }

    protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
        Objects.requireNonNull(facing);
        setFacingInternal(facing);
        this.verticalOrientation = verticalOrientation;
        if (facing.getAxis().isHorizontal()) {
            setPitch(0.0F);
            setYaw(facing.getHorizontalQuarterTurns() * 90);
        } else {
            setPitch(-90 * facing.getDirection().offset());
            setYaw(verticalOrientation.getAxis().isHorizontal() ? 180 + verticalOrientation.getPositiveHorizontalDegrees() : 0);
        }

        lastPitch = getPitch();
        lastYaw = getYaw();
        updateAttachmentPosition();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).withEyeHeight(0);
    }

    @Override
    protected Box calculateBoundingBox(BlockPos blockPos, Direction direction) {
        Vec3d pos = Vec3d.of(getAttachedBlockPos()).add(.5, .5, .5).subtract(Vec3d.of(direction.getVector()).multiply(0.46875));
        double d1 = pos.x;
        double d2 = pos.y;
        double d3 = pos.z;
        setPos(d1, d2, d3);

        Axis axis = direction.getAxis();
        if (size == 2)
            pos = pos.add(Vec3d.of(axis.isHorizontal() ? direction.rotateYCounterclockwise().getVector() : verticalOrientation.rotateYClockwise()
                    .getVector()).multiply(0.5))
                .add(Vec3d.of(axis.isHorizontal() ? Direction.UP.getVector() : direction == Direction.UP ? verticalOrientation.getVector() : verticalOrientation.getOpposite()
                    .getVector()).multiply(0.5));

        d1 = pos.x;
        d2 = pos.y;
        d3 = pos.z;

        double d4 = getEntityWidth();
        double d5 = getEntityHeight();
        double d6 = d4;
        Axis direction$axis = getHorizontalFacing().getAxis();
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

        return new Box(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6);
    }

    @Override
    protected void updateAttachmentPosition() {
        Direction direction = getHorizontalFacing();
        if (direction != null && verticalOrientation != null) {
            setBoundingBox(calculateBoundingBox(attachedBlockPos, direction));
        }
    }

    @Override
    public void setPosition(double pX, double pY, double pZ) {
        setPos(pX, pY, pZ);
        super.setPosition(pX, pY, pZ);
    }

    @Override
    public boolean canStayAttached() {
        World world = getEntityWorld();
        if (!world.isSpaceEmpty(this))
            return false;

        int i = Math.max(1, getEntityWidth() / 16);
        int j = Math.max(1, getEntityHeight() / 16);
        Direction direction = getHorizontalFacing();
        BlockPos blockpos = attachedBlockPos.offset(direction.getOpposite());
        Direction upDirection = direction.getAxis()
            .isHorizontal() ? Direction.UP : direction == Direction.UP ? verticalOrientation : verticalOrientation.getOpposite();
        Direction newDirection = direction.getAxis().isVertical() ? verticalOrientation.rotateYClockwise() : direction.rotateYCounterclockwise();
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k = 0; k < i; ++k) {
            for (int l = 0; l < j; ++l) {
                int i1 = (i - 1) / -2;
                int j1 = (j - 1) / -2;
                blockpos$mutable.set(blockpos).move(newDirection, k + i1).move(upDirection, l + j1);
                BlockState blockstate = world.getBlockState(blockpos$mutable);
                if (Block.sideCoversSmallSquare(world, blockpos$mutable, direction))
                    continue;
                if (!blockstate.isSolid() && !AbstractRedstoneGateBlock.isRedstoneGate(blockstate)) {
                    return false;
                }
            }
        }

        return hasNoIntersectingDecoration(true);
    }

    public int getEntityWidth() {
        return 16 * size;
    }

    public int getEntityHeight() {
        return 16 * size;
    }

    @Override
    public boolean handleAttack(Entity source) {
        if (!(source instanceof PlayerEntity player) || getEntityWorld().isClient())
            return super.handleAttack(source);

        double attrib = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + (player.isCreative() ? 0 : -0.5F);

        Vec3d eyePos = source.getCameraPosVec(1);
        Vec3d look = source.getRotationVec(1);
        Vec3d target = eyePos.add(look.multiply(attrib));

        Optional<Vec3d> rayTrace = getBoundingBox().raycast(eyePos, target);
        if (!rayTrace.isPresent())
            return super.handleAttack(source);

        Vec3d hitVec = rayTrace.get();
        BlueprintSection sectionAt = getSectionAt(hitVec.subtract(getEntityPos()));
        ItemStackHandler items = sectionAt.getItems();

        if (items.getStack(9).isEmpty())
            return super.handleAttack(source);
        for (int i = 0, size = items.size(); i < size; i++)
            items.setStack(i, ItemStack.EMPTY);
        sectionAt.save(items);
        return true;
    }

    @Override
    public void onBreak(ServerWorld world, @Nullable Entity p_110128_1_) {
        if (!world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
            return;

        playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
        if (p_110128_1_ instanceof PlayerEntity playerentity) {
            if (playerentity.getAbilities().creativeMode)
                return;
        }

        dropStack(world, AllItems.CRAFTING_BLUEPRINT.getDefaultStack());
    }

    @Override
    public @Nullable ItemStack getPickBlockStack() {
        return AllItems.CRAFTING_BLUEPRINT.getDefaultStack();
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return new ItemRequirement(ItemUseType.CONSUME, AllItems.CRAFTING_BLUEPRINT);
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void refreshPositionAndAngles(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
        this.setPosition(p_70012_1_, p_70012_3_, p_70012_5_);
    }

    @Override
    public void updateTrackedPositionAndAngles(Vec3d pos, float yaw, float pitch) {
        BlockPos blockpos = attachedBlockPos.add(BlockPos.ofFloored(pos.getX() - getX(), pos.getY() - getY(), pos.getZ() - getZ()));
        this.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, getRegistryManager());
            writeCustomData(view);
            return new NbtSpawnPacket(this, entityTrackerEntry, view.getNbt());
        }
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        NbtCompound nbt = ((NbtSpawnPacket) packet).getNbt();
        if (nbt == null) {
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            readCustomData(NbtReadView.create(logging, getRegistryManager(), nbt));
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (FakePlayerHandler.has(player))
            return ActionResult.PASS;

        boolean holdingWrench = player.getStackInHand(hand).isOf(AllItems.WRENCH);
        BlueprintSection section = getSectionAt(vec);
        ItemStackHandler items = section.getItems();

        World world = getEntityWorld();
        if (!holdingWrench && !world.isClient() && !items.getStack(9).isEmpty()) {
            DynamicRegistryManager registryManager = world.getRegistryManager();
            PlayerInventory playerInv = player.getInventory();
            int size = playerInv.size();
            boolean firstPass = true;
            int amountCrafted = 0;
            //TODO
            //            CommonHooks.setCraftingPlayer(player);
            RecipeEntry<CraftingRecipe> recipe = null;
            List<ItemStack> results = null;

            do {
                int[] stacksTaken = new int[size];
                int max = 0;
                List<ItemStack> craftingStacks = new ArrayList<>(9);
                boolean success = true;

                Search:
                for (int i = 0; i < 9; i++) {
                    ItemStack filter = items.getStack(i);
                    if (filter.isEmpty()) {
                        craftingStacks.add(ItemStack.EMPTY);
                        continue;
                    }
                    FilterItemStack requestedItem = FilterItemStack.of(filter);

                    for (int slot = 0; slot < size; slot++) {
                        ItemStack stack = playerInv.getStack(slot);
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
                    CraftingRecipeInput input = CraftingRecipeInput.create(3, 3, craftingStacks);
                    recipe = ((ServerWorld) world).getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world, recipe).orElse(null);
                    if (recipe == null) {
                        success = false;
                    } else {
                        CraftingRecipe craftingRecipe = recipe.value();
                        ItemStack result = craftingRecipe.craft(input, registryManager);
                        if (result.isEmpty() || result.getCount() + amountCrafted > 64) {
                            success = false;
                        } else {
                            amountCrafted += result.getCount();
                            result.onCraftByPlayer(player, 1);
                            //TODO
                            //                        EventHooks.firePlayerCraftingEvent(player, result, craftingInventory);

                            results = new ArrayList<>();
                            results.add(result);
                            for (ItemStack stack : craftingRecipe.getRecipeRemainders(input)) {
                                if (stack.isEmpty()) {
                                    continue;
                                }
                                results.add(stack);
                            }

                            if (firstPass) {
                                world.playSound(
                                    null,
                                    player.getBlockPos(),
                                    SoundEvents.ENTITY_ITEM_PICKUP,
                                    SoundCategory.PLAYERS,
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
                        ItemStack stack = playerInv.getStack(slot);
                        int count = stack.getCount();
                        if (count == used) {
                            stack = ItemStack.EMPTY;
                        } else {
                            stack.setCount(count - used);
                        }
                        playerInv.setStack(slot, stack);
                    }
                    playerInv.markDirty();
                    for (ItemStack stack : results) {
                        player.getInventory().offerOrDrop(stack);
                    }
                } else {
                    break;
                }

            } while (player.isSneaking());
            //TODO
            //            CommonHooks.setCraftingPlayer(null);
            PREVIEW_CACHE.invalidate(getId() + "_" + section.index + "_" + player.getId() + (player.isSneaking() ? "_sneaking" : ""));
            return ActionResult.SUCCESS;
        }

        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
            MenuProvider.openHandledScreen(serverPlayer, section);
        }

        return ActionResult.SUCCESS;
    }

    public static BlueprintPreviewPacket getPreview(BlueprintEntity be, int index, ServerPlayerEntity player, boolean sneaking) {
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

    private static BlueprintPreviewPacket createPreview(BlueprintEntity be, int index, ServerPlayerEntity player, boolean sneaking) {
        BlueprintSection section = be.getSection(index);
        ItemStackHandler items = section.getItems();
        if (items.isEmpty()) {
            return BlueprintPreviewPacket.EMPTY;
        }
        ServerWorld world = player.getEntityWorld();
        PlayerInventory playerInv = player.getInventory();
        int size = playerInv.size();
        int[] stacksTaken = new int[size];
        List<FilterItemStack> requestedItems = new ArrayList<>(9);
        List<ItemStack> craftingStacks = new ArrayList<>(9);
        Object2IntLinkedOpenCustomHashMap<ItemStack> missingStacks = BlueprintPreviewPacket.createMap();
        Object2IntLinkedOpenCustomHashMap<ItemStack> availableStacks = BlueprintPreviewPacket.createMap();
        Search:
        for (int i = 0; i < 9; i++) {
            FilterItemStack requestedItem = FilterItemStack.of(items.getStack(i));
            if (requestedItem.isEmpty()) {
                requestedItems.add(null);
                craftingStacks.add(ItemStack.EMPTY);
                continue;
            }
            requestedItems.add(requestedItem);
            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = playerInv.getStack(slot);
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
            return new BlueprintPreviewPacket(availableStacks, missingStacks, items.getStack(9));
        }
        CraftingRecipeInput input = CraftingRecipeInput.create(3, 3, craftingStacks);
        Optional<ItemStack> result = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world)
            .map(entry -> entry.value().craft(input, world.getRegistryManager())).filter(stack -> !stack.isEmpty());
        if (result.isEmpty()) {
            return new BlueprintPreviewPacket(availableStacks, List.of(), ItemStack.EMPTY);
        }
        ItemStack resultStack = result.get();
        if (sneaking) {
            int max = resultStack.getMaxCount();
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
                            ItemStack stack = playerInv.getStack(slot);
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

    public BlueprintSection getSectionAt(Vec3d vec) {
        int index = 0;
        if (size > 1) {
            vec = VecHelper.rotate(vec, getYaw(), Axis.Y);
            vec = VecHelper.rotate(vec, -getPitch(), Axis.X);
            vec = vec.add(0.5, 0.5, 0);
            if (size == 3)
                vec = vec.add(1, 1, 0);
            int x = MathHelper.clamp(MathHelper.floor(vec.x), 0, size - 1);
            int y = MathHelper.clamp(MathHelper.floor(vec.y), 0, size - 1);
            index = x + y * size;
        }

        return getSection(index);
    }

    public Optional<NbtCompound> getRecipeCompound(int index) {
        return dataTracker.get(RECIPES).getCompound(Integer.toString(index));
    }

    public void putRecipeCompound(int index, NbtCompound compound) {
        NbtCompound recipes = dataTracker.get(RECIPES);
        recipes.put(Integer.toString(index), compound);
        dataTracker.set(RECIPES, recipes, true);
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
            return getRecipeCompound(index).flatMap(nbt -> nbt.get("Inventory", CreateCodecs.ITEM_LIST_CODEC)
                .map(items -> Couple.create(items.get(9), items.get(10)))).orElse(EMPTY_DISPLAY);
        }

        public ItemStackHandler getItems() {
            ItemStackHandler newInv = new ItemStackHandler(11);
            getRecipeCompound(index).ifPresentOrElse(
                nbt -> {
                    try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
                        ReadView view = NbtReadView.create(logging, getRegistryManager(), nbt);
                        newInv.readSlots(view);
                        inferredIcon = view.getBoolean("InferredIcon", false);
                    }
                }, () -> inferredIcon = false
            );
            return newInv;
        }

        public void save(ItemStackHandler inventory) {
            cachedDisplayItems = null;
            if (!getEntityWorld().isClient()) {
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
                    NbtWriteView view = NbtWriteView.create(logging, getRegistryManager());
                    inventory.writeSlots(view);
                    view.putBoolean("InferredIcon", inferredIcon);
                    putRecipeCompound(index, view.getNbt());
                }
            }
        }

        public boolean isEntityAlive() {
            return isAlive();
        }

        public World getBlueprintWorld() {
            return getEntityWorld();
        }

        @Override
        public BlueprintMenu createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
            extraData.writeVarInt(getId());
            extraData.writeVarInt(index);
            return new BlueprintMenu(id, inv, this);
        }

        @Override
        public Text getDisplayName() {
            return AllItems.CRAFTING_BLUEPRINT.getName();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return BlueprintEntity.this.canPlayerUse(player);
        }

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        Box box = getBoundingBox();

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
