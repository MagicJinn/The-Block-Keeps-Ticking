package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.function.TriConsumer;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.blocks.TickingAbstractFurnaceBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingBambooBlock;
import magicjinn.theblockkeepsticking.blocks.TickingBambooShootBlock;
import magicjinn.theblockkeepsticking.blocks.TickingBrewingStandBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingCactusBlock;
import magicjinn.theblockkeepsticking.blocks.TickingCampfireBlockEntity;
import magicjinn.theblockkeepsticking.blocks.TickingCocoaBlock;
import magicjinn.theblockkeepsticking.blocks.TickingCropBlock;
import magicjinn.theblockkeepsticking.blocks.TickingKelpBlock;
// import magicjinn.theblockkeepsticking.blocks.TickingLeavesBlock; // Disabled until further notice
import magicjinn.theblockkeepsticking.blocks.TickingNetherWartBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSaplingBlock;
import magicjinn.theblockkeepsticking.blocks.TickingStemBlock;
import magicjinn.theblockkeepsticking.blocks.TickingSugarCaneBlock;
import magicjinn.theblockkeepsticking.entities.TickingPassiveEntity;
import magicjinn.theblockkeepsticking.util.TickingObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class WorldSimulator {

    private static final ArrayList<TickingObject> TickingBlockInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingBlockEntityInstances = new ArrayList<>();
    private static final ArrayList<TickingObject> TickingEntityInstances = new ArrayList<>();

    public static void Initialize() {
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
        RegisterTickingBlock(TickingBambooBlock.INSTANCE);
        RegisterTickingBlock(TickingBambooShootBlock.INSTANCE);
        RegisterTickingBlock(TickingSugarCaneBlock.INSTANCE);
        RegisterTickingBlock(TickingCactusBlock.INSTANCE);

        RegisterTickingBlock(TickingPassiveEntity.INSTANCE);
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
                .info("Registered TickingBlock: " + tickingObject.getClass().getSimpleName());
    }

    // Simulate the world for the given chunk
    public static void SimulateWorld(WorldChunk chunk, long ticksToSimulate) {
        if (chunk == null) {
            TheBlockKeepsTicking.LOGGER.warn("Tried to simulate null chunk!");
            return;
        }

        World world = chunk.getWorld();

        if (ticksToSimulate <= 0){
            return; // Nothing to simulate, abort
        }

        try {
            forEachBlockInChunk(chunk, (block, state, pos) -> {
                for (TickingObject tickingBlock : TickingBlockInstances) {
                if (checkIfBlockIs(tickingBlock, block)) {
                        boolean result =
                                tickingBlock.Simulate(block, ticksToSimulate, world, state, pos);
                        result = false;
                        if (result)
                        TheBlockKeepsTicking.LOGGER.info("Simulating block {} for {} ticks",
                                    block.getName().toString(), ticksToSimulate);
                    return; // lambda break; equivalent
                    // break to avoid multiple matches (which is impossible, so this saves time)
                }
            }
        });
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity != null) {
                    for (TickingObject tickingBlockEntity : TickingBlockEntityInstances) {
                        if (checkIfBlockIs(tickingBlockEntity, blockEntity)) {
                            boolean result = tickingBlockEntity.Simulate(blockEntity,
                                    ticksToSimulate,
                                    world, blockEntity.getCachedState(), blockEntity.getPos());
                            result = false;
                            if (result)
                                TheBlockKeepsTicking.LOGGER.info(
                                        "Simulating block entity {} for {} ticks",
                                        blockEntity.getNameForReport(), ticksToSimulate);
                            break;
                            // break to avoid multiple matches (which is impossible, so this
                            // saves time)
                            }
                        }
                }
            }
            ChunkPos chunkPos = chunk.getPos();
            Box box = new Box(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(),
                    chunkPos.getEndX(), world.getHeight(), chunkPos.getEndZ());

            // Get all passive entities within chunk
            var passiveEntities =
                    world.getEntitiesByClass(PassiveEntity.class, box, PassiveEntity -> true);

            for (PassiveEntity passiveEntity : passiveEntities) {
                for (TickingObject tickingEntity : TickingEntityInstances) {

                }

            }
    } catch (Exception e) {
        TheBlockKeepsTicking.LOGGER.error("Error during world simulation: ", e);
    }
    }

    /**
     * Checks if the given block is of the type defined by the TickingBlock
     * 
     * @param tickingBlock
     * @param block
     * @return
     */
    private static Boolean checkIfBlockIs(TickingObject tickingBlock, Object block) {
        return tickingBlock.getType().isInstance(block);
    }

    /**
     * Iterates over all blocks in the given chunk
     * 
     * @param chunk The chunk to iterate over
     * @param action The action to perform on each block
     */
    public static void forEachBlockInChunk(WorldChunk chunk,
            TriConsumer<Block, BlockState, BlockPos> action) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getStartX();
        int chunkBottomY = chunk.getBottomY();
        int chunkStartZ = chunkPos.getStartZ();
        for (int sectionY = 0; sectionY < chunk.getHeight() >> 4; ++sectionY) {
            ChunkSection section = chunk.getSectionArray()[sectionY];
            if (section == null || section.isEmpty())
                continue;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
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
