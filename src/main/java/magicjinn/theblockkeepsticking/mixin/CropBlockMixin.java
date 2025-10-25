package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(CropBlock.class)
public class CropBlockMixin implements TickingBlockAccessor {
    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        CropBlock crop = (CropBlock) (Object) this;

        int age = crop.getAge(state);
        int maxAge = crop.getMaxAge();
        // Already fully grown
        if (crop.isMature(state))
            return false;

        // Too dark to grow
        if (world.getBaseLightLevel(pos, 0) < 9)
            return false;

        // Determine the amount of random ticks that would have occurred
        int randomTickSpeed =
                ((ServerWorld) world).getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        int randomTicks = ticksToSimulate.intValue() / (16 * 16 * 16) * randomTickSpeed;


        // Simplified growth formula, fakes randomness
        float availableMoisture = CropBlock.getAvailableMoisture(crop, world, pos);
        int growth = (int) Math.floor(25f / availableMoisture + 1) * randomTicks;
        int newAge = Math.min(maxAge, age + growth);

        // Set the new age
        BlockState newStage = crop.withAge(newAge);
        world.setBlockState(pos, newStage, 2);

        return true; // Return true if the block state was changed, otherwise false
    }
}
