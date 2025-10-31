package magicjinn.theblockkeepsticking.config;

import java.util.LinkedHashMap;
import java.util.Map;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import net.fabricmc.loader.api.FabricLoader;

public final class ModConfig {

    public enum TimeMode {
        WORLD_TIME, REALTIME;

        @Override
        public String toString() {
            return switch (this) {
                case WORLD_TIME -> "World Time";
                case REALTIME -> "Real Time";
            };
        }
    }

    public static final ConfigClassHandler<ModConfig> HANDLER =
            ConfigClassHandler.createBuilder(ModConfig.class)
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir()
                                    .resolve(TheBlockKeepsTicking.MOD_ID + ".json"))
                            .build())
                    .build();

    @SerialEntry Map<String, Boolean> enabledByName = new LinkedHashMap<>();
    @SerialEntry int lazyTaxPercent = 0; // 0..99
    @SerialEntry TimeMode timeMode = TimeMode.WORLD_TIME;
    @SerialEntry boolean debugLogging = false;

    public ModConfig() {}

    public static ModConfig getInstance() {
        return HANDLER.instance();
    }

    public static boolean isEnabled(String tickingObjectName) {
        Boolean enabled = getInstance().enabledByName.get(tickingObjectName);
        return enabled == null ? true : enabled.booleanValue();
    }

    public static long applyLazyTax(long ticksToSimulate) {
        if (ticksToSimulate <= 0)
            return 0L;
        int p = getInstance().lazyTaxPercent;
        if (p == 0)
            return ticksToSimulate;
        double factor = 1.0 - (p / 100.0);
        long adjusted = (long) Math.floor(ticksToSimulate * factor);
        return Math.max(0L, adjusted);
    }

    public static TimeMode getTimeMode() {
        return getInstance().timeMode;
    }

    public static void setTimeMode(TimeMode mode) {
        getInstance().timeMode = mode;
        HANDLER.save();
    }

    public static int getLazyTaxPercent() {
        return getInstance().lazyTaxPercent;
    }

    public static void setLazyTaxPercent(int percent) {
        getInstance().lazyTaxPercent = percent;
        HANDLER.save();
    }

    public static Map<String, Boolean> getEnabledByName() {
        return getInstance().enabledByName;
    }

    public static boolean isDebugLogging() {
        return getInstance().debugLogging;
    }

    public static void setDebugLogging(boolean enabled) {
        getInstance().debugLogging = enabled;
        HANDLER.save();
    }

    public static void ensureDefaultsPresent() {
        for (var tickingObject : WorldSimulator.TickingObjectInstances) {
            getInstance().enabledByName.putIfAbsent(tickingObject.getName(), Boolean.TRUE);
        }
    }
}
