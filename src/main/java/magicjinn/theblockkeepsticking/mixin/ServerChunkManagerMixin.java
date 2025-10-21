package magicjinn.theblockkeepsticking.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.simulator.WorldSimulator;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow @Final private ChunkLevelManager levelManager;

    // Track chunks that were in simulation distance last tick
    private final LongOpenHashSet previousSimulationChunks = new LongOpenHashSet();

    @Inject(method = "tickChunks", at = @At("HEAD"))
    private void onTickChunks(CallbackInfo ci) {
        LongOpenHashSet currentSimulationChunks = new LongOpenHashSet();

        // Collect all chunks currently in simulation distance
        this.levelManager.forEachBlockTickingChunk(chunkPos -> {
            currentSimulationChunks.add(chunkPos);
        });

        if (currentSimulationChunks.isEmpty())
            return; // Temp?

        // Find chunks that entered simulation distance
        for (long chunkPos : currentSimulationChunks) {
            if (!this.previousSimulationChunks.contains(chunkPos)) {
                onChunkEnterSimulation(chunkPos);
            }
        }
        // Find chunks that left simulation distance
        for (long chunkPos : this.previousSimulationChunks) {
            if (!currentSimulationChunks.contains(chunkPos)) {
                onChunkLeaveSimulation(chunkPos);
            }
        }

        this.previousSimulationChunks.clear();
        this.previousSimulationChunks.addAll(currentSimulationChunks);
    }

    @Nullable
    private WorldChunk getChunkFromPos(long chunkPos) {
        ChunkHolder holder = this.levelManager.getChunkHolder(chunkPos);
        if (holder != null) {
            return holder.getWorldChunk();
        }
        return null;
    }

    private void onChunkEnterSimulation(long chunkPos) {
        WorldChunk chunk = getChunkFromPos(chunkPos);
        WorldSimulator.SimulateWorld(chunk);
    }

    private void onChunkLeaveSimulation(long chunkPos) {
        WorldChunk chunk = getChunkFromPos(chunkPos);
        if (chunk == null) return;

        chunk.setAttached(TheBlockKeepsTicking.LAST_UPDATE_TIME, chunk.getWorld().getTime());
    }
}
