package magicjinn.theblockkeepsticking.mixin;

import java.util.concurrent.Executor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import magicjinn.theblockkeepsticking.platform.NeoForgePlatformHelper;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * NeoForge equivalent of Fabric's FULL_CHUNK_STATUS_CHANGE tracking. NeoForge
 * doesn't expose identical API's, so this will do.
 */
@Mixin(ChunkHolder.class)
public abstract class NeoForgeChunkHolderMixin extends GenerationChunkHolder {
    @Shadow
    @Final
    private LevelHeightAccessor levelHeightAccessor;

    @Shadow
    private int oldTicketLevel;

    @Unique
    private static final FullChunkStatus[] TBKT_STATUSES = FullChunkStatus.values();

    @Unique
    private FullChunkStatus tbkt$currentStatus = FullChunkStatus.INACCESSIBLE;

    private NeoForgeChunkHolderMixin(ChunkPos pos) {
        super(pos);
    }

    @Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 0))
    private void tbkt$inaccessibleToFull(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        if (this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk chunk
                && this.tbkt$currentStatus == FullChunkStatus.INACCESSIBLE) {
            NeoForgePlatformHelper.fireChunkFullStatusChange((ServerLevel) levelHeightAccessor,
                    chunk, FullChunkStatus.INACCESSIBLE, FullChunkStatus.FULL);
            this.tbkt$currentStatus = FullChunkStatus.FULL;
        }
    }

    @Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void tbkt$fullToBlockTicking(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        if (this.tbkt$currentStatus == FullChunkStatus.FULL
                && this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk chunk) {
            NeoForgePlatformHelper.fireChunkFullStatusChange((ServerLevel) levelHeightAccessor,
                    chunk, FullChunkStatus.FULL, FullChunkStatus.BLOCK_TICKING);
            this.tbkt$currentStatus = FullChunkStatus.BLOCK_TICKING;
        }
    }

    @Inject(method = "updateFutures", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V", shift = At.Shift.AFTER, ordinal = 2))
    private void tbkt$blockTickingToEntityTicking(ChunkMap chunkMap, Executor executor,
            CallbackInfo ci) {
        if (this.tbkt$currentStatus == FullChunkStatus.BLOCK_TICKING
                && this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk chunk) {
            NeoForgePlatformHelper.fireChunkFullStatusChange((ServerLevel) levelHeightAccessor,
                    chunk, FullChunkStatus.BLOCK_TICKING, FullChunkStatus.ENTITY_TICKING);
            this.tbkt$currentStatus = FullChunkStatus.ENTITY_TICKING;
        }
    }

    @Inject(method = "demoteFullChunk", at = @At("HEAD"))
    private void tbkt$demote(ChunkMap chunkMap, FullChunkStatus target, CallbackInfo ci) {
        FullChunkStatus previous = ChunkLevel.fullStatus(this.oldTicketLevel);
        ServerLevel serverLevel = (ServerLevel) levelHeightAccessor;
        if (!(this.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk chunk)) {
            this.tbkt$currentStatus = target;
            return;
        }

        for (int i = previous.ordinal(); i > target.ordinal(); i--) {
            FullChunkStatus oldStatus = TBKT_STATUSES[i];
            FullChunkStatus newStatus = TBKT_STATUSES[i - 1];
            if (this.tbkt$currentStatus.isOrAfter(oldStatus)) {
                NeoForgePlatformHelper.fireChunkFullStatusChange(serverLevel, chunk, oldStatus,
                        newStatus);
                this.tbkt$currentStatus = newStatus;
            }
        }
    }
}
