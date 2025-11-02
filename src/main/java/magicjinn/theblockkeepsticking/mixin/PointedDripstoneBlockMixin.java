package magicjinn.theblockkeepsticking.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneBlockMixin implements TickingAccessor {
    @Shadow @Final private static float WATER_DRIP_CHANCE;
    @Shadow @Final private static float LAVA_DRIP_CHANCE;
    // Growth chance, mapping error
    @Shadow @Final private static float field_33567;
    // Max dripstone length, mapping error
    // @Shadow @Final private static int field_31210;

    // There are 2 fields with 11 in the same file, we don't know which one is the correct one, so
    // we shadow neither
    private static final int maxDripstoneLength = 11;


    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        PointedDripstoneBlock pointedDripstone = (PointedDripstoneBlock) (Object) this;

        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        if (!PointedDripstoneBlock.isHeldByPointedDripstone(state, world, pos))
            return false;

        final float growthChance = field_33567;

        final int waterTicks = (int) (randomTicks * WATER_DRIP_CHANCE);
        final int lavaTicks = (int) (randomTicks * LAVA_DRIP_CHANCE);
        final int growthTicks = (int) (randomTicks * growthChance);

        Direction direction = state.get(PointedDripstoneBlock.VERTICAL_DIRECTION);

        // If block above is not dripstone, don't bother
        if (!world.getBlockState(pos.offset(direction.getOpposite())).isOf(Blocks.DRIPSTONE_BLOCK))
            return false;

        // Get the fluid above the dripstone
        Fluid fluid = Fluids.EMPTY;
        if (direction == Direction.DOWN) {
            fluid = world.getBlockState(pos.offset(direction.getOpposite(), 2)).getFluidState()
                    .getFluid();
        } else
            return false; // Don't bother with up direction

        int randomTiteMite = world.random.nextInt(2);
        // Only grow if the fluid is water
        if (fluid == Fluids.WATER) {
            // Get the length of the dripstone
            int dripstoneLength = getDripstoneLength(world, pos, direction);
            int maxGrowthLength = maxDripstoneLength - dripstoneLength;
            if (maxGrowthLength <= 0)
                return false;
            int i = 0;
            while (dripstoneLength < maxDripstoneLength && i <= growthTicks) {
                BlockState otherBlock = world.getBlockState(pos.down(dripstoneLength));
                if (!otherBlock.isAir() || !otherBlock.isOf(Blocks.WATER))
                    break;

                if (growthTicks + randomTiteMite % 2 == 0) {
                    // stalagmite

                } else {
                    world.setBlockState(pos.down(dripstoneLength),
                            pointedDripstone.getDefaultState(), 3);
                    dripstoneLength++;
                }
            }

        }
        return false;
    }

    private int getDripstoneLength(World world, BlockPos pos, Direction direction) {
        int length = 1; // Count the current block

        // Count blocks in the growth direction (away from the dripstone base)
        BlockPos.Mutable mutablePos = pos.mutableCopy();
        while (true) {
            mutablePos.move(direction);
            if (!world.getBlockState(mutablePos).isOf(Blocks.POINTED_DRIPSTONE)) {
                break;
            }
            length++;
        }

        // Count blocks in the opposite direction (towards the dripstone base, but not including the
        // base)
        mutablePos = pos.mutableCopy();
        Direction oppositeDirection = direction.getOpposite();
        while (true) {
            mutablePos.move(oppositeDirection);
            if (!world.getBlockState(mutablePos).isOf(Blocks.POINTED_DRIPSTONE)) {
                break;
            }
            length++;
        }

        return length;
    }
}
