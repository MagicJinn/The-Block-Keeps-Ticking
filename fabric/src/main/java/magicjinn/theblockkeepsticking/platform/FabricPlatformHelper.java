package magicjinn.theblockkeepsticking.platform;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.Constants;
import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;
import magicjinn.theblockkeepsticking.platform.services.ITBKTPlatform;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class FabricPlatformHelper implements ITBKTPlatform {
    private AttachmentType<Long> lastUpdateTime;
    private AttachmentType<Long> lastRealtimeUpdateMs;

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public String getPublishedModId() {
        return Constants.FABRIC_MOD_ID;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerChunkAttachments() {
        // Namespace must stay FABRIC_MOD_ID so existing worlds keep their chunk timestamps.
        // NeoForge cannot use this hyphenated namespace (mod id rules) and also serializes
        // attachments under a different NBT scheme, so those timestamps do not survive a
        // Fabric <-> NeoForge world switch. Vanilla block/entity data is unaffected.
        lastUpdateTime = AttachmentRegistry.createPersistent(
                Identifier.fromNamespaceAndPath(Constants.FABRIC_MOD_ID, "last_update_time"),
                Codec.LONG);
        lastRealtimeUpdateMs = AttachmentRegistry.createPersistent(
                Identifier.fromNamespaceAndPath(Constants.FABRIC_MOD_ID, "last_realtime_update_ms"),
                Codec.LONG);
    }

    @Override
    public long getChunkLong(LevelChunk chunk, ChunkLongKey key, long defaultValue) {
        return chunk.getAttachedOrSet(attachment(key), defaultValue);
    }

    @Override
    public void setChunkLong(LevelChunk chunk, ChunkLongKey key, long value) {
        chunk.setAttached(attachment(key), value);
    }

    private AttachmentType<Long> attachment(ChunkLongKey key) {
        return switch (key) {
            case LAST_UPDATE_TIME -> lastUpdateTime;
            case LAST_REALTIME_UPDATE_MS -> lastRealtimeUpdateMs;
        };
    }

    @Override
    public void registerServerTickEnd(Consumer<MinecraftServer> callback) {
        ServerTickEvents.END_SERVER_TICK.register(callback::accept);
    }

    @Override
    public void registerServerStopped(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STOPPED.register(callback::accept);
    }

    @Override
    public void registerChunkFullStatusChange(ChunkFullStatusCallback callback) {
        ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.register(callback::onChange);
    }

    @Override
    public void invokeTickingBlockEntrypoints(InitializeTickingBlocks.TickingBlockRegistry registry) {
        List<EntrypointContainer<InitializeTickingBlocks>> entrypoints = FabricLoader.getInstance()
                .getEntrypointContainers(Constants.ENTRYPOINT_KEY, InitializeTickingBlocks.class);

        for (EntrypointContainer<InitializeTickingBlocks> container : entrypoints) {
            String modId = container.getProvider().getMetadata().getId();
            try {
                Constants.LOG.info("Loading ticking blocks from mod: {}", modId);
                container.getEntrypoint().registerTickingBlocks(registry);
            } catch (Throwable e) {
                Constants.LOG.error("Failed to load ticking blocks from mod {}: {}", modId,
                        e.getMessage(), e);
            }
        }
    }

    @Override
    public float getCropGrowthSpeed(Block block, BlockState state, BlockGetter level, BlockPos pos) {
        return CropBlock.getGrowthSpeed(block, level, pos);
    }
}
