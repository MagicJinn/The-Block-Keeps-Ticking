package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.TickableChunk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializedChunk.class)
public class SerializedChunkMixin {
	@Inject(method = "serialize", at = @At("RETURN"))
	private void onSave(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
		if (chunk instanceof TickableChunk tickableChunk) {
			NbtCompound nbt = cir.getReturnValue();
			// Save the current world time if it has never been updated
			long timeToSave = tickableChunk.getLastUpdateTime();
			if (timeToSave == -1L) {
				timeToSave = world.getTime();
			}
			nbt.putLong(BlockKeepsTicking.LAST_UPDATE_KEY, timeToSave);
		}
	}

	@Inject(method = "convert", at = @At("RETURN"))
	private void onConvert(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos,
			NbtCompound nbt, CallbackInfoReturnable<Chunk> cir) {
		Chunk chunk = cir.getReturnValue();
		if (chunk instanceof TickableChunk tickableChunk) {
			// If there is no saved time, use the current time
			long savedTime = nbt.getLong(BlockKeepsTicking.LAST_UPDATE_KEY).orElse(world.getTime());
			tickableChunk.setLastUpdateTime(savedTime);
		}
	}
}
