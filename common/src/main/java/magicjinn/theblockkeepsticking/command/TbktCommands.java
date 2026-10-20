package magicjinn.theblockkeepsticking.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

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
                        .executes(TbktCommands::reloadConfig))
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

    private static int showUsage(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Usage:"), false);
        source.sendSuccess(() -> Component.literal("  /" + name + " config"), false);
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

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ModConfig.HANDLER.load();
        ModConfig.ensureDefaultsPresent();
        ModConfig.HANDLER.save();
        context.getSource().sendSuccess(
                () -> Component.literal("Reloaded The Block Keeps Ticking config."), true);
        return Command.SINGLE_SUCCESS;
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
