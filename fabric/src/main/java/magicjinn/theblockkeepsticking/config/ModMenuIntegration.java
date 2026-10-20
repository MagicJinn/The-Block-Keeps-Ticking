package magicjinn.theblockkeepsticking.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

/**
 * Soft-depends on YACL. ModMenu may load this entrypoint without YACL
 * installed.
 */
public class ModMenuIntegration implements ModMenuApi {
    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded(YACL_MOD_ID))
            return parent -> null;

        // Reflect so missing YACL does not fail class loading of this entrypoint.
        return parent -> openYaclScreen(parent);
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
