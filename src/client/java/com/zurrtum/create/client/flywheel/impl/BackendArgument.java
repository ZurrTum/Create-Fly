package com.zurrtum.create.client.flywheel.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BackendArgument implements ArgumentType<Backend> {
    private static final List<String> EXAMPLES = List.of("off", "flywheel:off", "instancing");

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_BACKEND = new DynamicCommandExceptionType(arg -> {
        return Text.translatable("argument.flywheel_backend.id.unknown", arg);
    });

    public static final BackendArgument INSTANCE = new BackendArgument();
    public static final ConstantArgumentSerializer<BackendArgument> INFO = ConstantArgumentSerializer.of(() -> INSTANCE);

    @Override
    public Backend parse(StringReader reader) throws CommandSyntaxException {
        Identifier id = ResourceUtil.readFlywheelDefault(reader);
        Backend backend = Backend.REGISTRY.get(id);

        if (backend == null) {
            throw ERROR_UNKNOWN_BACKEND.createWithContext(reader, id.toString());
        }

        return backend;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Identifier id : Backend.REGISTRY.getAllIds()) {
            String idStr = id.toString();
            if (CommandSource.shouldSuggest(input, idStr) || CommandSource.shouldSuggest(input, id.getPath())) {
                builder.suggest(idStr);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
