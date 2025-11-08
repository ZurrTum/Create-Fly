package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.ContraptionHandler;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.MovementType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private World world;
    @Shadow
    private Vec3d pos;
    @Shadow
    private float nextStepSoundDistance;
    @Shadow
    @Final
    protected Random random;
    @Shadow
    private EntityDimensions dimensions;
    @Unique
    private boolean inModFluid;

    @Shadow
    protected abstract void playStepSound(BlockPos pos, BlockState state);

    @Shadow
    protected abstract float calculateNextStepSoundDistance();

    @Inject(method = "updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z", at = @At("HEAD"))
    private void clear(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        inModFluid = false;
    }

    @Inject(method = "updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getHeight(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    private void checkFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir, @Local FluidState state) {
        if (!inModFluid) {
            inModFluid = state.getFluid() instanceof FlowableFluid;
        }
    }

    @Inject(method = "onSwimmingStart()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticleClient(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), cancellable = true)
    private void cancelEffect(CallbackInfo ci) {
        if (inModFluid) {
            ci.cancel();
        }
    }

    @Unique
    private Stream<AbstractContraptionEntity> create$getIntersectionContraptionsStream() {
        return (world.isClient ? ContraptionHandlerClient.loadedContraptions : ContraptionHandler.loadedContraptions).get(world).values().stream()
            .map(Reference::get).filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey((Entity) (Object) this));
    }

    @Unique
    private Set<AbstractContraptionEntity> create$getIntersectingContraptions() {
        Set<AbstractContraptionEntity> contraptions = create$getIntersectionContraptionsStream().collect(Collectors.toSet());

        contraptions.addAll(world.getNonSpectatingEntities(AbstractContraptionEntity.class, ((Entity) (Object) this).getBoundingBox().expand(1f)));
        return contraptions;
    }

    @Unique
    private void create$forCollision(Vec3d worldPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
        create$getIntersectingContraptions().forEach(cEntity -> {
            Vec3d localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

            BlockPos blockPos = BlockPos.ofFloored(localPos);
            Contraption contraption = cEntity.getContraption();
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(blockPos);

            if (info != null) {
                BlockState blockstate = info.state();
                action.accept(contraption, blockstate, blockPos);
            }
        });
    }

    // involves block step sounds on contraptions
    // injecting before `!blockstate1.isAir(this.world, blockpos)`
    // `if (this.moveDist > this.nextStep && !blockstate1.isAir())
    @Inject(method = "applyMoveEffect(Lnet/minecraft/entity/Entity$MoveEffect;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z", ordinal = 0))
    private void create$contraptionStepSounds(
        Entity.MoveEffect moveEffect,
        Vec3d movement,
        BlockPos landingPos,
        BlockState landingState,
        CallbackInfo ci
    ) {
        Vec3d worldPos = pos.add(0, -0.2, 0);
        MutableBoolean stepped = new MutableBoolean(false);

        create$forCollision(
            worldPos, (contraption, state, pos) -> {
                playStepSound(pos, state);
                stepped.setTrue();
            }
        );

        if (stepped.booleanValue())
            nextStepSoundDistance = calculateNextStepSoundDistance();
    }

    // involves client-side view bobbing animation on contraptions
    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "TAIL"))
    private void create$onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (!world.isClient)
            return;
        Entity self = (Entity) (Object) this;
        if (self.isOnGround())
            return;
        if (self.hasVehicle())
            return;

        Vec3d worldPos = pos.add(0, -0.2, 0);
        boolean onAtLeastOneContraption = create$getIntersectionContraptionsStream().anyMatch(cEntity -> {
            Vec3d localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

            BlockPos blockPos = BlockPos.ofFloored(localPos);
            Contraption contraption = cEntity.getContraption();
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(blockPos);

            if (info == null)
                return false;

            cEntity.registerColliding(self);
            return true;
        });

        if (!onAtLeastOneContraption)
            return;

        self.setOnGround(true);
        AllSynchedDatas.CONTRAPTION_GROUNDED.set(self, true);
    }

    @Inject(method = "spawnSprintingParticles()V", at = @At(value = "TAIL"))
    private void create$onSpawnSprintParticle(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Vec3d worldPos = pos.add(0, -0.2, 0);

        create$forCollision(
            worldPos, (contraption, state, pos) -> {
                if (state.getRenderType() != BlockRenderType.INVISIBLE) {
                    Vec3d speed = self.getVelocity();
                    world.addParticleClient(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                        self.getX() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.width(),
                        self.getY() + 0.1D,
                        self.getZ() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.height(),
                        speed.x * -4.0D,
                        1.5D,
                        speed.z * -4.0D
                    );
                }
            }
        );
    }

    @WrapOperation(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canMoveVoluntarily()Z"))
    private boolean move(Entity instance, Operation<Boolean> original) {
        if (original.call(instance)) {
            return true;
        }
        return world instanceof PonderLevel;
    }
}
