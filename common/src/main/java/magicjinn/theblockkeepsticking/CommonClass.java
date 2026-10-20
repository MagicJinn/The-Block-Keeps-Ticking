package magicjinn.theblockkeepsticking;

import java.util.Map;
import java.util.WeakHashMap;
import magicjinn.theblockkeepsticking.command.TbktCommands;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.platform.Services;
import magicjinn.theblockkeepsticking.platform.services.ITBKTPlatform;
import magicjinn.theblockkeepsticking.simulator.ChunkSimScheduler;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.Benchmarker;
import magicjinn.theblockkeepsticking.util.ChunkUtil;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Shared mod bootstrap. Loader projects call {@link #init()} from their
 * entrypoints.
 */
public final class CommonClass {
    private static final Map<ServerLevel, Long> lastWorldTimes = new WeakHashMap<>();
    private static final long MIN_TIME_SKIP_FOR_SLEEP_SIMULATION = 200L;

    public static void init() {
        Constants.LOG.info("The Block Keeps Ticking is initializing on {}!",
                Services.PLATFORM.getPlatformName());

        Services.PLATFORM.registerChunkAttachments();

        ModConfig.HANDLER.load();
        WorldSimulator.InitializeTickingBlocks();
        Services.PLATFORM.invokeTickingBlockEntrypoints(WorldSimulator::RegisterTickingBlock);
        ModConfig.ensureDefaultsPresent();
        ModConfig.HANDLER.save();

        ChunkSimScheduler.Register();
        Timer.Register();

        Services.PLATFORM.registerCommands(TbktCommands::register);
        Services.PLATFORM.registerChunkFullStatusChange(CommonClass::onChunkLevelTypeChange);
        Services.PLATFORM.registerServerTickEnd(CommonClass::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            onWorldTick(level);
        }
    }

    private static void onWorldTick(ServerLevel level) {
        if (!ModConfig.isSimulateChunksWhenSleeping() || level.dimensionType().hasFixedTime())
            return;

        long worldTime = level.getOverworldClockTime();
        Long lastWorldTime = lastWorldTimes.get(level);

        if (lastWorldTime != null) {
            if (worldTime < lastWorldTime) {
                lastWorldTimes.put(level, worldTime);
                return;
            }

            long timeDiff = worldTime - lastWorldTime;
            if (timeDiff > MIN_TIME_SKIP_FOR_SLEEP_SIMULATION) {
                Benchmarker.StartBenchmark("onWorldTick.sleepSimulation");
                try {
                    for (LevelChunk chunk : ChunkUtil.getLoadedChunks(level)) {
                        scheduleSimulation(chunk, timeDiff);
                    }
                } finally {
                    Benchmarker.EndBenchmark("onWorldTick.sleepSimulation");
                }
            }
        }

        lastWorldTimes.put(level, worldTime);
    }

    private static void onChunkLevelTypeChange(ServerLevel level, LevelChunk chunk,
            FullChunkStatus oldType, FullChunkStatus newType) {
        long currentWorldTime = level.getGameTime();
        long nowMs = System.currentTimeMillis();
        ITBKTPlatform platform = Services.PLATFORM;

        if (newType == FullChunkStatus.BLOCK_TICKING && oldType != FullChunkStatus.BLOCK_TICKING) {
            long ticksToSimulate;
            if (ModConfig.getTimeMode() == ModConfig.TimeMode.REAL_TIME) {
                long lastMs = platform.getChunkLong(chunk,
                        ITBKTPlatform.ChunkLongKey.LAST_REALTIME_UPDATE_MS, nowMs);
                ticksToSimulate = Math.max(0L, (nowMs - lastMs) / 50L);
            } else {
                long lastTickTime = platform.getChunkLong(chunk,
                        ITBKTPlatform.ChunkLongKey.LAST_UPDATE_TIME, currentWorldTime);
                ticksToSimulate = currentWorldTime - lastTickTime;
            }
            scheduleSimulation(chunk, ticksToSimulate);
        } else if (oldType == FullChunkStatus.BLOCK_TICKING
                && newType != FullChunkStatus.BLOCK_TICKING) {
            platform.setChunkLong(chunk, ITBKTPlatform.ChunkLongKey.LAST_UPDATE_TIME,
                    currentWorldTime);
            platform.setChunkLong(chunk, ITBKTPlatform.ChunkLongKey.LAST_REALTIME_UPDATE_MS, nowMs);
        }
    }

    private static void scheduleSimulation(LevelChunk chunk, long ticksToSimulate) {
        ChunkSimScheduler.INSTANCE.enqueue(chunk, ticksToSimulate);
    }
}
