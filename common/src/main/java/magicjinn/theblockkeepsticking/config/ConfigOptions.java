package magicjinn.theblockkeepsticking.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Single source of truth for general config option ids, bounds, and accessors.
 */
public final class ConfigOptions {
    public static final String TICKING_OBJECT = "tickingObject";

    public static final BoolOption SIMULATE_CHUNKS_WHEN_SLEEPING = new BoolOption(
            "simulateChunksWhenSleeping",
            "Simulate After Sleeping",
            "When enabled, time skips (e.g. from sleeping) will simulate loaded chunks for the duration of the time skip.",
            ModConfig::isSimulateChunksWhenSleeping,
            ModConfig::setSimulateChunksWhenSleeping);

    public static final BoolOption DEBUG_LOGGING = new BoolOption(
            "debugLogging",
            "Debug Logging",
            "Enables detailed logging when simulation occurs. Shows which blocks, block entities, and entities are being simulated.",
            ModConfig::isDebugLogging,
            ModConfig::setDebugLogging);

    public static final IntOption LAZY_TAX_PERCENT = new IntOption(
            "lazyTaxPercent",
            "Lazy Tax (%)",
            "Simulated ticks are reduced by this percentage. Increasing this value slows down simulation in unloaded chunks.",
            0,
            99,
            1,
            0,
            ModConfig::getLazyTaxPercent,
            ModConfig::setLazyTaxPercent);

    public static final IntOption MIN_INHABITED_TICKS = new IntOption(
            "minInhabitedTicks",
            "Min Inhabited Time (ticks)",
            "Chunks with less inhabited time than this are not simulated. Reduces lag when traveling through fresh terrain. 6000 = 5 minutes. 0 = always simulate.",
            0,
            24000,
            200,
            6000,
            ModConfig::getMinInhabitedTicks,
            ModConfig::setMinInhabitedTicks);

    public static final EnumOption<ModConfig.TimeMode> TIME_MODE = new EnumOption<>(
            "timeMode",
            "Time Source",
            "Choose whether to use world time or real time for simulation calculations. World time only passes when you are in your level. Real time passes at the same rate as the real world, even when you are not playing the game.\n(Real Time works well with lazy tax.)",
            ModConfig.TimeMode.class,
            ModConfig::getTimeMode,
            ModConfig::setTimeMode,
            ConfigOptions::parseTimeMode);

    /** Fixed general options in display / command order. */
    public static final List<Option> GENERAL = List.of(
            TIME_MODE,
            LAZY_TAX_PERCENT,
            SIMULATE_CHUNKS_WHEN_SLEEPING,
            MIN_INHABITED_TICKS,
            DEBUG_LOGGING);

    private ConfigOptions() {
    }

    private static ModConfig.TimeMode parseTimeMode(String raw) {
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return switch (normalized) {
            case "WORLD_TIME", "WORLDTIME" -> ModConfig.TimeMode.WORLD_TIME;
            case "REAL_TIME", "REALTIME" -> ModConfig.TimeMode.REAL_TIME;
            default -> throw new IllegalArgumentException(
                    "Unknown time mode. Use WORLD_TIME or REAL_TIME.");
        };
    }

    public sealed interface Option permits BoolOption, IntOption, EnumOption {
        String id();

        String displayName();

        String description();

        String currentAsString();

        String usageValueHint();
    }

    public record BoolOption(
            String id,
            String displayName,
            String description,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter) implements Option {
        public boolean get() {
            return getter.get();
        }

        public void set(boolean value) {
            setter.accept(value);
        }

        @Override
        public String currentAsString() {
            return Boolean.toString(get());
        }

        @Override
        public String usageValueHint() {
            return "<true|false>";
        }
    }

    public record IntOption(
            String id,
            String displayName,
            String description,
            int min,
            int max,
            int step,
            int defaultValue,
            IntSupplier getter,
            IntConsumer setter) implements Option {
        public int get() {
            return getter.getAsInt();
        }

        public void set(int value) {
            setter.accept(value);
        }

        public int clamp(int value) {
            return Math.max(min, Math.min(max, value));
        }

        @Override
        public String currentAsString() {
            return Integer.toString(get());
        }

        @Override
        public String usageValueHint() {
            return "<" + min + "-" + max + ">";
        }
    }

    public record EnumOption<E extends Enum<E>>(
            String id,
            String displayName,
            String description,
            Class<E> type,
            Supplier<E> getter,
            Consumer<E> setter,
            java.util.function.Function<String, E> parser) implements Option {
        public E get() {
            return getter.get();
        }

        public void set(E value) {
            setter.accept(value);
        }

        public E parse(String raw) {
            return parser.apply(raw);
        }

        public List<String> suggestions() {
            return Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
        }

        @Override
        public String currentAsString() {
            return get().name();
        }

        @Override
        public String usageValueHint() {
            return "<" + String.join("|", suggestions()) + ">";
        }
    }
}
