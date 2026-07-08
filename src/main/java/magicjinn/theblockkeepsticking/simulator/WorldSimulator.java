package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.TriConsumer;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.blocks.TickingAbstractFurnaceBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingBambooSaplingBlock;
import magicjinn.theblockkeepsticking.blocks.TickingBambooStalkBlock;
import magicjinn.theblockkeepsticking.blocks.TickingBrewingStandBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingBuddingAmethystBlock;
import magicjinn.theblockkeepsticking.blocks.TickingCactusBlock;
import magicjinn.theblockkeepsticking.blocks.TickingCampfireBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingCocoaBlock;
import magicjinn.theblockkeepsticking.blocks.TickingCropBlock;
import magicjinn.theblockkeepsticking.blocks.TickingDriedGhastBlock;
import magicjinn.theblockkeepsticking.blocks.TickingKelpBlock;
import magicjinn.theblockkeepsticking.blocks.TickingLeavesBlock;
import magicjinn.theblockkeepsticking.blocks.TickingNetherWartBlock;
import magicjinn.theblockkeepsticking.blocks.TickingPointedDripstoneBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSaplingBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSnifferEggBlock;
import magicjinn.theblockkeepsticking.blocks.TickingStemBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSugarCaneBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSweetBerryBushBlock;
import magicjinn.theblockkeepsticking.config.ModConfig;
import magicjinn.theblockkeepsticking.entities.TickingAgeableMob;
import magicjinn.theblockkeepsticking.util.Benchmarker;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class WorldSimulator {
    public static final ArrayList<TickingObject> TickingObjectInstances = new ArrayList<>();

    private static final ArrayList<TickingObject> TickingBlockInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingBlockEntityInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingEntityInstances = new ArrayList<>();

    // Blocks are singletons, so the matching TickingObject for a given Block never
    // changes. Cache the result (NO_MATCH for blocks that match nothing) so the
    // hot scan loop does one identity lookup instead of ~18 isInstance checks.
    private static final TickingObject NO_MATCH = new TickingObject() {
        public Class<?> getType() {
            return Void.class;
        }

        public String getName() {
            return "";
        }

        public boolean Simulate(Object o, long t, Level l, BlockState s, BlockPos p) {
            return false;
        }
    };
    private static final Map<Block, TickingObject> blockMatchCache = new IdentityHashMap<>();

    // Palette-level test: does a block state map to any ticking object? Used to
    // skip whole chunk sections whose palette contains no ticking blocks.
    private static final Predicate<BlockState> HAS_TICKING_BLOCK = state -> resolveTickingBlock(
            state.getBlock()) != NO_MATCH;

    public static void InitializeTickingBlocks() {
        // Register built-in ticking blocks
        RegisterTickingBlock(new TickingAbstractFurnaceBlockEntity());
        RegisterTickingBlock(new TickingCampfireBlockEntity());
        RegisterTickingBlock(new TickingBrewingStandBlockEntity());

        RegisterTickingBlock(new TickingCropBlock());
        RegisterTickingBlock(new TickingStemBlock());
        RegisterTickingBlock(new TickingNetherWartBlock());
        RegisterTickingBlock(new TickingCocoaBlock());

        RegisterTickingBlock(new TickingSaplingBlock());
        RegisterTickingBlock(new TickingLeavesBlock());

        RegisterTickingBlock(new TickingKelpBlock());
        RegisterTickingBlock(new TickingBambooSaplingBlock());
        RegisterTickingBlock(new TickingBambooStalkBlock());
        RegisterTickingBlock(new TickingSugarCaneBlock());
        RegisterTickingBlock(new TickingCactusBlock());

        RegisterTickingBlock(new TickingAgeableMob()); // Includes chickens

        RegisterTickingBlock(new TickingDriedGhastBlock());
        RegisterTickingBlock(new TickingSnifferEggBlock());

        // Growing broken for now
        RegisterTickingBlock(new TickingPointedDripstoneBlock()); // Includes cauldrons
        RegisterTickingBlock(new TickingSweetBerryBushBlock());
        RegisterTickingBlock(new TickingBuddingAmethystBlock());
    }

    /**
     * Registers a TickingBlock to be simulated
     * 
     * @param instanceClass The TickingBlock to register
     */
    public static void RegisterTickingBlock(TickingObject tickingObject) {
        if (tickingObject == null) {
            TheBlockKeepsTicking.LOGGER.warn("Tried to register null TickingObject");
            return;
        }

        TickingObjectInstances.add(tickingObject); // Add it to global list

        Class<?> type = tickingObject.getType();
        if (type == null) {
            TheBlockKeepsTicking.LOGGER.warn(
                    "TickingObject returned null type: " + tickingObject.getClass().getName());
            return;
        }

        if (Block.class.isAssignableFrom(type)) {
            TickingBlockInstances.add(tickingObject);
        } else if (BlockEntity.class.isAssignableFrom(type)) {
            TickingBlockEntityInstances.add(tickingObject);
        } else if (Entity.class.isAssignableFrom(type)) {
            TickingEntityInstances.add(tickingObject);
        } else {
            TheBlockKeepsTicking.LOGGER.warn("Unsupported TickingObject type: " + type.getName());
            return;
        }

        TheBlockKeepsTicking.LOGGER
                .info("Registered TickingBlock: " + tickingObject.getName() + " by "
                        + tickingObject.getModId());
    }

    // Simulate the world for the given chunk
    public static void SimulateWorld(LevelChunk chunk, long ticksToSimulate) {
        if (chunk == null) {
            TheBlockKeepsTicking.LOGGER.warn("Tried to simulate null chunk!");
            return;
        }

        Level level = chunk.getLevel();

        // Apply lazy tax
        long ticks = ModConfig.applyLazyTax(ticksToSimulate);

        if (ticks <= 0)
            return; // Nothing to simulate, abort

        boolean doDebugLogging = ModConfig.isDebugLogging();

        Benchmarker.StartBenchmark("SimulateWorld");
        try {
            Benchmarker.StartBenchmark("SimulateWorld.blocks");
            try {
                forEachTickingBlock(chunk, (tickingBlock, state, pos) -> {
                    if (!ModConfig.isEnabled(tickingBlock.getName()))
                        return; // ticking type disabled in config

                    long start = doDebugLogging ? System.nanoTime() : 0L;
                    boolean result = tickingBlock.Simulate(state.getBlock(), ticks, level, state, pos);
                    if (doDebugLogging)
                        Benchmarker.RecordObject(tickingBlock.getName(),
                                System.nanoTime() - start, result);
                });
            } finally {
                Benchmarker.EndBenchmark("SimulateWorld.blocks");
            }

            Benchmarker.StartBenchmark("SimulateWorld.blockEntities");
            try {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity != null) {
                        for (TickingObject tickingBlockEntity : TickingBlockEntityInstances) {
                            if (checkIfInstanceOf(tickingBlockEntity, blockEntity)) {
                                if (!ModConfig.isEnabled(tickingBlockEntity.getName())) {
                                    break; // skip this entity type
                                }
                                long start = doDebugLogging ? System.nanoTime() : 0L;
                                boolean result = tickingBlockEntity.Simulate(blockEntity,
                                        ticks,
                                        level, blockEntity.getBlockState(), blockEntity.getBlockPos());
                                if (doDebugLogging)
                                    Benchmarker.RecordObject(tickingBlockEntity.getName(),
                                            System.nanoTime() - start, result);
                                break;
                                // break to avoid multiple matches (which is impossible, so this
                                // saves time)
                            }
                        }
                    }
                }
            } finally {
                Benchmarker.EndBenchmark("SimulateWorld.blockEntities");
            }
            Benchmarker.StartBenchmark("SimulateWorld.entities");
            try {
                ChunkPos chunkPos = chunk.getPos();
                AABB box = new AABB(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                        chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ());

                // Get all passive entities within chunk
                List<AgeableMob> ageableMobs = level.getEntitiesOfClass(AgeableMob.class, box, AgeableMob -> true);

                for (AgeableMob ageableMob : ageableMobs) {
                    for (TickingObject tickingEntity : TickingEntityInstances) {
                        if (checkIfInstanceOf(tickingEntity, ageableMob)) {
                            if (!ModConfig.isEnabled(tickingEntity.getName())) {
                                continue; // skip this entity type
                            }
                            long start = doDebugLogging ? System.nanoTime() : 0L;
                            boolean result = tickingEntity.Simulate(ageableMob, ticks,
                                    level, null, null);
                            if (doDebugLogging)
                                Benchmarker.RecordObject(tickingEntity.getName(),
                                        System.nanoTime() - start, result);
                        }
                    }
                }
            } finally {
                Benchmarker.EndBenchmark("SimulateWorld.entities");
            }
        } catch (Exception e) {
            TheBlockKeepsTicking.LOGGER.error("Error during world simulation: ", e);
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld");
            Benchmarker.MaybeFlush();
        }
    }

    /**
     * Checks if the given block is of the type defined by the TickingBlock
     * 
     * @param tickingBlock
     * @param block
     * @return
     */
    private static Boolean checkIfInstanceOf(TickingObject tickingBlock, Object block) {
        return tickingBlock.getType().isInstance(block);
    }

    /**
     * Returns the (cached) TickingObject that handles the given block, or
     * {@link #NO_MATCH} if none does. The first lookup per block type runs the
     * linear isInstance scan, subsequent ones are a single identity-map hit.
     */
    private static TickingObject resolveTickingBlock(Block block) {
        TickingObject cached = blockMatchCache.get(block);
        if (cached != null)
            return cached;

        TickingObject match = NO_MATCH;
        for (TickingObject tickingBlock : TickingBlockInstances) {
            if (checkIfInstanceOf(tickingBlock, block)) {
                match = tickingBlock;
                break;
            }
        }
        blockMatchCache.put(block, match);
        return match;
    }

    /**
     * Iterates over every ticking block in the chunk from top to bottom, invoking
     * {@code action} with the resolved {@link TickingObject}.
     *
     * <p>
     * Whole sections whose palette contains no ticking block are skipped after a
     * single cheap palette check, and a {@link BlockPos} is only allocated once a
     * block actually matches a ticking type.
     *
     * @param chunk  The chunk to iterate over
     * @param action Invoked for each ticking block (tickingObject, state, pos)
     */
    public static void forEachTickingBlock(LevelChunk chunk,
            TriConsumer<TickingObject, BlockState, BlockPos> action) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getMinBlockX();
        int chunkBottomY = chunk.getMinY();
        int chunkStartZ = chunkPos.getMinBlockZ();
        int sections = chunk.getHeight() >> 4;

        // Iterate sections top-down
        for (int sectionY = sections - 1; sectionY >= 0; sectionY--) {
            LevelChunkSection section = chunk.getSection(sectionY);
            if (section == null || section.hasOnlyAir())
                continue;
            // Skip the whole section if its palette holds no ticking block
            if (!section.maybeHas(HAS_TICKING_BLOCK))
                continue;

            for (int x = 0; x < 16; x++) {
                for (int y = 15; y >= 0; y--) { // Count down y because reasons
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (state.isAir())
                            continue; // Air is never a ticking block. Skip to save work.

                        TickingObject tickingBlock = resolveTickingBlock(state.getBlock());
                        if (tickingBlock == NO_MATCH)
                            continue; // not a ticking block

                        BlockPos blockPos = new BlockPos(chunkStartX + x,
                                (sectionY << 4) + chunkBottomY + y, chunkStartZ + z);

                        action.accept(tickingBlock, state, blockPos);
                    }
                }
            }
        }
    }
}
