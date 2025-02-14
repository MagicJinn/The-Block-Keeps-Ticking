package net.what42.aliveworld;

import net.what42.aliveworld.event.ChunkEvents;
import net.what42.aliveworld.manager.ChunkManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliveWorld implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("alive-world");
	public static final Config CONFIG = Config.getInstance();
	
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Alive World");
		
		// Registrar el ChunkManager como listener de eventos de chunks
		ChunkEvents.registerUnloadCallback(ChunkManager.getInstance());
	}
}
