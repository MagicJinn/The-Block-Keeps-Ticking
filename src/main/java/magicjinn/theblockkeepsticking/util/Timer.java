package magicjinn.theblockkeepsticking.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// Changing blockstates during chunk loading is VERY bad,
// so we use a timer to schedule the changes for later.
public class Timer implements ServerTickEvents.EndTick {
    public static final Timer INSTANCE = new Timer();
    private final Map<String, TimerEntry> pendingActions = new ConcurrentHashMap<>();

    private static class TimerEntry {
        private long ticksRemaining;
        private final BiConsumer<MinecraftServer, WorldChunk> callback;
        private final WorldChunk chunk;

        public TimerEntry(long ticksRemaining, BiConsumer<MinecraftServer, WorldChunk> callback,
                WorldChunk chunk) {
            this.ticksRemaining = ticksRemaining;
            this.callback = callback;
            this.chunk = chunk;
        }

        public boolean tick() {
            if (--this.ticksRemaining <= 0) {
                return true; // Ready to execute
            }
            return false; // Still waiting
        }

        public void execute(MinecraftServer server) {
            callback.accept(server, chunk);
        }
    }

    public void Schedule(String id, BiConsumer<MinecraftServer, WorldChunk> callback,
            WorldChunk chunk) {
        Schedule(id, 1L, callback, chunk);
    }

    public void Schedule(String id, long ticksUntilAction,
            BiConsumer<MinecraftServer, WorldChunk> callback, WorldChunk chunk) {
        this.pendingActions.put(id, new TimerEntry(ticksUntilAction, callback, chunk));
    }

    public void Cancel(String id) {
        this.pendingActions.remove(id);
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        // Process all pending actions
        pendingActions.entrySet().removeIf(entry -> {
            TimerEntry timerEntry = entry.getValue();

            if (timerEntry.tick()) {
                try {
                    timerEntry.execute(server);
                } catch (Exception e) {
                    // Log error but don't crash the server
                    System.err.println("Error executing timer callback: " + e.getMessage());
                }
                return true; // Remove this entry
            }
            return false; // Keep this entry
        });
    }

    /**
     * Clears all pending actions. Should be called during server shutdown to prevent callbacks from
     * executing with null references.
     */
    public void clearAllPendingActions() {
        pendingActions.clear();
    }

    public static void RegisterShutdownEvent() {
        ServerTickEvents.END_SERVER_TICK.register(INSTANCE);

        // Register server shutdown event to clear pending actions
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            INSTANCE.clearAllPendingActions();
        });
    }
}
