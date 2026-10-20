package magicjinn.theblockkeepsticking;

import magicjinn.theblockkeepsticking.platform.NeoForgePlatformHelper;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Constants.MOD_ID)
public class TheBlockKeepsTickingNeoForge {
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";

    public TheBlockKeepsTickingNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgePlatformHelper.registerAttachments(modBus);
        CommonClass.init();
        registerConfigScreen(container);
    }

    private static void registerConfigScreen(ModContainer container) {
        if (!ModList.get().isLoaded(YACL_MOD_ID))
            return;

        // Reflect so missing YACL classes do not fail mod construction if the soft
        // check races.
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> openYaclScreen(parent));
    }

    private static Screen openYaclScreen(Screen parent) {
        try {
            Class<?> screenClass = Class
                    .forName("magicjinn.theblockkeepsticking.config.ModConfigScreen");
            return (Screen) screenClass.getMethod("create", Screen.class).invoke(null, parent);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("YACL is present but config screen failed to load", e);
        }
    }
}
