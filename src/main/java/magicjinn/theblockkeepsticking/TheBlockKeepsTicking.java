package magicjinn.theblockkeepsticking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.server.world.ChunkLevelType;

public class TheBlockKeepsTicking implements ModInitializer {
	public static final String MOD_ID = "the-block-keeps-ticking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final AttachmentType<Long> LAST_UPDATE_TIME =
			AttachmentRegistry.createPersistent(Identifier.of(MOD_ID, "last_update_time"),
					Codec.LONG);

	@Override
	public void onInitialize() {
		LOGGER.info("The Block Keeps Ticking is initializing!");

		WorldSimulator.Initialize();
		Timer.RegisterShutdownEvent();

		// Add listener for CHUNK_LEVEL_TYPE_CHANGE to handle chunks entering/leaving simulation
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((world, chunk, oldType, newType) -> {
			// If chunk enters block-ticking/simulation, simulate it
			long currentWorldTime = world.getTime();
			// Get last update time, or set it to current time if not present
			long lastTickTime =
					chunk.getAttachedOrSet(TheBlockKeepsTicking.LAST_UPDATE_TIME, currentWorldTime);
			long ticksToSimulate = currentWorldTime - lastTickTime;
			if (newType == ChunkLevelType.BLOCK_TICKING && oldType != ChunkLevelType.BLOCK_TICKING) {
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
				chunk.setAttached(LAST_UPDATE_TIME, world.getTime());
			}
		});
	}
}
