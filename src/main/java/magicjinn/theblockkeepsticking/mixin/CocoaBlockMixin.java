package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CocoaBlock.class)
public class CocoaBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 5);
        if (randomTicks <= 0)
            return false;

        int age = state.get(CocoaBlock.AGE);
        if (age >= CocoaBlock.MAX_AGE)
            return false;


        world.setBlockState(pos,
                state.with(CocoaBlock.AGE, Math.min(CocoaBlock.MAX_AGE, age + randomTicks)), 2);
        return true;
    }
}
