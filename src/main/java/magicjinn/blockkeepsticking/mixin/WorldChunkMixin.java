package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.TickableChunk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Inject(method = "toNbt", at = @At("RETURN"))
    private void onSerialize(CallbackInfoReturnable<NbtCompound> cir) {
        if ((Object) this instanceof TickableChunk tickableChunk) {
            NbtCompound nbt = cir.getReturnValue();
            WorldChunk chunk = (WorldChunk) (Object) this;

            long timeToSave = tickableChunk.getLastUpdateTime();
            if (timeToSave == -1L) {
                timeToSave = chunk.getWorld().getTime();
            }
            nbt.putLong(BlockKeepsTicking.LAST_UPDATE_KEY, timeToSave);
        }
    }
}