package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.BlockKeepsTicking;
import magicjinn.blockkeepsticking.api.FurnaceAccess;
import magicjinn.blockkeepsticking.simulator.FurnaceSimulator;
import magicjinn.blockkeepsticking.util.BlockEntityUtils;
import magicjinn.blockkeepsticking.util.ContainerUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceAccess {
	@Shadow protected DefaultedList<ItemStack> inventory;
	@Shadow protected int litTimeRemaining;
	@Shadow protected int litTotalTime;
	@Shadow protected int cookingTimeSpent;
	@Shadow protected int cookingTotalTime;
	@Shadow @Final private Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed;
	@Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
	@Shadow public World world;

	@Shadow
	protected abstract int getFuelTime(net.minecraft.item.FuelRegistry fuelRegistry,
			ItemStack stack);

	@Override
	public FurnaceSimulator createSimulator(World world) {
		return new FurnaceSimulator(ContainerUtils.createContainer(inventory), litTimeRemaining,
				litTotalTime, cookingTimeSpent, cookingTotalTime, world);
	}

	@Override
	public int getFuelBurnTime(ItemStack fuel) {
		return getFuelTime(world.getFuelRegistry(), fuel);
	}

	// public ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends
	// AbstractCookingRecipe> getMatchGetter() {
	// return matchGetter;
	// }

	@Override
	public void apply(World world, BlockPos pos, BlockState state, FurnaceSimulator simulator) {
		boolean oldLit = litTimeRemaining > 0;
		boolean dataChanged = simulator.isDataChanged();

		if (dataChanged) {
			// Update inventory
			ContainerUtils.extractContainer(simulator.getContainer(), inventory);

			// Update states
			litTimeRemaining = simulator.getLitTime();
			litTotalTime = simulator.getLitDuration();
			cookingTimeSpent = simulator.getCookingProgress();
			cookingTotalTime = simulator.getCookingTotalTime();

			// Update used recipes
			for (Reference2IntMap.Entry<RegistryKey<Recipe<?>>> entry : simulator.getRecipesUsed()
					.reference2IntEntrySet()) {
				recipesUsed.addTo(entry.getKey(), entry.getIntValue());
			}

			BlockKeepsTicking.LOGGER.info(
					"Applying changes on furnace {}: litTimeRemaining={}, cookingTimeSpent={}/{}",
					pos, litTimeRemaining, cookingTimeSpent, cookingTotalTime);

			// Update lit state if changed
			if (oldLit != litTimeRemaining > 0) {
				state = state.with(AbstractFurnaceBlock.LIT, litTimeRemaining > 0);
				world.setBlockState(pos, state, 3);
			}

			// Mark changes
			BlockEntityUtils.markChanged(world, pos, state);
		}
	}
}
