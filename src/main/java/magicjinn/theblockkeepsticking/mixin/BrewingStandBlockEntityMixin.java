package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        return false;
    }
}
