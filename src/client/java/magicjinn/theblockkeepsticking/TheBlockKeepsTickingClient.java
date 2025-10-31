package magicjinn.theblockkeepsticking;

import magicjinn.theblockkeepsticking.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

public class TheBlockKeepsTickingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Load the config when the client initializes
		ModConfig.HANDLER.load();
	}
}