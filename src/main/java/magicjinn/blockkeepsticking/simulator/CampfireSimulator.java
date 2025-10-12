package magicjinn.blockkeepsticking.simulator;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CampfireSimulator {
	private final DefaultedList<ItemStack> items;
	private final int[] cookingTimes;
	private final int[] cookingTotalTimes;
	private boolean dataChanged;
	
	public CampfireSimulator(DefaultedList<ItemStack> items, int[] cookingTimes, int[] cookingTotalTimes) {
		this.items = DefaultedList.ofSize(items.size(), ItemStack.EMPTY);
		for (int i = 0; i < items.size(); i++) {
			this.items.set(i, items.get(i).copy());
		}
		this.cookingTimes = cookingTimes.clone();
		this.cookingTotalTimes = cookingTotalTimes.clone();
	}
	
	public boolean hasItemsCooking() {
		for (ItemStack item : items) {
			if (!item.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public void simulateFinalResult(int ticksPassed, World world) {
		boolean changed = false;

		for (int i = 0; i < items.size(); i++) {
			ItemStack item = items.get(i);
			if (!item.isEmpty() && cookingTotalTimes[i] > 0) {
				// Calculate if the item will finish cooking
				int totalTime = cookingTimes[i] + ticksPassed;
				if (totalTime >= cookingTotalTimes[i]) {
					// Get the cooking result using the recipe getter
					ServerRecipeManager.MatchGetter<SingleStackRecipeInput, CampfireCookingRecipe> recipeGetter =
							ServerRecipeManager
									.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);
					SingleStackRecipeInput recipeInput = new SingleStackRecipeInput(item);
					ItemStack result = recipeGetter
							.getFirstMatch(recipeInput, (ServerWorld) world).map(recipe -> recipe
									.value().craft(recipeInput, world.getRegistryManager()))
							.orElse(item);

					if (!result.isEmpty()) {
						items.set(i, result);
						cookingTimes[i] = 0;
						changed = true;
					}
				} else {
					// If not completed, update cooking time
					cookingTimes[i] = totalTime;
				}
			}
		}

		dataChanged = changed;
	}
	
	public DefaultedList<ItemStack> getItems() {
		return items;
	}
	
	public int[] getCookingTimes() {
		return cookingTimes;
	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}
	
	// private static class SimpleInventory implements Inventory {
	// private final ItemStack stack;
		
	// SimpleInventory(ItemStack stack) {
	// this.stack = stack;
	// }
		
	// @Override
	// public int size() {
	// return 1;
	// }
		
	// @Override
	// public boolean isEmpty() {
	// return stack.isEmpty();
	// }
		
	// @Override
	// public ItemStack getStack(int slot) {
	// return slot == 0 ? stack : ItemStack.EMPTY;
	// }
		
	// @Override
	// public ItemStack removeStack(int slot, int amount) {
	// return ItemStack.EMPTY;
	// }
		
	// @Override
	// public ItemStack removeStack(int slot) {
	// return ItemStack.EMPTY;
	// }
		
	// @Override
	// public void setStack(int slot, ItemStack stack) {}
		
	// @Override
	// public void markDirty() {}
		
	// @Override
	// public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
	// return true;
	// }
		
	// @Override
	// public void clear() {}
	// }
}
