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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.*;

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
    public Box getRenderBoundingBox() {
        Box bb = super.getRenderBoundingBox().stretch(0, 1, 0);
        if (target != null)
            bb = bb.union(new Box(BlockPos.ofFloored(target.getExactTargetLocation(this, world, pos)))).expand(0.5);
        return bb;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (world.isClient() || isAnimationInProgress())
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
        for (int i = 0, size = inventory.size(); i < size; i++)
            if (inventory.getStack(i).isEmpty()) {
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

        if (world.isClient() && wasOpen && manualOpenAnimationProgress.getValue() == 0)
            getBehaviour(FrogportAudioBehaviour.TYPE).close(world, pos);

        if (!isAnimationInProgress())
            return;

        animationProgress.tickChaser();

        float value = animationProgress.getValue();
        if (currentlyDepositing) {
            if (!world.isClient() || isVirtual()) {
                if (value > 0.5 && animatedPackage != null) {
                    if (target == null || !target.depositImmediately() && !target.export(world, pos, animatedPackage, false))
                        drop(animatedPackage);
                    animatedPackage = null;
                }
            } else {
                if (value > 0.7 && animatedPackage != null)
                    animatedPackage = null;
                if (animationProgress.getValue(0) < 0.2 && value > 0.2) {
                    Vec3d v = target.getExactTargetLocation(this, world, pos);
                    world.playSoundClient(v.x, v.y, v.z, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.BLOCKS, 0.25f, 1.2f, false);
                }
            }
        }

        if (value < 1)
            return;

        anticipationProgress.startWithValue(0);
        animationProgress.startWithValue(0);
        if (world.isClient()) {
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

        if (deposit && (target == null || target.depositImmediately() && !target.export(world, pos, box.copy(), false)))
            return;

        animationProgress.startWithValue(0);
        animationProgress.chase(1, 0.1, Chaser.LINEAR);
        animatedPackage = box;
        currentlyDepositing = deposit;

        if (world != null && !deposit && !world.isClient())
            award(AllAdvancements.FROGPORT);

        if (world != null && world.isClient()) {
            FrogportAudioBehaviour sounds = getBehaviour(FrogportAudioBehaviour.TYPE);
            sounds.open(world, pos);

            if (currentlyDepositing) {
                sounds.depositPackage(world, pos);

            } else {
                sounds.catchPackage(world, pos);
                Vec3d vec = target.getExactTargetLocation(this, world, pos);
                if (vec != null)
                    for (int i = 0; i < 5; i++)
                        world.addParticleClient(
                            new BlockStateParticleEffect(ParticleTypes.BLOCK, AllBlocks.ROPE.getDefaultState()),
                            vec.x,
                            vec.y - world.random.nextFloat() * 0.25,
                            vec.z,
                            0,
                            0,
                            0
                        );
            }
        }

        if (world != null && !world.isClient()) {
            world.markDirty(pos);
            sendData();
        }
    }

    protected void tryPushingToAdjacentInventories() {
        failedLastExport = false;
        if (inventory.isEmpty())
            return;
        Inventory handler = getAdjacentInventory(Direction.DOWN);
        if (handler == null)
            return;

        boolean dirty = false;
        for (int i = 0, size = inventory.size(); i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (inventory.canExtract(i, stack, null)) {
                int insert = handler.insertExist(stack, 1);
                if (insert == 1) {
                    int count = stack.getCount();
                    if (count == 1) {
                        inventory.setStack(i, ItemStack.EMPTY);
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
            inventory.markDirty();
            world.markDirty(pos);
        }
    }

    @Override
    protected void onOpenChange(boolean open) {
    }

    public void tryPullingFromOwnAndAdjacentInventories() {
        if (isAnimationInProgress())
            return;
        if (target == null || !target.export(world, pos, PackageStyles.getDefaultBox(), true))
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
            Inventory handler = getAdjacentInventory(side);
            if (handler == null)
                continue;
            if (tryPullingFrom(handler))
                return;
        }
    }

    public boolean tryPullingFrom(Inventory handler) {
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

    protected Inventory getAdjacentInventory(Direction side) {
        BlockPos pos = this.pos.offset(side);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
            return null;
        return ItemHelper.getInventory(world, pos, null, blockEntity, side.getOpposite());
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putFloat("PlacedYaw", passiveYaw);
        if (animatedPackage != null && isAnimationInProgress()) {
            view.put("AnimatedPackage", ItemStack.CODEC, animatedPackage);
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
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        passiveYaw = view.getFloat("PlacedYaw", 0);
        failedLastExport = view.getBoolean("FailedLastExport", false);
        goggles = view.getBoolean("Goggles", false);
        if (!clientPacket)
            animatedPackage = null;
        view.read("AnimatedPackage", ItemStack.CODEC).ifPresent(stack -> {
            deferAnimationInward = view.getBoolean("Deposit", false);
            deferAnimationStart = stack;
        });
        if (clientPacket && view.getBoolean("Anticipate", false))
            anticipate();
    }

    public float getYaw() {
        if (target == null)
            return passiveYaw;
        Vec3d diff = target.getExactTargetLocation(this, world, pos).subtract(Vec3d.ofCenter(pos));
        return (float) (MathHelper.atan2(diff.x, diff.z) * MathHelper.DEGREES_PER_RADIAN) + 180;
    }

    @Override
    protected void onOpenedManually() {
        if (world.isClient())
            getBehaviour(FrogportAudioBehaviour.TYPE).open(world, pos);
    }

    @Override
    public ActionResult use(PlayerEntity player) {
        if (player == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (!goggles) {
            ItemStack mainHandItem = player.getMainHandStack();
            if (mainHandItem.isOf(AllItems.GOGGLES)) {
                goggles = true;
                if (!world.isClient()) {
                    notifyUpdate();
                    world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(), SoundCategory.BLOCKS, 0.5f, 1.0f);
                }
                return ActionResult.SUCCESS;
            }
        }

        return super.use(player);
    }

}
