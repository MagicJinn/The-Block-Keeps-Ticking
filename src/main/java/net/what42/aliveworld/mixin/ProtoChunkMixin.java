package net.what42.aliveworld.mixin;

import net.what42.aliveworld.api.TickableChunk;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin implements TickableChunk {
    @Unique
    private long lastUpdateTime;

    @Override
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }
}