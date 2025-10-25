package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CropBlock.class)
public class CropBlockMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        CropBlock crop = (CropBlock) (Object) this;

        int growth = TickingBlock.CropGrowthAmount(ticksToSimulate, crop, world, state, pos);

        int age = crop.getAge(state);
        int maxAge = crop.getMaxAge();

        if (growth <= 0)
            return false;

        int newAge = Math.min(maxAge, age + growth);

        // Set the new age
        BlockState newStage = crop.withAge(newAge);
        world.setBlockState(pos, newStage, 2);

        return true; // Return true if the block state was changed
    }
}
