package magicjinn.theblockkeepsticking.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.config.ModConfig;

/**
 * Lightweight profiler for the world simulation.
 *
 * <p>
 * Instead of logging every single timing (which floods the log), measurements
 * are aggregated per name and a compact summary is flushed at most once every
 * {@link #FLUSH_INTERVAL_MS} milliseconds.
 *
 * <p>
 * Every public entry point short-circuits when debug logging is disabled, so
 * this class adds no measurable runtime cost when {@code debugLogging} is off.
 */
public class Benchmarker {

    private static final long FLUSH_INTERVAL_MS = 5000L;

    // Stopwatch starts for currently open named sections.
    private static final Map<String, Long> activeBenchmarks = new HashMap<>();
    // Timings of nested code sections (e.g. "SimulateWorld.scan").
    private static final Map<String, Stat> sectionStats = new HashMap<>();
    // Timings of individual ticking objects (e.g. "Leaves", "Cactus").
    private static final Map<String, Stat> objectStats = new HashMap<>();
    // Simple event / quantity counters (inhabited skips, cells, flushes).
    private static final Map<String, Long> counters = new HashMap<>();

    private static long windowStartMs = 0L;

    private static final class Stat {
        long count;
        long totalNanos;
        long minNanos = Long.MAX_VALUE;
        long maxNanos = Long.MIN_VALUE;
        long changedCount; // how many calls actually mutated the world

        void add(long nanos, boolean changed) {
            count++;
            totalNanos += nanos;
            if (nanos < minNanos)
                minNanos = nanos;
            if (nanos > maxNanos)
                maxNanos = nanos;
            if (changed)
                changedCount++;
        }
    }

    public static void StartBenchmark(String name) {
        if (!ModConfig.isDebugLogging())
            return;

        activeBenchmarks.put(name, System.nanoTime());
    }

    public static void EndBenchmark(String name) {
        if (!ModConfig.isDebugLogging())
            return;

        Long startTime = activeBenchmarks.remove(name);
        if (startTime == null) {
            TheBlockKeepsTicking.LOGGER
                    .warn("Guhh?? Benchmark EndBenchmark(\"{}\") called without matching StartBenchmark", name);
            return;
        }
        record(sectionStats, name, System.nanoTime() - startTime, false);
    }

    /**
     * Records a single ticking-object simulation timing.
     * 
     * @param name          the ticking object name (e.g. "Leaves")
     * @param durationNanos how long the simulation took, in nanoseconds
     * @param changed       whether the simulation actually took place
     */
    public static void RecordObject(String name, long durationNanos, boolean changed) {
        if (!ModConfig.isDebugLogging())
            return;
        record(objectStats, name, durationNanos, changed);
    }

    /** Increments a named counter by 1. */
    public static void RecordCounter(String name) {
        RecordCounter(name, 1L);
    }

    /** Increments a named counter by {@code amount}. */
    public static void RecordCounter(String name, long amount) {
        if (!ModConfig.isDebugLogging() || amount == 0L)
            return;
        counters.merge(name, amount, Long::sum);
    }

    public static void MaybeFlush() {
        if (!ModConfig.isDebugLogging())
            return;

        long now = System.currentTimeMillis();
        if (windowStartMs == 0L)
            windowStartMs = now;

        if (now - windowStartMs < FLUSH_INTERVAL_MS)
            return;

        if (!sectionStats.isEmpty() || !objectStats.isEmpty() || !counters.isEmpty())
            flush(now - windowStartMs);

        windowStartMs = now;
    }

    private static void record(Map<String, Stat> map, String name, long nanos, boolean changed) {
        Stat stat = map.get(name);
        if (stat == null) {
            stat = new Stat();
            map.put(name, stat);
        }
        stat.add(nanos, changed);
    }

    private static void flush(long windowMs) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(String.format(Locale.ROOT, "Simulation profile (last %.1fs)", windowMs / 1000.0));

        if (!counters.isEmpty()) {
            sb.append("\n  Counters:");
            List<Map.Entry<String, Long>> entries = new ArrayList<>(counters.entrySet());
            entries.sort(Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed());
            for (Map.Entry<String, Long> entry : entries) {
                sb.append(String.format(Locale.ROOT, "\n    %-22s %d", entry.getKey(), entry.getValue()));
            }
        }
        if (!sectionStats.isEmpty()) {
            sb.append("\n  Sections:");
            appendStats(sb, sectionStats, false);
        }
        if (!objectStats.isEmpty()) {
            sb.append("\n  Ticking objects (by total time):");
            appendStats(sb, objectStats, true);
        }

        TheBlockKeepsTicking.LOGGER.info(sb.toString());

        sectionStats.clear();
        objectStats.clear();
        counters.clear();
    }

    private static void appendStats(StringBuilder sb, Map<String, Stat> map, boolean showChanged) {
        List<Map.Entry<String, Stat>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparingLong((Map.Entry<String, Stat> e) -> e.getValue().totalNanos).reversed());

        for (Map.Entry<String, Stat> entry : entries) {
            Stat s = entry.getValue();
            long avg = s.count > 0 ? s.totalNanos / s.count : 0L;
            sb.append(String.format(Locale.ROOT,
                    "\n    %-22s x%-6d avg %-9s min %-9s max %-9s total %s",
                    entry.getKey(), s.count,
                    formatNanos(avg), formatNanos(s.minNanos), formatNanos(s.maxNanos),
                    formatNanos(s.totalNanos)));
            if (showChanged)
                sb.append(String.format(Locale.ROOT, "  (changed %d)", s.changedCount));
        }
    }

    /**
     * Formats a nanosecond duration using the most readable sub-unit (ns, µs, ms).
     */
    private static String formatNanos(long nanos) {
        if (nanos < 1_000L)
            return nanos + "ns";
        if (nanos < 1_000_000L)
            return String.format(Locale.ROOT, "%.1fus", nanos / 1_000.0);
        return String.format(Locale.ROOT, "%.2fms", nanos / 1_000_000.0);
    }
}
