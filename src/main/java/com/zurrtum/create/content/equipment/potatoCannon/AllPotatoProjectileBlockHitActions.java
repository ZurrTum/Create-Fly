package com.zurrtum.create.content.equipment.potatoCannon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.block.Block;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPotatoProjectileBlockHitActions {
    public static void register() {
        register("plant_crop", PlantCrop.CODEC);
        register("place_block_on_ground", PlaceBlockOnGround.CODEC);
    }

    private static void register(String name, MapCodec<? extends PotatoProjectileBlockHitAction> codec) {
        Registry.register(CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION, Identifier.of(MOD_ID, name), codec);
    }

    public record PlantCrop(RegistryEntry<Block> cropBlock) implements PotatoProjectileBlockHitAction {
        public static final MapCodec<PlantCrop> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Registries.BLOCK.getEntryCodec()
            .fieldOf("block").forGetter(PlantCrop::cropBlock)).apply(instance, PlantCrop::new));

        @SuppressWarnings("deprecation")
        public PlantCrop(Block cropBlock) {
            this(cropBlock.getRegistryEntry());
        }

        @Override
        public boolean execute(WorldAccess level, ItemStack projectile, BlockHitResult ray) {
            if (level.isClient())
                return true;

            BlockPos hitPos = ray.getBlockPos();
            if (level instanceof World l && !l.isPosLoaded(hitPos))
                return true;
            Direction face = ray.getSide();
            if (face != Direction.UP)
                return false;
            BlockPos placePos = hitPos.offset(face);
            if (!level.getBlockState(placePos).isReplaceable())
                return false;
            //TODO
            //            if (!(cropBlock.value() instanceof SpecialPlantable specialPlantable))
            //                return false;
            //            if (specialPlantable.canPlacePlantAtPosition(projectile, level, placePos, null))
            //                specialPlantable.spawnPlantAtPosition(projectile, level, placePos, null);
            return true;
        }

        @Override
        public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
            return CODEC;
        }
    }

    public record PlaceBlockOnGround(RegistryEntry<Block> block) implements PotatoProjectileBlockHitAction {
        public static final MapCodec<PlaceBlockOnGround> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Registries.BLOCK.getEntryCodec()
            .fieldOf("block").forGetter(PlaceBlockOnGround::block)).apply(instance, PlaceBlockOnGround::new));

        @SuppressWarnings("deprecation")
        public PlaceBlockOnGround(Block block) {
            this(block.getRegistryEntry());
        }

        @Override
        public boolean execute(WorldAccess levelAccessor, ItemStack projectile, BlockHitResult ray) {
            if (levelAccessor.isClient())
                return true;

            BlockPos hitPos = ray.getBlockPos();
            if (levelAccessor instanceof World l && !l.isPosLoaded(hitPos))
                return true;
            Direction face = ray.getSide();
            BlockPos placePos = hitPos.offset(face);
            if (!levelAccessor.getBlockState(placePos).isReplaceable())
                return false;

            if (face == Direction.UP) {
                levelAccessor.setBlockState(placePos, block.value().getDefaultState(), Block.NOTIFY_ALL);
            } else if (levelAccessor instanceof World level) {
                double y = ray.getBlockPos().getY() - 0.5;
                if (!level.isAir(placePos.up()))
                    y = Math.min(y, placePos.getY());
                if (!level.isAir(placePos.down()))
                    y = Math.max(y, placePos.getY());

                FallingBlockEntity falling = new FallingBlockEntity(
                    level,
                    placePos.getX() + 0.5,
                    y,
                    placePos.getZ() + 0.5,
                    block.value().getDefaultState()
                );
                falling.timeFalling = 1;
                level.spawnEntity(falling);
            }

            return true;
        }

        @Override
        public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
            return CODEC;
        }
    }
}
