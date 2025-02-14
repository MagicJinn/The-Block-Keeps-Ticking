package net.what42.aliveworld.mixin;

import net.what42.aliveworld.api.TickableChunk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    private static final String LAST_UPDATE_KEY = "AliveWorld_LastUpdateTime";
    
    @Inject(method = "serialize", at = @At("RETURN"))
    private static void onSave(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        if (chunk instanceof TickableChunk tickableChunk) {
            NbtCompound nbt = cir.getReturnValue();
            // Guardar el tiempo actual del mundo si nunca se ha actualizado
            long timeToSave = tickableChunk.getLastUpdateTime();
            if (timeToSave == -1L) {
                timeToSave = world.getTime();
            }
            nbt.putLong(LAST_UPDATE_KEY, timeToSave);
        }
    }

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void onRead(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk instanceof TickableChunk tickableChunk) {
            // Si no existe el tiempo guardado, usar el tiempo actual
            long savedTime = nbt.contains(LAST_UPDATE_KEY) ? nbt.getLong(LAST_UPDATE_KEY) : world.getTime();
            tickableChunk.setLastUpdateTime(savedTime);
        }
    }
}
