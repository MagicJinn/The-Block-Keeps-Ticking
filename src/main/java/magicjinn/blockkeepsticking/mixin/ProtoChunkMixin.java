package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.TickableChunk;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin implements TickableChunk {

	@Inject(method = "readNbt", at = @At("RETURN"))
	private void onDeserialize(NbtCompound nbt, CallbackInfo ci) {
		if ((Object) this instanceof TickableChunk tickableChunk) {
			long savedTime = nbt.getLong(BlockKeepsTicking.LAST_UPDATE_KEY).orElse(-1L);
			// ? nbt.getLong(BlockKeepsTicking.LAST_UPDATE_KEY)
			// : -1L;
			tickableChunk.setLastUpdateTime(savedTime);
		}
	}

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