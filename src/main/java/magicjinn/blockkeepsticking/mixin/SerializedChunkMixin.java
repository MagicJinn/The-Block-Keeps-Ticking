package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.SerializedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializedChunk.class)
public class SerializedChunkMixin {
    @Inject(method = "serialize", at = @At("RETURN"))
    private void onSerialize(CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound nbt = cir.getReturnValue();
        SerializedChunk serialized = (SerializedChunk) (Object) this;
        nbt.putLong(BlockKeepsTicking.LAST_UPDATE_KEY, serialized.lastUpdateTime());
    }
}
