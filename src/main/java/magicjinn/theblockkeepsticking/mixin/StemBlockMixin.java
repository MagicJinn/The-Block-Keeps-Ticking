package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

@Mixin(StemBlock.class)
public class StemBlockMixin implements TickingAccessor {
    @Shadow
    @Final
    private ResourceKey<Block> fruit;
    @Shadow
    @Final
    private ResourceKey<Block> attachedStem;

    @Shadow
    @Final
    private TagKey<Block> stemSupportBlocks;
    @Shadow
    @Final
    private TagKey<Block> fruitSupportBlocks;

    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        StemBlock stem = (StemBlock) (Object) this;

        int randomTicks =
                TickingCalculator.CropGrowthAmount(ticksToSimulate, stem, level, state, pos);

        // Check if the stem is already facing a direction (attached stem)
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) // Unsure if this works
            return false;

        if (randomTicks <= 0)
            return false;

        // Because mojang is stupid
        int age = (int) state.getValue(StemBlock.AGE);
        int maxAge = (int) StemBlock.MAX_AGE;
        int ageDiff = maxAge - age; // Difference between age and maxAge
        if (ageDiff > 0) {
            // Simulate stem growth
            ServerLevel serverLevel = (ServerLevel) level;
            BlockState currentState = serverLevel.getBlockState(pos);
            if (currentState.getBlock() instanceof StemBlock) {
                int currentAge = (int) currentState.getValue(StemBlock.AGE);
                int newAge = Math.min(StemBlock.MAX_AGE, currentAge + randomTicks);
                BlockState newState = currentState.setValue(StemBlock.AGE, newAge);
                serverLevel.setBlock(pos, newState, 2);
            }
        }

        // Calculate how many ticks are left to grow fruits
        int fruitTicks = randomTicks - ageDiff;

        if (fruitTicks <= 0)
            return false; // No fruitTicks

        // Simulate fruit placement
        ServerLevel serverLevel = (ServerLevel) level;
        BlockState currentState = serverLevel.getBlockState(pos);
        if (currentState.getBlock() instanceof StemBlock) {
            List<Direction> directions = Direction.Plane.HORIZONTAL.shuffledCopy(level.getRandom());
            RegistryAccess registryAccess = serverLevel.registryAccess();
            Registry<Block> registry = registryAccess.lookupOrThrow(Registries.BLOCK);
            Optional<Block> optionalfruit = registry.getOptional(fruit);
            Optional<Block> optionalStemBlock = registry.getOptional(attachedStem);

            if (optionalfruit.isPresent() && optionalStemBlock.isPresent()) {
                // Try all 4 directions
                for (Direction dir : directions) {
                    // Check whether the fruit fits on the block
                    BlockPos blockPos = pos.relative(dir);
                    BlockState blockfruitWillBeOn = serverLevel.getBlockState(blockPos.below());

                    if (serverLevel.isEmptyBlock(blockPos)
                            && blockfruitWillBeOn.is(fruitSupportBlocks)) {
                        serverLevel.setBlockAndUpdate(blockPos, // Set fruit
                                (BlockState) ((Block) optionalfruit.get()).defaultBlockState());
                        serverLevel.setBlockAndUpdate(pos,
                                (BlockState) ((Block) optionalStemBlock.get()).defaultBlockState()
                                        .setValue(HorizontalDirectionalBlock.FACING, dir));
                        break; // Placed fruit, exit
                    }
                }
            }
        }
        return ageDiff > 0 || fruitTicks > 0; // Everything went swell
    }
}