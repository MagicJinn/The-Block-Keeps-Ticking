package magicjinn.theblockkeepsticking.platform;

import java.util.ServiceLoader;
import magicjinn.theblockkeepsticking.Constants;
import magicjinn.theblockkeepsticking.platform.services.ITBKTPlatform;

public final class Services {
    public static final ITBKTPlatform PLATFORM = load(ITBKTPlatform.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
