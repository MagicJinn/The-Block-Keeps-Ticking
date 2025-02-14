package net.what42.aliveworld.manager;

import net.what42.aliveworld.AliveWorld;
import net.what42.aliveworld.api.TickableChunk;
import net.what42.aliveworld.simulator.ChunkLifeSimulator;
import net.what42.aliveworld.event.ChunkUnloadCallback;
import net.what42.aliveworld.util.TimeUtils;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import java.util.WeakHashMap;

public class ChunkManager implements ChunkUnloadCallback {
    private static final ChunkManager INSTANCE = new ChunkManager();
    private final WeakHashMap<WorldChunk, Long> lastProcessTimes = new WeakHashMap<>();
    
    private ChunkManager() {}

    public static ChunkManager getInstance() {
        return INSTANCE;
    }

    public void processChunk(WorldChunk chunk) {
        if (chunk == null || !(chunk.getWorld() instanceof ServerWorld)) {
            return;
        }

        // Solo procesar si hay entidades que nos interesan
        if (!hasProcessableEntities(chunk)) {
            return;
        }

        long currentTime = chunk.getWorld().getTime();
        Long lastProcess = lastProcessTimes.get(chunk);
        
        if (lastProcess == null) {
            if (chunk instanceof TickableChunk tickable) {
                lastProcess = tickable.getLastUpdateTime();
            }
        }

        long timePassed = TimeUtils.calculateTimePassed(currentTime, lastProcess != null ? lastProcess : -1L);
        
        if (timePassed >= TimeUtils.MIN_PROCESS_INTERVAL) {
                
            ChunkLifeSimulator.simulate(chunk, timePassed);
            
            lastProcessTimes.put(chunk, currentTime);
            if (chunk instanceof TickableChunk tickable) {
                tickable.setLastUpdateTime(currentTime);
            }
        }
    }

    public void processAllPendingChunks() {
        for (WorldChunk chunk : new WeakHashMap<>(lastProcessTimes).keySet()) {
            if (chunk != null && chunk.getWorld() != null && hasProcessableEntities(chunk)) {
                processChunk(chunk);
            }
        }
    }

    private boolean hasProcessableEntities(WorldChunk chunk) {
        boolean hasEntities = false;
        for (var blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof AbstractFurnaceBlockEntity ||
                blockEntity instanceof BrewingStandBlockEntity ||
                blockEntity instanceof CampfireBlockEntity) {
                hasEntities = true;
            }
        }
        return hasEntities;
    }

    @Override
    public void onChunkUnload(WorldChunk chunk) {
        if (hasProcessableEntities(chunk)) {
            processChunk(chunk);
        }
        lastProcessTimes.remove(chunk);
    }
}