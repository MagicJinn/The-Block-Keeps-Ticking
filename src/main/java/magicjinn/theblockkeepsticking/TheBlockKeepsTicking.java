package magicjinn.theblockkeepsticking;

import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.Benchmarker;
import magicjinn.theblockkeepsticking.util.ChunkUtil;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.server.world.ChunkLevelType;

public class TheBlockKeepsTicking implements ModInitializer {
	public static final String MOD_ID = "the-block-keeps-ticking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String ENTRYPOINT_KEY = "theblockkeepsticking";

	public static final AttachmentType<Long> LAST_UPDATE_TIME = AttachmentRegistry
			.createPersistent(Identifier.of(MOD_ID, "last_update_time"), Codec.LONG);
	public static final AttachmentType<Long> LAST_REALTIME_UPDATE_MS = AttachmentRegistry
			.createPersistent(Identifier.of(MOD_ID, "last_realtime_update_ms"), Codec.LONG);

	// Used to detect time skips (e.g. sleeping)
	private static long lastWorldTime = -1;
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

		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register(this::onChunkLevelTypeChange);

		ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
	}

	private void onWorldTick(ServerWorld world) {
		// Exit if disabled in the config, or if this dimension has fixed time
		// (no day/night -> no sleep time skip)
		if (!ModConfig.isSimulateChunksWhenSleeping() || world.getDimension().hasFixedTime())
			return;

		long worldTime = world.getTimeOfDay();

		if (lastWorldTime != -1) {
			long timeDiff = worldTime - lastWorldTime;
			// Only simulate on large time skips (e.g. sleeping: 1000+ ticks).
			// Small jumps (e.g. 2–20 per tick) can be caused by mods like TT20 that add
			// "missed" ticks to getTimeOfDay(); treating those as sleep would schedule
			// simulation for every chunk every tick and cause severe lag or a feedback
			// loop.
			if (timeDiff > MIN_TIME_SKIP_FOR_SLEEP_SIMULATION) {
				Benchmarker.StartBenchmark("onWorldTick.sleepSimulation");
				try {
					for (Chunk chunk : ChunkUtil.getLoadedChunks(world)) {
						ScheduleSimulation(chunk, timeDiff);
					}
				} finally {
					Benchmarker.EndBenchmark("onWorldTick.sleepSimulation");
				}
			}
		}

		lastWorldTime = worldTime;
	}

	private void onChunkLevelTypeChange(ServerWorld world, Chunk chunk, ChunkLevelType oldType,
			ChunkLevelType newType) {
		// Always set both tags regardless of config setting
		long currentWorldTime = world.getTime();
		long nowMs = System.currentTimeMillis();

		// If chunk enters block-ticking/simulation, simulate it
		if (newType == ChunkLevelType.BLOCK_TICKING
				&& oldType != ChunkLevelType.BLOCK_TICKING) {
			// Calculate ticksToSimulate based on config time mode
			long ticksToSimulate;
			if (ModConfig.getTimeMode() == ModConfig.TimeMode.REALTIME) {
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
		else if (oldType == ChunkLevelType.BLOCK_TICKING && newType != ChunkLevelType.BLOCK_TICKING) {
			// Always set both tags
			chunk.setAttached(LAST_UPDATE_TIME, currentWorldTime);
			chunk.setAttached(LAST_REALTIME_UPDATE_MS, nowMs);
		}
	}

	private void ScheduleSimulation(Chunk chunk, long ticksToSimulate) {
		// Schedule simulation in the future,
		// to avoid changing blockstates during chunk loading (BAD!)
		Timer.INSTANCE.Schedule("chunk_simulation_" + chunk.getPos().toLong(),
				(server, chunkToSimulate) -> {
					WorldSimulator.SimulateWorld((WorldChunk) chunkToSimulate,
							ticksToSimulate);
				}, (WorldChunk) chunk);
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
