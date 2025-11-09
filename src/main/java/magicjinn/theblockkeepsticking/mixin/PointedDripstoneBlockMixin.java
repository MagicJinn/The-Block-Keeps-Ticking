package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneBlockMixin implements TickingAccessor {
    @Shadow @Final private static float WATER_DRIP_CHANCE;
    @Shadow @Final private static float LAVA_DRIP_CHANCE;
    @Shadow @Final private static float field_33567; // Growth chance 0.011377778F
    private static final int MAX_LENGTH = 11; // for the tip position for dripping
    @Shadow @Final private static int MAX_STALACTITE_GROWTH; // 7, for the tip position for growth
    @Shadow @Final private static int STALACTITE_FLOOR_SEARCH_RANGE; // 10

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        if (!(world instanceof ServerWorld))
            return false;

        // Prepare random values for the simulation
        float randomFloat = world.random.nextFloat();
        int randomInt = world.random.nextInt(2); // 0 or 1

        // Check if the block above is a dripstone block and the block above that is fluid
        // This disqualifies any middle/tip blocks, as well as upward growing dripstone
        BlockState blockAbove = world.getBlockState(pos.up());
        BlockState blockAboveThat = world.getBlockState(pos.up(2));
        boolean canGrowDripstone = PointedDripstoneBlock.canGrow(blockAbove, blockAboveThat);
        boolean blockAboveThatIsLava = isStillFluid(Blocks.LAVA, blockAboveThat);
        boolean blockAboveThatIsMud = blockAboveThat.isOf(Blocks.MUD);
        boolean blockAboveThatIsAnythingElse =
                !canGrowDripstone && !blockAboveThatIsLava && !blockAboveThatIsMud;

        // Literally nothing can happen if this is true
        if (blockAboveThatIsAnythingElse)
            return false;

        // Because dripstone grows so incredibly slowly, we need to use random to account for
        // the situations where it wouldn't grow at all if fully strict-deterministic
        int cycleAmountGrowthInt = 0;
        if (canGrowDripstone) {
            final float growthChance = field_33567;
            final float cycleAmountGrowth = randomTicks * growthChance;
            cycleAmountGrowthInt = (int) cycleAmountGrowth;
            final float cycleAmountLeftover = cycleAmountGrowth % 1.0f;
            cycleAmountGrowthInt += randomFloat < cycleAmountLeftover ? 1 : 0;
        }

        // Same here for water and lava
        final float cycleAmountWater = randomTicks * WATER_DRIP_CHANCE;
        int cycleAmountWaterInt = (int) cycleAmountWater;
        final float cycleAmountWaterLeftover = cycleAmountWater % 1.0f;
        cycleAmountWaterInt += randomFloat < cycleAmountWaterLeftover ? 1 : 0;
        // Cap the water at the max level of cauldrons
        cycleAmountWaterInt = Math.min(cycleAmountWaterInt, LeveledCauldronBlock.MAX_LEVEL);

        // Lava has no stages, so we just check if it's greater than 0,
        // or if the random float is less than the chance
        final float cycleAmountLava = randomTicks * LAVA_DRIP_CHANCE;
        int cycleAmountLavaInt = (int) cycleAmountLava;
        final float cycleAmountLavaLeftover = cycleAmountLava % 1.0f;
        cycleAmountLavaInt += randomFloat < cycleAmountLavaLeftover ? 1 : 0;
        final boolean filledLava = cycleAmountLavaInt > 0;

        boolean changed = false;

        BlockPos tipPos = PointedDripstoneBlock.getTipPos(state, world, pos, MAX_LENGTH, false);
        if (tipPos == null)
            return false;

        // Vanilla uses scheduled ticks to fill cauldrons, but that sucks for our purposes, so we
        // use blockstate changes directly
        BlockPos cauldronPos = null;
        if (cycleAmountWaterInt > 0 && canGrowDripstone) {
            cauldronPos = PointedDripstoneBlock.getCauldronPos(world, tipPos, Fluids.WATER);
            if (cauldronPos != null) {
                BlockState currentState = world.getBlockState(cauldronPos);
                Block currentBlock = currentState.getBlock();

                int newLevel = 0;
                int currentLevel = 0;
                // If it already has water, get the current level
                if (currentBlock == Blocks.WATER_CAULDRON) {
                    currentLevel = currentState.get(LeveledCauldronBlock.LEVEL);
                } else if (currentBlock == Blocks.LAVA_CAULDRON) {
                    // Cancel the operation, since we can't fill a lava cauldron with water
                    cycleAmountWaterInt = 0;
                }

                newLevel = Math.min(currentLevel + cycleAmountWaterInt,
                        LeveledCauldronBlock.MAX_LEVEL);

                // If the new level is greater than 0, update the cauldron
                if (newLevel > 0) {
                    BlockState newState = Blocks.WATER_CAULDRON.getDefaultState()
                            .with(LeveledCauldronBlock.LEVEL, newLevel);
                    world.setBlockState(cauldronPos, newState, 3);
                    changed = true;
                }
            }
        } else if (filledLava && blockAboveThatIsLava) {
            cauldronPos = PointedDripstoneBlock.getCauldronPos(world, tipPos, Fluids.LAVA);
            if (cauldronPos != null) {
                BlockState currentState = world.getBlockState(cauldronPos);
                Block currentBlock = currentState.getBlock();

                // Only fill if it's an empty cauldron
                if (currentBlock == Blocks.CAULDRON) {
                    world.setBlockState(cauldronPos, Blocks.LAVA_CAULDRON.getDefaultState(), 3);
                    changed = true;
                }
            }
        }

        // Simulate growth attempts deterministically
        // Each attempt alternates between stalactite (down) and stalagmite (up)

        // Refresh tipPos for growth
        tipPos = PointedDripstoneBlock.getTipPos(state, world, pos, MAX_STALACTITE_GROWTH, false);
        BlockState tipState = world.getBlockState(tipPos);

        for (int i = 0; i < cycleAmountGrowthInt; i++) {
            // Non-random alternating growth
            boolean growStalactite = (i + randomInt) % 2 == 0;

            // Check if we can still grow from this tip
            // canGrow checks if the block in the growth direction is air or a tip
            // This works for both stalactites and stalagmites, and for merged tips

            boolean canGrow = PointedDripstoneBlock.canGrow(tipState, (ServerWorld) world, tipPos);
            if (!canGrow)
                break; // Can't grow anymore, stop attempting


            // Store state before growth attempt to check if growth occurred
            BlockPos oldTipPos = tipPos;
            BlockState oldTipState = tipState;

            if (growStalactite) {
                PointedDripstoneBlock.tryGrow((ServerWorld) world, tipPos, Direction.DOWN);
            } else {
                PointedDripstoneBlock.tryGrowStalagmite((ServerWorld) world, tipPos);
            }

            // Refresh tipState from world after growth attempt
            tipState = world.getBlockState(tipPos);

            // Check if growth occurred by verifying tip position or state changed
            // After growth, search from current tip position to find the new tip
            BlockPos newTipPos = PointedDripstoneBlock.getTipPos(tipState, world, tipPos,
                    MAX_STALACTITE_GROWTH, false);
            if (newTipPos != null && !newTipPos.equals(oldTipPos)) {
                // Growth succeeded
                changed = true;
                tipPos = newTipPos;
                tipState = world.getBlockState(tipPos);
            } else if (newTipPos != null) {
                // Check if the tip state changed (e.g., merged)
                BlockState newTipState = world.getBlockState(newTipPos);
                if (!newTipState.equals(oldTipState)) {
                    changed = true;
                    tipPos = newTipPos;
                    tipState = newTipState;
                }
            } else {
                // No valid tip found, stop growing
                break;
            }
        }

        return changed;
    }

    private boolean isStillFluid(Block fluid, BlockState state) {
        return state.getFluidState().isStill() && state.isOf(fluid);
    }
}
