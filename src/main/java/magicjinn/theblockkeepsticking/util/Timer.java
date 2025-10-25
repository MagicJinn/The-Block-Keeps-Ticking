package magicjinn.theblockkeepsticking.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Consumer;

// Changing blockstates during chunk loading is VERY bad, so we use a timer to schedule the changes
// for later.
public class Timer implements ServerTickEvents.EndTick {
    public static final Timer INSTANCE = new Timer();
    private final Map<String, TimerEntry> pendingActions = new ConcurrentHashMap<>();

    private static class TimerEntry {
        private long ticksRemaining;
        private final Consumer<MinecraftServer> callback;

        public TimerEntry(long ticksRemaining, Consumer<MinecraftServer> callback) {
            this.ticksRemaining = ticksRemaining;
            this.callback = callback;
        }

        public boolean tick() {
            if (--this.ticksRemaining <= 0) {
                return true; // Ready to execute
            }
            return false; // Still waiting
        }

        public void execute(MinecraftServer server) {
            callback.accept(server);
        }
    }

    public void Schedule(String id, Consumer<MinecraftServer> callback) {
        Schedule(id, 40L, callback); // Default to 40 ticks (2 seconds)
    }

    public void Schedule(String id, long ticksUntilAction, Consumer<MinecraftServer> callback) {
        this.pendingActions.put(id, new TimerEntry(ticksUntilAction, callback));
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
                timerEntry.execute(server);
                return true; // Remove this entry
            }
            return false; // Keep this entry
        });
    }

    public static void Register() {
        ServerTickEvents.END_SERVER_TICK.register(INSTANCE);
    }
}
