package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.CactusBlock;

@Mixin(CactusBlock.class)
public interface CactusBlockAccessor {
    @Accessor("MAX_CACTUS_GROWING_HEIGHT")
    static int getMaxGrowingHeight() {
        // Will return MAX_CACTUS_GROWING_HEIGHT instead
        throw new AssertionError();
    }
}
