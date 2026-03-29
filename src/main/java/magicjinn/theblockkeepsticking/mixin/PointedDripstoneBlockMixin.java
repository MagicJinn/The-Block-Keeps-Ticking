package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneBlockMixin implements TickingAccessor {
    @Shadow
    @Final
    private static int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON;
    @Shadow
    @Final
    private static int MAX_GROWTH_LENGTH;
    @Shadow
    @Final
    private static float GROWTH_PROBABILITY_PER_RANDOM_TICK;
    @Shadow
    @Final
    private static float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
    @Shadow
    @Final
    private static float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);
        if (randomTicks <= 0)
            return false;

        if (!(level instanceof ServerLevel serverLevel))
            return false;

        // Same source as vanilla cauldron drip: scans fluid above the stalactite (not
        // only canGrow, which misses waterlogged dripstone and other cases).
        Fluid cauldronFillFluid = PointedDripstoneBlock.getCauldronFillFluidType(serverLevel, pos);
        boolean hasCauldronDripFluid = !cauldronFillFluid.isSame(Fluids.EMPTY);

        // Prepare random values for the simulation
        float randomFloat = level.getRandom().nextFloat();
        @SuppressWarnings("unused")
        int randomInt = level.getRandom().nextInt(2); // 0 or 1

        // Check if the block above is a dripstone block and the block above that is fluid
        // This disqualifies any middle/tip blocks, as well as upward growing dripstone
        BlockState blockAbove = level.getBlockState(pos.above());
        BlockState blockAboveThat = level.getBlockState(pos.above(2));
        boolean canGrowDripstone = PointedDripstoneBlock.canGrow(blockAbove, blockAboveThat);
        boolean blockAboveThatIsMud = blockAboveThat.is(Blocks.MUD);
        boolean blockAboveThatIsAnythingElse =
                !canGrowDripstone && !blockAboveThatIsMud && !hasCauldronDripFluid;

        // Literally nothing can happen if this is true
        if (blockAboveThatIsAnythingElse)
            return false;

        // Because dripstone grows so incredibly slowly, we need to use random to account for
        // the situations where it wouldn't grow at all if fully strict-deterministic
        @SuppressWarnings("unused")
        int cycleAmountGrowthInt = 0;
        if (canGrowDripstone) {
            final float growthChance = GROWTH_PROBABILITY_PER_RANDOM_TICK;
            final float cycleAmountGrowth = randomTicks * growthChance;
            cycleAmountGrowthInt = (int) cycleAmountGrowth;
            final float cycleAmountLeftover = cycleAmountGrowth % 1.0f;
            cycleAmountGrowthInt += randomFloat < cycleAmountLeftover ? 1 : 0;
        }

        // Same here for water and lava
        final float cycleAmountWater = randomTicks * WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
        int cycleAmountWaterInt = (int) cycleAmountWater;
        final float cycleAmountWaterLeftover = cycleAmountWater % 1.0f;
        cycleAmountWaterInt += randomFloat < cycleAmountWaterLeftover ? 1 : 0;
        // Cap the water at the max level of cauldrons
        cycleAmountWaterInt = Math.min(cycleAmountWaterInt, LayeredCauldronBlock.MAX_FILL_LEVEL);

        // Lava has no stages, so we just check if it's greater than 0,
        // or if the random float is less than the chance
        final float cycleAmountLava = randomTicks * LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK;
        int cycleAmountLavaInt = (int) cycleAmountLava;
        final float cycleAmountLavaLeftover = cycleAmountLava % 1.0f;
        cycleAmountLavaInt += randomFloat < cycleAmountLavaLeftover ? 1 : 0;
        final boolean filledLava = cycleAmountLavaInt > 0;

        boolean changed = false;

        BlockPos tipPos = PointedDripstoneBlock.findTip(state, serverLevel, pos,
                MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON, false);
        if (tipPos == null)
            return false;

        // Vanilla uses scheduled ticks to fill cauldrons, but that sucks for our purposes, so we
        // use blockstate changes directly
        BlockPos cauldronPos = null;
        if (cycleAmountWaterInt > 0 && cauldronFillFluid.isSame(Fluids.WATER)) {
            cauldronPos = PointedDripstoneBlock.findFillableCauldronBelowStalactiteTip(serverLevel,
                    tipPos, Fluids.WATER);
            if (cauldronPos != null) {
                BlockState currentState = level.getBlockState(cauldronPos);
                Block currentBlock = currentState.getBlock();

                int newLevel = 0;
                int currentLevel = 0;
                // If it already has water, get the current level
                if (currentBlock == Blocks.WATER_CAULDRON) {
                    currentLevel = currentState.getValue(LayeredCauldronBlock.LEVEL);
                } else if (currentBlock == Blocks.LAVA_CAULDRON) {
                    // Cancel the operation, since we can't fill a lava cauldron with water
                    cycleAmountWaterInt = 0;
                }

                newLevel = Math.min(currentLevel + cycleAmountWaterInt,
                        LayeredCauldronBlock.MAX_FILL_LEVEL);

                // If the new level is greater than 0, update the cauldron
                if (newLevel > 0) {
                    BlockState newState = Blocks.WATER_CAULDRON.defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, newLevel);
                    level.setBlock(cauldronPos, newState, 3);
                    changed = true;
                }
            }
        } else if (filledLava && cauldronFillFluid.isSame(Fluids.LAVA)) {
            cauldronPos = PointedDripstoneBlock.findFillableCauldronBelowStalactiteTip(serverLevel,
                    tipPos, Fluids.LAVA);
            if (cauldronPos != null) {
                BlockState currentState = level.getBlockState(cauldronPos);
                Block currentBlock = currentState.getBlock();

                // Only fill if it's an empty cauldron
                if (currentBlock == Blocks.CAULDRON) {
                    level.setBlock(cauldronPos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
                    changed = true;
                }
            }
        }

        // Convert mud to clay if the water is dripping
        BlockPos mudPos = pos.above(2);
        if (cycleAmountWaterInt > 0 && blockAboveThatIsMud
                && !level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, mudPos)) {
            BlockState mudState = level.getBlockState(mudPos);
            BlockState clayState = Blocks.CLAY.defaultBlockState();
            level.setBlock(mudPos, clayState, 3);
            Block.pushEntitiesUp(mudState, clayState, serverLevel, mudPos);
            level.levelEvent(1504, tipPos, 0);
            changed = true;
        }

        // #TODO: Fix growth
        boolean growthSucceeded = false;
        // simulateGrowth(cycleAmountGrowthInt, randomInt, tipPos, serverLevel, state,
        // pos);
        return growthSucceeded || changed;
    }

    @SuppressWarnings("unused")
    private static boolean simulateGrowth(int cycleAmountGrowthInt, int randomInt, BlockPos tipPos,
            Level level, BlockState state, BlockPos pos) {
        // Simulate growth attempts deterministically
        // Each attempt alternates between stalactite (down) and stalagmite (up)

        boolean changed = false;

        // Refresh tipPos for growth
        tipPos = PointedDripstoneBlock.findTip(state, level, pos, MAX_GROWTH_LENGTH, false);
        BlockState tipState = level.getBlockState(tipPos);

        for (int i = 0; i < cycleAmountGrowthInt; i++) {
            // Non-random alternating growth
            boolean growStalactite = (i + randomInt) % 2 == 0;

            // Check if we can still grow from this tip
            // canTipGrow checks if the block in the growth direction is air or a tip
            // This works for both stalactites and stalagmites, and for merged tips

            boolean canGrow = PointedDripstoneBlock.canTipGrow(tipState, (ServerLevel) level, tipPos);
            if (!canGrow)
                break; // Can't grow anymore, stop attempting


            // Store state before growth attempt to check if growth occurred
            BlockPos oldTipPos = tipPos;
            BlockState oldTipState = tipState;

            if (growStalactite) {
                PointedDripstoneBlock.grow((ServerLevel) level, tipPos, Direction.DOWN);
            } else {
                PointedDripstoneBlock.growStalagmiteBelow((ServerLevel) level, tipPos);
            }

            // Refresh tipState from serverLevel after growth attempt
            tipState = level.getBlockState(tipPos);

            // Check if growth occurred by verifying tip position or state changed
            // After growth, search from current tip position to find the new tip
            BlockPos newTipPos = PointedDripstoneBlock.findTip(tipState, level, tipPos,
                    MAX_GROWTH_LENGTH, false);
            if (newTipPos != null && !newTipPos.equals(oldTipPos)) {
                // Growth succeeded
                changed = true;
                tipPos = newTipPos;
                tipState = level.getBlockState(tipPos);
            } else if (newTipPos != null) {
                // Check if the tip state changed (e.g., merged)
                BlockState newTipState = level.getBlockState(newTipPos);
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
}
