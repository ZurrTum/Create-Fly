package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

public class StandardBogeyBlock extends AbstractBogeyBlock<StandardBogeyBlockEntity> implements IBE<StandardBogeyBlockEntity>, ProperWaterloggedBlock, SpecialBlockItemRequirement {

    public StandardBogeyBlock(Settings props, BogeySize size) {
        super(props, size);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    public static StandardBogeyBlock small(Settings props) {
        return new StandardBogeyBlock(props, AllBogeySizes.SMALL);
    }

    public static StandardBogeyBlock large(Settings props) {
        return new StandardBogeyBlock(props, AllBogeySizes.LARGE);
    }

    @Override
    public Identifier getTrackType(BogeyStyle style) {
        return AllTrackMaterials.ANDESITE.getId();
    }

    @Override
    public double getWheelPointSpacing() {
        return 2;
    }

    @Override
    public double getWheelRadius() {
        return (size == AllBogeySizes.LARGE ? 12.5 : 6.5) / 16d;
    }

    @Override
    public Vec3d getConnectorAnchorOffset() {
        return new Vec3d(0, 7 / 32f, 1);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllBogeyStyles.STANDARD;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.RAILWAY_CASING.getDefaultStack();
    }

    @Override
    public Class<StandardBogeyBlockEntity> getBlockEntityClass() {
        return StandardBogeyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StandardBogeyBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BOGEY;
    }

}
