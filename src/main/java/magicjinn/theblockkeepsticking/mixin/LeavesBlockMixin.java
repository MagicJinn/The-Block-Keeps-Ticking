// package magicjinn.theblockkeepsticking.mixin;

// import org.spongepowered.asm.mixin.Mixin;
// import magicjinn.theblockkeepsticking.util.TickingAccessor;
// import magicjinn.theblockkeepsticking.util.TickingCalculator;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.world.level.block.LeavesBlock;
// import net.minecraft.server.level.ServerLevel;
// import net.minecraft.core.BlockPos;
// import net.minecraft.world.level.Level;

// @Mixin(LeavesBlock.class)
// public class LeavesBlockMixin implements TickingAccessor {
// @Override
// public boolean Simulate(long ticksToSimulate, Level level, BlockState state,
// BlockPos pos) {
// LeavesBlock leavesBlock = (LeavesBlock) (Object) this;
// int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);

// if (randomTicks <= 0)
// return false;

// // since there is no random chance, we can safely call randomTick
// leavesBlock.randomTick(state, (ServerLevel) world, pos, level.getRandom());
// // Check if block became empty
// if (level.getBlockState(pos).isEmptyBlock()) {
// return true;
// }
// return false;
// }
// }
