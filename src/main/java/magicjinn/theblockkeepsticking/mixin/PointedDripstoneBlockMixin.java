package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneBlockMixin implements TickingAccessor {
    @Shadow @Final private static float WATER_DRIP_CHANCE;
    @Shadow @Final private static float LAVA_DRIP_CHANCE;
    @Shadow @Final private static float field_33567; // Growth chance 0.011377778F
    private static final int MAX_LENGTH = 11; // Used for searching for the tip position
    @Shadow @Final private static int MAX_STALACTITE_GROWTH; // 7, ingame is 8?
    @Shadow @Final private static int STALACTITE_FLOOR_SEARCH_RANGE; // 10

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        float randomFloat = world.random.nextFloat();
        int randomInt = world.random.nextInt(2); // 0 or 1

        // Check if the block above is a dripstone block and the block above that is water
        // This disqualifies any middle/tip blocks, as well as upward growing dripstone
        BlockState blockAbove = world.getBlockState(pos.up());
        BlockState blockAboveThat = world.getBlockState(pos.up(2));

        boolean blockAboveIsDripstone = blockAbove.isOf(Blocks.DRIPSTONE_BLOCK);
        boolean blockAboveThatIsWater = isStillFluid(Blocks.WATER, blockAboveThat);
        boolean blockAboveThatIsLava = isStillFluid(Blocks.LAVA, blockAboveThat);
        boolean blockAboveThatIsMud = blockAboveThat.isOf(Blocks.MUD);
        boolean blockAboveThatIsAnythingElse =
                !blockAboveThatIsWater && !blockAboveThatIsLava && !blockAboveThatIsMud;

        // Literally nothing can happen if this is true
        if (blockAboveThatIsAnythingElse)
            return false;

        int cycleAmountGrowthInt = 0;
        if (blockAboveThatIsWater && blockAboveIsDripstone) {
            final float growthChance = field_33567;
            final float cycleAmountGrowth = randomTicks * growthChance;
            cycleAmountGrowthInt = (int) cycleAmountGrowth;
            final float cycleAmountLeftover = cycleAmountGrowth % 1.0f;
            // Because dripstone grows so incredibly slowly, we need to use random to account for
            // the situations where it wouldn't grow at all if deterministic
            cycleAmountGrowthInt += randomFloat < cycleAmountLeftover ? 1 : 0;
        }

        // same here for water and lava
        final float cycleAmountWater = randomTicks * WATER_DRIP_CHANCE;
        int cycleAmountWaterInt = (int) cycleAmountWater;
        final float cycleAmountWaterLeftover = cycleAmountWater % 1.0f;
        cycleAmountWaterInt += randomFloat < cycleAmountWaterLeftover ? 1 : 0;
        // Cap the water at the max level of cauldrons
        cycleAmountWaterInt = Math.min(cycleAmountWaterInt, LeveledCauldronBlock.MAX_LEVEL);

        // lava has no stages, so we just check if it's greater than 0,
        // or if the random float is less than the chance
        final float cycleAmountLava = randomTicks * LAVA_DRIP_CHANCE;
        int cycleAmountLavaInt = (int) cycleAmountLava;
        final float cycleAmountLavaLeftover = cycleAmountLava % 1.0f;
        cycleAmountLavaInt += randomFloat < cycleAmountLavaLeftover ? 1 : 0;
        final boolean filledLava = cycleAmountLavaInt > 0;

        BlockPos tipPos = PointedDripstoneBlock.getTipPos(state, world, pos, MAX_LENGTH, false);
        if (tipPos == null) {
            TheBlockKeepsTicking.LOGGER.info("Tip pos is null");
            return false;
        }

        boolean changed = false;

        BlockPos cauldronPos = null;
        if (cycleAmountWaterInt > 0 && blockAboveThatIsWater) {
            cauldronPos = PointedDripstoneBlock.getCauldronPos(world, tipPos, Fluids.WATER);
            if (cauldronPos != null) {
                BlockState currentState = world.getBlockState(cauldronPos);
                Block currentBlock = currentState.getBlock();
                for (int i = 0; i < cycleAmountWaterInt; i++) {
                    // First tick uses current block (could be CauldronBlock),
                    // subsequent ticks use WATER_CAULDRON (LeveledCauldronBlock) since
                    // the block type changes after the first tick
                    Block tickBlock = (i == 0) ? currentBlock
                            : Blocks.WATER_CAULDRON.getDefaultState()
                                    .with(LeveledCauldronBlock.LEVEL,
                                            Math.min(i + 1, LeveledCauldronBlock.MAX_LEVEL))
                                    .getBlock();
                    world.scheduleBlockTick(cauldronPos, tickBlock, 1 + i);
                    changed = true;
                }
            }
        } else if (filledLava && blockAboveThatIsLava) {
            cauldronPos = PointedDripstoneBlock.getCauldronPos(world, tipPos, Fluids.LAVA);
            if (cauldronPos != null) {
                world.scheduleBlockTick(cauldronPos, world.getBlockState(cauldronPos).getBlock(),
                        1);
                changed = true;
            }
        }


        int dripstoneLength = pos.getY() - tipPos.getY() + 1;
        while (cycleAmountGrowthInt > 0) {
            if (cycleAmountGrowthInt > 0) {
                if ((cycleAmountGrowthInt + randomInt) % 2 == 0) {
                    // TODO Try to grow a stalactite
                } else {
                    // TODO Try to grow a stalagmite
                }
                changed = true;
                cycleAmountGrowthInt--;
            }
        }


        return changed;
    }

    private boolean isStillFluid(Block fluid, BlockState state) {
        return state.getFluidState().isStill() && state.isOf(fluid);
    }
}
// // Calculate growth attempts deterministically
// final int growthAttempts = (int) (randomTicks * field_33567);
// if (growthAttempts <= 0)
// return false;


// // Deterministic choice: use growthAttempts itself to alternate
// for (int i = 0; i < growthAttempts; i++) {
// // Deterministically choose stalactite (down) or stalagmite (up)
// boolean growStalactite = (i % 2 == 0);

// if (growStalactite) {
// if (tryGrowStalactite(world, tipPos)) {
// changed = true;
// // Update tipPos after growth
// tipPos = PointedDripstoneBlock.getTipPos(state, world, pos,
// MAX_STALACTITE_GROWTH, false);
// if (tipPos == null)
// break;
// tipState = world.getBlockState(tipPos);
// if (!PointedDripstoneBlock.canDrip(tipState)
// || !PointedDripstoneBlock.canGrow(tipState,
// (net.minecraft.server.world.ServerWorld) world, tipPos))
// break;
// }
// } else {
// if (tryGrowStalagmite(world, tipPos)) {
// changed = true;
// }
// }
// }


// private boolean isTip(BlockState state, Direction direction) {
// if (!state.isOf(Blocks.POINTED_DRIPSTONE)) {
// return false;
// }
// Thickness thickness = state.get(PointedDripstoneBlock.THICKNESS);
// return (thickness == Thickness.TIP || thickness == Thickness.TIP_MERGE)
// && state.get(PointedDripstoneBlock.VERTICAL_DIRECTION) == direction;
// }

// private boolean tryGrow(World world, BlockPos pos, Direction direction) {
// BlockPos growPos = pos.offset(direction);
// BlockState growState = world.getBlockState(growPos);
// if (isTip(growState, direction.getOpposite())) {
// // Merge
// PointedDripstoneBlock.growMerged(growState, world, growPos);
// return true;
// } else if (growState.isAir() || growState.isOf(Blocks.WATER)) {
// // Place new tip
// place(world, growPos, direction, Thickness.TIP);
// return true;
// }
// return false;
// }

// private boolean tryGrowStalactite(World world, BlockPos pos) {
// return tryGrow(world, pos, Direction.DOWN);
// }

// private boolean tryGrowStalagmite(World world, BlockPos pos) {
// BlockPos.Mutable mutable = pos.mutableCopy();
// for (int i = 0; i < STALACTITE_FLOOR_SEARCH_RANGE; i++) {
// mutable.move(Direction.DOWN);
// BlockState blockState = world.getBlockState(mutable);
// if (!blockState.getFluidState().isEmpty()) {
// return false;
// }

// if (isTip(blockState, Direction.UP) &&
// PointedDripstoneBlock.canGrow(blockState,
// (net.minecraft.server.world.ServerWorld) world, mutable)) {
// // Grow at existing tip upward
// return tryGrow(world, mutable, Direction.UP);
// }

// if (PointedDripstoneBlock.canPlaceAtWithDirection(world, mutable,
// Direction.UP)
// && !world.isWater(mutable.down())) {
// // Place new tip upward
// return tryGrow(world, mutable.down(), Direction.UP);
// }

// if (!PointedDripstoneBlock.canDripThrough(world, mutable, blockState)) {
// return false;
// }
// }
// return false;
// }


// private void place(WorldAccess world, BlockPos pos, Direction direction,
// Thickness thickness)
// {
// BlockState blockState = Blocks.POINTED_DRIPSTONE.getDefaultState()
// .with(PointedDripstoneBlock.VERTICAL_DIRECTION, direction)
// .with(PointedDripstoneBlock.THICKNESS, thickness)
// .with(PointedDripstoneBlock.WATERLOGGED,
// world.getFluidState(pos).getFluid() == Fluids.WATER);
// world.setBlockState(pos, blockState, 3);
// }
