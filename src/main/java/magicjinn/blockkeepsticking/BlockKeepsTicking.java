package magicjinn.blockkeepsticking;

import magicjinn.blockkeepsticking.event.ChunkEvents;
import magicjinn.blockkeepsticking.manager.ChunkManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockKeepsTicking implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("block-keeps-ticking");
	public static final Config CONFIG = Config.getInstance();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Alive World");

		// Registrar el ChunkManager como listener de eventos de chunks
		ChunkEvents.registerUnloadCallback(ChunkManager.getInstance());
	}
}
