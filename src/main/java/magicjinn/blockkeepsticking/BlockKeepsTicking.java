package magicjinn.blockkeepsticking;

import magicjinn.blockkeepsticking.event.ChunkEvents;
import magicjinn.blockkeepsticking.manager.ChunkManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockKeepsTicking implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("block-keeps-ticking");
	public static final Config CONFIG = Config.getInstance();
	public static final String LAST_UPDATE_KEY = "BlockKeepsTicking_LastUpdateTime";

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Block Keeps Ticking");

        // Register the ChunkManager as a chunk events listener
		ChunkEvents.registerUnloadCallback(ChunkManager.getInstance());
	}
}
