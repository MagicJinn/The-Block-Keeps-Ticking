package magicjinn.theblockkeepsticking.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import magicjinn.theblockkeepsticking.config.ConfigOptions;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class TbktCommands {
    private static final SimpleCommandExceptionType ERROR_PLAYER_REQUIRED = new SimpleCommandExceptionType(
            Component.literal("A player is required when coordinates are omitted."));
    private static final SimpleCommandExceptionType ERROR_CHUNK_NOT_LOADED = new SimpleCommandExceptionType(
            Component.literal("That chunk is not loaded."));
    private static final SimpleCommandExceptionType ERROR_NO_TICKING_OBJECT = new SimpleCommandExceptionType(
            Component.literal("No ticking object handles that block."));
    private static final SimpleCommandExceptionType ERROR_UNKNOWN_TICKING_OBJECT = new SimpleCommandExceptionType(
            Component.literal("Unknown ticking object name."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root("theblockkeepsticking"));
        dispatcher.register(root("tbkt"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(String name) {
        return Commands.literal(name)
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> showUsage(ctx, name))
                .then(Commands.literal("config")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> showConfig(ctx, name))
                        .then(Commands.literal("reload")
                                .executes(TbktCommands::reloadConfig))
                        .then(buildConfigSet()))
                .then(Commands.literal("simulate")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> showSimulateUsage(ctx, name))
                        .then(Commands.literal("chunk")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("ticks", LongArgumentType.longArg(1))
                                        .executes(TbktCommands::simulateChunkAtPlayer)
                                        .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                                .suggests(TbktCommands::suggestChunkX)
                                                .then(Commands.argument("chunkZ",
                                                        IntegerArgumentType.integer())
                                                        .suggests(TbktCommands::suggestChunkZ)
                                                        .executes(TbktCommands::simulateChunkAtArgs)))))
                        .then(Commands.literal("block")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("ticks", LongArgumentType.longArg(1))
                                        .executes(TbktCommands::simulateBlockAtPlayer)
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(TbktCommands::simulateBlockAtArgs)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildConfigSet() {
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");
        for (ConfigOptions.Option option : ConfigOptions.GENERAL) {
            set.then(buildSetBranch(option));
        }
        set.then(Commands.literal(ConfigOptions.TICKING_OBJECT)
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(TbktCommands::suggestTickingObjectNames)
                                .executes(TbktCommands::setEnabled))));
        return set;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSetBranch(
            ConfigOptions.Option option) {
        LiteralArgumentBuilder<CommandSourceStack> branch = Commands.literal(option.id());
        return switch (option) {
            case ConfigOptions.BoolOption boolOption -> branch.then(
                    Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        boolean value = BoolArgumentType.getBool(ctx, "value");
                        boolOption.set(value);
                        return confirmSet(ctx, boolOption.id(), Boolean.toString(value));
                    }));
            case ConfigOptions.IntOption intOption -> branch.then(Commands
                    .argument("value", IntegerArgumentType.integer(intOption.min(), intOption.max()))
                    .executes(ctx -> {
                        int value = IntegerArgumentType.getInteger(ctx, "value");
                        intOption.set(value);
                        return confirmSet(ctx, intOption.id(),
                                Integer.toString(intOption.get()));
                    }));
            case ConfigOptions.EnumOption<?> enumOption -> branch.then(Commands
                    .argument("value", StringArgumentType.word())
                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                            enumOption.suggestions(), builder))
                    .executes(ctx -> setEnumOption(ctx, enumOption)));
        };
    }

    private static <E extends Enum<E>> int setEnumOption(
            CommandContext<CommandSourceStack> context, ConfigOptions.EnumOption<E> option)
            throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, "value");
        E value;
        try {
            value = option.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
        }
        option.set(value);
        return confirmSet(context, option.id(), value.name());
    }

    private static int confirmSet(CommandContext<CommandSourceStack> context, String id, String value) {
        context.getSource().sendSuccess(
                () -> Component.literal("Set " + id + " to " + value + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestChunkX(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null)
            SharedSuggestionProvider.suggest(new String[] {Integer.toString(player.chunkPosition().x())},
                    builder);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestChunkZ(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null)
            SharedSuggestionProvider.suggest(new String[] {Integer.toString(player.chunkPosition().z())},
                    builder);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTickingObjectNames(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ModConfig.ensureDefaultsPresent();
        List<String> names = new ArrayList<>(ModConfig.getEnabledByName().keySet());
        return SharedSuggestionProvider.suggest(names, builder);
    }

    private static int showUsage(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Usage:"), false);
        source.sendSuccess(() -> Component.literal("  /" + name + " config"), false);
        source.sendSuccess(() -> Component.literal("  /" + name + " config reload"), false);
        source.sendSuccess(() -> Component.literal("  /" + name + " config set <option> <value>"), false);
        source.sendSuccess(() -> Component.literal(
                "  /" + name + " simulate chunk <ticks> [<chunkX> <chunkZ>]"), false);
        source.sendSuccess(() -> Component.literal(
                "  /" + name + " simulate block <ticks> [<x> <y> <z>]"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showSimulateUsage(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Usage:"), false);
        source.sendSuccess(() -> Component.literal(
                "  /" + name + " simulate chunk <ticks> [<chunkX> <chunkZ>]"), false);
        source.sendSuccess(() -> Component.literal(
                "  /" + name + " simulate block <ticks> [<x> <y> <z>]"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showConfig(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        ModConfig.ensureDefaultsPresent();
        source.sendSuccess(() -> Component.literal("The Block Keeps Ticking config:"), false);
        for (ConfigOptions.Option option : ConfigOptions.GENERAL) {
            source.sendSuccess(
                    () -> Component.literal("  " + option.id() + " = " + option.currentAsString()),
                    false);
        }
        source.sendSuccess(() -> Component.literal("  " + ConfigOptions.TICKING_OBJECT + ":"), false);
        for (Map.Entry<String, Boolean> entry : ModConfig.getEnabledByName().entrySet()) {
            String key = entry.getKey();
            boolean value = entry.getValue() == null || entry.getValue();
            source.sendSuccess(() -> Component.literal("    " + key + " = " + value), false);
        }
        source.sendSuccess(() -> Component.literal("Usage:"), false);
        source.sendSuccess(() -> Component.literal("  /" + name + " config reload"), false);
        for (ConfigOptions.Option option : ConfigOptions.GENERAL) {
            source.sendSuccess(() -> Component.literal(
                    "  /" + name + " config set " + option.id() + " " + option.usageValueHint()),
                    false);
        }
        source.sendSuccess(() -> Component.literal(
                "  /" + name + " config set " + ConfigOptions.TICKING_OBJECT
                        + " <true|false> <name>"),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ModConfig.HANDLER.load();
        ModConfig.ensureDefaultsPresent();
        ModConfig.HANDLER.save();
        context.getSource().sendSuccess(
                () -> Component.literal("Reloaded The Block Keeps Ticking config."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setEnabled(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        boolean value = BoolArgumentType.getBool(context, "value");
        ModConfig.ensureDefaultsPresent();
        if (!ModConfig.getEnabledByName().containsKey(name))
            throw ERROR_UNKNOWN_TICKING_OBJECT.create();
        ModConfig.setEnabled(name, value);
        return confirmSet(context, ConfigOptions.TICKING_OBJECT + " \"" + name + "\"", Boolean.toString(value));
    }

    private static int simulateChunkAtPlayer(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null)
            throw ERROR_PLAYER_REQUIRED.create();
        ChunkPos chunkPos = player.chunkPosition();
        return simulateChunk(context.getSource(), LongArgumentType.getLong(context, "ticks"),
                chunkPos.x(), chunkPos.z());
    }

    private static int simulateChunkAtArgs(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return simulateChunk(context.getSource(), LongArgumentType.getLong(context, "ticks"),
                IntegerArgumentType.getInteger(context, "chunkX"),
                IntegerArgumentType.getInteger(context, "chunkZ"));
    }

    private static int simulateChunk(CommandSourceStack source, long ticks, int chunkX, int chunkZ)
            throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null)
            throw ERROR_CHUNK_NOT_LOADED.create();

        WorldSimulator.SimulateWorld(chunk, ticks);
        source.sendSuccess(() -> Component.literal(
                "Simulated chunk [" + chunkX + ", " + chunkZ + "] for " + ticks + " ticks."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int simulateBlockAtPlayer(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null)
            throw ERROR_PLAYER_REQUIRED.create();
        return simulateBlock(context.getSource(), LongArgumentType.getLong(context, "ticks"),
                player.blockPosition());
    }

    private static int simulateBlockAtArgs(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return simulateBlock(context.getSource(), LongArgumentType.getLong(context, "ticks"),
                BlockPosArgument.getLoadedBlockPos(context, "pos"));
    }

    private static int simulateBlock(CommandSourceStack source, long ticks, BlockPos pos)
            throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        for (TickingObject tickingObject : WorldSimulator.TickingObjectInstances) {
            Class<?> type = tickingObject.getType();
            if (type == null)
                continue;

            if (Block.class.isAssignableFrom(type) && type.isInstance(block)) {
                if (!ModConfig.isEnabled(tickingObject.getName())) {
                    source.sendFailure(Component.literal(
                            "Ticking object \"" + tickingObject.getName()
                                    + "\" is disabled."));
                    return 0;
                }
                boolean result = tickingObject.Simulate(block, ticks, level, state, pos);
                source.sendSuccess(() -> Component.literal("Simulated block " + pos.toShortString()
                        + " as \"" + tickingObject.getName() + "\" for " + ticks + " ticks"
                        + (result ? "." : " (no change).")), true);
                return Command.SINGLE_SUCCESS;
            }

            if (BlockEntity.class.isAssignableFrom(type)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null || !type.isInstance(blockEntity))
                    continue;
                if (!ModConfig.isEnabled(tickingObject.getName())) {
                    source.sendFailure(Component.literal(
                            "Ticking object \"" + tickingObject.getName()
                                    + "\" is disabled."));
                    return 0;
                }
                boolean result = tickingObject.Simulate(blockEntity, ticks, level,
                        blockEntity.getBlockState(), pos);
                source.sendSuccess(() -> Component.literal("Simulated block entity "
                        + pos.toShortString() + " as \"" + tickingObject.getName() + "\" for "
                        + ticks + " ticks" + (result ? "." : " (no change).")), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        throw ERROR_NO_TICKING_OBJECT.create();
    }
}
