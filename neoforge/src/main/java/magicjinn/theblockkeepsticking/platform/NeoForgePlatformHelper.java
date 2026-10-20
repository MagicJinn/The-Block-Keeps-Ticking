package magicjinn.theblockkeepsticking.platform;

import java.nio.file.Path;
import java.util.function.Consumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import magicjinn.theblockkeepsticking.Constants;
import magicjinn.theblockkeepsticking.api.InitializeTickingBlocks;
import magicjinn.theblockkeepsticking.neoforge.event.RegisterTickingBlocksEvent;
import magicjinn.theblockkeepsticking.platform.services.ITBKTPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NeoForgePlatformHelper implements ITBKTPlatform {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LAST_UPDATE_TIME =
            ATTACHMENT_TYPES.register("last_update_time",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG.fieldOf("value")).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LAST_REALTIME_UPDATE_MS =
            ATTACHMENT_TYPES.register("last_realtime_update_ms",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG.fieldOf("value")).build());

    private static volatile ChunkFullStatusCallback chunkStatusCallback;

    public static void registerAttachments(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    public static void fireChunkFullStatusChange(ServerLevel level, LevelChunk chunk,
            FullChunkStatus oldStatus, FullChunkStatus newStatus) {
        ChunkFullStatusCallback callback = chunkStatusCallback;
        if (callback != null) {
            callback.onChange(level, chunk, oldStatus, newStatus);
        }
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getPublishedModId() {
        return Constants.MOD_ID;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void registerChunkAttachments() {
        // Registered on the mod bus from the NeoForge entrypoint.
    }

    @Override
    public long getChunkLong(LevelChunk chunk, ChunkLongKey key, long defaultValue) {
        AttachmentType<Long> type = attachment(key);
        if (!chunk.hasData(type)) {
            chunk.setData(type, defaultValue);
            return defaultValue;
        }
        return chunk.getData(type);
    }

    @Override
    public void setChunkLong(LevelChunk chunk, ChunkLongKey key, long value) {
        chunk.setData(attachment(key), value);
    }

    @SuppressWarnings("unchecked")
    private AttachmentType<Long> attachment(ChunkLongKey key) {
        return (AttachmentType<Long>) switch (key) {
            case LAST_UPDATE_TIME -> LAST_UPDATE_TIME.get();
            case LAST_REALTIME_UPDATE_MS -> LAST_REALTIME_UPDATE_MS.get();
        };
    }

    @Override
    public void registerServerTickEnd(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,
                event -> callback.accept(event.getServer()));
    }

    @Override
    public void registerServerStarted(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class,
                event -> callback.accept(event.getServer()));
    }

    @Override
    public void registerServerStopped(Consumer<MinecraftServer> callback) {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class,
                event -> callback.accept(event.getServer()));
    }

    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class,
                event -> callback.accept(event.getDispatcher()));
    }

    @Override
    public void registerChunkFullStatusChange(ChunkFullStatusCallback callback) {
        chunkStatusCallback = callback;
    }

    @Override
    public void invokeTickingBlockEntrypoints(InitializeTickingBlocks.TickingBlockRegistry registry) {
        NeoForge.EVENT_BUS.post(new RegisterTickingBlocksEvent(registry));
    }

    @Override
    public float getCropGrowthSpeed(Block block, BlockState state, BlockGetter level, BlockPos pos) {
        return CropBlock.getGrowthSpeed(state, level, pos);
    }
}
