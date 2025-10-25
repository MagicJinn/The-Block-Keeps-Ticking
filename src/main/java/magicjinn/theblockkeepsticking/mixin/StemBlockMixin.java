package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Optional;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;

@Mixin(StemBlock.class)
public class StemBlockMixin implements TickingBlockAccessor {

    @Shadow @Final private RegistryKey<Block> gourdBlock;
    @Shadow @Final private RegistryKey<Block> attachedStemBlock;

    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        StemBlock stem = (StemBlock) (Object) this;

        int randomTicks = TickingBlock.CropGrowthAmount(ticksToSimulate, stem, world, state, pos);

        // Check if the stem is already facing a direction (attached stem)
        if (state.contains(HorizontalFacingBlock.FACING))
            return false;

        if (randomTicks <= 0)
            return false;

        // Because mojang is stupid
        int age = (int) state.get(StemBlock.AGE);
        int maxAge = (int) StemBlock.MAX_AGE;
        int ageDiff = maxAge - age; // Difference between age and maxAge
        if (ageDiff > 0) {
            // Schedule stem growth for 40 ticks later
            Timer.INSTANCE.Schedule(
                    "stem_growth_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ(), server -> {
                        ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
                        if (serverWorld != null) {
                            BlockState currentState = serverWorld.getBlockState(pos);
                            if (currentState.getBlock() instanceof StemBlock) {
                                int currentAge = (int) currentState.get(StemBlock.AGE);
                                int newAge = Math.min(StemBlock.MAX_AGE, currentAge + ageDiff);
                                BlockState newState = currentState.with(StemBlock.AGE, newAge);
                                serverWorld.setBlockState(pos, newState, 2);
                            }
                        }
                    });
        }

        // Calculate how many ticks are left to grow gourds
        int gourdTicks = randomTicks - ageDiff;

        if (gourdTicks <= 0)
            return false; // No gourdTicks

        // Schedule gourd placement for 40 ticks later
        Timer.INSTANCE.Schedule("stem_gourd_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ(),
                40L, server -> {
                    ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
                    if (serverWorld != null) {
                        BlockState currentState = serverWorld.getBlockState(pos);
                        if (currentState.getBlock() instanceof StemBlock) {
                            var directions = Type.HORIZONTAL.getShuffled(serverWorld.random);
                            Registry<Block> registry =
                                    serverWorld.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
                            Optional<Block> optionalGourd = registry.getOptionalValue(gourdBlock);
                            Optional<Block> optionalStemBlock =
                                    registry.getOptionalValue(attachedStemBlock);

                            if (optionalGourd.isPresent() && optionalStemBlock.isPresent()) {
                                // Try all 4 directions
                                for (Direction dir : directions) {
                                    // Check whether the gourd fits on the block
                                    BlockPos blockPos = pos.offset(dir);
                                    BlockState blockGourdWillBeOn =
                                            serverWorld.getBlockState(blockPos.down());
                                    if (serverWorld.getBlockState(blockPos).isAir()
                                            && (blockGourdWillBeOn.isOf(Blocks.FARMLAND)
                                                    || blockGourdWillBeOn.isIn(BlockTags.DIRT))) {
                                        serverWorld.setBlockState(blockPos, // Set gourd
                                                (BlockState) ((Block) optionalGourd.get())
                                                        .getDefaultState());
                                        serverWorld.setBlockState(pos,
                                                (BlockState) ((Block) optionalStemBlock.get())
                                                        .getDefaultState()
                                                        .with(HorizontalFacingBlock.FACING, dir));
                                        break; // Placed gourd, exit
                                    }
                                }
                            }
                        }
                    }
                });
        return ageDiff > 0 || gourdTicks > 0; // Everything went swell
    }
}
