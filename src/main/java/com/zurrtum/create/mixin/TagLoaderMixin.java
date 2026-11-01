package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import com.zurrtum.create.infrastructure.tag.FabricTagFile;
import com.zurrtum.create.infrastructure.tag.TagRemovalInternals;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.function.Consumer;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void loadRemoveEntries(
        ResourceManager resourceManager,
        CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir,
        @Local(name = "id") Identifier id,
        @Local(name = "parsedContents") TagFile parsedContents,
        @Local(name = "sourceId") String sourceId
    ) {
        for (TagEntry entry : FabricTagFile.getRemove(parsedContents)) {
            TagLoader.EntryWithSource entryWithSource = new TagLoader.EntryWithSource(entry, sourceId);
            TagRemovalInternals.addRemoveEntry(id, entryWithSource);
        }
        TagRemovalInternals.addTagSource(id, sourceId);
    }

    @WrapOperation(method = "lambda$build$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;tryBuildTag(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;"))
    private <T> Either<List<TagLoader.EntryWithSource>, List<T>> scopeIdToTryBuildTag(
        TagLoader<T> instance,
        TagEntry.Lookup<T> lookup,
        List<TagLoader.EntryWithSource> entries,
        Operation<Either<List<TagLoader.EntryWithSource>, List<T>>> original,
        @Local(argsOnly = true) Identifier id
    ) {
        // List is merged here to make
        return ScopedValue.where(TagRemovalInternals.TAG_ID_SCOPED_VALUE, id)
            .call(() -> original.call(instance, lookup, entries));
    }

    @WrapOperation(method = "tryBuildTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z"))
    private <T> boolean removeEntriesFromTags(
        TagEntry instance,
        TagEntry.Lookup<T> lookup,
        Consumer<T> output,
        Operation<Boolean> original,
        @Local(name = "values") SequencedSet<T> values,
        @Local(name = "entry") TagLoader.EntryWithSource entry
    ) {
        if (TagRemovalInternals.isEntryRemove(entry)) {
            instance.build(lookup, values::remove);
            return true;
        }

        return original.call(instance, lookup, output);
    }

    @ModifyArg(method = "lambda$build$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader$SortingEntry;<init>(Ljava/util/List;)V"))
    private static List<TagLoader.EntryWithSource> addTagRemovalReferencesToDependencySorter(
        List<TagLoader.EntryWithSource> entries,
        @Local(argsOnly = true) Identifier id
    ) {
        return TagRemovalInternals.mergeAddedAndRemovedEntries(id, entries);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    private void removeTagRemovalReferenceOnReplace(
        ResourceManager resourceManager,
        CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir,
        @Local(name = "id") Identifier id
    ) {
        TagRemovalInternals.removeTagRemovalReference(id);
    }

    @Inject(method = "build", at = @At("RETURN"))
    private <T> void removeTagRemovalReferencesWhenFinished(
        Map<Identifier, List<TagLoader.EntryWithSource>> builders,
        CallbackInfoReturnable<Map<Identifier, List<T>>> cir
    ) {
        TagRemovalInternals.removeTagRemovalReferences();
    }
}
