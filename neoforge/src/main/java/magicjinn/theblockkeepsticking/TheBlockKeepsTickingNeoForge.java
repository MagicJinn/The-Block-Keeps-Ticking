package magicjinn.theblockkeepsticking;

import magicjinn.theblockkeepsticking.platform.NeoForgePlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class TheBlockKeepsTickingNeoForge {
    public TheBlockKeepsTickingNeoForge(IEventBus modBus) {
        NeoForgePlatformHelper.registerAttachments(modBus);
        CommonClass.init();
    }
}
