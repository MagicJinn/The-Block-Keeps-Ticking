package magicjinn.theblockkeepsticking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import net.minecraft.util.Identifier;
// import net.minecraft.world.chunk.ChunkLevelType;
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

		// Add listener for CHUNK_LEVEL_TYPE_CHANGE to handle chunks entering/leaving simulation
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((world, chunk, oldType, newType) -> {
			// If chunk enters block-ticking/simulation, simulate it immediately
			if (newType == ChunkLevelType.BLOCK_TICKING) {
				WorldSimulator.SimulateWorld((WorldChunk) chunk);
			}
			// If chunk leaves block-ticking/simulation, record the last update time
			else if (oldType == ChunkLevelType.BLOCK_TICKING) {
				chunk.setAttached(LAST_UPDATE_TIME, world.getTime());
			}
		});
	}
}
