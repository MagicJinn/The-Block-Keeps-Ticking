package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
// import magicjinn.theblockkeepsticking.blocks.TickingLeavesBlock; // Disabled until further notice
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class WorldSimulator {
    public static final ArrayList<TickingObject> TickingObjectInstances = new ArrayList<>();

    private static final ArrayList<TickingObject> TickingBlockInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingBlockEntityInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingEntityInstances = new ArrayList<>();

    public static void InitializeTickingBlocks() {
        // Register built-in ticking blocks
        RegisterTickingBlock(TickingAbstractFurnaceBlockEntity.INSTANCE);
        RegisterTickingBlock(TickingCampfireBlockEntity.INSTANCE);
        RegisterTickingBlock(TickingBrewingStandBlockEntity.INSTANCE);

        RegisterTickingBlock(TickingCropBlock.INSTANCE);
        RegisterTickingBlock(TickingStemBlock.INSTANCE);
        RegisterTickingBlock(TickingNetherWartBlock.INSTANCE);
        RegisterTickingBlock(TickingCocoaBlock.INSTANCE);

        RegisterTickingBlock(TickingSaplingBlock.INSTANCE);
        // RegisterTickingBlock(TickingLeavesBlock.INSTANCE); // Laggy?

        RegisterTickingBlock(TickingKelpBlock.INSTANCE);
        RegisterTickingBlock(TickingBambooSaplingBlock.INSTANCE);
        RegisterTickingBlock(TickingBambooStalkBlock.INSTANCE);
        RegisterTickingBlock(TickingSugarCaneBlock.INSTANCE);
        RegisterTickingBlock(TickingCactusBlock.INSTANCE);

        RegisterTickingBlock(TickingAgeableMob.INSTANCE); // Includes chickens

        RegisterTickingBlock(TickingDriedGhastBlock.INSTANCE);
        RegisterTickingBlock(TickingSnifferEggBlock.INSTANCE);

        // Growing broken for now
        RegisterTickingBlock(TickingPointedDripstoneBlock.INSTANCE); // Includes cauldrons
        RegisterTickingBlock(TickingSweetBerryBushBlock.INSTANCE);
        RegisterTickingBlock(TickingBuddingAmethystBlock.INSTANCE);
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
                forEachBlockInChunk(chunk, (block, state, pos) -> {
                    for (TickingObject tickingBlock : TickingBlockInstances) {
                        if (checkIfInstanceOf(tickingBlock, block)) {
                            if (!ModConfig.isEnabled(tickingBlock.getName())) {
                                return; // skip this block type
                            }
                            boolean result = tickingBlock.Simulate(block, ticks, level, state, pos);
                            if (result && doDebugLogging)
                                TheBlockKeepsTicking.LOGGER.info("Simulating block {} for {} ticks",
                                        BuiltInRegistries.BLOCK.getKey(block),
                                        String.format(Locale.ROOT, "%,d", ticks));
                            return; // lambda break; equivalent
                            // break to avoid multiple matches (which is impossible, so this saves time)
                        }
                    }
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
                                boolean result = tickingBlockEntity.Simulate(blockEntity,
                                        ticks,
                                        level, blockEntity.getBlockState(), blockEntity.getBlockPos());
                                if (result && doDebugLogging)
                                    TheBlockKeepsTicking.LOGGER.info(
                                            "Simulating block entity {} for {} ticks",
                                            blockEntity.getNameForReporting(),
                                            String.format(Locale.ROOT, "%,d", ticks));
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
                            boolean result = tickingEntity.Simulate(ageableMob, ticks,
                                    level, null, null);
                            if (result && doDebugLogging)
                                TheBlockKeepsTicking.LOGGER.info("Simulating entity {} for {} ticks",
                                        ageableMob.getName(),
                                        String.format(Locale.ROOT, "%,d", ticks));
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
     * Iterates over all blocks in the given chunk from top to bottom
     * 
     * @param chunk  The chunk to iterate over
     * @param action The action to perform on each block
     */
    public static void forEachBlockInChunk(LevelChunk chunk,
            TriConsumer<Block, BlockState, BlockPos> action) {
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

            for (int x = 0; x < 16; x++) {
                for (int y = 15; y >= 0; y--) { // Count down y because reasons
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        BlockPos blockPos = new BlockPos(chunkStartX + x,
                                (sectionY << 4) + chunkBottomY + y, chunkStartZ + z);
                        Block block = state.getBlock();

                        action.accept(block, state, blockPos);
                    }
                }
            }
        }
    }

}
