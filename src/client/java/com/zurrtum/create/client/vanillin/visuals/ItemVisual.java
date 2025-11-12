package com.zurrtum.create.client.vanillin.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.InstanceRecycler;
import com.zurrtum.create.client.vanillin.item.ItemModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

public class ItemVisual extends AbstractEntityVisual<ItemEntity> implements SimpleDynamicVisual {

    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance);

    private final PoseStack pPoseStack = new PoseStack();
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    private ItemModel itemModel;
    private Model currentModel;
    private ItemStack currentStack;

    private final InstanceRecycler<TransformedInstance> instances;

    public ItemVisual(VisualizationContext ctx, ItemEntity entity, float partialTick) {
        super(ctx, entity, partialTick);

        updateModel(entity.getItem());

        instances = new InstanceRecycler<>(this::getInstance);

        animate(partialTick);
    }

    public static boolean isSupported(ItemEntity entity) {
        if (entity.getClass() != ItemEntity.class) {
            return false;
        }
        return ItemModels.isSupported(entity.getItem(), ItemDisplayContext.GROUND);
    }

    @Override
    public void beginFrame(Context ctx) {
        if (!isVisible(ctx.frustum())) {
            return;
        }

        ItemStack stack = entity.getItem();
        if (!ItemStack.isSameItemSameComponents(currentStack, stack)) {
            updateModel(stack);
            instances.delete();
        }
        animate(ctx.partialTick());
    }

    private void updateModel(ItemStack stack) {
        currentStack = stack.copy();
        itemModel = ItemModels.getModel(currentStack);
        currentModel = ItemModels.get(level, currentStack, ItemDisplayContext.GROUND);
    }

    private TransformedInstance getInstance() {
        return visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, currentModel).createInstance();
    }

    private void animate(float partialTick) {
        pPoseStack.setIdentity();
        TransformStack.of(pPoseStack).translate(getVisualPosition(partialTick));

        instances.resetCount();

        ItemStack itemstack = entity.getItem();
        if (itemstack.isEmpty()) {
            return;
        }

        itemRenderState.clear();
        ItemModelResolver manager = Minecraft.getInstance().getItemModelResolver();
        ClientLevel world = entity.level() instanceof ClientLevel clientWorld ? clientWorld : null;
        itemRenderState.displayContext = ItemDisplayContext.GROUND;
        itemModel.update(itemRenderState, itemstack, manager, ItemDisplayContext.GROUND, world, null, entity.getId());

        if (itemRenderState.isEmpty()) {
            return;
        }

        float age = entity.getAge() + partialTick;
        AABB box = itemRenderState.getModelBoundingBox();
        float f = -((float) box.minY) + 0.0625F;
        if (shouldBob()) {
            float g = Mth.sin(age / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
            pPoseStack.translate(0.0F, g + f, 0.0F);
        } else {
            pPoseStack.translate(0.0F, f, 0.0F);
        }
        float h = ItemEntity.getSpin(age, entity.bobOffs);
        pPoseStack.mulPose(Axis.YP.rotation(h));

        int i = this.getRenderAmount(itemstack);
        int seed = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        var random = RANDOM.get();
        random.setSeed(seed);

        int light = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, entity.blockPosition()),
            level.getBrightness(LightLayer.SKY, entity.blockPosition())
        );

        float lengthZ = (float) box.getZsize();
        if (lengthZ > 0.0625F) {
            instances.get().setTransform(pPoseStack.last()).light(light).setChanged();

            if (shouldSpreadItems()) {
                for (int j = 1; j < i; j++) {
                    pPoseStack.pushPose();
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    pPoseStack.translate(x, y, z);
                    instances.get().setTransform(pPoseStack.last()).light(light).setChanged();
                    pPoseStack.popPose();
                }
            }
        } else {
            float l = lengthZ * 1.5F;
            pPoseStack.translate(0.0F, 0.0F, -(l * (i - 1) / 2.0F));
            instances.get().setTransform(pPoseStack.last()).light(light).setChanged();
            pPoseStack.translate(0.0F, 0.0F, l);

            if (shouldSpreadItems()) {
                for (int m = 1; m < i; m++) {
                    pPoseStack.pushPose();
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    pPoseStack.translate(x, y, 0.0F);
                    instances.get().setTransform(pPoseStack.last()).light(light).setChanged();
                    pPoseStack.popPose();
                    pPoseStack.translate(0.0F, 0.0F, l);
                }
            }
        }

        instances.discardExtra();
    }

    protected int getRenderAmount(ItemStack pStack) {
        int i = 1;
        if (pStack.getCount() > 48) {
            i = 5;
        } else if (pStack.getCount() > 32) {
            i = 4;
        } else if (pStack.getCount() > 16) {
            i = 3;
        } else if (pStack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    /**
     * @return If items should spread out when rendered in 3D
     */
    public boolean shouldSpreadItems() {
        return true;
    }

    /**
     * @return If items should have a bob effect
     */
    public boolean shouldBob() {
        return true;
    }

    @Override
    protected void _delete() {
        instances.delete();
    }

}
