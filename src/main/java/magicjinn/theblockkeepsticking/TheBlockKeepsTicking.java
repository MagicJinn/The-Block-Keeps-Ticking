package magicjinn.theblockkeepsticking;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.Benchmarker;
import magicjinn.theblockkeepsticking.util.ChunkUtil;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class TheBlockKeepsTicking implements ModInitializer {
	public static final String MOD_ID = "the-block-keeps-ticking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String ENTRYPOINT_KEY = "theblockkeepsticking";

	public static final AttachmentType<Long> LAST_UPDATE_TIME = AttachmentRegistry
			.createPersistent(Identifier.fromNamespaceAndPath(MOD_ID, "last_update_time"), Codec.LONG);
	public static final AttachmentType<Long> LAST_REALTIME_UPDATE_MS = AttachmentRegistry
			.createPersistent(Identifier.fromNamespaceAndPath(MOD_ID, "last_realtime_update_ms"), Codec.LONG);

	// Used to detect time skips (e.g. sleeping), tracked per world
	private static final Map<ServerLevel, Long> lastWorldTimes = new WeakHashMap<>();
	// Minimum world-time jump (ticks) to trigger sleep-style simulation. Small
	// jumps (e.g. from TT20) are ignored to avoid lag.
	private static final long MIN_TIME_SKIP_FOR_SLEEP_SIMULATION = 200L;

	@Override
	public void onInitialize() {
		LOGGER.info("The Block Keeps Ticking is initializing!");

		// Load the config from file
		ModConfig.HANDLER.load();
		WorldSimulator.InitializeTickingBlocks();
		InitializeEntrypointTickingBlocks();
		// Ensure every ticking block has an entry in the config (so the JSON shows all
		// options)
		ModConfig.ensureDefaultsPresent();
		ModConfig.HANDLER.save();
		Timer.RegisterShutdownEvent();

		ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.register(this::onChunkLevelTypeChange);

		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {
		for (ServerLevel level : server.getAllLevels()) {
			onWorldTick(level);
		}
	}

	private void onWorldTick(ServerLevel level) {
		// Exit if disabled in the config, or if this dimension has fixed time
		// (no day/night -> no sleep time skip)
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
			// Only simulate on large time skips (e.g. sleeping: 1000+ ticks).
			// Small jumps (e.g. 2–20 per tick) can be caused by mods like TT20 that add
			// "missed" ticks to getOverworldClockTime(); treating those as sleep would
			// schedule
			// simulation for every chunk every tick and cause severe lag or a feedback
			// loop.
			if (timeDiff > MIN_TIME_SKIP_FOR_SLEEP_SIMULATION) {
				Benchmarker.StartBenchmark("onWorldTick.sleepSimulation");
				try {
					for (LevelChunk chunk : ChunkUtil.getLoadedChunks(level)) {
						ScheduleSimulation(chunk, timeDiff);
					}
				} finally {
					Benchmarker.EndBenchmark("onWorldTick.sleepSimulation");
				}
			}
		}

		lastWorldTimes.put(level, worldTime);
	}

	private void onChunkLevelTypeChange(ServerLevel level, LevelChunk chunk, FullChunkStatus oldType,
			FullChunkStatus newType) {
		// Always set both tags regardless of config setting
		long currentWorldTime = level.getGameTime();
		long nowMs = System.currentTimeMillis();

		// If chunk enters block-ticking/simulation, simulate it
		if (newType == FullChunkStatus.BLOCK_TICKING
				&& oldType != FullChunkStatus.BLOCK_TICKING) {
			// Calculate ticksToSimulate based on config time mode
			long ticksToSimulate;
			if (ModConfig.getTimeMode() == ModConfig.TimeMode.REAL_TIME) {
				long lastMs = chunk
						.getAttachedOrSet(TheBlockKeepsTicking.LAST_REALTIME_UPDATE_MS, nowMs);
				ticksToSimulate = Math.max(0L, (nowMs - lastMs) / 50L); // convert to ticks
			} else {
				long lastTickTime = chunk.getAttachedOrSet(
						TheBlockKeepsTicking.LAST_UPDATE_TIME, currentWorldTime);
				ticksToSimulate = currentWorldTime - lastTickTime;
			}

			// Schedule simulation for the chunk
			ScheduleSimulation(chunk, ticksToSimulate);
		}

		// If chunk leaves block-ticking/simulation, record the last update time
		else if (oldType == FullChunkStatus.BLOCK_TICKING && newType != FullChunkStatus.BLOCK_TICKING) {
			// Always set both tags
			chunk.setAttached(LAST_UPDATE_TIME, currentWorldTime);
			chunk.setAttached(LAST_REALTIME_UPDATE_MS, nowMs);
		}
	}

	private void ScheduleSimulation(LevelChunk chunk, long ticksToSimulate) {
		// Schedule simulation in the future,
		// to avoid changing blockstates during chunk loading (BAD!)
		Timer.INSTANCE.Schedule("chunk_simulation_" + chunk.getPos().pack(),
				(server, chunkToSimulate) -> {
					WorldSimulator.SimulateWorld((LevelChunk) chunkToSimulate,
							ticksToSimulate);
				}, (LevelChunk) chunk);
	}

	/**
	 * Loads and invokes all InitializeTickingBlocks entrypoints from other mods.
	 */
	private void InitializeEntrypointTickingBlocks() {
		List<EntrypointContainer<InitializeTickingBlocks>> entrypoints = FabricLoader.getInstance()
				.getEntrypointContainers(ENTRYPOINT_KEY, InitializeTickingBlocks.class);

		for (EntrypointContainer<InitializeTickingBlocks> container : entrypoints) {
			String modId = container.getProvider().getMetadata().getId();
			try {
				LOGGER.info("Loading ticking blocks from mod: {}", modId);
				InitializeTickingBlocks initializer = container.getEntrypoint();
				initializer.registerTickingBlocks(WorldSimulator::RegisterTickingBlock);
			} catch (Throwable e) {
				LOGGER.error("Failed to load ticking blocks from mod {}: {}", modId, e.getMessage(), e);
			}
		}
	}
}
