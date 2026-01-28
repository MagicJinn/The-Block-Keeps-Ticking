package magicjinn.apiexample;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIExample implements ModInitializer {
	public static final String MOD_ID = "api-example";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("The Block Keeps Ticking API Example loaded!");
	}
}