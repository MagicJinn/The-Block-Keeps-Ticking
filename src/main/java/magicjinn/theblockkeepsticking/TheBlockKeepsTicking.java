package magicjinn.theblockkeepsticking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.server.world.ChunkLevelType;

public class TheBlockKeepsTicking implements ModInitializer {
	public static final String MOD_ID = "the-block-keeps-ticking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final AttachmentType<Long> LAST_UPDATE_TIME = AttachmentRegistry
			.createPersistent(Identifier.of(MOD_ID, "last_update_time"), Codec.LONG);
	public static final AttachmentType<Long> LAST_REALTIME_UPDATE_MS = AttachmentRegistry
			.createPersistent(Identifier.of(MOD_ID, "last_realtime_update_ms"), Codec.LONG);

	@Override
	public void onInitialize() {
		LOGGER.info("The Block Keeps Ticking is initializing!");

		// Load the config from file
		ModConfig.HANDLER.load();

		WorldSimulator.Initialize();
		Timer.RegisterShutdownEvent();

		// Add listener for CHUNK_LEVEL_TYPE_CHANGE to handle chunks entering/leaving simulation
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((world, chunk, oldType, newType) -> {
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

				// Schedule simulation in the future,
				// to avoid changing blockstates during chunk loading (BAD!)
				Timer.INSTANCE.Schedule("chunk_simulation_" + chunk.getPos().toLong(),
						(server, chunkToSimulate) -> {
							WorldSimulator.SimulateWorld((WorldChunk) chunkToSimulate,
									ticksToSimulate);
						}, (WorldChunk) chunk);
			}
			// If chunk leaves block-ticking/simulation, record the last update time
			else if (oldType == ChunkLevelType.BLOCK_TICKING && newType != ChunkLevelType.BLOCK_TICKING) {
				// Always set both tags
				chunk.setAttached(LAST_UPDATE_TIME, currentWorldTime);
				chunk.setAttached(LAST_REALTIME_UPDATE_MS, nowMs);
			}
		});
	}
}
