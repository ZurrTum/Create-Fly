package com.zurrtum.create.client.flywheel.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zurrtum.create.catnip.config.ConfigBase.ConfigBool;
import com.zurrtum.create.catnip.config.ConfigBase.ConfigEnum;
import com.zurrtum.create.catnip.config.ConfigBase.ConfigString;
import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.BackendManager;
import com.zurrtum.create.client.flywheel.backend.BackendDebugFlags;
import com.zurrtum.create.client.flywheel.backend.compile.LightSmoothness;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.DebugMode;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.FrameUniforms;
import com.zurrtum.create.client.infrastructure.command.ClientCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class FlwCommands {
    private FlwCommands() {
    }

    public static void registerClientCommands(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        LiteralArgumentBuilder<ClientSuggestionProvider> command = ClientCommand.literal("flywheel");
        command.then(ClientCommand.literal(
            "backend", context -> {
                Backend backend = BackendManager.currentBackend();
                String idStr = Backend.REGISTRY.getIdOrThrow(backend).toString();
                context.getSource().minecraft.gui.chatListener()
                    .handleSystemMessage(Component.translatable("command.flywheel.backend.get", idStr), false);
                return Command.SINGLE_SUCCESS;
            }
        ).then(ClientCommand.literal(
            "DEFAULT", context -> {
                ConfigString backend = FabricFlwConfig.INSTANCE.client.backend;
                if (!backend.get().equals(FlwConfig.DEFAULT_BACKEND_STR)) {
                    backend.set(FlwConfig.DEFAULT_BACKEND_STR);

                    // Reload renderers so we can report the actual backend.
                    Minecraft.getInstance().levelExtractor.allChanged();

                    Backend actualBackend = BackendManager.currentBackend();
                    String actualIdStr = Backend.REGISTRY.getIdOrThrow(actualBackend).toString();
                    context.getSource().minecraft.gui.chatListener().handleSystemMessage(
                        Component.translatable("command.flywheel.backend.set", actualIdStr),
                        false
                    );
                }
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.argument(
            "id", BackendArgument.INSTANCE, context -> {
                Backend requestedBackend = context.getArgument("id", Backend.class);
                String requestedIdStr = Backend.REGISTRY.getIdOrThrow(requestedBackend).toString();
                ConfigString backend = FabricFlwConfig.INSTANCE.client.backend;
                if (!backend.get().equals(requestedIdStr)) {
                    backend.set(requestedIdStr);

                    // Reload renderers so we can report the actual backend.
                    Minecraft.getInstance().levelExtractor.allChanged();

                    ChatListener chatListener = context.getSource().minecraft.gui.chatListener();
                    Backend actualBackend = BackendManager.currentBackend();
                    if (actualBackend != requestedBackend) {
                        chatListener.handleSystemMessage(
                            Component.translatable("command.flywheel.backend.set.unavailable",
                                requestedIdStr
                            ).withStyle(ChatFormatting.RED), false
                        );
                    }

                    String actualIdStr = Backend.REGISTRY.getIdOrThrow(actualBackend).toString();
                    chatListener.handleSystemMessage(
                        Component.translatable(
                            "command.flywheel.backend.set",
                            actualIdStr
                        ), false
                    );
                }
                return Command.SINGLE_SUCCESS;
            }
        )));

        command.then(ClientCommand.literal(
            "limitUpdates", context -> {
                if (FabricFlwConfig.INSTANCE.client.limitUpdates.get()) {
                    context.getSource().minecraft.gui.chatListener()
                        .handleSystemMessage(Component.translatable("command.flywheel.limit_updates.get.on"), false);
                } else {
                    context.getSource().minecraft.gui.chatListener()
                        .handleSystemMessage(Component.translatable("command.flywheel.limit_updates.get.off"), false);
                }
                return Command.SINGLE_SUCCESS;
            }
        ).then(ClientCommand.literal(
            "on", context -> {
                ConfigBool limitUpdates = FabricFlwConfig.INSTANCE.client.limitUpdates;
                if (!limitUpdates.get()) {
                    limitUpdates.set(true);
                    context.getSource().minecraft.gui.chatListener()
                        .handleSystemMessage(Component.translatable("command.flywheel.limit_updates.set.on"), false);
                    Minecraft.getInstance().levelExtractor.allChanged();
                }
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.literal(
            "off", context -> {
                ConfigBool limitUpdates = FabricFlwConfig.INSTANCE.client.limitUpdates;
                if (limitUpdates.get()) {
                    limitUpdates.set(false);
                    context.getSource().minecraft.gui.chatListener()
                        .handleSystemMessage(Component.translatable("command.flywheel.limit_updates.set.off"), false);
                    Minecraft.getInstance().levelExtractor.allChanged();
                }
                return Command.SINGLE_SUCCESS;
            }
        )));

        command.then(ClientCommand.literal("lightSmoothness").then(ClientCommand.argument(
            "mode", LightSmoothnessArgument.INSTANCE, context -> {
                ConfigEnum<LightSmoothness> lightSmoothness = FabricFlwConfig.INSTANCE.client.flwBackends.lightSmoothness;
                LightSmoothness newValue = context.getArgument("mode", LightSmoothness.class);

                if (lightSmoothness.get() != newValue) {
                    lightSmoothness.set(newValue);
                    PipelineCompiler.deleteAll();
                }
                return Command.SINGLE_SUCCESS;
            }
        )));

        command.then(createDebugCommand());

        dispatcher.register(command);
    }

    private static LiteralArgumentBuilder<ClientSuggestionProvider> createDebugCommand() {
        LiteralArgumentBuilder<ClientSuggestionProvider> debug = ClientCommand.literal("debug");

        debug.then(ClientCommand.literal("crumbling")
            .then(ClientCommand.argument("pos", BlockPosArgument.blockPos()).then(ClientCommand.argument(
                "stage", IntegerArgumentType.integer(0, 9), context -> {
                    ClientSuggestionProvider source = context.getSource();
                    Minecraft minecraft = source.minecraft;
                    LocalPlayer player = minecraft.player;

                    if (player == null) {
                        return 0;
                    }

                    //noinspection DataFlowIssue
                    BlockPos pos = context.getArgument("pos", Coordinates.class).getBlockPos(new CommandSourceStack(
                        CommandSource.NULL,
                        player.position(),
                        player.getRotationVector(),
                        null,
                        source.permissions(),
                        null,
                        player
                    ));
                    int value = IntegerArgumentType.getInteger(context, "stage");

                    minecraft.level.destroyBlockProgress(player.getId(), pos, value);

                    return Command.SINGLE_SUCCESS;
                }
            ))));

        debug.then(ClientCommand.literal("shader").then(ClientCommand.argument(
            "mode", DebugModeArgument.INSTANCE, context -> {
                DebugMode mode = context.getArgument("mode", DebugMode.class);
                FrameUniforms.debugMode(mode);
                return Command.SINGLE_SUCCESS;
            }
        )));

        debug.then(ClientCommand.literal("frustum").then(ClientCommand.literal(
            "capture", context -> {
                FrameUniforms.captureFrustum();
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.literal(
            "unpause", context -> {
                FrameUniforms.unpauseFrustum();
                return Command.SINGLE_SUCCESS;
            }
        )));

        debug.then(ClientCommand.literal("lightSections").then(ClientCommand.literal(
            "on", context -> {
                BackendDebugFlags.LIGHT_STORAGE_VIEW = true;
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.literal(
            "off", context -> {
                BackendDebugFlags.LIGHT_STORAGE_VIEW = false;
                return Command.SINGLE_SUCCESS;
            }
        )));

        debug.then(ClientCommand.literal("pauseUpdates").then(ClientCommand.literal(
            "on", context -> {
                ImplDebugFlags.PAUSE_UPDATES = true;
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.literal(
            "off", context -> {
                ImplDebugFlags.PAUSE_UPDATES = false;
                return Command.SINGLE_SUCCESS;
            }
        )));

        debug.then(ClientCommand.literal(
            "info", context -> {
                context.getSource().minecraft.gui.chatListener()
                    .handleSystemMessage(FlwDebugInfo.getDebugCommandInfo(), false);
                return Command.SINGLE_SUCCESS;
            }
        ));

        return debug;
    }
}
