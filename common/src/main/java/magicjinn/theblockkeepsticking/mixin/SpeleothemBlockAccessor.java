package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.SpeleothemBlock;

@Mixin(SpeleothemBlock.class)
public interface SpeleothemBlockAccessor {
    @Accessor("GROWTH_PROBABILITY_PER_RANDOM_TICK")
    static float getGrowthProbabilityPerRandomTick() {
        throw new AssertionError();
    }

    @Accessor("MAX_GROWTH_LENGTH")
    static int getMaxGrowthLength() {
        throw new AssertionError();
    }
}
