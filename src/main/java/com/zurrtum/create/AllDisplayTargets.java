package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayBoardTarget;
import com.zurrtum.create.content.redstone.displayLink.target.LecternDisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.target.NixieTubeDisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.target.SignDisplayTarget;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.zurrtum.create.Create.MOD_ID;

public class AllDisplayTargets {
    public static final SignDisplayTarget SIGN = register("sign", SignDisplayTarget::new);
    public static final LecternDisplayTarget LECTERN = register("lectern", LecternDisplayTarget::new);
    public static final DisplayBoardTarget DISPLAY_BOARD = register("display_board", DisplayBoardTarget::new);
    public static final NixieTubeDisplayTarget NIXIE_TUBE = register("nixie_tube", NixieTubeDisplayTarget::new);

    private static <T extends DisplayTarget> T register(String id, Supplier<T> factory) {
        return Registry.register(CreateRegistries.DISPLAY_TARGET, ResourceLocation.fromNamespaceAndPath(MOD_ID, id), factory.get());
    }

    public static void register(DisplayTarget display, Block... blocks) {
        for (Block block : blocks) {
            DisplayTarget.BY_BLOCK.register(block, display);
        }
    }

    public static void register(DisplayTarget display, BlockEntityType<?> type) {
        DisplayTarget.BY_BLOCK_ENTITY.register(type, display);
    }

    public static void register() {
        register(SIGN, BlockEntityType.SIGN);
        register(LECTERN, BlockEntityType.LECTERN);
        register(DISPLAY_BOARD, AllBlocks.DISPLAY_BOARD);
        register(NIXIE_TUBE, AllBlockEntityTypes.NIXIE_TUBE);
    }
}
