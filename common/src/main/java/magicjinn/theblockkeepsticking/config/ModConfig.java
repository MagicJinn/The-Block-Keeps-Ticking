package magicjinn.theblockkeepsticking.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import magicjinn.theblockkeepsticking.Constants;
import magicjinn.theblockkeepsticking.platform.Services;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;

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
        public void write(JsonWriter out, TimeMode value) throws IOException {
            if (value == null)
                out.nullValue();
            else
                out.value(value.name());
        }

        @Override
        public TimeMode read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return TimeMode.WORLD_TIME;
            }
            return TimeMode.fromJsonString(in.nextString());
        }
    };

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TimeMode.class, TIME_MODE_ADAPTER)
            .setPrettyPrinting()
            .create();

    public static final ConfigHandler HANDLER = new ConfigHandler();

    Map<String, Boolean> enabledByName = new LinkedHashMap<>();
    int lazyTaxPercent = 0; // 0..99
    TimeMode timeMode = TimeMode.WORLD_TIME; // WORLD_TIME, REAL_TIME
    boolean simulateChunksWhenSleeping = true;
    boolean debugLogging = false;
    /**
     * Minimum chunk inhabited time (ticks) before catch-up is scheduled. 0 = always
     * simulate. Default 5 minutes.
     */
    int minInhabitedTicks = 6000;

    public static final class ConfigHandler {
        private ModConfig instance = new ModConfig();
        private Path path;

        public ModConfig instance() {
            return instance;
        }

        public void load() {
            path = resolveConfigPath();
            if (!Files.exists(path)) {
                instance = new ModConfig();
                return;
            }
            try (Reader reader = Files.newBufferedReader(path)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                instance = loaded != null ? loaded : new ModConfig();
                if (instance.enabledByName == null) {
                    instance.enabledByName = new LinkedHashMap<>();
                }
            } catch (IOException e) {
                Constants.LOG.error("Failed to load config from {}", path, e);
                instance = new ModConfig();
            }
        }

        public void save() {
            if (path == null) {
                path = resolveConfigPath();
            }
            try {
                Files.createDirectories(path.getParent());
                try (Writer writer = Files.newBufferedWriter(path)) {
                    GSON.toJson(instance, writer);
                }
            } catch (IOException e) {
                Constants.LOG.error("Failed to save config to {}", path, e);
            }
        }
    }

    /**
     * Single shared config path for Fabric and NeoForge installs that share a
     * {@code config/} folder. Migrates from the brief underscore-only filename if
     * present.
     */
    private static Path resolveConfigPath() {
        Path dir = Services.PLATFORM.getConfigDirectory();
        Path shared = dir.resolve(Constants.CONFIG_FILE);
        if (Files.exists(shared))
            return shared;

        Path underscore = dir.resolve(Constants.MOD_ID + ".json");
        if (Files.exists(underscore))
            return underscore;

        return shared;
    }

    public static ModConfig getInstance() {
        return HANDLER.instance();
    }

    public static boolean isEnabled(String tickingObjectName) {
        Boolean enabled = getInstance().enabledByName.get(tickingObjectName);
        return enabled == null || enabled;
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
        getInstance().lazyTaxPercent = ConfigOptions.LAZY_TAX_PERCENT.clamp(percent);
        HANDLER.save();
    }

    public static Map<String, Boolean> getEnabledByName() {
        return getInstance().enabledByName;
    }

    public static void setEnabled(String tickingObjectName, boolean enabled) {
        getInstance().enabledByName.put(tickingObjectName, enabled);
        HANDLER.save();
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

    public static int getMinInhabitedTicks() {
        return getInstance().minInhabitedTicks;
    }

    public static void setMinInhabitedTicks(int ticks) {
        getInstance().minInhabitedTicks = ConfigOptions.MIN_INHABITED_TICKS.clamp(ticks);
        HANDLER.save();
    }

    public static void ensureDefaultsPresent() {
        for (var tickingObject : WorldSimulator.TickingObjectInstances) {
            getInstance().enabledByName.putIfAbsent(tickingObject.getName(), Boolean.TRUE);
        }
        getInstance().enabledByName.putIfAbsent(CHICKEN_EGG_LAYING_IN_UNLOADED, Boolean.TRUE);
    }
}
