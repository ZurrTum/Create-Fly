package com.zurrtum.create.client.mixin;

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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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
    protected List<FeatureRenderer<S, M>> features;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addFeature(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof BipedEntityModel) {
            BacktankFeatureRenderer<?, ?> renderer = new BacktankFeatureRenderer<>((FeatureRendererContext<? extends BipedEntityRenderState, BipedEntityModel<? super BipedEntityRenderState>>) this);
            features.add((FeatureRenderer<S, M>) renderer);
        }
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        if (!(renderer instanceof ShulkerEntityRenderer || renderer instanceof ArmorStandEntityRenderer)) {
            features.add(new HatFeatureRenderer<>(renderer));
        }
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void addHat(T entity, S renderState, float f, CallbackInfo ci) {
        if (features.stream().noneMatch(feature -> feature instanceof HatFeatureRenderer)) {
            return;
        }
        HatState state = (HatState) renderState;
        PartialModel hat = null;
        if (entity.hasVehicle()) {
            ItemStack stack = entity.getEquippedStack(EquipmentSlot.HEAD);
            Entity vehicle = entity.getVehicle();
            if (stack.isEmpty() && vehicle instanceof CarriageContraptionEntity cce && (cce.hasSchedule() || entity instanceof PlayerEntity) && cce.getContraption() instanceof CarriageContraption cc) {
                BlockPos seatOf = cc.getSeatOf(entity.getUuid());
                if (seatOf != null && cc.conductorSeats.get(seatOf) != null) {
                    hat = AllPartialModels.TRAIN_HAT;
                    state.create$updateHatInfo(entity);
                }
            }
            if (hat == null && vehicle instanceof SeatEntity) {
                World level = entity.getWorld();
                BlockPos pos = entity.getBlockPos();
                boolean find = false;
                Find:
                for (Direction d : Iterate.horizontalDirections) {
                    for (int y : Iterate.zeroAndOne) {
                        if (!(level.getBlockState(pos.offset(d).up(y)).getBlock() instanceof StockTickerBlock))
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
        } else if (entity instanceof ParrotEntity parrot && entity.getEquippedStack(EquipmentSlot.HEAD)
            .isEmpty() && AllSynchedDatas.PARROT_TRAIN_HAT.get(parrot)) {
            hat = AllPartialModels.TRAIN_HAT;
            state.create$updateHatInfo(entity);
        }
        state.create$setHat(hat);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if ((LivingEntityRenderer<T, S, M>) (Object) this instanceof PlayerEntityRenderer renderer && CardboardArmorHandlerClient.playerRendersAsBoxWhenSneaking(renderer,
            (PlayerEntityRenderState) state,
            matrixStack,
            vertexConsumerProvider,
            light
        )) {
            ci.cancel();
        }
    }
}
