package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.blocks.TickingAbstractFurnaceBlockEntity;
import magicjinn.theblockkeepsticking.framework.ChangingBlock;
import magicjinn.theblockkeepsticking.framework.ProcessingBlock;
import magicjinn.theblockkeepsticking.framework.TickingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class WorldSimulator {
    // Simulate the world for the given chunk

    private static final List<ChangingBlock> changingBlockInstances = new ArrayList<>();
    private static final List<ProcessingBlock> processingBlockInstances = new ArrayList<>();

    public static void Initialize() {
        RegisterTickingBlock(TickingAbstractFurnaceBlockEntity.INSTANCE);
        // RegisterTickingBlock(TickingCampfire.INSTANCE);
        // RegisterTickingBlock(TickingBrewingStand.INSTANCE);
        // RegisterTickingBlock(TickingCropBlock.INSTANCE);
        //
    }

    /**
     * Registers a TickingBlock to be simulated
     * 
     * @param blockClass
     */
    public static void RegisterTickingBlock(TickingBlock blockClass) {
        if (blockClass instanceof ProcessingBlock)
            processingBlockInstances.add((ProcessingBlock) blockClass);
        else if (blockClass instanceof ChangingBlock)
            changingBlockInstances.add((ChangingBlock) blockClass);
    }

    public static void SimulateWorld(WorldChunk chunk) {
        if (chunk == null)
            return;
        TheBlockKeepsTicking.LOGGER.info("Simulating world chunk at {} , {}", chunk.getPos().x,
                chunk.getPos().z);
        World world = chunk.getWorld();
        long currentWorldTime = world.getTime();
        long lastTickTime =
                chunk.getAttachedOrSet(TheBlockKeepsTicking.LAST_UPDATE_TIME, currentWorldTime);
        long ticksToSimulate = currentWorldTime - lastTickTime;

        forEachBlockInChunk(chunk, (block) -> {
            for (ChangingBlock changingBlock : changingBlockInstances) {
                if (checkIfBlockIs(changingBlock, block)) {

                    TheBlockKeepsTicking.LOGGER.info("Simulating block {} for {} ticks",
                            block.toString(), ticksToSimulate);
                    changingBlock.Simulate(block, ticksToSimulate);
                    return;
                    // return to avoid multiple matches (which is impossible, so this saves time)
                }
            }
        });

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            for (ProcessingBlock processingBlock : processingBlockInstances) {
                if (checkIfBlockIs(processingBlock, blockEntity)) {

                    TheBlockKeepsTicking.LOGGER.info("Simulating block entity {} for {} ticks",
                            blockEntity.getType().toString(), ticksToSimulate);
                    processingBlock.Simulate(blockEntity, ticksToSimulate);
                    return;
                    // return to avoid multiple matches (which is impossible, so this saves time)
                }
            }
        }
    }

    /**
     * Checks if the given block is of the type defined by the TickingBlock
     * 
     * @param tickingBlock
     * @param block
     * @return
     */
    private static Boolean checkIfBlockIs(TickingBlock tickingBlock, Object block) {
        return tickingBlock.getType().isInstance(block);
    }

    /**
     * Iterates over all blocks in the given chunk
     * 
     * @param chunk The chunk to iterate over
     * @param action The action to perform on each block
     */
    public static void forEachBlockInChunk(WorldChunk chunk, Consumer<Block> action) {
        for (int sectionY = 0; sectionY < chunk.getHeight() >> 4; ++sectionY) {
            ChunkSection section = chunk.getSectionArray()[sectionY];
            if (section == null || section.isEmpty())
                continue;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        Block block = state.getBlock();

                        action.accept(block);
                    }
                }
            }
        }
    }
}
