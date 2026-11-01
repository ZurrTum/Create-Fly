package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.tag.FabricTagFile;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

@Mixin(TagFile.class)
public class TagFileMixin implements FabricTagFile {
    @Unique
    private List<TagEntry> remove = Collections.emptyList();

    @WrapOperation(method = "lambda$static$0(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/Products$P2;apply(Lcom/mojang/datafixers/kinds/Applicative;Ljava/util/function/BiFunction;)Lcom/mojang/datafixers/kinds/App;"))
    private static App<RecordCodecBuilder.Mu<TagFile>, TagFile> initCodec(
        Products.P2<RecordCodecBuilder.Mu<TagFile>, List<TagEntry>, Boolean> p2,
        Applicative<RecordCodecBuilder.Mu<TagFile>, ?> i,
        BiFunction<?, ?, ?> function,
        Operation<?> original
    ) {
        return p2.and(TagEntry.CODEC.listOf().lenientOptionalFieldOf("fabric:remove", Collections.emptyList())
            .forGetter(FabricTagFile::getRemove)).apply(i, FabricTagFile::create);
    }

    @Override
    public @NonNull List<TagEntry> create$remove() {
        return remove;
    }

    @Override
    public void create$setRemove(List<TagEntry> remove) {
        this.remove = remove;
    }
}
