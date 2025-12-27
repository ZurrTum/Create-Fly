package com.zurrtum.create.content.logistics.packagePort.frogport;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerItemHandler;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.audio.FrogportAudioBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FrogportBlockEntity extends PackagePortBlockEntity {

    public ItemStack animatedPackage;
    public LerpedFloat manualOpenAnimationProgress;
    public LerpedFloat animationProgress;
    public LerpedFloat anticipationProgress;
    public boolean currentlyDepositing;
    public boolean goggles;

    public boolean sendAnticipate;

    public float passiveYaw;

    public boolean failedLastExport;

    private ItemStack deferAnimationStart;
    private boolean deferAnimationInward;

    public FrogportBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PACKAGE_FROGPORT, pos, state);
        animationProgress = LerpedFloat.linear();
        anticipationProgress = LerpedFloat.linear();
        manualOpenAnimationProgress = LerpedFloat.linear().startWithValue(0).chase(0, 0.35, Chaser.LINEAR);
        goggles = false;
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.FROGPORT);
    }

    public boolean isAnimationInProgress() {
        return animationProgress.getChaseTarget() == 1;
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB bb = super.getRenderBoundingBox().expandTowards(0, 1, 0);
        if (target != null)
            bb = bb.minmax(new AABB(BlockPos.containing(target.getExactTargetLocation(this, level, worldPosition)))).inflate(0.5);
        return bb;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide() || isAnimationInProgress())
            return;

        boolean prevFail = failedLastExport;
        tryPushingToAdjacentInventories();
        tryPullingFromOwnAndAdjacentInventories();

        if (failedLastExport != prevFail)
            sendData();
    }

    public void sendAnticipate() {
        if (isAnimationInProgress())
            return;
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++)
            if (inventory.getItem(i).isEmpty()) {
                sendAnticipate = true;
                sendData();
                return;
            }
    }

    public void anticipate() {
        anticipationProgress.chase(1, 0.1, Chaser.LINEAR);
    }

    @Override
    public void tick() {
        super.tick();

        if (deferAnimationStart != null) {
            startAnimation(deferAnimationStart, deferAnimationInward);
            deferAnimationStart = null;
        }

        if (anticipationProgress.getValue() == 1)
            anticipationProgress.startWithValue(0);

        manualOpenAnimationProgress.updateChaseTarget(openTracker.openCount > 0 ? 1 : 0);
        boolean wasOpen = manualOpenAnimationProgress.getValue() > 0;

        anticipationProgress.tickChaser();
        manualOpenAnimationProgress.tickChaser();

        if (level.isClientSide() && wasOpen && manualOpenAnimationProgress.getValue() == 0)
            getBehaviour(FrogportAudioBehaviour.TYPE).close(level, worldPosition);

        if (!isAnimationInProgress())
            return;

        animationProgress.tickChaser();

        float value = animationProgress.getValue();
        if (currentlyDepositing) {
            if (!level.isClientSide() || isVirtual()) {
                if (value > 0.5 && animatedPackage != null) {
                    if (target == null || !target.depositImmediately() && !target.export(level, worldPosition, animatedPackage, false))
                        drop(animatedPackage);
                    animatedPackage = null;
                }
            } else {
                if (value > 0.7 && animatedPackage != null)
                    animatedPackage = null;
                if (animationProgress.getValue(0) < 0.2 && value > 0.2) {
                    Vec3 v = target.getExactTargetLocation(this, level, worldPosition);
                    level.playLocalSound(v.x, v.y, v.z, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.25f, 1.2f, false);
                }
            }
        }

        if (value < 1)
            return;

        anticipationProgress.startWithValue(0);
        animationProgress.startWithValue(0);
        if (level.isClientSide()) {
            //			sounds.close(level, worldPosition);
            animatedPackage = null;
            return;
        }

        if (!currentlyDepositing) {
            int count = animatedPackage.getCount();
            inventory.sendMode();
            int insert = inventory.insert(animatedPackage, count);
            inventory.receiveMode();
            if (insert != count) {
                if (insert == 0) {
                    drop(animatedPackage);
                } else {
                    drop(animatedPackage.copyWithCount(count - insert));
                }
            }
        }

        animatedPackage = null;
    }

    public void startAnimation(ItemStack box, boolean deposit) {
        if (!PackageItem.isPackage(box))
            return;

        if (deposit && (target == null || target.depositImmediately() && !target.export(level, worldPosition, box.copy(), false)))
            return;

        animationProgress.startWithValue(0);
        animationProgress.chase(1, 0.1, Chaser.LINEAR);
        animatedPackage = box;
        currentlyDepositing = deposit;

        if (level != null && !deposit && !level.isClientSide())
            award(AllAdvancements.FROGPORT);

        if (level != null && level.isClientSide()) {
            FrogportAudioBehaviour sounds = getBehaviour(FrogportAudioBehaviour.TYPE);
            sounds.open(level, worldPosition);

            if (currentlyDepositing) {
                sounds.depositPackage(level, worldPosition);

            } else {
                sounds.catchPackage(level, worldPosition);
                Vec3 vec = target.getExactTargetLocation(this, level, worldPosition);
                if (vec != null)
                    for (int i = 0; i < 5; i++)
                        level.addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, AllBlocks.ROPE.defaultBlockState()),
                            vec.x,
                            vec.y - level.random.nextFloat() * 0.25,
                            vec.z,
                            0,
                            0,
                            0
                        );
            }
        }

        if (level != null && !level.isClientSide()) {
            level.blockEntityChanged(worldPosition);
            sendData();
        }
    }

    protected void tryPushingToAdjacentInventories() {
        failedLastExport = false;
        if (inventory.isEmpty())
            return;
        Container handler = getAdjacentInventory(Direction.DOWN);
        if (handler == null)
            return;

        boolean dirty = false;
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (inventory.canTakeItemThroughFace(i, stack, null)) {
                int insert = handler.insertExist(stack, 1);
                if (insert == 1) {
                    int count = stack.getCount();
                    if (count == 1) {
                        inventory.setItem(i, ItemStack.EMPTY);
                    } else {
                        stack.setCount(count - 1);
                    }
                    dirty = true;
                } else {
                    failedLastExport = true;
                }
            }
        }
        if (dirty) {
            inventory.setChanged();
            level.blockEntityChanged(worldPosition);
        }
    }

    @Override
    protected void onOpenChange(boolean open) {
    }

    public void tryPullingFromOwnAndAdjacentInventories() {
        if (isAnimationInProgress())
            return;
        if (target == null || !target.export(level, worldPosition, PackageStyles.getDefaultBox(), true))
            return;
        inventory.sendMode();
        ItemStack stack = inventory.extractAny();
        inventory.receiveMode();
        if (!stack.isEmpty()) {
            startAnimation(stack, true);
            return;
        }
        for (Direction side : Iterate.directions) {
            if (side != Direction.DOWN)
                continue;
            Container handler = getAdjacentInventory(side);
            if (handler == null)
                continue;
            if (tryPullingFrom(handler))
                return;
        }
    }

    public boolean tryPullingFrom(Container handler) {
        ItemStack extract = handler.extract(stack -> {
            if (!PackageItem.isPackage(stack))
                return false;
            String filterString = getFilterString();
            return filterString == null || handler instanceof PackagerItemHandler || !PackageItem.matchAddress(stack, filterString);
        });
        if (extract.isEmpty())
            return false;
        startAnimation(extract, true);
        return true;

    }

    protected Container getAdjacentInventory(Direction side) {
        BlockPos pos = this.worldPosition.relative(side);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
            return null;
        return ItemHelper.getInventory(level, pos, null, blockEntity, side.getOpposite());
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putFloat("PlacedYaw", passiveYaw);
        if (animatedPackage != null && isAnimationInProgress()) {
            view.store("AnimatedPackage", ItemStack.CODEC, animatedPackage);
            view.putBoolean("Deposit", currentlyDepositing);
        }
        if (sendAnticipate) {
            sendAnticipate = false;
            view.putBoolean("Anticipate", true);
        }
        if (failedLastExport)
            view.putBoolean("FailedLastExport", true);
        if (goggles)
            view.putBoolean("Goggles", true);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        passiveYaw = view.getFloatOr("PlacedYaw", 0);
        failedLastExport = view.getBooleanOr("FailedLastExport", false);
        goggles = view.getBooleanOr("Goggles", false);
        if (!clientPacket)
            animatedPackage = null;
        view.read("AnimatedPackage", ItemStack.CODEC).ifPresent(stack -> {
            deferAnimationInward = view.getBooleanOr("Deposit", false);
            deferAnimationStart = stack;
        });
        if (clientPacket && view.getBooleanOr("Anticipate", false))
            anticipate();
    }

    public float getYaw() {
        if (target == null)
            return passiveYaw;
        Vec3 diff = target.getExactTargetLocation(this, level, worldPosition).subtract(Vec3.atCenterOf(worldPosition));
        return (float) (Mth.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) + 180;
    }

    @Override
    protected void onOpenedManually() {
        if (level.isClientSide())
            getBehaviour(FrogportAudioBehaviour.TYPE).open(level, worldPosition);
    }

    @Override
    public InteractionResult use(Player player) {
        if (player == null)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!goggles) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.is(AllItems.GOGGLES)) {
                goggles = true;
                if (!level.isClientSide()) {
                    notifyUpdate();
                    level.playSound(null, worldPosition, SoundEvents.ARMOR_EQUIP_GOLD.value(), SoundSource.BLOCKS, 0.5f, 1.0f);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(player);
    }

}
