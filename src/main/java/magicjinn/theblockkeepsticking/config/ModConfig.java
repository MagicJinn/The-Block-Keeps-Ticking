package magicjinn.theblockkeepsticking.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import net.fabricmc.loader.api.FabricLoader;

public final class ModConfig {

    /**
     * Key in {@link #enabledByName} for whether adult chickens advance their egg
     * timer when chunks are simulated while unloaded.
     */
    public static final String CHICKEN_EGG_LAYING_IN_UNLOADED = "Chicken Egg Laying";

    // WORLD_TIME, REAL_TIME. Legacy "REALTIME" in JSON is accepted and
    // autocorrected
    public enum TimeMode {
        WORLD_TIME,
        REAL_TIME;

        @Override
        public String toString() {
            return switch (this) {
                case WORLD_TIME -> "World Time";
                case REAL_TIME -> "Real Time";
            };
        }

        static TimeMode fromJsonString(String value) {
            if (value == null)
                return WORLD_TIME;
            return switch (value) {
                case "REALTIME" -> REAL_TIME; // legacy
                case "REAL_TIME" -> REAL_TIME;
                case "WORLD_TIME" -> WORLD_TIME;
                default -> WORLD_TIME;
            };
        }
    }

    private static final TypeAdapter<TimeMode> TIME_MODE_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, TimeMode value) throws java.io.IOException {
            if (value == null)
                out.nullValue();
            else
                out.value(value.name());
        }

        @Override
        public TimeMode read(JsonReader in) throws java.io.IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return TimeMode.WORLD_TIME;
            }
            return TimeMode.fromJsonString(in.nextString());
        }
    };

    public static final ConfigClassHandler<ModConfig> HANDLER =
            ConfigClassHandler.createBuilder(ModConfig.class)
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir()
                                    .resolve(TheBlockKeepsTicking.MOD_ID + ".json"))
                            .appendGsonBuilder(b -> b.registerTypeAdapter(TimeMode.class, TIME_MODE_ADAPTER))
                            .build())
                    .build();

    @SerialEntry
    Map<String, Boolean> enabledByName = new LinkedHashMap<>();
    @SerialEntry
    int lazyTaxPercent = 0; // 0..99
    @SerialEntry
    TimeMode timeMode = TimeMode.WORLD_TIME;
    @SerialEntry
    boolean simulateChunksWhenSleeping = true;
    @SerialEntry
    boolean debugLogging = false;

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

    public static boolean isSimulateChunksWhenSleeping() {
        return getInstance().simulateChunksWhenSleeping;
    }

    public static void setSimulateChunksWhenSleeping(boolean enabled) {
        getInstance().simulateChunksWhenSleeping = enabled;
        HANDLER.save();
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
        getInstance().enabledByName.putIfAbsent(CHICKEN_EGG_LAYING_IN_UNLOADED, Boolean.TRUE);
    }
}
