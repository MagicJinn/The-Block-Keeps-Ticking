package magicjinn.theblockkeepsticking;

import magicjinn.theblockkeepsticking.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

public class TheBlockKeepsTickingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
    }
}
