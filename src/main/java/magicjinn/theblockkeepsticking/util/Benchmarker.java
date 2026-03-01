package magicjinn.theblockkeepsticking.util;

import java.util.HashMap;
import java.util.Map;

import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.config.ModConfig;

public class Benchmarker {

    private static final Map<String, Long> benchmarkTimes = new HashMap<>();

    public static void StartBenchmark(String name) {
        if (!ModConfig.isDebugLogging())
            return;

        long startTime = System.nanoTime();
        benchmarkTimes.put(name, startTime);
    }

    public static void EndBenchmark(String name) {
        if (!ModConfig.isDebugLogging())
            return;

        Long startTime = benchmarkTimes.get(name);
        if (startTime == null) {
            TheBlockKeepsTicking.LOGGER.warn("Benchmark EndBenchmark(\"{}\") called without matching StartBenchmark", name);
            return;
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        TheBlockKeepsTicking.LOGGER.info("Benchmark {} took {}ms", name, duration / 1000000);
    }
}
