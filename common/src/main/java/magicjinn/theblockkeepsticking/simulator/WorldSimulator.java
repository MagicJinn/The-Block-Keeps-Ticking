package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.TriConsumer;
import magicjinn.theblockkeepsticking.Constants;
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
import magicjinn.theblockkeepsticking.blocks.TickingFarmlandBlock;
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

    /** Resumable position inside a chunk block scan. */
    public static final class ScanCursor {
        /** Section indices (chunk section array index) that need a walk, top-down. */
        final int[] scannableSections;
        /** Index into {@link #scannableSections}. */
        int sectionListIndex;
        int x;
        /** Local Y within section, counts down 15..0. */
        int y;
        int z;
        int cellsVisited;
        final int totalCells;
        boolean done;

        ScanCursor(int[] scannableSections) {
            this.scannableSections = scannableSections;
            this.totalCells = scannableSections.length * 4096;
            this.sectionListIndex = 0;
            this.x = 0;
            this.y = 15;
            this.z = 0;
            this.done = scannableSections.length == 0;
        }
    }

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

        RegisterTickingBlock(new TickingFarmlandBlock());
    }

    /**
     * Registers a TickingBlock to be simulated
     *
     * @param tickingObject The TickingBlock to register
     */
    public static void RegisterTickingBlock(TickingObject tickingObject) {
        if (tickingObject == null) {
            Constants.LOG.warn("Tried to register null TickingObject");
            return;
        }

        TickingObjectInstances.add(tickingObject); // Add it to global list

        Class<?> type = tickingObject.getType();
        if (type == null) {
            Constants.LOG.warn(
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
            Constants.LOG.warn("Unsupported TickingObject type: " + type.getName());
            return;
        }

        Constants.LOG
                .info("Registered TickingBlock: " + tickingObject.getName() + " by "
                        + tickingObject.getModId());
    }

    /**
     * Builds a scan cursor listing every section that needs a walk.
     */
    public static ScanCursor createScanCursor(LevelChunk chunk) {
        int sectionCount = chunk.getHeight() >> 4;
        int[] tmp = new int[sectionCount];
        int n = 0;
        for (int sectionY = sectionCount - 1; sectionY >= 0; sectionY--) {
            LevelChunkSection section = chunk.getSection(sectionY);
            if (section == null || section.hasOnlyAir())
                continue;
            if (!section.maybeHas(HAS_TICKING_BLOCK))
                continue;
            tmp[n++] = sectionY;
        }
        int[] scannable = new int[n];
        System.arraycopy(tmp, 0, scannable, 0, n);
        return new ScanCursor(scannable);
    }

    /**
     * Full catch-up in one call (used for tests / forced flush). Prefer
     * {@link ChunkSimScheduler} for normal play.
     */
    public static void SimulateWorld(LevelChunk chunk, long ticksToSimulate) {
        if (chunk == null) {
            Constants.LOG.warn("Tried to simulate null chunk!");
            return;
        }

        // Apply lazy tax
        long ticks = ModConfig.applyLazyTax(ticksToSimulate);
        if (ticks <= 0)
            return;

        Benchmarker.StartBenchmark("SimulateWorld");
        try {
            ScanCursor cursor = createScanCursor(chunk);
            simulateBlockSlice(chunk, ticks, cursor, Integer.MAX_VALUE);
            simulateBlockEntities(chunk, ticks);
            simulateEntities(chunk, ticks);
        } catch (Exception e) {
            Constants.LOG.error("Error during world simulation: ", e);
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld");
            Benchmarker.MaybeFlush();
        }
    }

    /**
     * Visits up to {@code maxCells} cells in scannable sections, simulating
     * matches.
     * Updates {@code cursor}; sets {@code cursor.done} when the scan is finished.
     */
    public static void simulateBlockSlice(LevelChunk chunk, long ticks, ScanCursor cursor,
            int maxCells) {
        if (cursor.done || maxCells <= 0)
            return;

        Level level = chunk.getLevel();
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getMinBlockX();
        int chunkBottomY = chunk.getMinY();
        int chunkStartZ = chunkPos.getMinBlockZ();
        boolean doDebugLogging = ModConfig.isDebugLogging();

        int cellsThisSlice = 0;
        int matches = 0;

        Benchmarker.StartBenchmark("SimulateWorld.scan");
        try {
            while (!cursor.done && cellsThisSlice < maxCells) {
                int sectionY = cursor.scannableSections[cursor.sectionListIndex];
                LevelChunkSection section = chunk.getSection(sectionY);
                if (section == null || section.hasOnlyAir()) {
                    // Section became empty mid-job. Skip remaining cells in it
                    int skipped = remainingCellsInSection(cursor);
                    cursor.cellsVisited += skipped;
                    cellsThisSlice += skipped;
                    advanceToNextSection(cursor);
                    continue;
                }

                while (!cursor.done && cellsThisSlice < maxCells
                        && cursor.sectionListIndex < cursor.scannableSections.length
                        && cursor.scannableSections[cursor.sectionListIndex] == sectionY) {

                    BlockState state = section.getBlockState(cursor.x, cursor.y, cursor.z);
                    cursor.cellsVisited++;
                    cellsThisSlice++;

                    if (!state.isAir()) {
                        TickingObject tickingBlock = resolveTickingBlock(state.getBlock());
                        if (tickingBlock != NO_MATCH) {
                            matches++;
                            BlockPos blockPos = new BlockPos(chunkStartX + cursor.x,
                                    (sectionY << 4) + chunkBottomY + cursor.y,
                                    chunkStartZ + cursor.z);

                            Benchmarker.EndBenchmark("SimulateWorld.scan");
                            Benchmarker.StartBenchmark("SimulateWorld.simulate");
                            try {
                                if (ModConfig.isEnabled(tickingBlock.getName())) {
                                    long start = doDebugLogging ? System.nanoTime() : 0L;
                                    boolean result = tickingBlock.Simulate(state.getBlock(), ticks,
                                            level, state, blockPos);
                                    if (doDebugLogging)
                                        Benchmarker.RecordObject(tickingBlock.getName(),
                                                System.nanoTime() - start, result);
                                }
                            } finally {
                                Benchmarker.EndBenchmark("SimulateWorld.simulate");
                                Benchmarker.StartBenchmark("SimulateWorld.scan");
                            }
                        }
                    }

                    advanceCell(cursor);
                }
            }
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld.scan");
        }

        Benchmarker.RecordCounter("cellsVisited", cellsThisSlice);
        Benchmarker.RecordCounter("scanMatches", matches);
    }

    private static int remainingCellsInSection(ScanCursor cursor) {
        // Cells left including current: from (x,y,z) through end of section.
        // Order: x outer, y down 15..0, z inner.
        int cells = 0;
        for (int x = cursor.x; x < 16; x++) {
            int yStart = (x == cursor.x) ? cursor.y : 15;
            for (int y = yStart; y >= 0; y--) {
                int zStart = (x == cursor.x && y == cursor.y) ? cursor.z : 0;
                cells += 16 - zStart;
            }
        }
        return cells;
    }

    private static void advanceCell(ScanCursor cursor) {
        cursor.z++;
        if (cursor.z < 16)
            return;
        cursor.z = 0;
        cursor.y--;
        if (cursor.y >= 0)
            return;
        cursor.y = 15;
        cursor.x++;
        if (cursor.x < 16)
            return;
        cursor.x = 0;
        advanceToNextSection(cursor);
    }

    private static void advanceToNextSection(ScanCursor cursor) {
        cursor.sectionListIndex++;
        cursor.x = 0;
        cursor.y = 15;
        cursor.z = 0;
        if (cursor.sectionListIndex >= cursor.scannableSections.length)
            cursor.done = true;
    }

    public static void simulateBlockEntities(LevelChunk chunk, long ticks) {
        boolean doDebugLogging = ModConfig.isDebugLogging();
        Level level = chunk.getLevel();

        Benchmarker.StartBenchmark("SimulateWorld.blockEntities");
        try {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity == null)
                    continue;
                for (TickingObject tickingBlockEntity : TickingBlockEntityInstances) {
                    if (!checkIfInstanceOf(tickingBlockEntity, blockEntity))
                        continue;
                    if (!ModConfig.isEnabled(tickingBlockEntity.getName()))
                        break;
                    long start = doDebugLogging ? System.nanoTime() : 0L;
                    boolean result = tickingBlockEntity.Simulate(blockEntity, ticks, level,
                            blockEntity.getBlockState(), blockEntity.getBlockPos());
                    if (doDebugLogging)
                        Benchmarker.RecordObject(tickingBlockEntity.getName(),
                                System.nanoTime() - start, result);
                    break;
                }
            }
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld.blockEntities");
        }
    }

    public static void simulateEntities(LevelChunk chunk, long ticks) {
        boolean doDebugLogging = ModConfig.isDebugLogging();
        Level level = chunk.getLevel();
        ChunkPos chunkPos = chunk.getPos();
        AABB box = new AABB(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ());

        Benchmarker.StartBenchmark("SimulateWorld.entities");
        try {
            List<AgeableMob> ageableMobs = level.getEntitiesOfClass(AgeableMob.class, box,
                    AgeableMob -> true);

            for (AgeableMob ageableMob : ageableMobs) {
                for (TickingObject tickingEntity : TickingEntityInstances) {
                    if (!checkIfInstanceOf(tickingEntity, ageableMob))
                        continue;
                    if (!ModConfig.isEnabled(tickingEntity.getName()))
                        continue;
                    long start = doDebugLogging ? System.nanoTime() : 0L;
                    boolean result = tickingEntity.Simulate(ageableMob, ticks, level, null, null);
                    if (doDebugLogging)
                        Benchmarker.RecordObject(tickingEntity.getName(),
                                System.nanoTime() - start, result);
                }
            }
        } finally {
            Benchmarker.EndBenchmark("SimulateWorld.entities");
        }
    }

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
     */
    public static void forEachTickingBlock(LevelChunk chunk,
            TriConsumer<TickingObject, BlockState, BlockPos> action) {
        ScanCursor cursor = createScanCursor(chunk);
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getMinBlockX();
        int chunkBottomY = chunk.getMinY();
        int chunkStartZ = chunkPos.getMinBlockZ();

        while (!cursor.done) {
            int sectionY = cursor.scannableSections[cursor.sectionListIndex];
            LevelChunkSection section = chunk.getSection(sectionY);
            if (section == null || section.hasOnlyAir()) {
                advanceToNextSection(cursor);
                continue;
            }

            BlockState state = section.getBlockState(cursor.x, cursor.y, cursor.z);
            if (!state.isAir()) {
                TickingObject tickingBlock = resolveTickingBlock(state.getBlock());
                if (tickingBlock != NO_MATCH) {
                    BlockPos blockPos = new BlockPos(chunkStartX + cursor.x,
                            (sectionY << 4) + chunkBottomY + cursor.y, chunkStartZ + cursor.z);
                    action.accept(tickingBlock, state, blockPos);
                }
            }
            advanceCell(cursor);
        }
    }
}
