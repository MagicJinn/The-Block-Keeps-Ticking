package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.util.Benchmarker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Spreads each chunk catch-up across at most {@link #MAX_SPREAD_TICKS} server
 * ticks.
 * Enqueued work is never dropped. The last tick flushes whatever remains.
 */
public final class ChunkSimScheduler implements ServerTickEvents.EndTick {
    public static final ChunkSimScheduler INSTANCE = new ChunkSimScheduler();

    /** Max ticks to spread one job's block scan across (inclusive flush tick). */
    public static final int MAX_SPREAD_TICKS = 20;

    /** One tick delay before first slice, so we do not mutate during chunk load. */
    private static final int START_DELAY_TICKS = 1;

    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();

    private ChunkSimScheduler() {
    }

    public static void Register() {
        ServerTickEvents.END_SERVER_TICK.register(INSTANCE);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCE.clear());
    }

    public void clear() {
        jobs.clear();
    }

    /**
     * Enqueues catch-up for {@code chunk}, or coalesces into an existing job.
     * Returns false if skipped (inhabited gate / nothing to do).
     */
    public boolean enqueue(LevelChunk chunk, long ticksToSimulate) {
        if (chunk == null)
            return false;

        int minInhabited = ModConfig.getMinInhabitedTicks();
        if (minInhabited > 0 && chunk.getInhabitedTime() < minInhabited) {
            Benchmarker.RecordCounter("inhabitedSkips");
            return false;
        }

        long ticks = ModConfig.applyLazyTax(ticksToSimulate);
        if (ticks <= 0)
            return false;

        long key = jobKey(chunk);
        jobs.compute(key, (k, existing) -> {
            if (existing == null)
                return new Job(chunk, ticks);
            existing.ticksToSimulate = Math.max(existing.ticksToSimulate, ticks);
            // Keep the live chunk reference in case of reload of the same pos.
            existing.chunk = chunk;
            return existing;
        });
        return true;
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (jobs.isEmpty())
            return;

        ArrayList<Long> finished = null;
        for (Map.Entry<Long, Job> entry : jobs.entrySet()) {
            Job job = entry.getValue();
            try {
                if (advance(job)) {
                    if (finished == null)
                        finished = new ArrayList<>();
                    finished.add(entry.getKey());
                }
            } catch (Exception e) {
                TheBlockKeepsTicking.LOGGER.error("Error advancing chunk simulation job: ", e);
                if (finished == null)
                    finished = new ArrayList<>();
                finished.add(entry.getKey());
            }
        }

        if (finished != null) {
            for (Long key : finished)
                jobs.remove(key);
        }

        Benchmarker.MaybeFlush();
    }

    /**
     * @return true if the job is complete and should be removed
     */
    private boolean advance(Job job) {
        LevelChunk chunk = job.chunk;
        if (chunk == null || chunk.getLevel() == null)
            return true;

        // Still waiting out the post-load delay
        if (job.startDelayRemaining > 0) {
            job.startDelayRemaining--;
            return false;
        }

        if (job.cursor == null) {
            job.cursor = WorldSimulator.createScanCursor(chunk);
            job.totalCells = job.cursor.totalCells;
        }

        boolean flush = job.spreadTicksRemaining <= 1;
        int remainingTicks = Math.max(1, job.spreadTicksRemaining);
        int remainingCells = Math.max(0, job.totalCells - job.cursor.cellsVisited);
        int quota = flush ? Integer.MAX_VALUE
                : (remainingCells + remainingTicks - 1) / remainingTicks;

        Benchmarker.RecordCounter(flush ? "jobFlushes" : "jobSlices");

        Benchmarker.StartBenchmark("SimulateWorld");
        try {
            if (!job.cursor.done) {
                WorldSimulator.simulateBlockSlice(chunk, job.ticksToSimulate, job.cursor, quota);
            }

            if (job.cursor.done && !job.postScanDone) {
                WorldSimulator.simulateBlockEntities(chunk, job.ticksToSimulate);
                WorldSimulator.simulateEntities(chunk, job.ticksToSimulate);
                job.postScanDone = true;
            }
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld");
        }

        job.spreadTicksRemaining--;

        if (job.cursor.done && job.postScanDone)
            return true;

        // Safety. If somehow not done after flush tick, keep going.
        // (should not happen when quota is MAX on last tick).
        if (job.spreadTicksRemaining <= 0) {
            if (!job.cursor.done) {
                WorldSimulator.simulateBlockSlice(chunk, job.ticksToSimulate, job.cursor,
                        Integer.MAX_VALUE);
            }
            if (!job.postScanDone) {
                WorldSimulator.simulateBlockEntities(chunk, job.ticksToSimulate);
                WorldSimulator.simulateEntities(chunk, job.ticksToSimulate);
                job.postScanDone = true;
            }
            return true;
        }

        return false;
    }

    private static long jobKey(LevelChunk chunk) {
        Level level = chunk.getLevel();
        long dim = level != null ? level.dimension().hashCode() : 0L;
        return (dim << 32) ^ chunk.getPos().pack();
    }

    private static final class Job {
        LevelChunk chunk;
        long ticksToSimulate;
        int startDelayRemaining = START_DELAY_TICKS;
        int spreadTicksRemaining = MAX_SPREAD_TICKS;
        WorldSimulator.ScanCursor cursor;
        int totalCells;
        boolean postScanDone;

        Job(LevelChunk chunk, long ticksToSimulate) {
            this.chunk = chunk;
            this.ticksToSimulate = ticksToSimulate;
        }
    }
}
