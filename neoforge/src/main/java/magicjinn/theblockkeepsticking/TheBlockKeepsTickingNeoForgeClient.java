package magicjinn.theblockkeepsticking;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class TheBlockKeepsTickingNeoForgeClient {
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";

    public TheBlockKeepsTickingNeoForgeClient(ModContainer container) {
        if (!ModList.get().isLoaded(YACL_MOD_ID))
            return;

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
