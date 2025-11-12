package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.equipment.armor.BacktankFeatureRenderer;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorHandlerClient;
import com.zurrtum.create.client.content.equipment.hats.HatFeatureRenderer;
import com.zurrtum.create.client.content.equipment.hats.HatState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    @Final
    protected List<RenderLayer<S, M>> layers;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addFeature(EntityRendererProvider.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof HumanoidModel) {
            BacktankFeatureRenderer<?, ?> renderer = new BacktankFeatureRenderer<>((RenderLayerParent<? extends HumanoidRenderState, HumanoidModel<? super HumanoidRenderState>>) this);
            layers.add((RenderLayer<S, M>) renderer);
        }
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        if (!(renderer instanceof ShulkerRenderer || renderer instanceof ArmorStandRenderer)) {
            layers.add(new HatFeatureRenderer<>(renderer));
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void addHat(T entity, S renderState, float f, CallbackInfo ci) {
        if (layers.stream().noneMatch(feature -> feature instanceof HatFeatureRenderer)) {
            return;
        }
        HatState state = (HatState) renderState;
        PartialModel hat = null;
        if (entity.isPassenger()) {
            ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
            Entity vehicle = entity.getVehicle();
            if (stack.isEmpty() && vehicle instanceof CarriageContraptionEntity cce && (cce.hasSchedule() || entity instanceof Player) && cce.getContraption() instanceof CarriageContraption cc) {
                BlockPos seatOf = cc.getSeatOf(entity.getUUID());
                if (seatOf != null && cc.conductorSeats.get(seatOf) != null) {
                    hat = AllPartialModels.TRAIN_HAT;
                    state.create$updateHatInfo(entity);
                }
            }
            if (hat == null && vehicle instanceof SeatEntity) {
                Level level = entity.level();
                BlockPos pos = entity.blockPosition();
                boolean find = false;
                Find:
                for (Direction d : Iterate.horizontalDirections) {
                    for (int y : Iterate.zeroAndOne) {
                        if (!(level.getBlockState(pos.relative(d).above(y)).getBlock() instanceof StockTickerBlock))
                            continue;
                        if (find) {
                            find = false;
                            break Find;
                        }
                        find = true;
                    }
                }
                if (find) {
                    hat = AllPartialModels.LOGISTICS_HAT;
                    state.create$updateHatInfo(entity);
                }
            }
        } else if (entity instanceof Parrot parrot && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && AllSynchedDatas.PARROT_TRAIN_HAT.get(
            parrot)) {
            hat = AllPartialModels.TRAIN_HAT;
            state.create$updateHatInfo(entity);
        }
        state.create$setHat(hat);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void render(S state, PoseStack matrixStack, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if ((LivingEntityRenderer<T, S, M>) (Object) this instanceof AvatarRenderer<?> renderer && CardboardArmorHandlerClient.playerRendersAsBoxWhenSneaking(renderer,
            (AvatarRenderState) state,
            matrixStack,
            queue
        )) {
            ci.cancel();
        }
    }
}
